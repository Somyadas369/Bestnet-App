package com.example

import com.example.data.remote.CommunityDto
import com.example.data.remote.MembershipDto
import com.example.data.remote.NotificationDto
import com.example.data.remote.UnitDto
import com.example.data.remote.TicketCategoryDto
import com.example.data.remote.TicketDto
import com.example.data.remote.ticketStatusLabel
import com.example.data.remote.toComplaint
import com.example.data.remote.UserDto
import com.example.data.remote.VisitorVisitDto
import com.example.data.remote.formatUnit
import com.example.data.remote.toVisitor
import com.example.data.remote.visitorStatusLabel
import com.example.data.remote.relativeTime
import com.example.data.remote.toNotice
import com.example.data.remote.toResident
import com.example.data.repository.matchCategory
import com.example.data.repository.toE164
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

/**
 * Covers the pure translation logic between the API and the app's models.
 *
 * This is the part worth testing without a device: it's where a wrong
 * assumption produces a screen that renders confidently and shows the wrong
 * thing, rather than crashing.
 */
class ApiMappingTest {

  private fun membership(
    unitNumber: String = "103",
    building: String? = "A",
    display: String? = null,
    community: String? = "Green Meadows",
    isPrimary: Boolean = true,
  ) = MembershipDto(
    id = "m1",
    role = "OWNER",
    isPrimary = isPrimary,
    unit = UnitDto(id = "u1", unitNumber = unitNumber, displayCode = display, buildingCode = building),
    community = community?.let { CommunityDto(id = "c1", name = it) },
  )

  @Test
  fun `unit label prefers the server's own display code`() {
    assertEquals("T2-1804", formatUnit(membership(display = "T2-1804")))
  }

  @Test
  fun `unit label combines building and number`() {
    assertEquals("A-103", formatUnit(membership()))
  }

  @Test
  fun `unit label degrades to bare number rather than emitting null`() {
    val label = formatUnit(membership(building = null))
    assertEquals("103", label)
    assertFalse(label.contains("null"))
  }

  @Test
  fun `resident falls back to a neutral name when the server has none`() {
    // fullName is genuinely null until someone sets it — this must not render
    // as "null" or crash.
    val r = membership().toResident(UserDto(id = "u", phone = "+919000000001"), isCurrent = true)
    assertEquals("Resident", r.name)
    assertEquals("+919000000001", r.phone)
    assertTrue(r.isCurrent)
  }

  @Test
  fun `missing community shows a dash, not null`() {
    val r = membership(community = null)
      .toResident(UserDto(id = "u", phone = "+91", fullName = "Somya Das"), isCurrent = false)
    assertEquals("—", r.communityName)
    assertEquals("Somya Das", r.name)
  }

  @Test
  fun `unknown notification categories still land somewhere visible`() {
    // Anything unrecognised must fall into a tab the user can actually reach;
    // silently filtering it out would hide real notifications.
    val n = NotificationDto(id = "n", title = "T", body = "B", category = "wibble").toNotice()
    assertEquals("Announcements", n.category)
  }

  @Test
  fun `service and community categories map to their own tabs`() {
    assertEquals("Services", NotificationDto("n", "T", category = "service").toNotice().category)
    assertEquals("Community", NotificationDto("n", "T", category = "event").toNotice().category)
  }

  @Test
  fun `read state comes from readAt`() {
    assertTrue(NotificationDto("n", "T", readAt = "2026-09-05T18:42:09.924Z").toNotice().isRead)
    assertFalse(NotificationDto("n", "T", readAt = null).toNotice().isRead)
  }

  @Test
  fun `relative time renders recent instants`() {
    assertEquals("just now", relativeTime(Instant.now().toString()))
    assertEquals("2h ago", relativeTime(Instant.now().minusSeconds(2 * 3600).toString()))
    assertEquals("3d ago", relativeTime(Instant.now().minusSeconds(3 * 86400).toString()))
  }

  @Test
  fun `relative time shows nothing rather than a wrong time`() {
    assertEquals("", relativeTime(null))
    assertEquals("", relativeTime("not-a-date"))
  }

  // ---- visitor mapping ----

  @Test
  fun `visitor status labels are readable`() {
    assertEquals("Pre-approved", visitorStatusLabel("PRE_APPROVED"))
    assertEquals("Inside", visitorStatusLabel("CHECKED_IN"))
    assertEquals("Denied", visitorStatusLabel("DENIED"))
    assertEquals("Cancelled", visitorStatusLabel("CANCELLED"))
  }

  @Test
  fun `an unmapped visit status is never shown as approved`() {
    // Telling a resident a denied or unknown visitor was "Approved" is the
    // worst failure this mapping could have, so unknowns pass through as-is.
    assertEquals("SOMETHING_NEW", visitorStatusLabel("SOMETHING_NEW"))
  }

