package com.example.data.sip

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.linphone.core.Account
import org.linphone.core.AccountParams
import org.linphone.core.AudioDevice
import org.linphone.core.Call
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.MediaEncryption
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType

/** What the UI needs to know about registration. */
enum class SipRegistration { NONE, PROGRESS, REGISTERED, FAILED }

/** What the UI needs to know about the current call. */
enum class SipCallState { IDLE, INCOMING, OUTGOING, CONNECTED, ENDED, ERROR }

data class SipCallInfo(
  val state: SipCallState,
  /** Remote extension, e.g. "817545". */
  val remote: String? = null,
  /** Populated on ERROR/ENDED so the UI can say why rather than just closing. */
  val reason: String? = null,
)

/**
 * SIP client, wrapping linphone-sdk.
 *
 * Registers against the BestNet PBX over TLS and places/receives calls to other
 * extensions. Before this existed the app showed a full in-call UI — mute,
 * speaker, hang up — while no call was placed anywhere, because there was no
 * SIP stack at all.
 *
 * Deliberately a single process-wide instance held by the Application: a
 * linphone Core owns native resources, network sockets and audio devices, and
 * two of them registering the same extension would fight over the registration.
 *
 * ## What this does not do
 * - **No push wake-up.** The Core only receives calls while the process is
 *   alive and registered. Android will eventually kill a backgrounded app, and
 *   the phone then simply doesn't ring. Solving that properly needs push
 *   (FCM) to wake the app, which the backend does not send yet.
 * - **No call history.** Ended calls are not recorded anywhere.
 */
class SipManager(private val context: Context) {

  private var core: Core? = null
  private var account: Account? = null

  private val _registration = MutableStateFlow(SipRegistration.NONE)
  val registration: StateFlow<SipRegistration> = _registration.asStateFlow()

  private val _call = MutableStateFlow(SipCallInfo(SipCallState.IDLE))
  val call: StateFlow<SipCallInfo> = _call.asStateFlow()

  private val _muted = MutableStateFlow(false)
  val muted: StateFlow<Boolean> = _muted.asStateFlow()

  private val _speaker = MutableStateFlow(false)
  val speaker: StateFlow<Boolean> = _speaker.asStateFlow()

  private val listener = object : CoreListenerStub() {
    override fun onAccountRegistrationStateChanged(
      core: Core,
      account: Account,
      state: RegistrationState?,
      message: String,
    ) {
      _registration.value = when (state) {
        RegistrationState.Ok -> SipRegistration.REGISTERED
        RegistrationState.Progress -> SipRegistration.PROGRESS
        RegistrationState.Failed -> SipRegistration.FAILED
        else -> SipRegistration.NONE
      }
      Log.d(TAG, "Registration -> $state ($message)")
    }

    override fun onCallStateChanged(core: Core, call: Call, state: Call.State?, message: String) {
      val remote = call.remoteAddress.username
      Log.d(TAG, "Call $remote -> $state ($message)")
      _call.value = when (state) {
        Call.State.IncomingReceived, Call.State.IncomingEarlyMedia ->
          SipCallInfo(SipCallState.INCOMING, remote)
        Call.State.OutgoingInit, Call.State.OutgoingProgress, Call.State.OutgoingRinging ->
          SipCallInfo(SipCallState.OUTGOING, remote)
        Call.State.Connected, Call.State.StreamsRunning ->
          SipCallInfo(SipCallState.CONNECTED, remote)
        Call.State.Error ->
          SipCallInfo(SipCallState.ERROR, remote, message)
        Call.State.End, Call.State.Released ->
          SipCallInfo(SipCallState.ENDED, remote)
        else -> _call.value
      }
      if (state == Call.State.End || state == Call.State.Released || state == Call.State.Error) {
        _muted.value = false
        _speaker.value = false
      }
    }
  }

  /** Idempotent: safe to call on every app start. */
  @Synchronized
  fun start() {
    if (core != null) return
    try {
      // 5.5 has no setLogLevel on Factory; this is the toggle that exists.
      // Off by default so SIP traffic — including auth headers — doesn't land in
      // logcat on a user's device.
      Factory.instance().enableLogcatLogs(false)
      val created = Factory.instance().createCore(null, null, context)
      created.addListener(listener)
      // Set explicitly rather than trusting the default: the Core does nothing at
      // all — no registration, no incoming calls — unless something drives
      // iterate(). On Android the SDK can do this itself, and this makes sure it
      // does instead of leaving it to a version-dependent default.
      created.isAutoIterateEnabled = true
      created.start()
      core = created
    } catch (t: Throwable) {
      Log.e(TAG, "Failed to initialize linphone Core safely", t)
    }
  }

