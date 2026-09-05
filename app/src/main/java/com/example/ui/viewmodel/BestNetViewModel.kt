package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BestNetDatabase
import com.example.data.model.Complaint
import com.example.data.model.IntercomContact
import com.example.data.model.Notice
import com.example.data.model.Resident
import com.example.data.model.Visitor
import com.example.data.repository.BestNetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BestNetViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: BestNetRepository

  val currentResident: StateFlow<Resident?>
  val allResidents: StateFlow<List<Resident>>
  val allComplaints: StateFlow<List<Complaint>>
  val allVisitors: StateFlow<List<Visitor>>
  val allNotices: StateFlow<List<Notice>>

  val intercomStaff: List<IntercomContact>
  val intercomNeighbors: List<IntercomContact>

  private val _isLoggedIn = MutableStateFlow(true)
  val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

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

    currentResident = repository.currentResident.stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000),
      Resident(name = "Rahul Sharma", phone = "+91 98765 43210", unit = "A-1201", communityName = "Sunrise Apartments", isCurrent = true)
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

    intercomStaff = repository.getIntercomStaff()
    intercomNeighbors = repository.getIntercomNeighbors()
  }

  fun setTab(tab: String) {
    _activeTab.value = tab
  }

  fun login() {
    _isLoggedIn.value = true
  }

  fun logout() {
    _isLoggedIn.value = false
    _activeTab.value = "home"
  }

  fun switchResident(id: Long) {
    viewModelScope.launch {
      repository.switchResident(id)
      _snackbarMessage.value = "Switched current home"
    }
  }

  fun submitComplaint(category: String, description: String, onComplete: (String) -> Unit) {
    viewModelScope.launch {
      val ticket = repository.submitComplaint(category, description)
      _snackbarMessage.value = "Complaint $ticket registered successfully"
      onComplete(ticket)
    }
  }

  fun preApproveVisitor(name: String, type: String) {
    viewModelScope.launch {
      val currentUnit = currentResident.value?.unit ?: "A-1201"
      val pass = repository.addPreApprovedVisitor(name, type, currentUnit)
      _showPreApproveDialog.value = false
      _snackbarMessage.value = "Pass code generated: $pass"
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

  fun startCall(contact: IntercomContact) {
    _activeCallContact.value = contact
  }

  fun endCall() {
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
