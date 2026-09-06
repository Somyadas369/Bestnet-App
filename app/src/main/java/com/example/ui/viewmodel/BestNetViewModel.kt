package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BestNetDatabase
import com.example.data.model.CommunityNotice
import com.example.data.model.Complaint
import com.example.data.model.IntercomContact
import com.example.data.model.Notice
import com.example.data.model.Resident
import com.example.data.model.Visitor
import com.example.data.repository.BestNetRepository
import com.example.data.remote.CommunityEventDto
import com.example.data.remote.EmergencyContactDto
import com.example.data.remote.SubscriptionDto
import com.example.BestNetApp
import com.example.data.repository.SessionRepository
import com.example.data.sip.SipCallInfo
import android.util.Log
import com.example.data.sip.SipManager
import com.example.data.sip.SipRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit

class BestNetViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: BestNetRepository
  private val sessionRepository: SessionRepository

  val currentResident: StateFlow<Resident?>
  val allResidents: StateFlow<List<Resident>>
  val allComplaints: StateFlow<List<Complaint>>
  val allVisitors: StateFlow<List<Visitor>>
  val allNotices: StateFlow<List<Notice>>
  val allCommunityNotices: StateFlow<List<CommunityNotice>>

  val intercomStaff: List<IntercomContact>
  val intercomNeighbors: List<IntercomContact>

  // Set from the stored session in init{}. Starts false so a cold start with no
  // session lands on Login rather than flashing the signed-in app first.
  private val _isLoggedIn = MutableStateFlow(false)
  val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

  private val _authBusy = MutableStateFlow(false)
  val authBusy: StateFlow<Boolean> = _authBusy.asStateFlow()

  /** Server-supplied failure text, shown verbatim rather than a generic message. */
  private val _authError = MutableStateFlow<String?>(null)
  val authError: StateFlow<String?> = _authError.asStateFlow()

  private val _complaintSubmitting = MutableStateFlow(false)
  val complaintSubmitting: StateFlow<Boolean> = _complaintSubmitting.asStateFlow()

  private val _complaintError = MutableStateFlow<String?>(null)
  val complaintError: StateFlow<String?> = _complaintError.asStateFlow()

  private val _visitorSubmitting = MutableStateFlow(false)
  val visitorSubmitting: StateFlow<Boolean> = _visitorSubmitting.asStateFlow()

  private val _visitorError = MutableStateFlow<String?>(null)
  val visitorError: StateFlow<String?> = _visitorError.asStateFlow()

  // Services / Community. Held in memory rather than Room — read-only lists
  // that are always refetched on sync. Null means "not loaded yet", which the
  // screens distinguish from an empty list so they can show "Loading…" instead
  // of wrongly claiming there is nothing.
  private val _subscriptions = MutableStateFlow<List<SubscriptionDto>?>(null)
  val subscriptions: StateFlow<List<SubscriptionDto>?> = _subscriptions.asStateFlow()

  private val _events = MutableStateFlow<List<CommunityEventDto>?>(null)
  val events: StateFlow<List<CommunityEventDto>?> = _events.asStateFlow()

  private val _emergencyContacts = MutableStateFlow<List<EmergencyContactDto>?>(null)
  val emergencyContacts: StateFlow<List<EmergencyContactDto>?> = _emergencyContacts.asStateFlow()

  /** The resident's own extension, from GET /me/intercom. */
  private val _myExtension = MutableStateFlow<String?>(null)
  val myExtension: StateFlow<String?> = _myExtension.asStateFlow()

  /** Neighbours' extensions. null = not loaded, empty = nobody else has one. */
  private val _intercomDirectory = MutableStateFlow<List<IntercomContact>?>(null)
  val intercomDirectory: StateFlow<List<IntercomContact>?> = _intercomDirectory.asStateFlow()

  // SIP. The manager is process-wide (held by the Application) because a
  // linphone Core owns native resources and a single registration.
  private val sipManager: SipManager = BestNetApp.sipManager(application)
  val sipRegistration: StateFlow<SipRegistration> = sipManager.registration
  val sipCall: StateFlow<SipCallInfo> = sipManager.call
  val sipMuted: StateFlow<Boolean> = sipManager.muted
  val sipSpeaker: StateFlow<Boolean> = sipManager.speaker

  private val _sipBusy = MutableStateFlow(false)
  val sipBusy: StateFlow<Boolean> = _sipBusy.asStateFlow()

  private val _sipError = MutableStateFlow<String?>(null)
  val sipError: StateFlow<String?> = _sipError.asStateFlow()

  /** True once this device holds SIP credentials, whether or not it's registered. */
  val sipConfigured: Boolean get() = sessionRepository.storedSipCredentials() != null

  private val _snackbarMessage = MutableStateFlow<String?>(null)
  val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

  private val _activeCallContact = MutableStateFlow<IntercomContact?>(null)
  val activeCallContact: StateFlow<IntercomContact?> = _activeCallContact.asStateFlow()

  private val _showSpeedTest = MutableStateFlow(false)
  val showSpeedTest: StateFlow<Boolean> = _showSpeedTest.asStateFlow()

  private val _showPreApproveDialog = MutableStateFlow(false)
  val showPreApproveDialog: StateFlow<Boolean> = _showPreApproveDialog.asStateFlow()

  private val _showSwitchHomeSheet = MutableStateFlow(false)
  val showSwitchHomeSheet: StateFlow<Boolean> = _showSwitchHomeSheet.asStateFlow()

  private val _activeTab = MutableStateFlow("home")
  val activeTab: StateFlow<String> = _activeTab.asStateFlow()

  init {
    val database = BestNetDatabase.getDatabase(application)
    repository = BestNetRepository(database.bestNetDao())
    sessionRepository = SessionRepository(application, database.bestNetDao())

    // Start from the stored session rather than assuming logged-in, so a
    // returning user skips login and a new one cannot walk straight into the
    // app without authenticating.
    _isLoggedIn.value = sessionRepository.isLoggedIn
    if (_isLoggedIn.value) {
      refreshFromServer()
      // A device that was already set up should be reachable straight away,
      // without the resident opening the Intercom screen first. Offloaded
      // to IO dispatcher so it never blocks startup rendering.
      viewModelScope.launch(Dispatchers.IO) {
        registerSipIfPossible()
      }
    }

    // No placeholder identity: an invented "Rahul Sharma" would be shown as
    // though it were the signed-in user during the first frames after login.
    currentResident = repository.currentResident.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      null
    )

    allResidents = repository.allResidents.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    allComplaints = repository.allComplaints.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    allVisitors = repository.allVisitors.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    allNotices = repository.allNotices.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    allCommunityNotices = repository.allCommunityNotices.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      emptyList()
    )

    intercomStaff = repository.getIntercomStaff()
    intercomNeighbors = repository.getIntercomNeighbors()
  }

  fun setTab(tab: String) {
    _activeTab.value = tab
  }

  /** Step 1 of login: ask the server to send a code over WhatsApp. */
  fun requestOtp(phone: String, onResult: (Boolean) -> Unit) {
    _authBusy.value = true
    _authError.value = null
    viewModelScope.launch {
      val result = sessionRepository.requestOtp(phone)
      _authBusy.value = false
      result
        .onSuccess { onResult(true) }
        .onFailure {
          _authError.value = it.message ?: "Could not send the code"
          onResult(false)
        }
    }
  }

  /**
   * Step 2: verify the code, then pull the resident's real data down before
   * letting them in. Sync happens before `isLoggedIn` flips so the app never
   * renders a signed-in shell with no home in it.
   */
  fun verifyOtp(phone: String, code: String, onResult: (Boolean) -> Unit) {
    _authBusy.value = true
    _authError.value = null
    viewModelScope.launch {
      val verified = sessionRepository.verifyOtp(phone, code)
      if (verified.isFailure) {
        _authBusy.value = false
        _authError.value = verified.exceptionOrNull()?.message ?: "Invalid or expired code"
        onResult(false)
        return@launch
      }

      sessionRepository.syncFromServer()
        .onFailure {
          // Session is valid but unusable — don't strand the user inside a
          // half-loaded app pretending to work.
          _authBusy.value = false
          _authError.value = it.message ?: "Signed in, but your details could not be loaded"
          sessionRepository.logout()
          onResult(false)
          return@launch
        }

      _authBusy.value = false
      _isLoggedIn.value = true
      loadServicesAndCommunity()
      onResult(true)
    }
  }

  /** Re-pulls server data for an already-authenticated session. */
  fun refreshFromServer() {
    viewModelScope.launch {
      sessionRepository.syncFromServer().onFailure {
        _snackbarMessage.value = "Couldn't refresh — showing saved data"
      }
      loadServicesAndCommunity()
    }
  }

  /**
   * Loads the Services and Community lists. Kept separate from the main sync so
   * a failure here can't block sign-in — none of it is needed to render Home.
   */
  fun loadServicesAndCommunity() {
    viewModelScope.launch {
      _subscriptions.value = sessionRepository.mySubscriptions()
      _events.value = sessionRepository.myEvents()
      _emergencyContacts.value = sessionRepository.myEmergencyContacts()
      _myExtension.value = sessionRepository.myIntercom()?.sipUsername
      _intercomDirectory.value = sessionRepository.intercomDirectory().map { entry ->
        IntercomContact(
          id = entry.id,
          // The server sends no names, by design — the unit is the identity.
          name = "Unit ${entry.unitLabel}",
          role = "Resident",
          extension = entry.extension,
          isStaff = false,
          unit = entry.unitLabel,
        )
      }
    }
  }

  fun logout() {
    viewModelScope.launch {
      sipManager.unregister()
      sessionRepository.logout()
      // In-memory lists are not in Room, so clearing the database doesn't touch
      // them — without this the next account would briefly see the previous
      // resident's plan, events and contacts.
      _subscriptions.value = null
      _events.value = null
      _emergencyContacts.value = null
      _complaintError.value = null
      _visitorError.value = null
      _authError.value = null
      _isLoggedIn.value = false
      _activeTab.value = "home"
    }
  }

  fun switchResident(id: Long) {
    viewModelScope.launch {
      repository.switchResident(id)
      _snackbarMessage.value = "Switched current home"
    }
  }

  fun submitComplaint(
    title: String,
    category: String,
    description: String,
    priority: String = "Medium",
    onComplete: (String) -> Unit
  ) {
    viewModelScope.launch {
      val currentUnit = currentResident.value?.unit ?: "A-1201"
      val ticket = repository.submitComplaint(
        title = title,
        category = category,
        description = description,
        priority = priority,
        unit = currentUnit
      )
      _snackbarMessage.value = "Complaint $ticket registered successfully"
      onComplete(ticket)
    }
  }

  /**
   * Raises a real ticket on the server.
   *
   * `onComplete` is called with the server's ticket id only on success. It used
   * to be called with a locally-invented ticket number that was never sent
   * anywhere, so the resident was told their complaint was "registered" when
   * nothing had left the phone.
   */
  fun submitComplaintToServer(
    category: String,
    description: String,
    onResult: (success: Boolean, reference: String?) -> Unit,
  ) {
    _complaintSubmitting.value = true
    _complaintError.value = null
    viewModelScope.launch {
      sessionRepository.submitComplaint(category, description)
        .onSuccess { ticket ->
          _complaintSubmitting.value = false
          // Short, human-quotable reference from the server's UUID.
          val reference = ticket.id.take(8).uppercase()
          _snackbarMessage.value = "Complaint registered — reference $reference"
          onResult(true, reference)
        }
        .onFailure { err ->
          _complaintSubmitting.value = false
          _complaintError.value = err.message ?: "Could not register the complaint"
          onResult(false, null)
        }
    }
  }

  fun updateComplaintStatus(id: Long, newStatus: String) {
    viewModelScope.launch {
      repository.updateComplaintStatus(id, newStatus)
      _snackbarMessage.value = "Status updated to $newStatus"
    }
  }

  fun deleteComplaint(id: Long) {
    viewModelScope.launch {
      repository.deleteComplaint(id)
      _snackbarMessage.value = "Complaint record removed"
    }
  }

  /**
   * Pre-approves a visitor on the server.
   *
   * The old version stored the visitor in Room and announced a locally-invented
   * "pass code" that no guard could ever check. There is no pass code in the
   * product, so none is claimed here.
   */
  fun preApproveVisitorOnServer(
    name: String,
    type: String,
    hoursFromNow: Long,
    onResult: (Boolean) -> Unit,
  ) {
    _visitorSubmitting.value = true
    _visitorError.value = null
    viewModelScope.launch {
      sessionRepository.preApproveVisitor(
        name = name,
        type = type,
        scheduledAt = Instant.now().plus(hoursFromNow, ChronoUnit.HOURS),
      )
        .onSuccess {
          _visitorSubmitting.value = false
          _showPreApproveDialog.value = false
          _snackbarMessage.value = "${it.visitorName} is expected — the gate has been notified"
          onResult(true)
        }
        .onFailure { err ->
          _visitorSubmitting.value = false
          _visitorError.value = err.message ?: "Could not pre-approve this visitor"
          onResult(false)
        }
    }
  }

  fun markNoticeAsRead(id: Long) {
    viewModelScope.launch {
      repository.markNoticeAsRead(id)
    }
  }

  fun markAllNoticesAsRead() {
    viewModelScope.launch {
      repository.markAllNoticesAsRead()
      _snackbarMessage.value = "All notices marked as read"
    }
  }

  fun createCommunityNotice(
    title: String,
    description: String,
    category: String = "Announcement",
    priority: String = "Normal",
    author: String = "Society Management",
    isPinned: Boolean = false
  ) {
    viewModelScope.launch {
      repository.createCommunityNotice(
        title = title,
        description = description,
        category = category,
        priority = priority,
        author = author,
        isPinned = isPinned
      )
      _snackbarMessage.value = "Community notice posted successfully"
    }
  }

  fun deleteCommunityNotice(id: Long) {
    viewModelScope.launch {
      repository.deleteCommunityNotice(id)
      _snackbarMessage.value = "Notice removed"
    }
  }

  /**
   * Places a real SIP call, if this device is registered.
   *
   * An unregistered device genuinely cannot place the call, so it says so and
   * offers the extension instead — better than a button that appears dead.
   */
  fun startCall(contact: IntercomContact) {
    if (sipManager.registration.value != SipRegistration.REGISTERED) {
      if (sipConfigured) {
        registerSipIfPossible()
      } else {
        _snackbarMessage.value =
          "Calling isn't set up on this device — please tap 'Turn on calling here' above"
        return
      }
    }
    _activeCallContact.value = contact
    sipManager.callExtension(contact.extension)
  }

  /**
   * Turns on in-app calling for this device.
   *
   * Resets the SIP password as a side effect, because the server only returns a
   * plaintext password when it issues one — so every other device on this
   * extension is disconnected. The UI warns before calling this.
   */
  fun enableSipCalling(onResult: (Boolean) -> Unit) {
    _sipBusy.value = true
    _sipError.value = null
    viewModelScope.launch {
      sessionRepository.enableSipCalling()
        .onSuccess {
          _sipBusy.value = false
          registerSipIfPossible()
          onResult(true)
        }
        .onFailure {
          _sipBusy.value = false
          _sipError.value = it.message ?: "Could not set up calling on this device"
          onResult(false)
        }
    }
  }

  /** Registers with the PBX when credentials are already stored on this device. */
  fun registerSipIfPossible() {
    try {
      val creds = sessionRepository.storedSipCredentials() ?: return
      sipManager.register(
        extension = creds.extension,
        password = creds.password,
        domain = creds.domain,
        port = creds.port,
        useTls = creds.transport.equals("TLS", ignoreCase = true),
      )
    } catch (t: Throwable) {
      Log.e("BestNetViewModel", "Failed to register SIP stack", t)
    }
  }

  fun answerCall() = sipManager.answer()

  fun setMuted(value: Boolean) = sipManager.setMuted(value)

  fun setSpeaker(value: Boolean) = sipManager.setSpeaker(value)

  fun acknowledgeCallEnded() {
    sipManager.acknowledgeCallEnded()
    _activeCallContact.value = null
  }

  fun endCall() {
    sipManager.hangUp()
    _activeCallContact.value = null
  }

  fun openSpeedTest() {
    _showSpeedTest.value = true
  }

  fun closeSpeedTest() {
    _showSpeedTest.value = false
  }

  fun openPreApproveDialog() {
    _showPreApproveDialog.value = true
  }

  fun closePreApproveDialog() {
    _showPreApproveDialog.value = false
  }

  fun openSwitchHomeSheet() {
    _showSwitchHomeSheet.value = true
  }

  fun closeSwitchHomeSheet() {
    _showSwitchHomeSheet.value = false
  }

  fun showComingSoon(feature: String) {
    _snackbarMessage.value = "$feature isn't connected to a live system yet."
  }

  fun clearSnackbar() {
    _snackbarMessage.value = null
  }
}
