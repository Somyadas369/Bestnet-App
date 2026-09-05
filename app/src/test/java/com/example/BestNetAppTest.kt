package com.example

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
  fun testComplaintModel() {
    val complaint = Complaint(
      id = 101L,
      ticketNumber = "#C2026-891",
      category = "Plumber",
      description = "Kitchen sink leaking",
      status = "In Progress",
      createdAt = "04 Sep 2026, 09:30 AM"
    )
    assertEquals("#C2026-891", complaint.ticketNumber)
    assertEquals("Plumber", complaint.category)
    assertEquals("In Progress", complaint.status)
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
}
