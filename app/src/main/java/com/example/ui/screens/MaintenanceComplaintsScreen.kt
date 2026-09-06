package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Complaint
import com.example.ui.components.DetailTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BestNetAmber
import com.example.ui.theme.BestNetAmberLight
import com.example.ui.theme.BestNetBackground
import com.example.ui.theme.BestNetBlue
import com.example.ui.theme.BestNetBlueLight
import com.example.ui.theme.BestNetBorder
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetGreenDark
import com.example.ui.theme.BestNetGreenLight
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted
import com.example.ui.theme.BestNetRed
import com.example.ui.theme.BestNetRedLight
import com.example.ui.theme.BestNetSurface
import com.example.ui.theme.BestNetSurfaceVariant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MaintenanceComplaintsScreen(
  complaints: List<Complaint>,
  currentUnit: String = "A-1201",
  onBackClick: () -> Unit,
  // onResult reports what the *server* did. It used to be a bare
  // `onComplete(ticketNumber)` that always fired, with the number invented
  // locally — so a failed submission still showed a confirmation dialog.
  onSubmitComplaint: (
    title: String,
    category: String,
    description: String,
    priority: String,
    onResult: (success: Boolean, reference: String?) -> Unit,
  ) -> Unit,
  onUpdateStatus: (id: Long, newStatus: String) -> Unit,
  onDeleteComplaint: (id: Long) -> Unit,
  submitting: Boolean = false,
  errorMessage: String? = null,
) {
  var selectedTab by remember { mutableIntStateOf(0) } // 0: Track Complaints, 1: Submit Complaint
  var statusFilter by remember { mutableStateOf("All") } // All, Pending, In Progress, Resolved
  var searchQuery by remember { mutableStateOf("") }
  var recentlySubmittedTicket by remember { mutableStateOf<String?>(null) }

  // Counts
  val pendingCount = complaints.count { it.status.equals("Pending", ignoreCase = true) }
  val inProgressCount = complaints.count { it.status.equals("In Progress", ignoreCase = true) }
  val resolvedCount = complaints.count { it.status.equals("Resolved", ignoreCase = true) }

  val filteredComplaints = remember(complaints, statusFilter, searchQuery) {
    complaints.filter { complaint ->
      val matchesStatus = when (statusFilter) {
        "Pending" -> complaint.status.equals("Pending", ignoreCase = true)
        "In Progress" -> complaint.status.equals("In Progress", ignoreCase = true)
        "Resolved" -> complaint.status.equals("Resolved", ignoreCase = true)
        else -> true
      }
      val matchesSearch = if (searchQuery.isBlank()) true else {
        complaint.title.contains(searchQuery, ignoreCase = true) ||
          complaint.ticketNumber.contains(searchQuery, ignoreCase = true) ||
          complaint.category.contains(searchQuery, ignoreCase = true) ||
          complaint.description.contains(searchQuery, ignoreCase = true)
      }
      matchesStatus && matchesSearch
    }
  }

  Scaffold(
    topBar = {
      DetailTopBar(
        title = "Maintenance Complaints",
        onBackClick = onBackClick,
        actions = {
          if (selectedTab == 0) {
            Button(
              onClick = { selectedTab = 1 },
              colors = ButtonDefaults.buttonColors(containerColor = BestNetGreenLight),
              shape = RoundedCornerShape(20.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = null, tint = BestNetGreen, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("New", color = BestNetGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
          }
        }
      )
    },
    floatingActionButton = {
      if (selectedTab == 0) {
        FloatingActionButton(
          onClick = { selectedTab = 1 },
          containerColor = BestNetGreen,
          contentColor = Color.White,
          shape = RoundedCornerShape(16.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.Add, contentDescription = "New Complaint")
            Text("New Complaint", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(BestNetBackground)
        .padding(paddingValues)
    ) {
      // Tab Bar: Track Complaints vs Submit Complaint
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = BestNetSurface,
        contentColor = BestNetGreen,
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
            color = BestNetGreen,
            height = 3.dp
          )
        }
      ) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                "Track Complaints",
                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp
              )
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (selectedTab == 0) BestNetGreenLight else BestNetSurfaceVariant)
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "${complaints.size}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (selectedTab == 0) BestNetGreen else BestNetMuted
                )
              }
            }
          }
        )

        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = {
            Text(
              "+ Submit Complaint",
              fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
              fontSize = 14.sp
            )
          }
        )
      }

      if (selectedTab == 0) {
        // Track View
        TrackComplaintsView(
          complaints = filteredComplaints,
          totalCount = complaints.size,
          pendingCount = pendingCount,
          inProgressCount = inProgressCount,
          resolvedCount = resolvedCount,
          currentFilter = statusFilter,
          onFilterChange = { statusFilter = it },
          searchQuery = searchQuery,
          onSearchChange = { searchQuery = it },
          onUpdateStatus = onUpdateStatus,
          onDeleteComplaint = onDeleteComplaint,
          onGoToSubmit = { selectedTab = 1 }
        )
      } else {
        // Submit View
        SubmitComplaintFormView(
          currentUnit = currentUnit,
          submitting = submitting,
          errorMessage = errorMessage,
          onSubmit = { title, category, description, priority ->
            onSubmitComplaint(title, category, description, priority) { success, reference ->
              // Confirmation and the jump to the list only happen when the
              // server actually accepted the ticket.
              if (success) {
                recentlySubmittedTicket = reference
                selectedTab = 0
              }
            }
          }
        )
      }
    }
  }

  // Confirmation dialog after submitting
  recentlySubmittedTicket?.let { ticket ->
    AlertDialog(
      onDismissRequest = { recentlySubmittedTicket = null },
      confirmButton = {
        Button(
          onClick = { recentlySubmittedTicket = null },
          colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("View in Tracker", fontWeight = FontWeight.Bold)
        }
      },
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BestNetGreen)
          Text("Complaint Registered", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Your maintenance complaint has been logged with ticket $ticket.",
            fontSize = 14.sp,
            color = BestNetInk
          )
          Text(
            text = "Initial Status: Pending",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BestNetAmber
          )
          Text(
            text = "Society facility management will review and dispatch a technician to $currentUnit shortly.",
            fontSize = 12.5.sp,
            color = BestNetMuted
          )
        }
      },
      shape = RoundedCornerShape(18.dp),
      containerColor = BestNetSurface
    )
  }
}

