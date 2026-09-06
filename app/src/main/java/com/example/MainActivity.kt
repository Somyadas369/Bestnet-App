package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BestNetViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = BestNetBackground
        ) {
          BestNetApp()
        }
      }
    }
  }
}

@Composable
fun BestNetApp(
  viewModel: BestNetViewModel = viewModel()
) {
  val navController = rememberNavController()
  val allComplaints by viewModel.allComplaints.collectAsStateWithLifecycle()
  val allVisitors by viewModel.allVisitors.collectAsStateWithLifecycle()
  val allNotices by viewModel.allNotices.collectAsStateWithLifecycle()
  val allCommunityNotices by viewModel.allCommunityNotices.collectAsStateWithLifecycle()

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
      IntercomScreen(
        staffList = viewModel.intercomStaff,
        neighborsList = viewModel.intercomNeighbors,
        onBackClick = { navController.popBackStack() },
        onCallContact = { contact -> viewModel.startCall(contact) },
        myExtension = myExtension,
        directory = directory,
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
}

