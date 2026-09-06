package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.SubscriptionDto
import com.example.data.remote.absoluteDate
import com.example.data.remote.subscriptionStatusLabel
import com.example.ui.components.PreviewBanner
import com.example.ui.theme.BestNetBackground
import com.example.ui.theme.BestNetBorder
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetGreenDark
import com.example.ui.theme.BestNetGreenLight
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted
import com.example.ui.theme.BestNetSurface

@Composable
fun ServicesScreen(
  onOpenSpeedTest: () -> Unit,
  onShowComingSoon: (String) -> Unit,
  // null = not loaded yet. Distinguished from an empty list so the card can say
  // "Loading…" rather than claiming there is no active plan.
  subscriptions: List<SubscriptionDto>? = null,
) {
  val activeSubscription = subscriptions?.firstOrNull { it.status == "ACTIVE" } ?: subscriptions?.firstOrNull()
  var showWifiSettingsDialog by remember { mutableStateOf(false) }
  var showBillingDialog by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BestNetBackground)
      .statusBarsPadding()
  ) {
    // Title Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(BestNetSurface)
        .padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "My Services",
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        color = BestNetInk
      )
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(4.dp))
        PreviewBanner(text = "Preview — Connected BestNet Fiber plan details and service management.")
      }

      // Active Plan Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = BestNetSurface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, BestNetBorder, RoundedCornerShape(18.dp))
          ) {
            // Header banner
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  Brush.horizontalGradient(
                    colors = listOf(BestNetGreenDark, BestNetGreen)
                  )
                )
                .padding(16.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                  )
                }

                Column {
                  Text(
                    text = activeSubscription?.plan?.name ?: "BestNet",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = when {
                      subscriptions == null -> "Loading…"
                      activeSubscription == null -> "No active plan on this home"
                      else -> subscriptionStatusLabel(activeSubscription.status)
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp
                  )
                }
              }
            }

            // Plan Details
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = "Plan Details",
                  fontSize = 12.sp,
                  color = BestNetMuted,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  // The plan's own description (e.g. speed) when the tenant set
                  // one; otherwise the plan name rather than an invented speed.
                  text = activeSubscription?.plan?.description
                    ?: activeSubscription?.plan?.name
                    ?: if (subscriptions == null) "…" else "—",
                  fontSize = 22.sp,
                  fontWeight = FontWeight.Bold,
                  color = BestNetInk
                )
                Text(
                  text = activeSubscription?.currentPeriodEnd
                    ?.let { "Valid till " + absoluteDate(it) }
                    ?: activeSubscription?.plan?.monthlyPriceRupees?.let { "₹$it / month" }
                    ?: "",
                  fontSize = 12.sp,
                  color = BestNetMuted
                )
              }

              Button(
                onClick = { onShowComingSoon("Upgrade Plan") },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen)
              ) {
                Text("Upgrade Plan", fontSize = 13.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      // Menu list rows
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
            ServiceMenuRow(
              icon = Icons.Default.Speed,
              title = "Usage & Speed Test",
              subtitle = "Unlimited GB · Live Speed Check",
              onClick = onOpenSpeedTest
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            ServiceMenuRow(
              icon = Icons.Default.Receipt,
              title = "Billing & Payments",
              subtitle = "₹825/month · Next Due: 15 Sep 2026",
              onClick = { showBillingDialog = true }
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            ServiceMenuRow(
              icon = Icons.Default.SupportAgent,
              title = "Service Requests",
              subtitle = "1 Ticket Resolved",
              onClick = { onShowComingSoon("Service Requests") }
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            ServiceMenuRow(
              icon = Icons.Default.Wifi,
              title = "Wi-Fi Settings",
              subtitle = "BestNet_5G_1201 · 4 Devices",
              onClick = { showWifiSettingsDialog = true }
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Wi-Fi Settings Dialog
  if (showWifiSettingsDialog) {
    AlertDialog(
      onDismissRequest = { showWifiSettingsDialog = false },
      confirmButton = {
        Button(
          onClick = { showWifiSettingsDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Done", fontWeight = FontWeight.Bold)
        }
      },
      title = {
        Text("Wi-Fi Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("SSID (Network Name):", fontSize = 12.sp, color = BestNetMuted)
          Text("BestNet_5G_A1201", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BestNetInk)

          Text("Band:", fontSize = 12.sp, color = BestNetMuted)
          Text("Dual Band 2.4 GHz + 5 GHz", fontSize = 14.sp, color = BestNetInk)

          Text("Connected Devices:", fontSize = 12.sp, color = BestNetMuted)
          Text("4 Devices (Living Room TV, Rahul's Phone, Laptop, Tablet)", fontSize = 13.sp, color = BestNetInk)
        }
      },
      shape = RoundedCornerShape(18.dp),
      containerColor = BestNetSurface
    )
  }

  // Billing Dialog
  if (showBillingDialog) {
    AlertDialog(
      onDismissRequest = { showBillingDialog = false },
      confirmButton = {
        Button(
          onClick = { showBillingDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Close", fontWeight = FontWeight.Bold)
        }
      },
      title = {
        Text("Billing & Invoices", fontWeight = FontWeight.Bold, fontSize = 18.sp)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Current Cycle:", fontSize = 12.sp, color = BestNetMuted)
          Text("01 Aug 2026 - 31 Aug 2026 (Paid ₹825)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = BestNetGreen)

          Text("Next Invoice Due:", fontSize = 12.sp, color = BestNetMuted)
          Text("15 Sep 2026 · Amount: ₹825", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = BestNetInk)

          Text("Payment Method:", fontSize = 12.sp, color = BestNetMuted)
          Text("Auto-Pay Active (HDFC Bank UPI)", fontSize = 13.sp, color = BestNetInk)
        }
      },
      shape = RoundedCornerShape(18.dp),
      containerColor = BestNetSurface
    )
  }
}

@Composable
fun ServiceMenuRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
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
          .size(40.dp)
          .clip(CircleShape)
          .background(BestNetGreenLight),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = BestNetGreen,
          modifier = Modifier.size(20.dp)
        )
      }

      Column {
        Text(
          text = title,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = BestNetInk
        )
        Text(
          text = subtitle,
          fontSize = 12.sp,
          color = BestNetMuted
        )
      }
    }

    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
      contentDescription = null,
      tint = BestNetMuted,
      modifier = Modifier.size(14.dp)
    )
  }
}
