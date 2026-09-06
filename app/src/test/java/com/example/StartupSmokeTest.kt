package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BestNetDatabase
import com.example.data.remote.TokenStore
import com.example.data.repository.SessionRepository
import com.example.data.sip.SipManager
import com.example.ui.viewmodel.BestNetViewModel
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Reproduces app startup on the JVM.
 *
 * Written after the app began showing a white screen on launch, which is what a
 * crash during Application/ViewModel construction looks like from outside.
 * Every object below is built during startup on a real device, so anything that
 * throws here throws there — and unlike a device, this says exactly what and
 * where.
 *
 * Robolectric has no native libraries, so it also stands in for the case this
 * is meant to survive: a device where linphone's native library cannot load.
 * Constructing the SIP layer must not throw even then.
 */
// compileSdk is 36; Robolectric has no runtime for it yet and refuses to start.
// Pinned to 34, which is close enough for what this checks — object
// construction, not platform behaviour.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StartupSmokeTest {

  private val app: Application get() = ApplicationProvider.getApplicationContext()

  @Test
  fun `SipManager can be constructed without native libraries`() {
    // The constructor must touch no linphone class. If a field of a linphone
    // type or an eagerly-created listener forces the native library to load,
    // this is where it surfaces.
    val manager = SipManager(app)
    assertNotNull(manager)
    assertNotNull(manager.registration.value)
    assertNotNull(manager.call.value)
  }

  @Test
  fun `starting the SIP stack without native libraries fails softly`() {
    val manager = SipManager(app)
    // Must not throw: a device that cannot run SIP should lose calling, not
    // the whole app.
    manager.start()
  }

  @Test
  fun `TokenStore falls back when the keystore is unavailable`() {
    val store = TokenStore(app)
    assertNotNull(store)
    // Reading an unset value must not throw.
    store.accessToken
    store.sipExtension
  }

  @Test
  fun `SessionRepository can be constructed`() {
    val dao = BestNetDatabase.getDatabase(app).bestNetDao()
    assertNotNull(SessionRepository(app, dao))
  }

  @Test
  fun `ViewModel can be constructed - this is what the white screen was`() {
    // BestNetApp.sipManager(), the Room database, the repositories and every
    // StateFlow the UI collects are all built here, exactly as they are when
    // MainActivity composes the app.
    val vm = BestNetViewModel(app)
    assertNotNull(vm.isLoggedIn.value)
    assertNotNull(vm.sipRegistration.value)
    assertNotNull(vm.sipCall.value)
  }
}
