package com.example.data.remote

import com.example.data.model.Complaint
import com.example.data.model.Notice
import com.example.data.model.Resident
import com.example.data.model.Visitor
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Translates server shapes into the app's Room entities.
 *
 * Kept separate from both so the UI never learns the API's vocabulary and the
 * API never has to match Room's. The app's entities are flat and
 * display-oriented (`Resident.unit` is the string "A-101"); the server's are
 * normalised (User + UnitMembership + Unit + Community).
 */

/**
 * One linked home becomes one `Resident` row — the app's model of "who am I,
 * in which home", which is why a person with two homes produces two rows and
 * the switch-home sheet works unchanged.
 *
 * Room ids are autoGenerate Longs while server ids are UUID strings, so the
 * server id cannot be carried across. Rows are keyed on their natural content
 * instead, which is why sync replaces the table rather than upserting.
 */
fun MembershipDto.toResident(user: UserDto, isCurrent: Boolean): Resident = Resident(
  name = user.fullName?.takeIf { it.isNotBlank() } ?: "Resident",
  phone = user.phone,
  unit = formatUnit(this),
  communityName = community?.name ?: "—",
  isCurrent = isCurrent,
)

/**
 * Renders "A-101" style labels from whatever parts the server actually has.
 * Every component is optional in the schema, so this degrades to the bare unit
 * number rather than emitting "null-101".
 */
fun formatUnit(membership: MembershipDto): String {
  val u = membership.unit
  u.displayCode?.takeIf { it.isNotBlank() }?.let { return it }
  val building = u.buildingCode?.takeIf { it.isNotBlank() }
  val number = u.unitNumber
  return if (building != null) "$building-$number" else number
}

/**
 * A server visit becomes the app's flat `Visitor` row.
 *
 * `passCode` is deliberately always null: the server issues no pass code, and
 * the previous local implementation invented a 6-digit one that no guard could
 * ever verify.
 */
fun VisitorVisitDto.toVisitor(unitLabel: String): Visitor = Visitor(
  name = visitorName,
  type = visitReason?.takeIf { it.isNotBlank() } ?: "Visitor",
  unit = unitLabel,
  timestampText = visitorTimestamp(this),
  status = visitorStatusLabel(status),
  isPreApproved = kind == "PRE_APPROVED",
  passCode = null,
)

/**
 * Shows the moment that actually matters for each state — when they came in,
 * when they're expected, or when the record was made — rather than always the
 * creation time, which would read as wrong for a scheduled visit.
 */
private fun visitorTimestamp(v: VisitorVisitDto): String = when {
  v.checkedOutAt != null -> "Left ${relativeTime(v.checkedOutAt)}"
  v.checkedInAt != null -> "Entered ${relativeTime(v.checkedInAt)}"
  v.scheduledAt != null && v.status == "PRE_APPROVED" -> "Expected ${absoluteShort(v.scheduledAt)}"
  else -> relativeTime(v.createdAt)
}

/**
 * The server's state machine has 14 statuses; the UI shows a short label.
 * Unknown values fall through to the raw status rather than being forced into
 * "Approved" — telling a resident a denied visitor was approved would be the
 * worst possible failure here.
 */
fun visitorStatusLabel(status: String): String = when (status) {
  "PRE_APPROVED" -> "Pre-approved"
  "RESIDENT_APPROVED", "SELF_APPROVED", "VERIFIED" -> "Approved"
  "CHECKED_IN" -> "Inside"
  "CHECKED_OUT" -> "Left"
  "DENIED" -> "Denied"
  "CANCELLED" -> "Cancelled"
  "EXPIRED" -> "Expired"
  "NO_SHOW" -> "No show"
  "ARRIVED" -> "At gate"
  "CALLING_RESIDENT" -> "Calling you"
  "DRAFT" -> "Pending"
  else -> status
}

/** "6 Sep, 14:30" for a scheduled visit, where a relative time reads oddly. */
fun absoluteShort(iso: String?): String {
  if (iso.isNullOrBlank()) return ""
  return try {
    Instant.parse(iso)
      .atZone(ZoneId.systemDefault())
      .format(DateTimeFormatter.ofPattern("d MMM, HH:mm"))
  } catch (_: DateTimeParseException) {
    ""
  }
}

/**
 * A server ticket becomes the app's `Complaint` row.
 *
 * `ticketNumber` is derived from the server's UUID rather than invented — the
 * previous local implementation generated "#CMP-2026-" plus a random number
 * that corresponded to nothing.
 */
