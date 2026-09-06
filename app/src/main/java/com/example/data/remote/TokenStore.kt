package com.example.data.remote

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Session tokens, encrypted at rest.
 *
 * A phone is a far more exposed storage environment than a server, and these
 * tokens are the whole session — so plain SharedPreferences would leave them
 * readable on a rooted or backed-up device.
 *
 * Falls back to plain SharedPreferences if the keystore is unavailable. That
 * happens on a small number of devices with broken keystore implementations,
 * and losing encryption is strictly better than an app that cannot log in at
 * all — but it is logged as an error rather than passed over silently.
 */
class TokenStore(context: Context) {

  private val prefs: SharedPreferences = try {
    val masterKey = MasterKey.Builder(context)
      .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
      .build()
    EncryptedSharedPreferences.create(
      context,
      "bestnet_session",
      masterKey,
      EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
      EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
  } catch (err: Exception) {
    Log.e("TokenStore", "Encrypted storage unavailable, falling back to plaintext prefs", err)
    context.getSharedPreferences("bestnet_session_plain", Context.MODE_PRIVATE)
  }

  var accessToken: String?
    get() = prefs.getString(KEY_ACCESS, null)
    set(value) = prefs.edit().putString(KEY_ACCESS, value).apply()

  var refreshToken: String?
    get() = prefs.getString(KEY_REFRESH, null)
    set(value) = prefs.edit().putString(KEY_REFRESH, value).apply()

  /**
   * Which home the resident is currently viewing. Purely a UI preference — it
   * is never sent to the server, because none of the endpoints this app calls
   * are unit-scoped.
   */
  var selectedMembershipId: String?
    get() = prefs.getString(KEY_MEMBERSHIP, null)
    set(value) = prefs.edit().putString(KEY_MEMBERSHIP, value).apply()

  /**
   * Server ids for the selected home. Room's `Resident` is a display model with
   * autoGenerate Long ids, so it cannot carry the server's UUIDs — but raising a
   * ticket needs both (the unit to file against, the tenant to list categories
   * from). Kept here rather than widening the entity and forcing a migration.
   */
  var selectedUnitId: String?
    get() = prefs.getString(KEY_UNIT, null)
    set(value) = prefs.edit().putString(KEY_UNIT, value).apply()

  var selectedTenantId: String?
    get() = prefs.getString(KEY_TENANT, null)
    set(value) = prefs.edit().putString(KEY_TENANT, value).apply()

  /**
   * SIP credentials for in-app calling.
   *
   * The server stores the SIP password one-way hashed and returns the plaintext
   * exactly once, at provisioning or password reset — so once it is issued,
   * this device is the only place it exists. Losing it means resetting, which
   * disconnects every other device on the extension.
   *
   * Encrypted at rest with the tokens, for the same reason.
   */
  var sipExtension: String?
    get() = prefs.getString(KEY_SIP_EXT, null)
    set(value) = prefs.edit().putString(KEY_SIP_EXT, value).apply()

  var sipPassword: String?
    get() = prefs.getString(KEY_SIP_PW, null)
    set(value) = prefs.edit().putString(KEY_SIP_PW, value).apply()

  var sipDomain: String?
    get() = prefs.getString(KEY_SIP_DOMAIN, null)
    set(value) = prefs.edit().putString(KEY_SIP_DOMAIN, value).apply()

  var sipPort: Int
    get() = prefs.getInt(KEY_SIP_PORT, 5061)
    set(value) = prefs.edit().putInt(KEY_SIP_PORT, value).apply()

  var sipTransport: String?
    get() = prefs.getString(KEY_SIP_TRANSPORT, null)
    set(value) = prefs.edit().putString(KEY_SIP_TRANSPORT, value).apply()

  val hasSipCredentials: Boolean
    get() = !sipExtension.isNullOrBlank() && !sipPassword.isNullOrBlank()

  val isLoggedIn: Boolean get() = !accessToken.isNullOrBlank()

  fun save(tokens: TokensDto) {
    prefs.edit()
      .putString(KEY_ACCESS, tokens.accessToken)
      .putString(KEY_REFRESH, tokens.refreshToken)
      .apply()
  }

  fun clear() {
    prefs.edit()
      .remove(KEY_ACCESS)
      .remove(KEY_REFRESH)
      .remove(KEY_MEMBERSHIP)
      .remove(KEY_UNIT)
      .remove(KEY_TENANT)
      .remove(KEY_SIP_EXT)
      .remove(KEY_SIP_PW)
      .remove(KEY_SIP_DOMAIN)
      .remove(KEY_SIP_PORT)
      .remove(KEY_SIP_TRANSPORT)
      .apply()
  }

  private companion object {
    const val KEY_ACCESS = "access_token"
    const val KEY_REFRESH = "refresh_token"
    const val KEY_MEMBERSHIP = "selected_membership"
    const val KEY_UNIT = "selected_unit"
    const val KEY_TENANT = "selected_tenant"
    const val KEY_SIP_EXT = "sip_extension"
    const val KEY_SIP_PW = "sip_password"
    const val KEY_SIP_DOMAIN = "sip_domain"
    const val KEY_SIP_PORT = "sip_port"
    const val KEY_SIP_TRANSPORT = "sip_transport"
  }
}
