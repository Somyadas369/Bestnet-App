package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Renders the app's root composable.
 *
 * StartupSmokeTest proved every object constructs; this covers the layer after
 * that. A white screen with no crash dialog is what an exception thrown during
 * composition looks like, and nothing below the ViewModel would catch it.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RenderSmokeTest {

  @get:Rule
  val compose = createComposeRule()

  @Test
  fun `the root composable renders`() {
    compose.setContent { BestNetApp() }
    compose.waitForIdle()
  }
}
