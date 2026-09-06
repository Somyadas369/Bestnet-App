package com.example.data.remote

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Production API. HTTPS only — `usesCleartextTraffic` is false in the manifest,
 * so an http:// URL here would fail at runtime rather than silently downgrade.
 *
 * There is no dedicated mobile API host yet, so this rides the CRM portal's
 * nginx vhost. Every *.bestnet.in vhost proxies /api/v1/ to the same backend,
 * so this is functionally correct — just not semantically tidy.
 * TODO: move to api.bestnet.in when that exists.
 */
private const val BASE_URL = "https://crm.bestnet.in/api/v1/"

/** An API error carrying the server's RFC 7807 problem details, when present. */
class ApiException(val status: Int, val problem: ApiProblem?) :
  RuntimeException(problem?.detail ?: problem?.title ?: "Request failed ($status)")

class ApiClient(private val tokenStore: TokenStore) {

  private val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
  private val problemAdapter = moshi.adapter(ApiProblem::class.java)

  /**
   * Refreshes without the interceptor stack, so a failing refresh cannot
   * recurse back into the Authenticator below.
   */
  private val bareRetrofit: Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(OkHttpClient.Builder().callTimeout(30, TimeUnit.SECONDS).build())
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

  private val refreshApi: BestNetApi = bareRetrofit.create(BestNetApi::class.java)

  private val authInterceptor = Interceptor { chain ->
    val token = tokenStore.accessToken
    val request = if (token.isNullOrBlank()) {
      chain.request()
    } else {
      chain.request().newBuilder().header("Authorization", "Bearer $token").build()
    }
    chain.proceed(request)
  }

  /**
   * Refresh-on-401, then retry once.
   *
   * Two things this must get right, both of which are easy to miss:
   *  - `responseCount` guards against an infinite loop when the refreshed token
   *    is *also* rejected.
   *  - the lock plus the re-read of accessToken means that when several
   *    requests 401 at once, only the first refreshes and the rest pick up the
   *    token it obtained, instead of each burning the (single-use, rotating)
   *    refresh token and logging the user out.
   */
  private val refreshAuthenticator = Authenticator { _: Route?, response: Response ->
    if (responseCount(response) >= 2) return@Authenticator null

    val staleToken = response.request.header("Authorization")?.removePrefix("Bearer ")

    synchronized(this) {
      val current = tokenStore.accessToken
      // Someone else already refreshed while this request was queued.
      if (!current.isNullOrBlank() && current != staleToken) {
        return@Authenticator response.request.newBuilder()
          .header("Authorization", "Bearer $current")
          .build()
      }

      val refresh = tokenStore.refreshToken ?: return@Authenticator null
      val tokens = try {
        runBlocking { refreshApi.refresh(RefreshBody(refresh)) }
      } catch (err: Exception) {
        // Refresh token expired or revoked: the session is genuinely over.
        tokenStore.clear()
        return@Authenticator null
      }

      tokenStore.save(tokens)
      return@Authenticator response.request.newBuilder()
        .header("Authorization", "Bearer ${tokens.accessToken}")
        .build()
    }
  }

  private val okHttp: OkHttpClient = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .authenticator(refreshAuthenticator)
    .addInterceptor(
      HttpLoggingInterceptor().apply {
        // Headers only: BASIC would be useless for debugging, BODY would write
        // access tokens and OTP codes into logcat.
        level = HttpLoggingInterceptor.Level.HEADERS
      },
    )
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .build()

  val api: BestNetApi = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttp)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()
    .create(BestNetApi::class.java)

  /**
   * Runs an API call, turning Retrofit's HttpException into an ApiException
   * carrying the server's problem-details message so the UI can show something
   * true rather than a generic failure.
   */
  suspend fun <T> call(block: suspend BestNetApi.() -> T): Result<T> = try {
    Result.success(api.block())
  } catch (err: HttpException) {
    val body = err.response()?.errorBody()?.string()
    val problem = body?.let { runCatching { problemAdapter.fromJson(it) }.getOrNull() }
    Result.failure(ApiException(err.code(), problem))
  } catch (err: Exception) {
    Result.failure(err)
  }

  private fun responseCount(response: Response): Int {
    var count = 1
    var prior: Response? = response.priorResponse
    while (prior != null) {
      count++
      prior = prior.priorResponse
    }
    return count
  }
}

/** Used by the login screen, which has no token yet. */
fun unauthenticatedApi(): BestNetApi = Retrofit.Builder()
  .baseUrl(BASE_URL)
  .client(
    OkHttpClient.Builder()
      .connectTimeout(15, TimeUnit.SECONDS)
      .readTimeout(30, TimeUnit.SECONDS)
      .build(),
  )
  .addConverterFactory(
    MoshiConverterFactory.create(Moshi.Builder().add(KotlinJsonAdapterFactory()).build()),
  )
  .build()
  .create(BestNetApi::class.java)
