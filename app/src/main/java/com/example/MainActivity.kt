package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
      LoginScreen(
        onLoginSuccess = {
          viewModel.login()
          navController.navigate(NavRoutes.MAIN_SHELL) {
            popUpTo(NavRoutes.LOGIN) { inclusive = true }
          }
        },
        onShowComingSoon = { feature ->
          viewModel.showComingSoon(feature)
        }
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
      IntercomScreen(
        staffList = viewModel.intercomStaff,
        neighborsList = viewModel.intercomNeighbors,
        onBackClick = { navController.popBackStack() },
        onCallContact = { contact -> viewModel.startCall(contact) }
      )
    }

    // 5. Maintenance Complaints & Tracker Screen
    composable(NavRoutes.RAISE_COMPLAINT) {
      val currentResident by viewModel.currentResident.collectAsStateWithLifecycle()
      MaintenanceComplaintsScreen(
        complaints = allComplaints,
        currentUnit = currentResident?.unit ?: "A-1201",
        onBackClick = { navController.popBackStack() },
        onSubmitComplaint = { title, category, description, priority, onComplete ->
          viewModel.submitComplaint(title, category, description, priority, onComplete)
        },
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
      VisitorsScreen(
        visitors = allVisitors,
        onBackClick = { navController.popBackStack() },
        onOpenPreApproveDialog = { viewModel.openPreApproveDialog() }
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
}

