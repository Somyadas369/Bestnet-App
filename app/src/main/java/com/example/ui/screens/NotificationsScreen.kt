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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WaterDrop
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Notice
import com.example.ui.components.DetailTopBar
import com.example.ui.theme.BestNetAmber
import com.example.ui.theme.BestNetAmberLight
import com.example.ui.theme.BestNetBackground
import com.example.ui.theme.BestNetBlue
import com.example.ui.theme.BestNetBlueLight
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
fun NotificationsScreen(
  notices: List<Notice>,
  onBackClick: () -> Unit,
  onNoticeClick: (Long) -> Unit,
  onMarkAllReadClick: () -> Unit
) {
  var selectedCategory by remember { mutableStateOf("All") }

  val filteredNotices = remember(selectedCategory, notices) {
    when (selectedCategory) {
      "Announcements" -> notices.filter { it.category.equals("Announcements", ignoreCase = true) }
      "Services" -> notices.filter { it.category.equals("Services", ignoreCase = true) }
      else -> notices
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BestNetBackground)
      .statusBarsPadding()
  ) {
    DetailTopBar(
      title = "Notifications",
      onBackClick = onBackClick,
      actions = {
        TextButton(onClick = onMarkAllReadClick) {
          Text("Mark all read", fontSize = 12.sp, color = BestNetGreen, fontWeight = FontWeight.Bold)
        }
      }
    )

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(4.dp))
        // Filter Pills
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          listOf("All", "Announcements", "Services").forEach { cat ->
            val isSelected = selectedCategory == cat
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) BestNetGreen else BestNetSurfaceVariant)
                .clickable { selectedCategory = cat }
                .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
              Text(
                text = cat,
                color = if (isSelected) Color.White else BestNetInk,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              )
            }
          }
        }
      }

      items(filteredNotices) { notice ->
        val (bgIconColor, tintColor) = when (notice.iconType) {
          "water" -> BestNetBlueLight to BestNetBlue
          "maintenance" -> BestNetAmberLight to BestNetAmber
          "event" -> BestNetRedLight to BestNetRed
          "complaint" -> BestNetGreenLight to BestNetGreen
          else -> BestNetBlueLight to BestNetBlue
        }

        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onNoticeClick(notice.id) },
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (!notice.isRead) BestNetSurface else BestNetSurface.copy(alpha = 0.85f)
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = if (!notice.isRead) 1.5.dp else 0.5.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .border(
                1.dp,
                if (!notice.isRead) BestNetBorder else BestNetBorder.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
              )
              .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(bgIconColor),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = when (notice.iconType) {
                  "water" -> Icons.Default.WaterDrop
                  "maintenance" -> Icons.Default.Build
                  "event" -> Icons.Default.Event
                  "complaint" -> Icons.Default.CheckCircle
                  else -> Icons.Default.Person
                },
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(22.dp)
              )
            }

            Column(modifier = Modifier.weight(1f)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = notice.title,
                  fontSize = 14.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = BestNetInk
                )

                if (!notice.isRead) {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(BestNetBlue)
                  )
                }
              }

              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = notice.body,
                fontSize = 12.5.sp,
                color = if (!notice.isRead) BestNetInk.copy(alpha = 0.85f) else BestNetMuted,
                lineHeight = 16.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = notice.timestampText,
                fontSize = 11.sp,
                color = BestNetMuted
              )
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}
