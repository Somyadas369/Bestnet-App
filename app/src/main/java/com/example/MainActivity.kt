package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.sip.SipCallState
import com.example.ui.components.InCallBottomSheet
import com.example.ui.components.PreApproveVisitorDialog
import com.example.ui.navigation.NavRoutes
import com.example.ui.screens.CommunityNoticesScreen
import com.example.ui.screens.IntercomScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainShellScreen
import com.example.ui.screens.MaintenanceComplaintsScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.RaiseComplaintScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VisitorsScreen
import com.example.ui.theme.BestNetBackground
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted
import com.example.ui.theme.BestNetRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BestNetViewModel

class MainActivity : ComponentActivity() {

  /**
   * RECORD_AUDIO is a runtime permission. Declaring it in the manifest is not
   * enough — without the grant a call connects and carries no audio at all,
   * which looks like a broken PBX rather than a missing permission.
   * POST_NOTIFICATIONS (API 33+) is requested alongside so an incoming-call
   * notification isn't silently dropped.
   *
   * Requested when the resident turns calling on, not at launch. Asking at
   * launch put a system dialog over a window that had not drawn its first frame
   * yet, and it is poor practice besides: a permission prompt with no context
   * is one most people decline.
   */
  private val permissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

  fun requestCallPermissions() {
    val needed = mutableListOf<String>()
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
      != PackageManager.PERMISSION_GRANTED
    ) needed += Manifest.permission.RECORD_AUDIO
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
      ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
      != PackageManager.PERMISSION_GRANTED
    ) needed += Manifest.permission.POST_NOTIFICATIONS
    if (needed.isNotEmpty()) permissionLauncher.launch(needed.toTypedArray())
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      Log.e("MainActivity", "Uncaught exception on thread ${thread.name}", throwable)
    }

    setContent {
      MyApplicationTheme {
        var initError by remember { mutableStateOf<Throwable?>(null) }
        val vm: BestNetViewModel? = remember {
          try {
            ViewModelProvider(this)[BestNetViewModel::class.java]
          } catch (t: Throwable) {
            Log.e("MainActivity", "Failed to create BestNetViewModel", t)
            initError = t
            null
          }
        }

        if (initError != null || vm == null) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(BestNetBackground)
              .statusBarsPadding()
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = Color.White),
              elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
              Column(
                modifier = Modifier
                  .padding(20.dp)
                  .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "Unable to Start App",
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = BestNetRed
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = initError?.localizedMessage ?: "Initialization error",
                  fontSize = 13.sp,
                  color = BestNetInk
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = initError?.stackTraceToString()?.take(500) ?: "",
                  fontSize = 10.5.sp,
                  color = BestNetMuted
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                  onClick = { recreate() },
                  colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen)
                ) {
                  Text("Retry", color = Color.White)
                }
              }
            }
          }
        } else {
          Surface(
            modifier = Modifier.fillMaxSize(),
            color = BestNetBackground
          ) {
            BestNetApp(viewModel = vm)
          }
        }
      }
    }
  }
}

