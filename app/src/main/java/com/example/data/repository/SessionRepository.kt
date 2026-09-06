package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.BestNetDao
import com.example.data.remote.ApiClient
import com.example.data.remote.CancelVisitBody
import com.example.data.remote.CommunityEventDto
import com.example.data.remote.EmergencyContactDto
import com.example.data.remote.IntercomDirectoryEntryDto
import com.example.data.remote.CreateTicketBody
import com.example.data.remote.IntercomEndpointDto
import com.example.data.remote.PreApproveVisitorBody
import com.example.data.remote.TicketCategoryDto
import com.example.data.remote.SubscriptionDto
import com.example.data.remote.TicketDto
import com.example.data.remote.VisitorVisitDto
import com.example.data.remote.toComplaint
import com.example.data.remote.toVisitor
import java.time.Instant
import java.time.format.DateTimeFormatter
import com.example.data.remote.LogoutBody
import com.example.data.remote.OtpRequestBody
import com.example.data.remote.OtpVerifyBody
import com.example.data.remote.TokenStore
import com.example.data.remote.toNotice
import com.example.data.remote.toResident
import com.example.data.remote.unauthenticatedApi

/**
 * Everything that talks to the BestNet server: login, session, and pulling the
 * resident's real data down into Room.
 *
 * Room stays the single source the UI reads from — screens keep observing the
 * same Flows they always did, and gain real data without being rewritten.
 * That also means the app still renders the last known state with no network,
 * which matters on a phone.
 */
class SessionRepository(context: Context, private val dao: BestNetDao) {

  val tokenStore = TokenStore(context.applicationContext)
  private val client = ApiClient(tokenStore)

  val isLoggedIn: Boolean get() = tokenStore.isLoggedIn

  /** Sends the OTP. The code arrives over WhatsApp; nothing comes back here. */
  suspend fun requestOtp(phone: String): Result<Unit> = runCatching {
    val res = unauthenticatedApi().requestOtp(OtpRequestBody(toE164(phone)))
    if (!res.isSuccessful) error("Could not send the code (${res.code()})")
  }

  /** Verifies the code and stores the session. */
  suspend fun verifyOtp(phone: String, code: String): Result<Unit> = runCatching {
    val tokens = unauthenticatedApi().verifyOtp(OtpVerifyBody(toE164(phone), code.trim()))
    tokenStore.save(tokens)
  }

  /**
   * Pulls the resident's identity, homes and notifications into Room.
   *
   * Returns failure only if identity could not be fetched — that means the
   * session is unusable. Notifications failing is not fatal: the user should
   * still get into an app that shows their home.
   */
  suspend fun syncFromServer(): Result<Unit> {
    val me = client.call { getMe() }.getOrElse { return Result.failure(it) }
    val memberships = client.call { getMyUnits() }.getOrElse { return Result.failure(it) }

    val previouslySelected = tokenStore.selectedMembershipId
    val selectedIndex = memberships.indexOfFirst { it.id == previouslySelected }
      .takeIf { it >= 0 }
      ?: memberships.indexOfFirst { it.isPrimary }.takeIf { it >= 0 }
      ?: 0

    dao.clearResidents()
    if (memberships.isNotEmpty()) {
      dao.insertResidents(
        memberships.mapIndexed { index, m -> m.toResident(me, isCurrent = index == selectedIndex) },
      )
      val selected = memberships[selectedIndex]
      tokenStore.selectedMembershipId = selected.id
      tokenStore.selectedUnitId = selected.unit.id
      tokenStore.selectedTenantId = selected.community?.tenantId
    }

    client.call { getNotifications() }
      .onSuccess { notifications ->
        dao.clearNotices()
        dao.insertNotices(notifications.map { it.toNotice() })
      }
      .onFailure { Log.w(TAG, "Notifications sync failed; keeping cached notices", it) }

    syncVisits()
    syncTickets()

    return Result.success(Unit)
  }

