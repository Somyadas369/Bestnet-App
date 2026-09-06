package com.example.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * The BestNet API surface this app uses.
 *
 * Note what is NOT here: no `x-tenant-id` / `x-community-id` headers. Those are
 * for staff, whose access comes from a scoped RoleAssignment. A resident has no
 * RoleAssignment at all — they are linked to a home through UnitMembership — so
 * these endpoints are self-service and scoped by the bearer token alone.
 * Sending scope headers from this app would be meaningless at best.
 */
interface BestNetApi {

  /** 204 on success. The code goes out over WhatsApp; nothing is returned. */
  @POST("auth/otp/request")
  suspend fun requestOtp(@Body body: OtpRequestBody): Response<Unit>

  @POST("auth/otp/verify")
  suspend fun verifyOtp(@Body body: OtpVerifyBody): TokensDto

  @POST("auth/refresh")
  suspend fun refresh(@Body body: RefreshBody): TokensDto

  @POST("auth/logout")
  suspend fun logout(@Body body: LogoutBody): Response<Unit>

  @GET("users/me")
  suspend fun getMe(): UserDto

  /** The resident's linked homes. Empty list is normal for a new account. */
  @GET("me/units")
  suspend fun getMyUnits(): List<MembershipDto>

  @GET("notifications")
  suspend fun getNotifications(): List<NotificationDto>

  @GET("me/intercom")
  suspend fun getMyIntercom(): List<IntercomEndpointDto>

  /** Extensions the resident can dial in their own communities. */
  @GET("me/intercom-directory")
  suspend fun getIntercomDirectory(): List<IntercomDirectoryEntryDto>

  /**
   * Issues a new SIP password and returns it **once**.
   *
   * This is the only way the app can obtain a password to register with: the
   * server stores it one-way hashed and cannot hand back the existing one.
   * Side effect: every device currently registered on this extension is
   * disconnected, so this must only run on explicit user action.
   */
  @POST("intercom-endpoints/{id}/password-reset")
  suspend fun resetSipPassword(@Path("id") endpointId: String): IntercomEndpointDto

  /**
   * Ticket categories carry per-tenant SLA targets, so the ids differ per
   * tenant and cannot be hard-coded in the app. The tenant id comes from
   * `/me/units` (community.tenantId).
   */
  @GET("tenants/{tenantId}/ticket-categories")
  suspend fun getTicketCategories(@Path("tenantId") tenantId: String): List<TicketCategoryDto>

  /**
   * Resident-raised ticket. Self-service: authorised by the caller's active
   * UnitMembership on this unit, not by a staff permission.
   */
  @POST("units/{unitId}/tickets")
  suspend fun createTicket(@Path("unitId") unitId: String, @Body body: CreateTicketBody): TicketDto

  @GET("my/tickets")
  suspend fun getMyTickets(): List<TicketDto>

  /**
   * Pre-approve an expected visitor. Self-service, authorised by the caller's
   * UnitMembership. Always creates a PRE_APPROVED visit — a resident cannot
   * assert that someone is at the gate.
   */
  @POST("me/visitor-visits")
  suspend fun preApproveVisitor(@Body body: PreApproveVisitorBody): VisitorVisitDto

  /** Visits for every home the resident belongs to — their pre-approvals and guard-logged walk-ins. */
  @GET("me/visitor-visits")
  suspend fun getMyVisits(): List<VisitorVisitDto>

  @POST("me/visitor-visits/{id}/cancel")
  suspend fun cancelVisit(@Path("id") id: String, @Body body: CancelVisitBody): VisitorVisitDto

  /** What the resident's home(s) are subscribed to. Not a billing endpoint. */
  @GET("me/subscriptions")
  suspend fun getMySubscriptions(): List<SubscriptionDto>

  /** Upcoming scheduled events in the resident's communities. */
  @GET("me/events")
  suspend fun getMyEvents(): List<CommunityEventDto>

  @GET("me/emergency-contacts")
  suspend fun getMyEmergencyContacts(): List<EmergencyContactDto>
}