/**
 * Track Complaints Tab Content
 */
@Composable
fun TrackComplaintsView(
  complaints: List<Complaint>,
  totalCount: Int,
  pendingCount: Int,
  inProgressCount: Int,
  resolvedCount: Int,
  currentFilter: String,
  onFilterChange: (String) -> Unit,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  onUpdateStatus: (Long, String) -> Unit,
  onDeleteComplaint: (Long) -> Unit,
  onGoToSubmit: () -> Unit
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Status Summary Metric Cards
    item {
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        StatusSummaryCard(
          label = "Pending",
          count = pendingCount,
          accentColor = BestNetAmber,
          bgColor = BestNetAmberLight,
          isSelected = currentFilter == "Pending",
          onClick = { onFilterChange(if (currentFilter == "Pending") "All" else "Pending") },
          modifier = Modifier.weight(1f)
        )
        StatusSummaryCard(
          label = "In Progress",
          count = inProgressCount,
          accentColor = BestNetBlue,
          bgColor = BestNetBlueLight,
          isSelected = currentFilter == "In Progress",
          onClick = { onFilterChange(if (currentFilter == "In Progress") "All" else "In Progress") },
          modifier = Modifier.weight(1f)
        )
        StatusSummaryCard(
          label = "Resolved",
          count = resolvedCount,
          accentColor = BestNetGreenDark,
          bgColor = BestNetGreenLight,
          isSelected = currentFilter == "Resolved",
          onClick = { onFilterChange(if (currentFilter == "Resolved") "All" else "Resolved") },
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Search bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Search by ticket #, issue or category...", fontSize = 13.sp, color = BestNetMuted) },
        leadingIcon = {
          Icon(Icons.Default.Search, contentDescription = null, tint = BestNetMuted, modifier = Modifier.size(20.dp))
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchChange("") }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear", tint = BestNetMuted, modifier = Modifier.size(18.dp))
            }
          }
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BestNetGreen,
          unfocusedBorderColor = BestNetBorder,
          focusedContainerColor = BestNetSurface,
          unfocusedContainerColor = BestNetSurface
        ),
        modifier = Modifier.fillMaxWidth()
      )
    }

    // Filter Chips
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("All", "Pending", "In Progress", "Resolved").forEach { filter ->
          val isSelected = currentFilter == filter
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(if (isSelected) BestNetGreen else BestNetSurfaceVariant)
              .clickable { onFilterChange(filter) }
              .padding(horizontal = 14.dp, vertical = 7.dp)
          ) {
            Text(
              text = filter,
              color = if (isSelected) Color.White else BestNetInk,
              fontSize = 12.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
          }
        }
      }
    }

    if (complaints.isEmpty()) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = BestNetSurface)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, BestNetBorder, RoundedCornerShape(16.dp))
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BestNetGreen, modifier = Modifier.size(48.dp))
            Text(
              text = if (currentFilter == "All") "No maintenance complaints found" else "No complaints with status '$currentFilter'",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = BestNetInk
            )
            Text(
              text = "Everything is running smoothly. Need maintenance help? Submit a new request anytime.",
              fontSize = 13.sp,
              color = BestNetMuted,
              textAlign = TextAlign.Center
            )
            Button(
              onClick = onGoToSubmit,
              colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Submit New Complaint")
            }
          }
        }
      }
    } else {
      items(complaints, key = { it.id }) { complaint ->
        ComplaintCard(
          complaint = complaint,
          onUpdateStatus = onUpdateStatus,
          onDeleteComplaint = onDeleteComplaint
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

/**
 * Individual Maintenance Complaint Card with status indicators and timeline
 */
@Composable
fun ComplaintCard(
  complaint: Complaint,
  onUpdateStatus: (Long, String) -> Unit,
  onDeleteComplaint: (Long) -> Unit
) {
  val dateFormatted = remember(complaint.createdAt) {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    sdf.format(Date(complaint.createdAt))
  }

  var isExpanded by remember { mutableStateOf(false) }

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
        .padding(16.dp)
    ) {
      // Top row: Ticket # & Status Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = complaint.ticketNumber,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = BestNetInk
          )

          // Priority badge
          val (prioBg, prioText) = when (complaint.priority.lowercase()) {
            "urgent" -> BestNetRedLight to BestNetRed
            "high" -> BestNetAmberLight to BestNetAmber
            else -> BestNetSurfaceVariant to BestNetMuted
          }
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(prioBg)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = complaint.priority,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = prioText
            )
          }
        }

        StatusBadge(status = complaint.status)
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Title
      Text(
        text = if (complaint.title.isNotBlank()) complaint.title else "${complaint.category} Issue",
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = BestNetInk,
        lineHeight = 20.sp
      )

      // Category and Unit
      Row(
        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Icon(
            imageVector = getCategoryIcon(complaint.category),
            contentDescription = null,
            tint = BestNetGreen,
            modifier = Modifier.size(15.dp)
          )
          Text(
            text = complaint.category,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = BestNetGreenDark
          )
        }
        Text("•", color = BestNetMuted, fontSize = 12.sp)
        Text("Unit ${complaint.unit}", fontSize = 12.sp, color = BestNetMuted)
        Text("•", color = BestNetMuted, fontSize = 12.sp)
        Text(dateFormatted, fontSize = 11.5.sp, color = BestNetMuted)
      }

      // Description
      Text(
        text = complaint.description,
        fontSize = 13.sp,
        color = BestNetInk.copy(alpha = 0.85f),
        lineHeight = 18.sp,
        modifier = Modifier.padding(vertical = 4.dp)
      )

      // Status Stepper Progression Line
      Spacer(modifier = Modifier.height(10.dp))
      ComplaintStatusStepper(currentStatus = complaint.status)

      // Assigned or Resolution note
      if (!complaint.assignedTo.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Default.Engineering, contentDescription = null, tint = BestNetMuted, modifier = Modifier.size(15.dp))
          Text(
            text = complaint.assignedTo,
            fontSize = 12.sp,
            color = BestNetMuted
          )
        }
      }

      if (!complaint.resolutionNotes.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(BestNetGreenLight.copy(alpha = 0.4f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(
            text = "Resolution: ${complaint.resolutionNotes}",
            fontSize = 11.5.sp,
            color = BestNetGreenDark,
            fontWeight = FontWeight.Medium
          )
        }
      }

      // Interactive Action Bar to simulate real-world status progression
      Spacer(modifier = Modifier.height(12.dp))
      Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Change Status:",
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = BestNetMuted
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          when (complaint.status.lowercase()) {
            "pending" -> {
              OutlinedButton(
                onClick = { onUpdateStatus(complaint.id, "In Progress") },
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(Icons.Default.Autorenew, contentDescription = null, tint = BestNetBlue, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Start Work", fontSize = 11.sp, color = BestNetBlue, fontWeight = FontWeight.Bold)
              }
              Button(
                onClick = { onUpdateStatus(complaint.id, "Resolved") },
                colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Resolve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
            "in progress" -> {
              OutlinedButton(
                onClick = { onUpdateStatus(complaint.id, "Pending") },
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text("Put on Hold", fontSize = 11.sp, color = BestNetAmber, fontWeight = FontWeight.Bold)
              }
              Button(
                onClick = { onUpdateStatus(complaint.id, "Resolved") },
                colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Mark Resolved", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
            "resolved" -> {
              OutlinedButton(
                onClick = { onUpdateStatus(complaint.id, "In Progress") },
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text("Re-open Issue", fontSize = 11.sp, color = BestNetInk, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Stepper Bar indicating progression: Submitted -> In Progress -> Resolved
 */
@Composable
fun ComplaintStatusStepper(currentStatus: String) {
  val step = when (currentStatus.lowercase()) {
    "pending", "submitted" -> 1
    "in progress" -> 2
    "resolved" -> 3
    else -> 1
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(BestNetSurfaceVariant.copy(alpha = 0.6f))
      .padding(horizontal = 12.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Step 1: Pending
    StatusStepIndicator(
      number = "1",
      label = "Pending",
      isActive = step >= 1,
      isCurrent = step == 1,
      color = BestNetAmber
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .height(2.dp)
        .padding(horizontal = 6.dp)
        .background(if (step >= 2) BestNetBlue else BestNetBorder)
    )

    // Step 2: In Progress
    StatusStepIndicator(
      number = "2",
      label = "In Progress",
      isActive = step >= 2,
      isCurrent = step == 2,
      color = BestNetBlue
    )

    Box(
      modifier = Modifier
        .weight(1f)
        .height(2.dp)
        .padding(horizontal = 6.dp)
        .background(if (step >= 3) BestNetGreen else BestNetBorder)
    )

    // Step 3: Resolved
    StatusStepIndicator(
      number = "3",
      label = "Resolved",
      isActive = step >= 3,
      isCurrent = step == 3,
      color = BestNetGreen
    )
  }
}

@Composable
fun StatusStepIndicator(
  number: String,
  label: String,
  isActive: Boolean,
  isCurrent: Boolean,
  color: Color
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(18.dp)
        .clip(CircleShape)
        .background(if (isActive) color else BestNetBorder),
      contentAlignment = Alignment.Center
    ) {
      if (isActive && !isCurrent && number == "3") {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
      } else {
        Text(
          text = number,
          color = if (isActive) Color.White else BestNetMuted,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
    Text(
      text = label,
      fontSize = 10.5.sp,
      fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
      color = if (isActive) color else BestNetMuted
    )
  }
}

/**
 * Metric summary card on top of Tracker
 */
@Composable
fun StatusSummaryCard(
  label: String,
  count: Int,
  accentColor: Color,
  bgColor: Color,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() },
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) bgColor else BestNetSurface
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(
          1.5.dp,
          if (isSelected) accentColor else BestNetBorder,
          RoundedCornerShape(12.dp)
        )
        .padding(vertical = 10.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "$count",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = if (isSelected) accentColor else BestNetInk
      )
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) accentColor else BestNetMuted
      )
    }
  }
}

/**
 * Submit Complaint Form View
 */
@Composable
fun SubmitComplaintFormView(
  currentUnit: String,
  onSubmit: (title: String, category: String, description: String, priority: String) -> Unit,
  submitting: Boolean = false,
  errorMessage: String? = null,
) {
  var title by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Plumber") }
  var description by remember { mutableStateOf("") }
  var priority by remember { mutableStateOf("Medium") }
  var hasAttachedPhoto by remember { mutableStateOf(false) }

  val categories = listOf(
    Pair("Plumber", Icons.Default.WaterDamage),
    Pair("Electrician", Icons.Default.Bolt),
    Pair("House Keeping", Icons.Default.CleaningServices),
    Pair("Internet Issue", Icons.Default.Wifi),
    Pair("General Maintenance", Icons.Default.Build),
    Pair("Others", Icons.Default.MoreHoriz)
  )

  val priorities = listOf("Low", "Medium", "High", "Urgent")

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Select Category",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = BestNetInk
      )
    }

    // 3x2 Category Grid
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (row in 0 until 2) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            for (col in 0 until 3) {
              val index = row * 3 + col
              val cat = categories[index]
              val isSelected = selectedCategory == cat.first

              Card(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(14.dp))
                  .clickable { selectedCategory = cat.first },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isSelected) BestNetGreenLight else BestNetSurface
                )
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .border(
                      1.5.dp,
                      if (isSelected) BestNetGreen else BestNetBorder,
                      RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = cat.second,
                    contentDescription = null,
                    tint = if (isSelected) BestNetGreen else BestNetInk,
                    modifier = Modifier.size(24.dp)
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = cat.first,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) BestNetGreen else BestNetInk,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                  )
                }
              }
            }
          }
        }
      }
    }

    // Title / Issue summary
    item {
      Text(
        text = "Issue Title",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = BestNetInk
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        placeholder = { Text("e.g. Tap leaking under kitchen sink", fontSize = 13.5.sp, color = BestNetMuted) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BestNetGreen,
          unfocusedBorderColor = BestNetBorder,
          focusedContainerColor = BestNetSurface,
          unfocusedContainerColor = BestNetSurface
        ),
        modifier = Modifier.fillMaxWidth()
      )
    }

    // Priority Selection
    item {
      Text(
        text = "Priority",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = BestNetInk
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        priorities.forEach { p ->
          val isSelected = priority == p
          val activeColor = when (p) {
            "Urgent" -> BestNetRed
            "High" -> BestNetAmber
            "Medium" -> BestNetBlue
            else -> BestNetGreen
          }
          val activeBg = when (p) {
            "Urgent" -> BestNetRedLight
            "High" -> BestNetAmberLight
            "Medium" -> BestNetBlueLight
            else -> BestNetGreenLight
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSelected) activeBg else BestNetSurfaceVariant)
              .border(
                1.dp,
                if (isSelected) activeColor else Color.Transparent,
                RoundedCornerShape(10.dp)
              )
              .clickable { priority = p }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = p,
              color = if (isSelected) activeColor else BestNetInk,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          }
        }
      }
    }

    // Detailed Description
    item {
      Text(
        text = "Detailed Description",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = BestNetInk
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        placeholder = { Text("Please provide any relevant details so technician brings proper spare parts...", fontSize = 13.5.sp, color = BestNetMuted) },
        minLines = 4,
        maxLines = 6,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = BestNetGreen,
          unfocusedBorderColor = BestNetBorder,
          focusedContainerColor = BestNetSurface,
          unfocusedContainerColor = BestNetSurface
        ),
        modifier = Modifier.fillMaxWidth()
      )
    }

    // Photo Attachment simulation
    item {
      Text(
        text = "Add Photos (Optional)",
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = BestNetInk
      )
      Spacer(modifier = Modifier.height(6.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BestNetSurface)
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BestNetBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          if (!hasAttachedPhoto) {
            Button(
              onClick = { hasAttachedPhoto = true },
              colors = ButtonDefaults.buttonColors(containerColor = BestNetSurfaceVariant),
              shape = RoundedCornerShape(10.dp)
            ) {
              Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = BestNetInk, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("+ Attach Photo", color = BestNetInk, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
          } else {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BestNetGreenLight),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = BestNetGreen)
                }
                Column {
                  Text("photo_issue_01.jpg", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BestNetInk)
                  Text("1.8 MB · Attached to Ticket", fontSize = 11.sp, color = BestNetMuted)
                }
              }

              IconButton(onClick = { hasAttachedPhoto = false }) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
              }
            }
          }
        }
      }
    }

    // Submit Action
    item {
      Spacer(modifier = Modifier.height(6.dp))
      Button(
        onClick = {
          val finalTitle = if (title.isNotBlank()) title.trim() else "$selectedCategory maintenance issue"
          val finalDesc = if (description.isNotBlank()) description.trim() else "Maintenance requested for $selectedCategory."
          onSubmit(finalTitle, selectedCategory, finalDesc, priority)
        },
        enabled = (title.isNotBlank() || description.isNotBlank()) && !submitting,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp),
        shape = RoundedCornerShape(26.dp),
        colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen)
      ) {
        Text(
          if (submitting) "Submitting…" else "Submit Maintenance Complaint",
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold,
        )
      }

      // A complaint that did not reach the server has to say so here, on the
      // form, rather than leaving the resident to assume it was filed.
      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          errorMessage,
          color = Color(0xFFDC2626),
          fontSize = 13.sp,
          modifier = Modifier.fillMaxWidth(),
        )
      }
      Spacer(modifier = Modifier.height(36.dp))
    }
  }
}

fun getCategoryIcon(category: String): ImageVector {
  return when (category.lowercase()) {
    "plumber", "plumbing" -> Icons.Default.WaterDamage
    "electrician", "electrical" -> Icons.Default.Bolt
    "house keeping", "housekeeping" -> Icons.Default.CleaningServices
    "internet issue", "internet", "wi-fi", "wifi" -> Icons.Default.Wifi
    "general maintenance", "carpentry" -> Icons.Default.Build
    else -> Icons.Default.MoreHoriz
  }
}