  /**
   * Raises a real ticket against the resident's unit.
   *
   * Returns the server's ticket id on success. Failure is returned, never
   * swallowed: the previous local-only implementation invented a ticket number
   * and stored it in Room, so residents believed they had reported something
   * nobody would ever see.
   */
  suspend fun submitComplaint(uiCategory: String, description: String): Result<TicketDto> {
    val unitId = tokenStore.selectedUnitId
      ?: return Result.failure(IllegalStateException("No home selected — pull down to refresh and try again"))
    val tenantId = tokenStore.selectedTenantId
      ?: return Result.failure(IllegalStateException("No community linked to this home"))

    val categories = client.call { getTicketCategories(tenantId) }
      .getOrElse { return Result.failure(it) }
    val category = matchCategory(uiCategory, categories)
      ?: return Result.failure(
        IllegalStateException("\"$uiCategory\" isn't set up for your community yet"),
      )

    return client.call {
      createTicket(
        unitId,
        CreateTicketBody(
          categoryId = category.id,
          // The API requires a title; the UI only collects a category and a
          // description, so the category doubles as the title rather than
          // truncating the description into something misleading.
          title = uiCategory,
          description = description.trim(),
        ),
      )
    }.also { if (it.isSuccess) syncTickets() }
  }

  /**
   * Pulls the resident's real tickets into Room, replacing whatever is there.
   *
   * This was the gap that made complaints confusing: submitting filed a real
   * ticket on the server, but the list on screen still came from Room, so the
   * resident saw seeded sample complaints and never their own.
   *
   * Category names need a second call — a ticket carries only `categoryId` —
   * and a failure there is not fatal: the ticket's title is used instead of
   * hiding the ticket entirely.
   */
  private suspend fun syncTickets() {
    client.call { getMyTickets() }
      .onSuccess { tickets ->
        val unitLabel = dao.currentResidentOnce()?.unit ?: ""
        val names: Map<String, String> = tokenStore.selectedTenantId
          ?.let { tenantId ->
            client.call { getTicketCategories(tenantId) }
              .getOrDefault(emptyList())
              .associate { it.id to it.name }
          }
          .orEmpty()
        dao.clearComplaints()
        dao.insertComplaints(tickets.map { it.toComplaint(unitLabel, names[it.categoryId]) })
      }
      .onFailure { Log.w(TAG, "Ticket sync failed; keeping cached complaints", it) }
  }

  /**
   * Pre-approves an expected visitor against the resident's own unit.
   *
   * Returns the created visit. Note there is no pass code in it — the server
   * doesn't issue one, and the previous local implementation invented a 6-digit
   * code that no guard could have checked.
   */
  suspend fun preApproveVisitor(
    name: String,
    type: String,
    scheduledAt: Instant,
    phone: String? = null,
    vehicleNumber: String? = null,
  ): Result<VisitorVisitDto> {
    val unitId = tokenStore.selectedUnitId
      ?: return Result.failure(IllegalStateException("No home selected — pull down to refresh and try again"))

    val result = client.call {
      preApproveVisitor(
        PreApproveVisitorBody(
          unitId = unitId,
          visitorName = name.trim(),
          scheduledAt = DateTimeFormatter.ISO_INSTANT.format(scheduledAt),
          visitorPhone = phone?.trim()?.takeIf { it.isNotEmpty() },
          vehicleNumber = vehicleNumber?.trim()?.takeIf { it.isNotEmpty() },
          visitReason = type.trim().takeIf { it.isNotEmpty() },
        ),
      )
    }
    // Refresh the local log so the new visit appears immediately rather than
    // only after the next full sync.
    if (result.isSuccess) syncVisits()
    return result
  }

  suspend fun cancelVisit(visitId: String, reason: String? = null): Result<VisitorVisitDto> =
    client.call { cancelVisit(visitId, CancelVisitBody(reason)) }.also { if (it.isSuccess) syncVisits() }

  /** Pulls the visit log into Room. Safe to call on its own. */
  private suspend fun syncVisits() {
    client.call { getMyVisits() }
      .onSuccess { visits ->
        val unitLabel = dao.currentResidentOnce()?.unit ?: ""
        dao.clearVisitors()
        dao.insertVisitors(visits.map { it.toVisitor(unitLabel) })
      }
      .onFailure { Log.w(TAG, "Visit log sync failed; keeping cached visits", it) }
  }

