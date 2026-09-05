package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Resident
import com.example.ui.theme.BestNetBackground
import com.example.ui.theme.BestNetBorder
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetGreenLight
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted
import com.example.ui.theme.BestNetRed
import com.example.ui.theme.BestNetRedLight
import com.example.ui.theme.BestNetSurface
import com.example.ui.theme.BestNetSurfaceVariant

@Composable
fun ProfileScreen(
  resident: Resident?,
  onOpenSwitchHome: () -> Unit,
  onNavigateToVisitors: () -> Unit,
  onLogout: () -> Unit,
  onShowComingSoon: (String) -> Unit
) {
  var showLogoutConfirm by remember { mutableStateOf(false) }
  var showVehiclesDialog by remember { mutableStateOf(false) }
  var showFamilyDialog by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BestNetBackground)
      .statusBarsPadding()
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(BestNetSurface)
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "My Profile",
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        color = BestNetInk
      )
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Profile Header Card
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = BestNetSurface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, BestNetBorder, RoundedCornerShape(18.dp))
              .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .border(2.5.dp, BestNetGreen, CircleShape)
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_user_avatar),
                contentDescription = "Profile Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = resident?.name ?: "Rahul Sharma",
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold,
              color = BestNetInk
            )
            Text(
              text = "${resident?.unit ?: "A-1201"}, ${resident?.communityName ?: "Sunrise Apartments"}",
              fontSize = 13.sp,
              color = BestNetMuted
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Switch Home Button
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(BestNetGreenLight)
                .clickable { onOpenSwitchHome() }
                .padding(horizontal = 16.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = BestNetGreen,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = "Switch Home",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = BestNetGreen
              )
            }
          }
        }
      }

      // Profile Options List
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = BestNetSurface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, BestNetBorder, RoundedCornerShape(16.dp))
          ) {
            ProfileRow(
              icon = Icons.Default.Edit,
              title = "Edit Profile",
              onClick = { onShowComingSoon("Edit Profile") }
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            ProfileRow(
              icon = Icons.Default.Group,
              title = "Family Members",
              onClick = { showFamilyDialog = true }
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            ProfileRow(
              icon = Icons.Default.DirectionsCar,
              title = "My Vehicles",
              onClick = { showVehiclesDialog = true }
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            ProfileRow(
              icon = Icons.Default.PersonAdd,
              title = "Pre-Approved Visitors",
              onClick = onNavigateToVisitors
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            ProfileRow(
              icon = Icons.Default.Lock,
              title = "Change Password",
              onClick = { onShowComingSoon("Change Password") }
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            ProfileRow(
              icon = Icons.Default.HelpOutline,
              title = "Help & Support",
              onClick = { onShowComingSoon("Help & Support") }
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            ProfileRow(
              icon = Icons.Default.Info,
              title = "About BestNet",
              onClick = { showAboutDialog = true }
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            ProfileRow(
              icon = Icons.AutoMirrored.Filled.Logout,
              title = "Logout",
              isDestructive = true,
              onClick = { showLogoutConfirm = true }
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Vehicles Dialog
  if (showVehiclesDialog) {
    AlertDialog(
      onDismissRequest = { showVehiclesDialog = false },
      confirmButton = {
        Button(
          onClick = { showVehiclesDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Done", fontWeight = FontWeight.Bold)
        }
      },
      title = {
        Text("Registered Vehicles", fontWeight = FontWeight.Bold, fontSize = 18.sp)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("1. Honda City (White) - MH 02 AB 4521", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Text("Parking Slot: P1 - #104 (Basement 1)", fontSize = 12.sp, color = BestNetMuted)

          Text("2. Ather 450X (Grey) - MH 02 CD 8892", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Text("Parking Slot: P2 - #022 (EV Charging Slot)", fontSize = 12.sp, color = BestNetMuted)
        }
      },
      shape = RoundedCornerShape(18.dp),
      containerColor = BestNetSurface
    )
  }

  // Family Dialog
  if (showFamilyDialog) {
    AlertDialog(
      onDismissRequest = { showFamilyDialog = false },
      confirmButton = {
        Button(
          onClick = { showFamilyDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Done", fontWeight = FontWeight.Bold)
        }
      },
      title = {
        Text("Family Members", fontWeight = FontWeight.Bold, fontSize = 18.sp)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("• Rahul Sharma (Primary Resident)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Text("• Priya Sharma (Spouse) · Access: Full", fontSize = 13.sp)
          Text("• Aarav Sharma (Child) · Access: Intercom Only", fontSize = 13.sp)
        }
      },
      shape = RoundedCornerShape(18.dp),
      containerColor = BestNetSurface
    )
  }

  // About Dialog
  if (showAboutDialog) {
    AlertDialog(
      onDismissRequest = { showAboutDialog = false },
      confirmButton = {
        Button(
          onClick = { showAboutDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Close", fontWeight = FontWeight.Bold)
        }
      },
      title = {
        Text("About BestNet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text("BestNet Customer App", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text("Version 1.0.0 (Production Build)", fontSize = 12.sp, color = BestNetMuted)
          Text(
            "Apartment Living Made Smarter. Providing high-speed Internet, smart Intercom, Community living, and 24x7 customer support.",
            fontSize = 12.5.sp,
            color = BestNetInk,
            lineHeight = 16.sp
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text("Connected Communities · Brighter Lives", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BestNetGreen)
        }
      },
      shape = RoundedCornerShape(18.dp),
      containerColor = BestNetSurface
    )
  }

  // Logout Confirm Dialog
  if (showLogoutConfirm) {
    AlertDialog(
      onDismissRequest = { showLogoutConfirm = false },
      confirmButton = {
        Button(
          onClick = {
            showLogoutConfirm = false
            onLogout()
          },
          colors = ButtonDefaults.buttonColors(containerColor = BestNetRed),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Logout", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogoutConfirm = false }) {
          Text("Cancel", color = BestNetMuted)
        }
      },
      title = {
        Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 18.sp)
      },
      text = {
        Text("Are you sure you want to sign out from your BestNet account?", fontSize = 13.5.sp)
      },
      shape = RoundedCornerShape(18.dp),
      containerColor = BestNetSurface
    )
  }
}

@Composable
fun ProfileRow(
  icon: ImageVector,
  title: String,
  isDestructive: Boolean = false,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(if (isDestructive) BestNetRedLight else BestNetSurfaceVariant),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = if (isDestructive) BestNetRed else BestNetInk,
          modifier = Modifier.size(18.dp)
        )
      }

      Text(
        text = title,
        fontSize = 14.5.sp,
        fontWeight = if (isDestructive) FontWeight.Bold else FontWeight.Medium,
        color = if (isDestructive) BestNetRed else BestNetInk
      )
    }

    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
      contentDescription = null,
      tint = BestNetMuted,
      modifier = Modifier.size(14.dp)
    )
  }
}
