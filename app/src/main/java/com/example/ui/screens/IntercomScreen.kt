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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IntercomContact
import com.example.ui.components.DetailTopBar
import com.example.ui.theme.BestNetBackground
import com.example.ui.theme.BestNetBorder
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetGreenLight
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted
import com.example.ui.theme.BestNetSurface
import com.example.ui.theme.BestNetSurfaceVariant

@Composable
fun IntercomScreen(
  staffList: List<IntercomContact>,
  neighborsList: List<IntercomContact>,
  onBackClick: () -> Unit,
  onCallContact: (IntercomContact) -> Unit
) {
  var selectedPill by remember { mutableStateOf("Call") }
  var searchQuery by remember { mutableStateOf("") }

  val filteredStaff = remember(searchQuery, staffList) {
    if (searchQuery.isBlank()) staffList
    else staffList.filter { it.name.contains(searchQuery, ignoreCase = true) || it.role.contains(searchQuery, ignoreCase = true) }
  }

  val filteredNeighbors = remember(searchQuery, neighborsList) {
    if (searchQuery.isBlank()) neighborsList
    else neighborsList.filter {
      it.name.contains(searchQuery, ignoreCase = true) ||
        (it.unit != null && it.unit.contains(searchQuery, ignoreCase = true))
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BestNetBackground)
      .statusBarsPadding()
  ) {
    DetailTopBar(title = "Intercom", onBackClick = onBackClick)

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
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
          listOf("Call", "Contacts", "Recent").forEach { pill ->
            val isSelected = selectedPill == pill
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) BestNetGreen else BestNetSurfaceVariant)
                .clickable { selectedPill = pill }
                .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
              Text(
                text = pill,
                color = if (isSelected) Color.White else BestNetInk,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
              )
            }
          }
        }
      }

      // Search Box
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search by name or flat no.", fontSize = 13.5.sp, color = BestNetMuted) },
          leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = BestNetMuted, modifier = Modifier.size(20.dp))
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
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

      // SIP connection info card
      item {
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
              .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "My Intercom Connection",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BestNetInk
              )
              Text(
                text = "SIP Ext: 1201 · Port: 5060 · UDP",
                fontSize = 11.sp,
                color = BestNetMuted
              )
            }
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(BestNetGreenLight)
                .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
              Text(
                text = "Online",
                color = BestNetGreen,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Staff directory
      if (filteredStaff.isNotEmpty()) {
        item {
          Text(
            text = "Gate & Management",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = BestNetInk,
            modifier = Modifier.padding(top = 4.dp)
          )
        }

        items(filteredStaff) { staff ->
          IntercomContactRow(
            name = staff.name,
            role = staff.role,
            icon = when (staff.id) {
              "sec" -> Icons.Default.Security
              "mgmt" -> Icons.Default.Business
              else -> Icons.Default.Apartment
            },
            onCallClick = { onCallContact(staff) }
          )
        }
      }

      // Neighbors directory
      if (filteredNeighbors.isNotEmpty()) {
        item {
          Text(
            text = "Neighbors",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = BestNetInk,
            modifier = Modifier.padding(top = 8.dp)
          )
        }

        items(filteredNeighbors) { neighbor ->
          IntercomContactRow(
            name = neighbor.unit ?: "",
            role = neighbor.name,
            isNeighbor = true,
            onCallClick = { onCallContact(neighbor) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }
}

@Composable
fun IntercomContactRow(
  name: String,
  role: String,
  icon: ImageVector? = null,
  isNeighbor: Boolean = false,
  onCallClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = BestNetSurface),
    elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, BestNetBorder, RoundedCornerShape(14.dp))
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        if (isNeighbor) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(BestNetGreenLight),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = role.split(" ").lastOrNull()?.take(1) ?: "N",
              color = BestNetGreen,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold
            )
          }
        } else {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(BestNetSurfaceVariant),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = icon ?: Icons.Default.Apartment,
              contentDescription = null,
              tint = BestNetInk,
              modifier = Modifier.size(22.dp)
            )
          }
        }

        Column {
          Text(
            text = name,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = BestNetInk
          )
          Text(
            text = role,
            fontSize = 12.5.sp,
            color = BestNetMuted
          )
        }
      }

      // Green Call Button
      IconButton(
        onClick = onCallClick,
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(BestNetGreenLight)
      ) {
        Icon(
          imageVector = Icons.Default.Phone,
          contentDescription = "Call",
          tint = BestNetGreen,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