  /**
   * Registers an extension. Replaces any existing account, so switching homes
   * or re-provisioning doesn't leave a stale registration behind.
   *
   * @param domain PBX hostname, e.g. pbx.bestnet.in
   * @param port   5061 for TLS
   */
  @Synchronized
  fun register(extension: String, password: String, domain: String, port: Int, useTls: Boolean = true) {
    try {
      start()
      val c = core ?: run {
        _registration.value = SipRegistration.FAILED
        return
      }
      clearAccounts()

      val identity = Factory.instance().createAddress("sip:$extension@$domain") ?: run {
        Log.e(TAG, "Could not build SIP identity for $extension@$domain")
        _registration.value = SipRegistration.FAILED
        return
      }

      // The auth realm must match what Asterisk challenges with. Passing null
      // lets linphone accept whatever realm the server sends, which is correct
      // here because the PBX's realm is a server-side setting we don't control
      // from the app and it has changed once already.
      val auth = Factory.instance().createAuthInfo(extension, null, password, null, null, domain)
      c.addAuthInfo(auth)

      val params: AccountParams = c.createAccountParams().apply {
        identityAddress = identity
        val server = Factory.instance().createAddress("sip:$domain:$port")
        server?.transport = if (useTls) TransportType.Tls else TransportType.Udp
        serverAddress = server
        isRegisterEnabled = true
        // Re-REGISTER well inside the default expiry. Without a short-ish
        // refresh the OS drops the idle socket on mobile networks and the phone
        // silently stops receiving calls.
        expires = 300
      }

      val acc = c.createAccount(params)
      c.addAccount(acc)
      c.defaultAccount = acc
      account = acc
      _registration.value = SipRegistration.PROGRESS

      // Signalling is TLS; media is not encrypted. Requiring SRTP here would fail
      // calls against the current PBX config, which does not offer it.
      // TODO: switch to MediaEncryption.SRTP once the PBX enables it.
      c.mediaEncryption = MediaEncryption.None
    } catch (t: Throwable) {
      Log.e(TAG, "Registration exception", t)
      _registration.value = SipRegistration.FAILED
    }
  }

  @Synchronized
  fun unregister() {
    clearAccounts()
    _registration.value = SipRegistration.NONE
  }

  private fun clearAccounts() {
    val c = core ?: return
    c.accountList.forEach { existing ->
      existing.params.clone().apply { isRegisterEnabled = false }.let { existing.params = it }
      c.removeAccount(existing)
    }
    c.clearAccounts()
    c.clearAllAuthInfo()
    account = null
  }

  /** Dials another extension on the same PBX. */
  fun callExtension(extension: String) {
    val c = core ?: return
    val domain = account?.params?.identityAddress?.domain ?: return
    val target = Factory.instance().createAddress("sip:$extension@$domain") ?: return
    val params = c.createCallParams(null) ?: return
    c.inviteAddressWithParams(target, params)
  }

  fun answer() {
    core?.currentCall?.accept()
  }

  fun hangUp() {
    val c = core ?: return
    // currentCall is null for a call that is still only ringing, so fall back
    // to terminating everything rather than leaving a call the user thinks
    // they rejected still alive.
    c.currentCall?.terminate() ?: c.terminateAllCalls()
  }

  fun setMuted(value: Boolean) {
    core?.isMicEnabled = !value
    _muted.value = value
  }

  fun setSpeaker(value: Boolean) {
    val c = core ?: return
    val target = if (value) AudioDevice.Type.Speaker else AudioDevice.Type.Earpiece
    c.audioDevices.firstOrNull { it.type == target && it.hasCapability(AudioDevice.Capabilities.CapabilityPlay) }
      ?.let { c.outputAudioDevice = it }
    _speaker.value = value
  }

  /** Clears the ENDED/ERROR state once the UI has shown it. */
  fun acknowledgeCallEnded() {
    if (_call.value.state == SipCallState.ENDED || _call.value.state == SipCallState.ERROR) {
      _call.value = SipCallInfo(SipCallState.IDLE)
    }
  }

  private companion object {
    const val TAG = "SipManager"
  }
}
