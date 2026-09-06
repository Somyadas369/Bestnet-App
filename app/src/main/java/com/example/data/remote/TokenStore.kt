package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Session tokens, encrypted at rest.
 *
 * A phone is a far more exposed storage environment than a server, and these
 * tokens are the whole session — so plain SharedPreferences would leave them
 * readable on a rooted or backed-up device.
 *
 * Falls back to plain SharedPreferences if the keystore is unavailable. That
 * happens on a small number of devices with broken keystore implementations,
 * and losing encryption is strictly better than an app that cannot log in at
 * all — but it is logged as an error rather than passed over silently.
 */
class TokenStore(private val context: Context) {

  private var prefs: SharedPreferences = createPrefs(context)

  private fun createPrefs(ctx: Context): SharedPreferences {
    return try {
      val masterKey = MasterKey.Builder(ctx)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
      val esp = EncryptedSharedPreferences.create(
        ctx,
        "bestnet_session",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
      )
      // Probe read to detect corrupted or unreadable keys immediately.
      esp.getString("__probe__", null)
      esp
    } catch (err: Throwable) {
      Log.e("TokenStore", "Encrypted storage unavailable or corrupted, resetting to plain prefs", err)
      purgeEncryptedPrefs(ctx)
      ctx.getSharedPreferences("bestnet_session_plain", Context.MODE_PRIVATE)
    }
  }

  private fun purgeEncryptedPrefs(ctx: Context) {
    try {
      ctx.deleteSharedPreferences("bestnet_session")
    } catch (_: Throwable) {}
  }

  private fun recoverPrefs() {
    purgeEncryptedPrefs(context)
    prefs = context.getSharedPreferences("bestnet_session_plain", Context.MODE_PRIVATE)
  }

  private fun safeGetString(key: String): String? {
    return try {
      prefs.getString(key, null)
    } catch (err: Throwable) {
      Log.e("TokenStore", "Failed reading $key from prefs, recovering", err)
      recoverPrefs()
      null
    }
  }

  private fun safeSetString(key: String, value: String?) {
    try {
      prefs.edit().putString(key, value).apply()
    } catch (err: Throwable) {
      Log.e("TokenStore", "Failed writing $key to prefs, recovering", err)
      recoverPrefs()
      try { prefs.edit().putString(key, value).apply() } catch (_: Throwable) {}
    }
  }

  private fun safeGetInt(key: String, default: Int): Int {
    return try {
      prefs.getInt(key, default)
    } catch (err: Throwable) {
      Log.e("TokenStore", "Failed reading $key from prefs, recovering", err)
      recoverPrefs()
      default
    }
  }

  private fun safeSetInt(key: String, value: Int) {
    try {
      prefs.edit().putInt(key, value).apply()
    } catch (err: Throwable) {
      Log.e("TokenStore", "Failed writing $key to prefs, recovering", err)
      recoverPrefs()
      try { prefs.edit().putInt(key, value).apply() } catch (_: Throwable) {}
    }
  }

  var accessToken: String?
    get() = safeGetString(KEY_ACCESS)
    set(value) = safeSetString(KEY_ACCESS, value)

  var refreshToken: String?
    get() = safeGetString(KEY_REFRESH)
    set(value) = safeSetString(KEY_REFRESH, value)

  /**
   * Which home the resident is currently viewing. Purely a UI preference — it
   * is never sent to the server, because none of the endpoints this app calls
   * are unit-scoped.
   */
  var selectedMembershipId: String?
    get() = safeGetString(KEY_MEMBERSHIP)
    set(value) = safeSetString(KEY_MEMBERSHIP, value)

  /**
   * Server ids for the selected home. Room's `Resident` is a display model with
   * autoGenerate Long ids, so it cannot carry the server's UUIDs — but raising a
   * ticket needs both (the unit to file against, the tenant to list categories
   * from). Kept here rather than widening the entity and forcing a migration.
   */
  var selectedUnitId: String?
    get() = safeGetString(KEY_UNIT)
    set(value) = safeSetString(KEY_UNIT, value)

  var selectedTenantId: String?
    get() = safeGetString(KEY_TENANT)
    set(value) = safeSetString(KEY_TENANT, value)

  /**
   * SIP credentials for in-app calling.
   *
   * The server stores the SIP password one-way hashed and returns the plaintext
   * exactly once, at provisioning or password reset — so once it is issued,
   * this device is the only place it exists. Losing it means resetting, which
   * disconnects every other device on the extension.
   *
   * Encrypted at rest with the tokens, for the same reason.
   */
  var sipExtension: String?
    get() = safeGetString(KEY_SIP_EXT)
    set(value) = safeSetString(KEY_SIP_EXT, value)

  var sipPassword: String?
    get() = safeGetString(KEY_SIP_PW)
    set(value) = safeSetString(KEY_SIP_PW, value)

  var sipDomain: String?
    get() = safeGetString(KEY_SIP_DOMAIN)
    set(value) = safeSetString(KEY_SIP_DOMAIN, value)

  var sipPort: Int
    get() = safeGetInt(KEY_SIP_PORT, 5061)
    set(value) = safeSetInt(KEY_SIP_PORT, value)

  var sipTransport: String?
    get() = safeGetString(KEY_SIP_TRANSPORT)
    set(value) = safeSetString(KEY_SIP_TRANSPORT, value)

  val hasSipCredentials: Boolean
    get() = !sipExtension.isNullOrBlank() && !sipPassword.isNullOrBlank()

  val isLoggedIn: Boolean get() = !accessToken.isNullOrBlank()

  fun save(tokens: TokensDto) {
    safeSetString(KEY_ACCESS, tokens.accessToken)
    safeSetString(KEY_REFRESH, tokens.refreshToken)
  }

  fun clear() {
    try {
      prefs.edit()
        .remove(KEY_ACCESS)
        .remove(KEY_REFRESH)
        .remove(KEY_MEMBERSHIP)
        .remove(KEY_UNIT)
        .remove(KEY_TENANT)
        .remove(KEY_SIP_EXT)
        .remove(KEY_SIP_PW)
        .remove(KEY_SIP_DOMAIN)
        .remove(KEY_SIP_PORT)
        .remove(KEY_SIP_TRANSPORT)
        .apply()
    } catch (err: Throwable) {
      recoverPrefs()
    }
  }

  private companion object {
    const val KEY_ACCESS = "access_token"
    const val KEY_REFRESH = "refresh_token"
    const val KEY_MEMBERSHIP = "selected_membership"
    const val KEY_UNIT = "selected_unit"
    const val KEY_TENANT = "selected_tenant"
    const val KEY_SIP_EXT = "sip_extension"
    const val KEY_SIP_PW = "sip_password"
    const val KEY_SIP_DOMAIN = "sip_domain"
    const val KEY_SIP_PORT = "sip_port"
    const val KEY_SIP_TRANSPORT = "sip_transport"
  }
}
