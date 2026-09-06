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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.LocalTaxi
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Visitor
import com.example.ui.components.DetailTopBar
import com.example.ui.components.PreviewBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BestNetBackground
import com.example.ui.theme.BestNetBorder
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetGreenLight
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted
import com.example.ui.theme.BestNetSurface
import com.example.ui.theme.BestNetSurfaceVariant

@Composable
fun VisitorsScreen(
  visitors: List<Visitor>,
  onBackClick: () -> Unit,
  onOpenPreApproveDialog: () -> Unit,
) {
  var selectedTab by remember { mutableStateOf("Visitor Log") }

  val filteredVisitors = remember(selectedTab, visitors) {
    if (selectedTab == "Visitor Log") visitors
    else visitors.filter { it.isPreApproved }
  }

  Scaffold(
    topBar = {
      DetailTopBar(title = "Visitors", onBackClick = onBackClick)
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onOpenPreApproveDialog,
        containerColor = BestNetGreen,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = "Pre-Approve")
          Text("Pre-Approve", fontWeight = FontWeight.Bold)
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(BestNetBackground)
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(4.dp))
        // Filter Pills
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("Visitor Log", "Pre-Approved").forEach { tab ->
            val isSelected = selectedTab == tab
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) BestNetGreen else BestNetSurfaceVariant)
                .clickable { selectedTab = tab }
                .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
              Text(
                text = tab,
                color = if (isSelected) Color.White else BestNetInk,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              )
            }
          }
        }
      }

      item {
        // Narrowed to what is genuinely still missing. The log and pre-approval
        // are real now; QR/passcode entry does not exist in the product at all.
        PreviewBanner(text = "Visitor log and pre-approval are live. QR / passcode gate entry isn't built yet.")
      }

      if (filteredVisitors.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 40.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (selectedTab == "Pre-Approved") "No pre-approved visitors active.\nTap + Pre-Approve to generate a gate pass." else "No visitor logs yet.",
              color = BestNetMuted,
              fontSize = 14.sp,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      } else {
        items(filteredVisitors) { visitor ->
          VisitorCard(visitor = visitor)
        }
      }

      item {
        Spacer(modifier = Modifier.height(80.dp))
      }
    }
  }

}

@Composable
fun VisitorCard(visitor: Visitor) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = BestNetSurface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, BestNetBorder, RoundedCornerShape(14.dp))
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(BestNetGreenLight),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = when (visitor.type.lowercase()) {
              "delivery" -> Icons.Default.DeliveryDining
              "service" -> Icons.Default.Engineering
              "cab" -> Icons.Default.LocalTaxi
              else -> Icons.Default.Person
            },
            contentDescription = null,
            tint = BestNetGreen,
            modifier = Modifier.size(24.dp)
          )
        }

        Column {
          Text(
            text = visitor.name,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = BestNetInk
          )
          Text(
            text = "${visitor.type} · ${visitor.unit}",
            fontSize = 12.sp,
            color = BestNetMuted
          )
          Text(
            text = visitor.timestampText,
            fontSize = 11.sp,
            color = BestNetMuted
          )
          if (visitor.passCode != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Passcode: ${visitor.passCode}",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = BestNetGreen
            )
          }
        }
      }

      StatusBadge(status = visitor.status)
    }
  }
}