fun TicketDto.toComplaint(unitLabel: String, categoryName: String?): Complaint = Complaint(
  ticketNumber = "#" + id.take(8).uppercase(),
  title = title,
  category = categoryName ?: title,
  description = description.orEmpty(),
  unit = unitLabel,
  priority = priority?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Medium",
  status = ticketStatusLabel(status),
  createdAt = epochMillis(createdAt),
  updatedAt = epochMillis(updatedAt ?: createdAt),
)

/**
 * The server's 10-state ticket machine onto the three buckets the UI filters
 * by. Unknown states pass through rather than defaulting to "Resolved", which
 * would tell a resident their problem was fixed when it wasn't.
 */
fun ticketStatusLabel(status: String?): String = when (status) {
  "NEW", "TRIAGED" -> "Pending"
  "ASSIGNED", "ACCEPTED", "EN_ROUTE", "ON_SITE", "HAPPY_CODE_PENDING" -> "In Progress"
  "ON_HOLD" -> "On Hold"
  "CLOSED" -> "Resolved"
  "CANCELLED" -> "Cancelled"
  null -> "Pending"
  else -> status
}

/** Room stores timestamps as epoch millis; the API sends ISO-8601. */
fun epochMillis(iso: String?): Long = try {
  if (iso.isNullOrBlank()) System.currentTimeMillis() else Instant.parse(iso).toEpochMilli()
} catch (_: DateTimeParseException) {
  System.currentTimeMillis()
}

/**
 * Subscription status for display. SUSPENDED especially must not be softened —
 * a resident whose connection is suspended needs to see that word.
 */
fun subscriptionStatusLabel(status: String): String = when (status) {
  "ACTIVE" -> "Active"
  "SUSPENDED" -> "Suspended"
  "EXPIRED" -> "Expired"
  "CANCELLED" -> "Cancelled"
  else -> status
}

/** "Sun 11 Sep · 18:30" — an event's date and time in one line. */
fun eventWhen(iso: String?): String? {
  if (iso.isNullOrBlank()) return null
  return try {
    Instant.parse(iso)
      .atZone(ZoneId.systemDefault())
      .format(DateTimeFormatter.ofPattern("EEE d MMM · HH:mm"))
  } catch (_: DateTimeParseException) {
    null
  }
}

/** "29 Sep 2026" — for dates where a relative time would be unhelpful. */
fun absoluteDate(iso: String?): String {
  if (iso.isNullOrBlank()) return ""
  return try {
    Instant.parse(iso).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMM yyyy"))
  } catch (_: DateTimeParseException) {
    ""
  }
}

fun NotificationDto.toNotice(): Notice = Notice(
  title = title,
  body = body.orEmpty(),
  category = mapCategory(category),
  timestampText = relativeTime(createdAt),
  isRead = readAt != null,
  iconType = mapIcon(category),
)

/**
 * The server's free-form `category` string onto the three tabs the UI filters
 * by. Anything unrecognised falls into "Announcements" rather than vanishing:
 * a notification the user cannot see anywhere is worse than one filed oddly.
 */
private fun mapCategory(category: String?): String = when (category?.lowercase()) {
  "service", "services", "maintenance", "ticket" -> "Services"
  "community", "event", "events" -> "Community"
  else -> "Announcements"
}

private fun mapIcon(category: String?): String = when (category?.lowercase()) {
  "maintenance", "ticket" -> "maintenance"
  "service", "services" -> "water"
  "community", "event", "events" -> "event"
  "visitor", "visit" -> "visitor"
  "complaint" -> "complaint"
  else -> "event"
}

/**
 * "2h ago" from an ISO-8601 instant. Returns "" for missing or unparseable
 * input — showing nothing beats showing a wrong or fabricated time.
 */
fun relativeTime(iso: String?): String {
  if (iso.isNullOrBlank()) return ""
  val then = try {
    Instant.parse(iso)
  } catch (_: DateTimeParseException) {
    return ""
  }
  val elapsed = Duration.between(then, Instant.now())
  if (elapsed.isNegative) return "just now"
  val minutes = elapsed.toMinutes()
  return when {
    minutes < 1 -> "just now"
    minutes < 60 -> "${minutes}m ago"
    minutes < 60 * 24 -> "${elapsed.toHours()}h ago"
    elapsed.toDays() < 7 -> "${elapsed.toDays()}d ago"
    else -> "${elapsed.toDays() / 7}w ago"
  }
}