@Composable
fun BestNetApp(
  viewModel: BestNetViewModel = viewModel()
) {
  val context = LocalContext.current
  val navController = rememberNavController()
  val allComplaints by viewModel.allComplaints.collectAsStateWithLifecycle()
  val allVisitors by viewModel.allVisitors.collectAsStateWithLifecycle()
  val allNotices by viewModel.allNotices.collectAsStateWithLifecycle()
  val allCommunityNotices by viewModel.allCommunityNotices.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

  LaunchedEffect(snackbarMsg) {
    snackbarMsg?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearSnackbar()
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    NavHost(
      navController = navController,
      startDestination = NavRoutes.SPLASH
    ) {
    // 1. Splash Screen
    composable(NavRoutes.SPLASH) {
      val loggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
      // A returning resident with a stored session shouldn't be made to sign in
      // again — skip straight past the splash. Runs as an effect so it also
      // fires once the ViewModel has read the token store.
      LaunchedEffect(loggedIn) {
        if (loggedIn) {
          navController.navigate(NavRoutes.MAIN_SHELL) {
            popUpTo(NavRoutes.SPLASH) { inclusive = true }
          }
        }
      }
      SplashScreen(
        onGetStartedClick = {
          navController.navigate(NavRoutes.LOGIN) {
            popUpTo(NavRoutes.SPLASH) { inclusive = true }
          }
        }
      )
    }

    // 2. Login Screen
    composable(NavRoutes.LOGIN) {
      val authBusy by viewModel.authBusy.collectAsStateWithLifecycle()
      val authError by viewModel.authError.collectAsStateWithLifecycle()
      LoginScreen(
        // Reached only after verifyOtp has succeeded AND the resident's data
        // has been pulled down — viewModel.login() no longer exists, because a
        // client-side "you're logged in now" was exactly the lie to remove.
        onLoginSuccess = {
          navController.navigate(NavRoutes.MAIN_SHELL) {
            popUpTo(NavRoutes.LOGIN) { inclusive = true }
          }
        },
        onShowComingSoon = { feature ->
          viewModel.showComingSoon(feature)
        },
        onRequestOtp = { phone, cb -> viewModel.requestOtp(phone, cb) },
        onVerifyOtp = { phone, code, cb -> viewModel.verifyOtp(phone, code, cb) },
        busy = authBusy,
        errorMessage = authError,
      )
    }

    // 3. Main Shell (Home, Services, Community, Profile tabs)
    composable(NavRoutes.MAIN_SHELL) {
      MainShellScreen(
        viewModel = viewModel,
        onNavigateToIntercom = { navController.navigate(NavRoutes.INTERCOM) },
        onNavigateToComplaint = { navController.navigate(NavRoutes.RAISE_COMPLAINT) },
        onNavigateToVisitors = { navController.navigate(NavRoutes.VISITORS) },
        onNavigateToNotices = { navController.navigate(NavRoutes.COMMUNITY_NOTICES) },
        onLogout = {
          viewModel.logout()
          navController.navigate(NavRoutes.LOGIN) {
            popUpTo(NavRoutes.MAIN_SHELL) { inclusive = true }
          }
        }
      )
    }

    // 4. Intercom Screen
    composable(NavRoutes.INTERCOM) {
      val myExtension by viewModel.myExtension.collectAsStateWithLifecycle()
      val directory by viewModel.intercomDirectory.collectAsStateWithLifecycle()
      val sipRegistration by viewModel.sipRegistration.collectAsStateWithLifecycle()
      val sipBusy by viewModel.sipBusy.collectAsStateWithLifecycle()
      val sipError by viewModel.sipError.collectAsStateWithLifecycle()
      IntercomScreen(
        staffList = viewModel.intercomStaff,
        neighborsList = viewModel.intercomNeighbors,
        onBackClick = { navController.popBackStack() },
        onCallContact = { contact ->
          (context as? MainActivity)?.requestCallPermissions()
          viewModel.startCall(contact)
        },
        myExtension = myExtension,
        directory = directory,
        registration = sipRegistration,
        sipConfigured = viewModel.sipConfigured,
        sipBusy = sipBusy,
        sipError = sipError,
        onEnableCalling = {
          // Ask for the microphone at the moment it becomes relevant.
          (context as? MainActivity)?.requestCallPermissions()
          viewModel.enableSipCalling { }
        },
      )
    }

    // 5. Maintenance Complaints & Tracker Screen
    composable(NavRoutes.RAISE_COMPLAINT) {
      val currentResident by viewModel.currentResident.collectAsStateWithLifecycle()
      val submitting by viewModel.complaintSubmitting.collectAsStateWithLifecycle()
      val complaintError by viewModel.complaintError.collectAsStateWithLifecycle()
      MaintenanceComplaintsScreen(
        complaints = allComplaints,
        currentUnit = currentResident?.unit ?: "—",
        onBackClick = { navController.popBackStack() },
        // Real ticket against the resident's unit. `title` and `priority` are
        // collected by the form but the server derives its own SLA from the
        // category, so only category + description are sent.
        onSubmitComplaint = { _, category, description, _, onResult ->
          viewModel.submitComplaintToServer(category, description, onResult)
        },
        submitting = submitting,
        errorMessage = complaintError,
        onUpdateStatus = { id, newStatus ->
          viewModel.updateComplaintStatus(id, newStatus)
        },
        onDeleteComplaint = { id ->
          viewModel.deleteComplaint(id)
        }
      )
    }

    // 6. Visitors Screen
    composable(NavRoutes.VISITORS) {
      // This screen only raises the flag; the dialog is rendered once below the
      // NavHost so it works from here as well as from the main shell.
      VisitorsScreen(
        visitors = allVisitors,
        onBackClick = { navController.popBackStack() },
        onOpenPreApproveDialog = { viewModel.openPreApproveDialog() },
      )
    }

    // 7. Notifications Screen
    composable(NavRoutes.NOTIFICATIONS) {
      NotificationsScreen(
        notices = allNotices,
        onBackClick = { navController.popBackStack() },
        onNoticeClick = { id -> viewModel.markNoticeAsRead(id) },
        onMarkAllReadClick = { viewModel.markAllNoticesAsRead() }
      )
    }

    // 8. Community Notices Screen (Chronological notices feed)
    composable(NavRoutes.COMMUNITY_NOTICES) {
      CommunityNoticesScreen(
        notices = allCommunityNotices,
        onBackClick = { navController.popBackStack() }
      )
    }
  }

  // Hoisted above the NavHost on purpose. It used to live inside
  // MainShellScreen, but MAIN_SHELL and VISITORS are sibling destinations — so
  // tapping "Pre-Approve" on the Visitors screen set the flag while the only
  // thing rendering the dialog was not composed, and nothing happened at all.
  val showPreApprove by viewModel.showPreApproveDialog.collectAsStateWithLifecycle()
  if (showPreApprove) {
    val visitorSubmitting by viewModel.visitorSubmitting.collectAsStateWithLifecycle()
    val visitorError by viewModel.visitorError.collectAsStateWithLifecycle()
    PreApproveVisitorDialog(
      onDismiss = { viewModel.closePreApproveDialog() },
      // Stays open until the server confirms; the ViewModel closes it on
      // success, so a failure leaves the entered details intact.
      onPreApprove = { name, type, hours ->
        viewModel.preApproveVisitorOnServer(name, type, hours) { }
      },
      submitting = visitorSubmitting,
      errorMessage = visitorError,
    )
  }

  // Active in-app SIP call overlay (outgoing or incoming).
  // Hoisted at the app root level so it is active and displayed on EVERY screen:
  // Home, Intercom directory, Complaints, Visitors, Notices, etc.
  val sipCall by viewModel.sipCall.collectAsStateWithLifecycle()
  val sipMuted by viewModel.sipMuted.collectAsStateWithLifecycle()
  val sipSpeaker by viewModel.sipSpeaker.collectAsStateWithLifecycle()
  val activeCall by viewModel.activeCallContact.collectAsStateWithLifecycle()
  val directory by viewModel.intercomDirectory.collectAsStateWithLifecycle()

  val callVisible = sipCall.state == SipCallState.INCOMING ||
    sipCall.state == SipCallState.OUTGOING ||
    sipCall.state == SipCallState.CONNECTED

  // Clear a finished call so the sheet closes instead of sticking on "Ended".
  LaunchedEffect(sipCall.state) {
    if (sipCall.state == SipCallState.ENDED || sipCall.state == SipCallState.ERROR) {
      viewModel.acknowledgeCallEnded()
    }
  }

  if (callVisible) {
    val remote = sipCall.remote ?: activeCall?.extension ?: "Unknown"
    // Match the contact if possible (from activeCall, directory, staff or neighbors)
    val matchedContact = activeCall
      ?: directory?.firstOrNull { it.extension == remote }
      ?: (viewModel.intercomStaff + viewModel.intercomNeighbors).firstOrNull { it.extension == remote }

    val displayName = if (sipCall.state == SipCallState.INCOMING) {
      matchedContact?.name ?: "Intercom Call ($remote)"
    } else {
      matchedContact?.name ?: activeCall?.name ?: "Extension $remote"
    }

    val displayRole = matchedContact?.role ?: "Extension $remote"

    InCallBottomSheet(
      contactName = displayName,
      contactRole = displayRole,
      statusText = when (sipCall.state) {
        SipCallState.INCOMING -> "Incoming call"
        SipCallState.OUTGOING -> "Calling…"
        else -> "Connected"
      },
      countUp = sipCall.state == SipCallState.CONNECTED,
      isMuted = sipMuted,
      isSpeaker = sipSpeaker,
      onToggleMute = { viewModel.setMuted(it) },
      onToggleSpeaker = { viewModel.setSpeaker(it) },
      showAnswer = sipCall.state == SipCallState.INCOMING,
      onAnswer = {
        (context as? MainActivity)?.requestCallPermissions()
        viewModel.answerCall()
      },
      onEndCall = { viewModel.endCall() }
    )
  }

  SnackbarHost(
    hostState = snackbarHostState,
    modifier = Modifier
      .align(Alignment.BottomCenter)
      .padding(bottom = 16.dp)
  )
}
}

