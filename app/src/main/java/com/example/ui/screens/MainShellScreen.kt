package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SpeedTestDialog
import com.example.ui.components.SwitchHomeBottomSheet
import com.example.ui.theme.BestNetBorder
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetGreenLight
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted
import com.example.ui.theme.BestNetSurface
import com.example.ui.viewmodel.BestNetViewModel

@Composable
fun MainShellScreen(
  viewModel: BestNetViewModel,
  onNavigateToIntercom: () -> Unit,
  onNavigateToComplaint: () -> Unit,
  onNavigateToVisitors: () -> Unit,
  onNavigateToNotices: () -> Unit,
  onLogout: () -> Unit
) {
  val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
  val currentResident by viewModel.currentResident.collectAsStateWithLifecycle()
  val allResidents by viewModel.allResidents.collectAsStateWithLifecycle()
  val allComplaints by viewModel.allComplaints.collectAsStateWithLifecycle()
  val allCommunityNotices by viewModel.allCommunityNotices.collectAsStateWithLifecycle()
  val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
  val showSpeedTest by viewModel.showSpeedTest.collectAsStateWithLifecycle()
  val showSwitchHome by viewModel.showSwitchHomeSheet.collectAsStateWithLifecycle()
  val subscriptions by viewModel.subscriptions.collectAsStateWithLifecycle()
  val events by viewModel.events.collectAsStateWithLifecycle()
  val emergencyContacts by viewModel.emergencyContacts.collectAsStateWithLifecycle()

  val activeComplaintsCount = remember(allComplaints) {
    allComplaints.count { it.status.lowercase() != "resolved" }
  }

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(snackbarMsg) {
    snackbarMsg?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearSnackbar()
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    bottomBar = {
      NavigationBar(
        containerColor = BestNetSurface,
        tonalElevation = 6.dp,
        modifier = Modifier
          .border(0.8.dp, BestNetBorder)
          .windowInsetsPadding(WindowInsets.navigationBars)
      ) {
        // Tab 1: Home
        NavigationBarItem(
          selected = activeTab == "home",
          onClick = { viewModel.setTab("home") },
          icon = {
            Icon(
              imageVector = if (activeTab == "home") Icons.Filled.Home else Icons.Outlined.Home,
              contentDescription = "Home",
              modifier = Modifier.size(24.dp)
            )
          },
          label = {
            Text(
              "Home",
              fontSize = 11.5.sp,
              fontWeight = if (activeTab == "home") FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = BestNetGreen,
            selectedTextColor = BestNetGreen,
            unselectedIconColor = BestNetMuted,
            unselectedTextColor = BestNetMuted,
            indicatorColor = BestNetGreenLight
          )
        )

        // Tab 2: Services
        NavigationBarItem(
          selected = activeTab == "services",
          onClick = { viewModel.setTab("services") },
          icon = {
            Icon(
              imageVector = if (activeTab == "services") Icons.Filled.Router else Icons.Outlined.Router,
              contentDescription = "Services",
              modifier = Modifier.size(24.dp)
            )
          },
          label = {
            Text(
              "Services",
              fontSize = 11.5.sp,
              fontWeight = if (activeTab == "services") FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = BestNetGreen,
            selectedTextColor = BestNetGreen,
            unselectedIconColor = BestNetMuted,
            unselectedTextColor = BestNetMuted,
            indicatorColor = BestNetGreenLight
          )
        )

        // Tab 3: Community
        NavigationBarItem(
          selected = activeTab == "community",
          onClick = { viewModel.setTab("community") },
          icon = {
            Icon(
              imageVector = if (activeTab == "community") Icons.Filled.People else Icons.Outlined.People,
              contentDescription = "Community",
              modifier = Modifier.size(24.dp)
            )
          },
          label = {
            Text(
              "Community",
              fontSize = 11.5.sp,
              fontWeight = if (activeTab == "community") FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = BestNetGreen,
            selectedTextColor = BestNetGreen,
            unselectedIconColor = BestNetMuted,
            unselectedTextColor = BestNetMuted,
            indicatorColor = BestNetGreenLight
          )
        )

        // Tab 4: Profile
        NavigationBarItem(
          selected = activeTab == "profile",
          onClick = { viewModel.setTab("profile") },
          icon = {
            Icon(
              imageVector = if (activeTab == "profile") Icons.Filled.Person else Icons.Outlined.Person,
              contentDescription = "Profile",
              modifier = Modifier.size(24.dp)
            )
          },
          label = {
            Text(
              "Profile",
              fontSize = 11.5.sp,
              fontWeight = if (activeTab == "profile") FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = BestNetGreen,
            selectedTextColor = BestNetGreen,
            unselectedIconColor = BestNetMuted,
            unselectedTextColor = BestNetMuted,
            indicatorColor = BestNetGreenLight
          )
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (activeTab) {
        "home" -> HomeScreen(
          resident = currentResident,
          activeComplaintsCount = activeComplaintsCount,
          noticesCount = allCommunityNotices.size,
          onNavigateToIntercom = onNavigateToIntercom,
          onNavigateToComplaint = onNavigateToComplaint,
          onNavigateToVisitors = onNavigateToVisitors,
          onNavigateToNotices = onNavigateToNotices,
          onSwitchToServicesTab = { viewModel.setTab("services") },
          onSwitchToCommunityTab = { viewModel.setTab("community") },
          onNavigateToProfile = { viewModel.setTab("profile") },
          onOpenSpeedTest = { viewModel.openSpeedTest() },
          onOpenSwitchHome = { viewModel.openSwitchHomeSheet() }
        )

        "services" -> ServicesScreen(
          onOpenSpeedTest = { viewModel.openSpeedTest() },
          onShowComingSoon = { viewModel.showComingSoon(it) },
          subscriptions = subscriptions,
        )

        "community" -> CommunityScreen(
          onNavigateToNotices = onNavigateToNotices,
          onShowComingSoon = { viewModel.showComingSoon(it) },
          events = events,
          emergencyContacts = emergencyContacts,
        )

        "profile" -> ProfileScreen(
          resident = currentResident,
          onOpenSwitchHome = { viewModel.openSwitchHomeSheet() },
          onNavigateToVisitors = onNavigateToVisitors,
          onLogout = onLogout,
          onShowComingSoon = { viewModel.showComingSoon(it) }
        )
      }
    }
  }

  // Modals & Bottom Sheets
  if (showSpeedTest) {
    SpeedTestDialog(onDismiss = { viewModel.closeSpeedTest() })
  }

  // The pre-approve dialog is rendered once at the NavHost level (MainActivity)
  // so it also works from the Visitors screen, which is a sibling destination.

  if (showSwitchHome) {
    SwitchHomeBottomSheet(
      residents = allResidents,
      currentResidentId = currentResident?.id ?: 1L,
      onSelectResident = { viewModel.switchResident(it) },
      onDismiss = { viewModel.closeSwitchHomeSheet() }
    )
  }
}