  @Test
  fun `a visit never carries an invented pass code`() {
    // The server issues no pass code; the old local implementation made one up.
    val v = VisitorVisitDto(id = "v1", visitorName = "Ramesh", status = "PRE_APPROVED", kind = "PRE_APPROVED")
      .toVisitor("A-103")
    assertNull(v.passCode)
    assertTrue(v.isPreApproved)
    assertEquals("Ramesh", v.name)
    assertEquals("A-103", v.unit)
  }

  @Test
  fun `visit type falls back rather than showing an empty string`() {
    val v = VisitorVisitDto(id = "v", visitorName = "N", status = "ARRIVED", visitReason = null).toVisitor("A-1")
    assertEquals("Visitor", v.type)
  }

  @Test
  fun `walk-ins are not shown as pre-approved`() {
    val v = VisitorVisitDto(id = "v", visitorName = "N", status = "CHECKED_IN", kind = "WALK_IN").toVisitor("A-1")
    assertFalse(v.isPreApproved)
  }

  // ---- ticket mapping ----

  @Test
  fun `ticket statuses map to the buckets the UI filters by`() {
    assertEquals("Pending", ticketStatusLabel("NEW"))
    assertEquals("In Progress", ticketStatusLabel("EN_ROUTE"))
    assertEquals("Resolved", ticketStatusLabel("CLOSED"))
    assertEquals("Cancelled", ticketStatusLabel("CANCELLED"))
  }

  @Test
  fun `an unknown ticket status is never reported as resolved`() {
    // Telling a resident their problem is fixed when it isn't is the worst
    // failure available here, so unknowns pass through untouched.
    assertEquals("SOMETHING_NEW", ticketStatusLabel("SOMETHING_NEW"))
    assertEquals("Pending", ticketStatusLabel(null))
  }

  @Test
  fun `ticket reference comes from the server id, not invented`() {
    val t = TicketDto(id = "df58c6b9-ed9a-445e-b7f0-13ff5549c0b1", title = "Electrician", status = "NEW")
      .toComplaint("A-103", "Electrician")
    assertEquals("#DF58C6B9", t.ticketNumber)
    assertEquals("A-103", t.unit)
    assertEquals("Pending", t.status)
  }

  @Test
  fun `a ticket with no known category name falls back to its title`() {
    val t = TicketDto(id = "abc12345", title = "Plumber", status = "NEW").toComplaint("A-1", null)
    assertEquals("Plumber", t.category)
  }

  @Test
  fun `missing timestamps do not crash the mapper`() {
    val t = TicketDto(id = "abc12345", title = "X", status = "NEW", createdAt = null).toComplaint("A-1", null)
    assertTrue(t.createdAt > 0)
  }

  // ---- ticket category matching ----

  private val serverCategories = listOf(
    TicketCategoryDto(id = "c-plumb", name = "Plumbing"),
    TicketCategoryDto(id = "c-elec", name = "Electrician"),
    TicketCategoryDto(id = "c-house", name = "Housekeeping"),
    TicketCategoryDto(id = "c-net", name = "Internet Issue"),
    TicketCategoryDto(id = "c-gen", name = "General Maintenance"),
    TicketCategoryDto(id = "c-other", name = "Others"),
  )

  @Test
  fun `exact category names match`() {
    assertEquals("c-elec", matchCategory("Electrician", serverCategories)?.id)
    assertEquals("c-net", matchCategory("Internet Issue", serverCategories)?.id)
  }

  @Test
  fun `the app's Plumber tile maps to the server's Plumbing category`() {
    // The two vocabularies were written independently; without the alias this
    // silently fails and the resident cannot file a plumbing complaint at all.
    assertEquals("c-plumb", matchCategory("Plumber", serverCategories)?.id)
  }

  @Test
  fun `matching ignores case and surrounding space`() {
    assertEquals("c-house", matchCategory("  housekeeping ", serverCategories)?.id)
  }

  @Test
  fun `an unknown category returns null rather than guessing`() {
    // Filing under an arbitrary category would send the complaint to the wrong
    // team with the wrong SLA, which is worse than refusing.
    assertNull(matchCategory("Elevator", serverCategories))
  }

  @Test
  fun `a tenant missing the category returns null`() {
    assertNull(matchCategory("Plumber", listOf(TicketCategoryDto(id = "x", name = "Security"))))
  }

  @Test
  fun `phone numbers become E164 for the API`() {
    assertEquals("+919000000001", toE164("9000000001"))
    assertEquals("+919000000001", toE164("919000000001"))
    assertEquals("+919000000001", toE164("+91 90000 00001"))
  }
}
