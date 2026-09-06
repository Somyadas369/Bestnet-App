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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalPhone
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.remote.CommunityEventDto
import com.example.data.remote.EmergencyContactDto
import com.example.data.remote.eventWhen
import com.example.ui.theme.BestNetBackground
import com.example.ui.theme.BestNetBorder
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetGreenLight
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted
import com.example.ui.theme.BestNetSurface

@Composable
fun CommunityScreen(
  onNavigateToNotices: () -> Unit,
  onShowComingSoon: (String) -> Unit,
  // null = not loaded yet, distinguished from "none scheduled".
  events: List<CommunityEventDto>? = null,
  emergencyContacts: List<EmergencyContactDto>? = null,
) {
  var showEventsDialog by remember { mutableStateOf(false) }
  var showAmenitiesDialog by remember { mutableStateOf(false) }
  var showEmergencyDialog by remember { mutableStateOf(false) }

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
        text = "Community",
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
      // Banner Card
      item {
        Spacer(modifier = Modifier.height(4.dp))
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
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
              Text(
                text = "Stronger Together",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BestNetInk
              )
              Text(
                text = "A Cleaner, Safer & Happier Community",
                fontSize = 12.5.sp,
                color = BestNetMuted,
                modifier = Modifier.padding(top = 2.dp)
              )
            }

            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp))
            ) {
              Image(
                painter = painterResource(id = R.drawable.img_community_banner),
                contentDescription = "Community",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
              )
            }
          }
        }
      }

      // Community Menu Rows
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
            CommunityRow(
              icon = Icons.Default.Campaign,
              title = "Notices & Announcements",
              subtitle = "Society guidelines, water & power schedules",
              onClick = onNavigateToNotices
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            CommunityRow(
              icon = Icons.Default.Event,
              title = "Events",
              subtitle = "Independence Gala, Yoga workshop",
              onClick = { showEventsDialog = true }
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            CommunityRow(
              icon = Icons.Default.Contacts,
              title = "Community Directory",
              subtitle = "Society President, Secretary, Facility Mgr",
              onClick = { onShowComingSoon("Community Directory") }
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            CommunityRow(
              icon = Icons.Default.FitnessCenter,
              title = "Amenities Booking",
              subtitle = "Clubhouse, Tennis Court, Swimming Pool",
              onClick = { showAmenitiesDialog = true }
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BestNetBorder))
            CommunityRow(
              icon = Icons.Default.LocalPhone,
              title = "Emergency Contacts",
              subtitle = "Security Guard, Fire, Ambulance, Police",
              onClick = { showEmergencyDialog = true }
            )
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Events Dialog
  if (showEventsDialog) {
    AlertDialog(
      onDismissRequest = { showEventsDialog = false },
      confirmButton = {
        Button(
          onClick = { showEventsDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Done", fontWeight = FontWeight.Bold)
        }
      },
      title = {
        Text("Upcoming Community Events", fontWeight = FontWeight.Bold, fontSize = 18.sp)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          when {
            events == null -> Text("Loading…", fontSize = 13.sp, color = BestNetMuted)
            events.isEmpty() -> Text(
              "Nothing scheduled right now.",
              fontSize = 13.sp,
              color = BestNetMuted,
            )
            else -> events.forEach { e ->
              Text(e.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text(
                listOfNotNull(eventWhen(e.startsAt), e.venue).joinToString(" · "),
                fontSize = 12.sp,
                color = BestNetMuted,
              )
            }
          }
        }
      },
      shape = RoundedCornerShape(18.dp),
      containerColor = BestNetSurface
    )
  }

  // Amenities Dialog
  if (showAmenitiesDialog) {
    AlertDialog(
      onDismissRequest = { showAmenitiesDialog = false },
      confirmButton = {
        Button(
          onClick = { showAmenitiesDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Close", fontWeight = FontWeight.Bold)
        }
      },
      title = {
        Text("Amenities Booking", fontWeight = FontWeight.Bold, fontSize = 18.sp)
      },
      text = {
        // Sample data, and said so plainly. There is no amenity or booking model
        // in the backend, so these availabilities are not real and booking one
        // is not possible — showing them unlabelled would have residents turn
        // up at a court they think they reserved.
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            "Amenities booking isn't built yet. The list below is an example, not live availability.",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFFB45309),
          )
          Text("• Clubhouse Banquet", fontSize = 13.sp, color = BestNetMuted)
          Text("• Tennis Court", fontSize = 13.sp, color = BestNetMuted)
          Text("• Swimming Pool", fontSize = 13.sp, color = BestNetMuted)
          Text("• Gymnasium", fontSize = 13.sp, color = BestNetMuted)
        }
      },
      shape = RoundedCornerShape(18.dp),
      containerColor = BestNetSurface
    )
  }

  // Emergency Dialog
  if (showEmergencyDialog) {
    AlertDialog(
      onDismissRequest = { showEmergencyDialog = false },
      confirmButton = {
        Button(
          onClick = { showEmergencyDialog = false },
          colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Done", fontWeight = FontWeight.Bold)
        }
      },
      title = {
        Text("Emergency Society Contacts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          when {
            emergencyContacts == null -> Text("Loading…", fontSize = 13.sp, color = BestNetMuted)
            emergencyContacts.isEmpty() -> Text(
              "Your community hasn't added emergency contacts yet.",
              fontSize = 13.sp,
              color = BestNetMuted,
            )
            else -> emergencyContacts.forEach { c ->
              Text("${c.label}: ${c.phone}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
          }
        }
      },
      shape = RoundedCornerShape(18.dp),
      containerColor = BestNetSurface
    )
  }
}

@Composable
fun CommunityRow(
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
