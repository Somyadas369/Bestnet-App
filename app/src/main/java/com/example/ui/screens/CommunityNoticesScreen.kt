package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Announcement
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommunityNotice
import com.example.ui.components.DetailTopBar
import com.example.ui.theme.BestNetAmber
import com.example.ui.theme.BestNetAmberLight
import com.example.ui.theme.BestNetBackground
import com.example.ui.theme.BestNetBlue
import com.example.ui.theme.BestNetBlueLight
import com.example.ui.theme.BestNetBorder
import com.example.ui.theme.BestNetCyan
import com.example.ui.theme.BestNetCyanLight
import com.example.ui.theme.BestNetDarkNavy
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetGreenLight
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted
import com.example.ui.theme.BestNetPurple
import com.example.ui.theme.BestNetPurpleLight
import com.example.ui.theme.BestNetRed
import com.example.ui.theme.BestNetRedLight
import com.example.ui.theme.BestNetSurface
import com.example.ui.theme.BestNetSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CommunityNoticesScreen(
  notices: List<CommunityNotice>,
  onBackClick: () -> Unit
) {
  var selectedCategory by remember { mutableStateOf("All") }
  var searchQuery by remember { mutableStateOf("") }
  var sortDescending by remember { mutableStateOf(true) } // Chronological: newest first by default

  val categories = listOf("All", "Maintenance", "Association", "Security", "Cultural", "Environment")

  // Filter and sort notices chronologically
  val processedNotices by remember(notices, selectedCategory, searchQuery, sortDescending) {
    derivedStateOf {
      var list = notices

      // Category filter
      if (selectedCategory != "All") {
        list = list.filter { it.category.equals(selectedCategory, ignoreCase = true) }
      }

      // Search query
      if (searchQuery.isNotBlank()) {
        val q = searchQuery.trim().lowercase()
        list = list.filter {
          it.title.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.author.lowercase().contains(q) ||
            it.category.lowercase().contains(q)
        }
      }

      // Chronological sort: timestamp DESC (newest first) or ASC (oldest first)
      if (sortDescending) {
        // Pinned on top if newest first, then timestamp descending
        list.sortedWith(
          compareByDescending<CommunityNotice> { it.isPinned }
            .thenByDescending { it.timestamp }
        )
      } else {
        list.sortedBy { it.timestamp }
      }
    }
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(BestNetBackground)
      .statusBarsPadding()
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(BestNetBackground)
        .padding(paddingValues)
    ) {
      // Top Navigation Bar
      DetailTopBar(
        title = "Community Notices",
        onBackClick = onBackClick,
        actions = {
          // Chronological Sort Order Toggle
          IconButton(
            onClick = { sortDescending = !sortDescending },
            modifier = Modifier.testTag("btn_toggle_sort")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Sort,
              contentDescription = if (sortDescending) "Sorted Newest First" else "Sorted Oldest First",
              tint = if (sortDescending) BestNetGreen else BestNetInk
            )
          }
        }
      )

      // Subheader with active count and chronological direction pill
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(BestNetSurface)
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .background(BestNetGreen, CircleShape)
          )
          Text(
            text = "${processedNotices.size} Circulars & Notices",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = BestNetMuted
          )
        }

        // Chronological order indicator badge
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = BestNetGreenLight,
          modifier = Modifier.clickable { sortDescending = !sortDescending }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Schedule,
              contentDescription = null,
              tint = BestNetGreen,
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = if (sortDescending) "Chronological: Newest First" else "Chronological: Oldest First",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = BestNetGreen
            )
          }
        }
      }

      HorizontalDivider(color = BestNetBorder, thickness = 1.dp)

      // Search Field
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(BestNetSurface)
          .padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search notices, agenda, circulars...", fontSize = 13.sp, color = BestNetMuted) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = BestNetMuted, modifier = Modifier.size(20.dp))
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = BestNetMuted, modifier = Modifier.size(18.dp))
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_search_notices"),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = BestNetBackground,
            unfocusedContainerColor = BestNetBackground,
            focusedBorderColor = BestNetGreen,
            unfocusedBorderColor = BestNetBorder
          ),
          singleLine = true
        )
      }

      // Horizontal Category Filter Chips
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(BestNetSurface)
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categories.forEach { category ->
          val isSelected = selectedCategory == category
          val chipBg = if (isSelected) BestNetGreen else BestNetSurfaceVariant
          val chipText = if (isSelected) Color.White else BestNetInk
          val chipBorder = if (isSelected) BestNetGreen else BestNetBorder

          Surface(
            shape = RoundedCornerShape(20.dp),
            color = chipBg,
            border = androidx.compose.foundation.BorderStroke(1.dp, chipBorder),
            modifier = Modifier
              .clickable { selectedCategory = category }
              .testTag("chip_$category")
          ) {
            Text(
              text = category,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = chipText,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
          }
        }
      }

      HorizontalDivider(color = BestNetBorder, thickness = 1.dp)

      // Notice Feed (Chronological)
      if (processedNotices.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Box(
              modifier = Modifier
                .size(64.dp)
                .background(BestNetGreenLight, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Announcement,
                contentDescription = null,
                tint = BestNetGreen,
                modifier = Modifier.size(32.dp)
              )
            }
            Text(
              text = "No Notices Found",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = BestNetInk
            )
            Text(
              text = if (searchQuery.isNotEmpty()) "No community circulars matched '$searchQuery'." else "No notices available in this category.",
              fontSize = 13.sp,
              color = BestNetMuted,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            if (searchQuery.isNotEmpty() || selectedCategory != "All") {
              OutlinedButton(
                onClick = {
                  searchQuery = ""
                  selectedCategory = "All"
                }
              ) {
                Text("Reset Filters")
              }
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 24.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          items(processedNotices, key = { it.id }) { notice ->
            CommunityNoticeCard(notice = notice)
          }
        }
      }
    }
  }
}

