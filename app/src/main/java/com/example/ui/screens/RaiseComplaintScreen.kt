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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.example.ui.components.DetailTopBar
import com.example.ui.components.PreviewBanner
import com.example.ui.theme.BestNetBackground
import com.example.ui.theme.BestNetBorder
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetGreenLight
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted
import com.example.ui.theme.BestNetSurface
import com.example.ui.theme.BestNetSurfaceVariant

data class ComplaintCategory(val name: String, val icon: ImageVector)

@Composable
fun RaiseComplaintScreen(
  onBackClick: () -> Unit,
  onSubmitComplaint: (
    category: String,
    description: String,
    onResult: (success: Boolean, reference: String?) -> Unit,
  ) -> Unit,
  submitting: Boolean = false,
  errorMessage: String? = null,
) {
  var selectedCategory by remember { mutableStateOf("Plumber") }
  var description by remember { mutableStateOf("") }
  var hasAttachedPhoto by remember { mutableStateOf(false) }
  var currentStep by remember { mutableIntStateOf(1) }
  var submittedTicketNumber by remember { mutableStateOf<String?>(null) }

  val categories = listOf(
    ComplaintCategory("Plumber", Icons.Default.WaterDamage),
    ComplaintCategory("Electrician", Icons.Default.Bolt),
    ComplaintCategory("House Keeping", Icons.Default.CleaningServices),
    ComplaintCategory("Internet Issue", Icons.Default.Wifi),
    ComplaintCategory("General Maintenance", Icons.Default.Build),
    ComplaintCategory("Others", Icons.Default.MoreHoriz)
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(BestNetBackground)
      .statusBarsPadding()
  ) {
    DetailTopBar(title = "Raise Complaint", onBackClick = onBackClick)

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Stepper Bar
      item {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Step 1
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(BestNetGreen),
              contentAlignment = Alignment.Center
            ) {
              Text("1", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Text(
              text = " Details",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = BestNetInk
            )
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .height(2.dp)
              .padding(horizontal = 12.dp)
              .background(if (currentStep >= 2) BestNetGreen else BestNetBorder)
          )

          // Step 2
          Box(
            modifier = Modifier
              .size(26.dp)
              .clip(CircleShape)
              .background(if (currentStep >= 2) BestNetGreen else BestNetSurfaceVariant),
            contentAlignment = Alignment.Center
          ) {
            Text("2", color = if (currentStep >= 2) Color.White else BestNetMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          Box(
            modifier = Modifier
              .weight(1f)
              .height(2.dp)
              .padding(horizontal = 12.dp)
              .background(if (currentStep >= 3) BestNetGreen else BestNetBorder)
          )

          // Step 3
          Box(
            modifier = Modifier
              .size(26.dp)
              .clip(CircleShape)
              .background(if (currentStep >= 3) BestNetGreen else BestNetSurfaceVariant),
            contentAlignment = Alignment.Center
          ) {
            Text("3", color = if (currentStep >= 3) Color.White else BestNetMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      }

      item {
        PreviewBanner(text = "Preview — Ticket dispatch system is recorded in your resident log.")
      }

      // Select Category
      item {
        Text(
          text = "Select Category",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = BestNetInk
        )
      }

      // 3x2 Grid
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
                val isSelected = selectedCategory == cat.name

                Card(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { selectedCategory = cat.name },
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
                      .padding(vertical = 14.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                  ) {
                    Icon(
                      imageVector = cat.icon,
                      contentDescription = null,
                      tint = if (isSelected) BestNetGreen else BestNetInk,
                      modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = cat.name,
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

      // Description
      item {
        Text(
          text = "Description",
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold,
          color = BestNetInk
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          placeholder = { Text("Describe your issue in detail...", fontSize = 13.5.sp, color = BestNetMuted) },
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

      // Add Photos (Optional)
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
                Icon(
                  imageVector = Icons.Default.AddPhotoAlternate,
                  contentDescription = null,
                  tint = BestNetInk,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.size(6.dp))
                Text("+ Add Photo", color = BestNetInk, fontSize = 13.sp, fontWeight = FontWeight.Bold)
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
                    Text("1.8 MB · Attached", fontSize = 11.sp, color = BestNetMuted)
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

      // Next / Submit Button
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Button(
          onClick = {
            if (description.isNotBlank()) {
              // Only advance to the confirmation step once the server has
              // actually accepted the ticket. This previously moved to step 3
              // immediately, so the resident saw a success screen and a ticket
              // number whether or not anything had been filed.
              onSubmitComplaint(selectedCategory, description.trim()) { success, reference ->
                if (success) {
                  submittedTicketNumber = reference
                  currentStep = 3
                }
              }
            }
          },
          enabled = description.isNotBlank() && !submitting,
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
          shape = RoundedCornerShape(25.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen)
        ) {
          Text(
            if (submitting) "Submitting…" else "Submit Complaint",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
          )
        }

        // The server's reason, shown as-is. A complaint that failed to send must
        // say so on this screen, not vanish into a snackbar the user may miss.
        if (errorMessage != null) {
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            errorMessage,
            color = Color(0xFFDC2626),
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth(),
          )
        }
        Spacer(modifier = Modifier.height(24.dp))
      }
    }
  }

  // Confirmation dialog
  submittedTicketNumber?.let { ticket ->
    AlertDialog(
      onDismissRequest = onBackClick,
      confirmButton = {
        Button(
          onClick = onBackClick,
          colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Done", fontWeight = FontWeight.Bold)
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
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Your complaint has been assigned ticket $ticket.",
            fontSize = 14.sp,
            color = BestNetInk
          )
          Text(
            text = "Category: $selectedCategory",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = BestNetMuted
          )
          Text(
            text = "A technician will be assigned shortly by society management.",
            fontSize = 12.sp,
            color = BestNetMuted
          )
        }
      },
      shape = RoundedCornerShape(18.dp),
      containerColor = BestNetSurface
    )
  }
}
