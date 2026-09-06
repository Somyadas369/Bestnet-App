package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Wire models for the BestNet API (https://crm.bestnet.in/api/v1).
 *
 * These deliberately mirror the server's shapes rather than the app's Room
 * entities — the two are different vocabularies (the server has User +
 * UnitMembership + Community; the app has a flat `Resident`). Mapping happens
 * in ApiMappers.kt so a server change doesn't ripple into the UI.
 *
 * Every field the server may omit is nullable. The API returns nulls for real
 * (`fullName` is null until someone sets it), so non-null types here would
 * crash at parse time on perfectly valid responses.
 */

@JsonClass(generateAdapter = true)
data class OtpRequestBody(val phone: String)

@JsonClass(generateAdapter = true)
data class OtpVerifyBody(val phone: String, val code: String)

@JsonClass(generateAdapter = true)
data class RefreshBody(val refreshToken: String)

@JsonClass(generateAdapter = true)
data class LogoutBody(val refreshToken: String)

@JsonClass(generateAdapter = true)
data class TokensDto(
  val accessToken: String,
  val refreshToken: String,
  val expiresIn: Int? = null,
  val user: UserDto? = null,
)

@JsonClass(generateAdapter = true)
data class UserDto(
  val id: String,
  val phone: String,
  val fullName: String? = null,
  val status: String? = null,
)

@JsonClass(generateAdapter = true)
data class UnitDto(
  val id: String,
  val unitNumber: String,
  val displayCode: String? = null,
  val floorCode: String? = null,
  val buildingCode: String? = null,
)

@JsonClass(generateAdapter = true)
data class CommunityDto(
  val id: String,
  val name: String,
  val slug: String? = null,
  val tenantId: String? = null,
)

@JsonClass(generateAdapter = true)
data class MembershipDto(
  val id: String,
  val role: String,
  val isPrimary: Boolean = false,
  val unit: UnitDto,
  val community: CommunityDto? = null,
)

@JsonClass(generateAdapter = true)
data class NotificationDto(
  val id: String,
  val title: String,
  val body: String? = null,
  val category: String? = null,
  val createdAt: String? = null,
  val readAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class SipServerProfileDto(
  val name: String? = null,
  val domain: String? = null,
  val port: Int? = null,
  val transport: String? = null,
)

@JsonClass(generateAdapter = true)
data class IntercomEndpointDto(
  val id: String,
  val sipUsername: String,
  val status: String? = null,
  val type: String? = null,
  val serverProfile: SipServerProfileDto? = null,
)

@JsonClass(generateAdapter = true)
data class TicketCategoryDto(
  val id: String,
  val name: String,
  val slaResponseMinutes: Int? = null,
  val slaResolutionMinutes: Int? = null,
)

@JsonClass(generateAdapter = true)
data class CreateTicketBody(
  val categoryId: String,
  val title: String,
  val description: String,
  val priority: String = "MEDIUM",
)

@JsonClass(generateAdapter = true)
data class TicketDto(
  val id: String,
  val title: String,
  val description: String? = null,
  val status: String? = null,
  val priority: String? = null,
  val categoryId: String? = null,
  val createdAt: String? = null,
  val updatedAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class PreApproveVisitorBody(
  val unitId: String,
  val visitorName: String,
  val scheduledAt: String,
  val visitorPhone: String? = null,
  val vehicleNumber: String? = null,
  val visitReason: String? = null,
)

/**
 * Note there is no pass code here. The server does not issue one — the previous
 * local implementation invented a 6-digit code and showed it to the resident as
 * though a guard could check it.
 */
@JsonClass(generateAdapter = true)
data class VisitorVisitDto(
  val id: String,
  val visitorName: String,
  val status: String,
  val kind: String? = null,
  val gateLabel: String? = null,
  val visitorPhone: String? = null,
  val vehicleNumber: String? = null,
  val visitReason: String? = null,
  val scheduledAt: String? = null,
  val createdAt: String? = null,
  val checkedInAt: String? = null,
  val checkedOutAt: String? = null,
)

@JsonClass(generateAdapter = true)
data class CancelVisitBody(val reason: String? = null)

@JsonClass(generateAdapter = true)
data class ServicePlanDto(
  val id: String,
  val name: String,
  val description: String? = null,
  val monthlyPriceRupees: Int? = null,
)

/**
 * Note what is absent: invoices, payment status, auto-pay, usage. There is no
 * billing system behind this — `currentPeriodEnd` is simply the "valid till"
 * date staff set. The screen must not imply more than that.
 */
@JsonClass(generateAdapter = true)
data class SubscriptionDto(
  val id: String,
  val status: String,
  val startDate: String? = null,
  val currentPeriodEnd: String? = null,
  val plan: ServicePlanDto,
)

@JsonClass(generateAdapter = true)
data class CommunityEventDto(
  val id: String,
  val title: String,
  val description: String? = null,
  val venue: String? = null,
  val startsAt: String,
  val endsAt: String? = null,
  val status: String? = null,
)

@JsonClass(generateAdapter = true)
data class EmergencyContactDto(
  val id: String,
  val label: String,
  val phone: String,
  val category: String? = null,
  val sortOrder: Int = 0,
)

/**
 * RFC 7807 problem details — what the API returns on every error. Parsed so a
 * failure can be shown to the user as something meaningful rather than
 * "request failed".
 */
@JsonClass(generateAdapter = true)
data class ApiProblem(
  val type: String? = null,
  val title: String? = null,
  val status: Int? = null,
  val detail: String? = null,
  @Json(name = "instance") val instance: String? = null,
)
