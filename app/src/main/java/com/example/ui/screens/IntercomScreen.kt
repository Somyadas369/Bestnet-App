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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import com.example.data.sip.SipRegistration
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
  neighborsList: List<IntercomContact> = emptyList(),
  onBackClick: () -> Unit,
  onCallContact: (IntercomContact) -> Unit,
  // The resident's own extension, and the directory of extensions they can
  // dial. Both come from the server; null means "still loading", which the UI
  // distinguishes from "nobody has one".
  myExtension: String? = null,
  directory: List<IntercomContact>? = null,
  registration: SipRegistration = SipRegistration.NONE,
  sipConfigured: Boolean = false,
  sipBusy: Boolean = false,
  sipError: String? = null,
  onEnableCalling: () -> Unit = {},
) {
  var showEnableDialog by remember { mutableStateOf(false) }
  var selectedPill by remember { mutableStateOf("Call") }
  var searchQuery by remember { mutableStateOf("") }

  val filteredStaff = remember(searchQuery, staffList) {
    if (searchQuery.isBlank()) staffList
    else staffList.filter { it.name.contains(searchQuery, ignoreCase = true) || it.role.contains(searchQuery, ignoreCase = true) }
  }

  val filteredDirectory = remember(searchQuery, directory) {
    val all = directory.orEmpty()
    if (searchQuery.isBlank()) all
    else all.filter {
      it.name.contains(searchQuery, ignoreCase = true) || it.extension.contains(searchQuery)
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
          placeholder = { Text("Search by unit or extension...", fontSize = 13.5.sp, color = BestNetMuted) },
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

      // The resident's own extension. Shown prominently because in-app calling
      // does not exist — this is the number they type into their SIP app, and
      // the number a neighbour would dial to reach them.
      item {
        MyExtensionCard(
          extension = myExtension,
          registration = registration,
          sipConfigured = sipConfigured,
          sipBusy = sipBusy,
          onEnableCalling = { showEnableDialog = true },
        )
        if (sipError != null) {
          Text(
            sipError,
            color = Color(0xFFDC2626),
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 6.dp),
          )
        }
      }

      // Neighbours, from GET /me/intercom-directory. The server sends unit
      // labels and extensions only — no names or phone numbers.
      item {
        Text(
          text = "Directory",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = BestNetInk,
          modifier = Modifier.padding(top = 4.dp)
        )
      }

      if (directory == null) {
        item { Text("Loading…", fontSize = 13.sp, color = BestNetMuted) }
      } else if (filteredDirectory.isEmpty()) {
        item {
          Text(
            if (searchQuery.isBlank())
              "No other homes in your community have an intercom extension yet."
            else "No match for \"$searchQuery\".",
            fontSize = 13.sp,
            color = BestNetMuted,
          )
        }
      } else {
        items(filteredDirectory) { contact ->
          IntercomContactRow(
            name = contact.name,
            role = "Extension ${contact.extension}",
            icon = Icons.Default.Apartment,
            onCallClick = { onCallContact(contact) }
          )
        }
      }

      // Gate & Management directory
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
              "mg" -> Icons.Default.Security
              "mgmt" -> Icons.Default.Business
              else -> Icons.Default.Apartment
            },
            onCallClick = { onCallContact(staff) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // The consequence has to be stated before it happens, not explained after:
  // enabling calling resets the SIP password, which signs out every other
  // device on this extension.
  if (showEnableDialog) {
    AlertDialog(
      onDismissRequest = { if (!sipBusy) showEnableDialog = false },
      title = { Text("Turn on calling here?", fontWeight = FontWeight.Bold) },
      text = {
        Text(
          "This device will be able to make and receive intercom calls.\n\n" +
            "Any other device signed in to this extension — a softphone, another " +
            "handset — will be signed out and needs setting up again.",
          fontSize = 13.sp,
          color = BestNetMuted,
        )
      },
      confirmButton = {
        Button(
          onClick = { onEnableCalling(); showEnableDialog = false },
          enabled = !sipBusy,
          colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
        ) { Text(if (sipBusy) "Setting up…" else "Turn on") }
      },
      dismissButton = {
        TextButton(onClick = { showEnableDialog = false }, enabled = !sipBusy) {
          Text("Cancel", color = BestNetMuted)
        }
      },
      containerColor = BestNetSurface,
    )
  }
}

@Composable
fun IntercomContactRow(
  name: String,
  role: String,
  icon: ImageVector? = null,
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

/**
 * The resident's own extension, plus the connection details a SIP app needs.
 *
 * This is here because the app cannot place calls itself — there is no SIP
 * stack in it. Showing the extension and server is the honest, useful thing:
 * it is what the resident types into Zoiper or Linphone, and what a neighbour
 * dials to reach them.
 */
@Composable
private fun MyExtensionCard(
  extension: String?,
  registration: SipRegistration,
  sipConfigured: Boolean,
  sipBusy: Boolean,
  onEnableCalling: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = BestNetSurface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text("My extension", fontSize = 12.sp, color = BestNetMuted, fontWeight = FontWeight.Medium)
      Text(
        text = extension ?: "Not set up for this home",
        fontSize = if (extension != null) 26.sp else 15.sp,
        fontWeight = FontWeight.Bold,
        color = if (extension != null) BestNetGreen else BestNetMuted,
      )
      if (extension != null) {
        Text(
          when {
            registration == SipRegistration.REGISTERED -> "Calling is on — you can be reached here"
            registration == SipRegistration.PROGRESS -> "Connecting…"
            registration == SipRegistration.FAILED -> "Couldn't connect to the phone system"
            sipConfigured -> "Calling is set up but not connected"
            else -> "Calling is off on this device"
          },
          fontSize = 12.sp,
          color = if (registration == SipRegistration.REGISTERED) BestNetGreen else BestNetMuted,
          modifier = Modifier.padding(top = 2.dp),
        )
        Text(
          "pbx.bestnet.in · port 5061 · TLS",
          fontSize = 11.sp,
          color = BestNetMuted,
        )
        if (!sipConfigured) {
          Spacer(modifier = Modifier.height(10.dp))
          Button(
            onClick = onEnableCalling,
            enabled = !sipBusy,
            colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
            shape = RoundedCornerShape(10.dp),
          ) { Text(if (sipBusy) "Setting up…" else "Turn on calling here", fontSize = 13.sp) }
        }
      }
    }
  }
}
