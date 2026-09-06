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
      .apply()
  }

  private companion object {
    const val KEY_ACCESS = "access_token"
    const val KEY_REFRESH = "refresh_token"
    const val KEY_MEMBERSHIP = "selected_membership"
    const val KEY_UNIT = "selected_unit"
    const val KEY_TENANT = "selected_tenant"
  }
}
