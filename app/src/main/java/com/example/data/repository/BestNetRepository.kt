package com.example.data.repository

import com.example.data.local.BestNetDao
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

  suspend fun switchResident(residentId: Long) {
    dao.switchCurrentResident(residentId)
  }

  suspend fun submitComplaint(category: String, description: String): String {
    val ticketNumber = "#C2026-" + (100..999).random()
    dao.insertComplaint(
      Complaint(
        ticketNumber = ticketNumber,
        category = category,
        description = description,
        status = "Submitted",
        createdAt = System.currentTimeMillis()
      )
    )
    return ticketNumber
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

  fun getIntercomStaff(): List<IntercomContact> {
    return listOf(
      IntercomContact(id = "mg", name = "Main Gate", role = "Call to Main Gate", extension = "101", isStaff = true),
      IntercomContact(id = "sec", name = "Security Office", role = "Call to Security", extension = "102", isStaff = true),
      IntercomContact(id = "mgmt", name = "Management Office", role = "Call to Management", extension = "103", isStaff = true)
    )
  }

  fun getIntercomNeighbors(): List<IntercomContact> {
    return listOf(
      IntercomContact(id = "n1", name = "Mr. Amit Kumar", role = "Resident", extension = "1202", isStaff = false, unit = "A-1202"),
      IntercomContact(id = "n2", name = "Mrs. Priya Sharma", role = "Resident", extension = "1203", isStaff = false, unit = "A-1203"),
      IntercomContact(id = "n3", name = "Mr. Sandeep Mehta", role = "Resident", extension = "1101", isStaff = false, unit = "A-1101"),
      IntercomContact(id = "n4", name = "Mr. Rajesh Patel", role = "Resident", extension = "904", isStaff = false, unit = "A-904"),
      IntercomContact(id = "n5", name = "Dr. Sneha Rao", role = "Resident", extension = "302", isStaff = false, unit = "B-302")
    )
  }
}
