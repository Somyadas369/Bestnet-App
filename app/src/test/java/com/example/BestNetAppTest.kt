package com.example

import com.example.data.model.CommunityNotice
import com.example.data.model.Complaint
import com.example.data.model.Notice
import com.example.data.model.Resident
import com.example.data.model.Visitor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BestNetAppTest {

  @Test
  fun testResidentModel() {
    val resident = Resident(
      id = 1L,
      name = "Rahul Sharma",
      phone = "+91 98765 43210",
      unit = "A-1201",
      communityName = "Sunrise Apartments",
      isCurrent = true
    )
    assertEquals("Rahul Sharma", resident.name)
    assertEquals("A-1201", resident.unit)
    assertTrue(resident.isCurrent)
  }

  @Test
  fun testComplaintModelStatuses() {
    val pendingComplaint = Complaint(
      id = 101L,
      ticketNumber = "#CMP-2026-302",
      title = "Main Kitchen Switchboard Sparking",
      category = "Electrician",
      description = "16A socket has loose wiring",
      unit = "A-1201",
      priority = "High",
      status = "Pending",
      assignedTo = "Awaiting Assignment"
    )
    assertEquals("Pending", pendingComplaint.status)
    assertEquals("Electrician", pendingComplaint.category)
    assertEquals("High", pendingComplaint.priority)

    val inProgressComplaint = pendingComplaint.copy(
      status = "In Progress",
      assignedTo = "Technician Ramesh K."
    )
    assertEquals("In Progress", inProgressComplaint.status)
    assertEquals("Technician Ramesh K.", inProgressComplaint.assignedTo)

    val resolvedComplaint = inProgressComplaint.copy(
      status = "Resolved",
      resolutionNotes = "Wiring tightened and replaced."
    )
    assertEquals("Resolved", resolvedComplaint.status)
    assertEquals("Wiring tightened and replaced.", resolvedComplaint.resolutionNotes)
  }

  @Test
  fun testVisitorModel() {
    val visitor = Visitor(
      id = 1L,
      name = "Rakesh Mehta",
      type = "Delivery",
      unit = "A-1201",
      timestampText = "04 Sep 2026, 10:25 AM",
      status = "Approved",
      isPreApproved = true,
      passCode = "582190"
    )
    assertEquals("Approved", visitor.status)
    assertNotNull(visitor.passCode)
    assertEquals("582190", visitor.passCode)
  }

  @Test
  fun testNoticeModel() {
    val notice = Notice(
      id = 1L,
      title = "Water Supply Update",
      body = "Water supply shut down tomorrow from 10 AM to 2 PM.",
      category = "Announcements",
      timestampText = "2 hours ago",
      iconType = "water",
      isRead = false
    )
    assertEquals("Water Supply Update", notice.title)
    assertEquals(false, notice.isRead)
  }

  @Test
  fun testCommunityNoticeModelAndChronologicalSorting() {
    val t0 = 1757000000000L
    val noticeOld = CommunityNotice(
      id = 1L,
      title = "Monsoon Tree Trimming",
      description = "Tree pruning along main driveway",
      timestamp = t0,
      category = "Environment",
      priority = "Normal",
      author = "Horticulture Team"
    )
    val noticeMid = CommunityNotice(
      id = 2L,
      title = "Elevator Maintenance",
      description = "Lift 2 under maintenance",
      timestamp = t0 + 3600000L,
      category = "Maintenance",
      priority = "Normal",
      author = "Elevator Desk"
    )
    val noticeNew = CommunityNotice(
      id = 3L,
      title = "Urgent: Water Supply Halt",
      description = "Tanks cleaning tomorrow",
      timestamp = t0 + 7200000L,
      category = "Maintenance",
      priority = "Urgent",
      author = "Facility Management"
    )

    assertEquals("Urgent: Water Supply Halt", noticeNew.title)
    assertEquals("Tanks cleaning tomorrow", noticeNew.description)
    assertEquals(t0 + 7200000L, noticeNew.timestamp)
    assertEquals("Urgent", noticeNew.priority)

    // Verify chronological sorting (newest first)
    val unsortedList = listOf(noticeOld, noticeNew, noticeMid)
    val sortedChronological = unsortedList.sortedByDescending { it.timestamp }

    assertEquals("Urgent: Water Supply Halt", sortedChronological[0].title)
    assertEquals("Elevator Maintenance", sortedChronological[1].title)
    assertEquals("Monsoon Tree Trimming", sortedChronological[2].title)
  }
}