  /**
   * Services and community data.
   *
   * These are read-only lists held in memory by the ViewModel rather than
   * mirrored into Room, unlike residents/notices/visits. They are small, always
   * fetched together on sync, and nothing writes to them — adding three Room
   * entities to gain offline caching of a plan name and four phone numbers
   * wasn't worth the schema churn.
   * TODO: emergency contacts arguably *should* be cached offline — they're the
   * one thing you want when the network is down.
   */
  suspend fun mySubscriptions(): List<SubscriptionDto> =
    client.call { getMySubscriptions() }.getOrDefault(emptyList())

  suspend fun myEvents(): List<CommunityEventDto> =
    client.call { getMyEvents() }.getOrDefault(emptyList())

  suspend fun myEmergencyContacts(): List<EmergencyContactDto> =
    client.call { getMyEmergencyContacts() }.getOrDefault(emptyList())

  /** The resident's own SIP extension, or null if none is provisioned. */
  suspend fun myIntercom(): IntercomEndpointDto? =
    client.call { getMyIntercom() }.getOrNull()?.firstOrNull { it.status == "ACTIVE" }

  /** Extensions the resident can dial. Empty when nobody else has one yet. */
  suspend fun intercomDirectory(): List<IntercomDirectoryEntryDto> =
    client.call { getIntercomDirectory() }.getOrDefault(emptyList())

  /**
   * Ends the session server-side, then locally. The local clear happens even if
   * the network call fails — a user who taps Logout must end up logged out.
   */
  suspend fun logout() {
    val refresh = tokenStore.refreshToken
    if (!refresh.isNullOrBlank()) {
      runCatching { client.api.logout(LogoutBody(refresh)) }
        .onFailure { Log.w(TAG, "Server logout failed; clearing local session anyway", it) }
    }
    tokenStore.clear()
    // Every table holding server data must go, not just the obvious two.
    // Visitors and complaints were missed originally, so after a logout the
    // next person to sign in on the same device saw the previous resident's
    // visitor log and tickets until a sync happened to replace them — and sync
    // only replaces on success. On a shared test handset that is a real leak
    // between accounts.
    runCatching {
      dao.clearResidents()
      dao.clearNotices()
      dao.clearVisitors()
      dao.clearComplaints()
    }
  }

  private companion object {
    const val TAG = "SessionRepository"
  }
}

/**
 * Matches a UI category tile to a server ticket category.
 *
 * The two vocabularies were written independently and don't line up — the app
 * says "Plumber", the tenant's category is called "Plumbing". Rather than
 * renaming server data that staff also use, or hard-coding UUIDs that differ per
 * tenant, match on name with a small alias table.
 *
 * Returns null when nothing matches, so the caller can tell the resident their
 * community hasn't set that category up — instead of silently filing the
 * complaint under the wrong one.
 */
fun matchCategory(uiCategory: String, categories: List<TicketCategoryDto>): TicketCategoryDto? {
  val wanted = uiCategory.trim().lowercase()
  categories.firstOrNull { it.name.trim().lowercase() == wanted }?.let { return it }

  val aliases = when (wanted) {
    "plumber" -> listOf("plumbing")
    "electrician" -> listOf("electrical", "electricity")
    "internet issue" -> listOf("internet", "broadband", "network")
    "general maintenance" -> listOf("maintenance", "general")
    "others" -> listOf("other", "general")
    else -> emptyList()
  }
  for (alias in aliases) {
    categories.firstOrNull { it.name.trim().lowercase() == alias }?.let { return it }
  }
  return null
}

/**
 * The UI collects a bare 10-digit number; the API wants E.164. India-only,
 * matching the rest of the product — the login screen hard-codes the +91 prefix.
 */
fun toE164(phone: String): String {
  val digits = phone.filter(Char::isDigit)
  return when {
    digits.length == 10 -> "+91$digits"
    digits.length == 12 && digits.startsWith("91") -> "+$digits"
    phone.startsWith("+") -> phone
    else -> "+$digits"
  }
}