@Composable
fun CommunityNoticeCard(
  notice: CommunityNotice
) {
  var isExpanded by remember { mutableStateOf(false) }
  val clipboardManager = LocalClipboardManager.current
  var showCopiedFeedback by remember { mutableStateOf(false) }

  // Visual styling based on category and priority
  val categoryInfo = getNoticeCategoryVisuals(notice.category)
  val priorityInfo = getNoticePriorityVisuals(notice.priority)

  val formattedTime = remember(notice.timestamp) {
    formatTimestampDetailed(notice.timestamp)
  }
  val relativeTime = remember(notice.timestamp) {
    formatRelativeTime(notice.timestamp)
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .testTag("notice_card_${notice.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = BestNetSurface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, if (notice.isPinned) BestNetAmber else BestNetBorder, RoundedCornerShape(16.dp))
        .padding(16.dp)
    ) {
      // Pinned Banner if active
      if (notice.isPinned) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.PushPin,
            contentDescription = "Pinned",
            tint = BestNetAmber,
            modifier = Modifier.size(15.dp)
          )
          Text(
            text = "PINNED CIRCULAR",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = BestNetAmber,
            letterSpacing = 0.5.sp
          )
        }
      }

      // Metadata Header Row: Category Badge + Priority Pill + Timestamp
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Category Badge
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(categoryInfo.bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = categoryInfo.icon,
            contentDescription = null,
            tint = categoryInfo.textColor,
            modifier = Modifier.size(13.dp)
          )
          Text(
            text = notice.category,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = categoryInfo.textColor
          )
        }

        // Priority Badge
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = priorityInfo.bgColor,
          border = androidx.compose.foundation.BorderStroke(0.5.dp, priorityInfo.borderColor)
        ) {
          Text(
            text = priorityInfo.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = priorityInfo.textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Notice Title
      Text(
        text = notice.title,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = BestNetInk,
        lineHeight = 22.sp
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Notice Description (Click to expand if long)
      Text(
        text = notice.description,
        fontSize = 13.sp,
        color = BestNetDarkNavy,
        lineHeight = 20.sp,
        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
        overflow = TextOverflow.Ellipsis
      )

      if (notice.description.length > 140) {
        Text(
          text = if (isExpanded) "Show Less" else "Read More...",
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = BestNetGreen,
          modifier = Modifier
            .clickable { isExpanded = !isExpanded }
            .padding(vertical = 4.dp)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = BestNetBorder, thickness = 0.8.dp)
      Spacer(modifier = Modifier.height(10.dp))

      // Footer Row: Chronological Timestamp + Author + Quick Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Timestamp Display
        Column {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CalendarToday,
              contentDescription = "Date",
              tint = BestNetMuted,
              modifier = Modifier.size(13.dp)
            )
            Text(
              text = formattedTime,
              fontSize = 11.sp,
              fontWeight = FontWeight.Medium,
              color = BestNetInk
            )
            Text(
              text = "• $relativeTime",
              fontSize = 11.sp,
              color = BestNetMuted
            )
          }

          Text(
            text = "By ${notice.author}",
            fontSize = 11.sp,
            color = BestNetMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp)
          )
        }

        // Action Icons: Copy & Delete
        Row(
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = {
              val shareText = "${notice.title}\n\n${notice.description}\n\nIssued by: ${notice.author}\nDate: $formattedTime"
              clipboardManager.setText(AnnotatedString(shareText))
              showCopiedFeedback = true
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = if (showCopiedFeedback) Icons.Default.Check else Icons.Default.Share,
              contentDescription = "Share or Copy Notice",
              tint = if (showCopiedFeedback) BestNetGreen else BestNetMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}

// Helpers for visual presentation
data class NoticeCategoryVisual(
  val icon: ImageVector,
  val bgColor: Color,
  val textColor: Color
)

fun getNoticeCategoryVisuals(category: String): NoticeCategoryVisual {
  return when (category.lowercase(Locale.getDefault())) {
    "maintenance" -> NoticeCategoryVisual(Icons.Default.Build, BestNetAmberLight, BestNetAmber)
    "association" -> NoticeCategoryVisual(Icons.Default.Groups, BestNetBlueLight, BestNetBlue)
    "security" -> NoticeCategoryVisual(Icons.Default.Security, BestNetRedLight, BestNetRed)
    "cultural", "event" -> NoticeCategoryVisual(Icons.Default.Event, BestNetPurpleLight, BestNetPurple)
    "environment", "sustainability" -> NoticeCategoryVisual(Icons.Default.Park, BestNetGreenLight, BestNetGreen)
    else -> NoticeCategoryVisual(Icons.Default.Announcement, BestNetCyanLight, BestNetCyan)
  }
}

data class NoticePriorityVisual(
  val label: String,
  val bgColor: Color,
  val textColor: Color,
  val borderColor: Color
)

fun getNoticePriorityVisuals(priority: String): NoticePriorityVisual {
  return when (priority.lowercase(Locale.getDefault())) {
    "urgent" -> NoticePriorityVisual("Urgent", BestNetRedLight, BestNetRed, BestNetRed)
    "important" -> NoticePriorityVisual("Important", BestNetAmberLight, BestNetAmber, BestNetAmber)
    else -> NoticePriorityVisual("Normal", BestNetSurfaceVariant, BestNetMuted, BestNetBorder)
  }
}

fun formatTimestampDetailed(timestamp: Long): String {
  val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
  return sdf.format(Date(timestamp))
}

fun formatRelativeTime(timestamp: Long): String {
  val now = System.currentTimeMillis()
  val diff = now - timestamp
  val minute = 60 * 1000L
  val hour = 60 * minute
  val day = 24 * hour

  return when {
    diff < minute -> "Just now"
    diff < hour -> "${diff / minute}m ago"
    diff < day -> "${diff / hour}h ago"
    diff < 2 * day -> "Yesterday"
    diff < 7 * day -> "${diff / day}d ago"
    else -> {
      val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
      sdf.format(Date(timestamp))
    }
  }
}
