package com.example.data.repository

import com.example.data.local.BestNetDao
import com.example.data.model.CommunityNotice
import com.example.data.model.Complaint
import com.example.data.model.IntercomContact
import com.example.data.model.Notice
import com.example.data.model.Resident
import com.example.data.model.Visitor
import kotlinx.coroutines.flow.Flow

class BestNetRepository(private val dao: BestNetDao) {

  val allResidents: Flow<List<Resident>> = dao.getAllResidents()
  val currentResident: Flow<Resident?> = dao.getCurrentResident()
  val allComplaints: Flow<List<Complaint>> = dao.getAllComplaints()
  val allVisitors: Flow<List<Visitor>> = dao.getAllVisitors()
  val allNotices: Flow<List<Notice>> = dao.getAllNotices()
  val allCommunityNotices: Flow<List<CommunityNotice>> = dao.getAllCommunityNotices()

  suspend fun switchResident(residentId: Long) {
    dao.switchCurrentResident(residentId)
  }

  fun getComplaintsByStatus(status: String): Flow<List<Complaint>> {
    return dao.getComplaintsByStatus(status)
  }

  suspend fun submitComplaint(
    title: String,
    category: String,
    description: String,
    priority: String = "Medium",
    unit: String = "A-1201"
  ): String {
    val ticketNumber = "#CMP-2026-" + (100..999).random()
    dao.insertComplaint(
      Complaint(
        ticketNumber = ticketNumber,
        title = title,
        category = category,
        description = description,
        unit = unit,
        priority = priority,
        status = "Pending",
        assignedTo = "Awaiting Assignment",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
      )
    )
    return ticketNumber
  }

  suspend fun updateComplaintStatus(id: Long, status: String) {
    dao.updateComplaintStatus(id, status)
  }

  suspend fun deleteComplaint(id: Long) {
    dao.deleteComplaintById(id)
  }

  suspend fun addPreApprovedVisitor(name: String, type: String, unit: String): String {
    val passCode = (100000..999999).random().toString()
    dao.insertVisitor(
      Visitor(
        name = name,
        type = type,
        unit = unit,
        timestampText = "Pre-Approved (Today)",
        status = "Approved",
        isPreApproved = true,
        passCode = passCode
      )
    )
    return passCode
  }

  suspend fun markNoticeAsRead(noticeId: Long) {
    dao.markNoticeAsRead(noticeId)
  }

  suspend fun markAllNoticesAsRead() {
    dao.markAllNoticesAsRead()
  }

  suspend fun createCommunityNotice(
    title: String,
    description: String,
    category: String = "Announcement",
    priority: String = "Normal",
    author: String = "Society Management",
    isPinned: Boolean = false
  ): Long {
    return dao.insertCommunityNotice(
      CommunityNotice(
        title = title,
        description = description,
        timestamp = System.currentTimeMillis(),
        category = category,
        priority = priority,
        author = author,
        isPinned = isPinned
      )
    )
  }

  suspend fun deleteCommunityNotice(id: Long) {
    dao.deleteCommunityNotice(id)
  }

  /**
   * Empty on purpose.
   *
   * This used to return "Main Gate" on extension 101 and "Management Office" on
   * 103. Neither exists: real extensions are the 6-digit sipUsername the backend
   * generates, and no gate or office endpoint has ever been provisioned.
   * Dialling either would have reached nothing.
   *
   * Gate and management stations become real the moment someone provisions
   * endpoints for them — at which point they arrive through
   * GET /me/intercom-directory like any other extension, and this can go.
   */
  fun getIntercomStaff(): List<IntercomContact> = emptyList()

  /** Superseded by the server directory — see BestNetViewModel.intercomDirectory. */
  fun getIntercomNeighbors(): List<IntercomContact> = emptyList()
}
