package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Resident
import com.example.ui.theme.BestNetAmber
import com.example.ui.theme.BestNetAmberLight
import com.example.ui.theme.BestNetBlue
import com.example.ui.theme.BestNetBlueLight
import com.example.ui.theme.BestNetBorder
import com.example.ui.theme.BestNetDarkNavy
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetGreenDark
import com.example.ui.theme.BestNetGreenLight
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted
import com.example.ui.theme.BestNetRed
import com.example.ui.theme.BestNetRedLight
import com.example.ui.theme.BestNetSurface
import com.example.ui.theme.BestNetSurfaceVariant
import kotlinx.coroutines.delay

@Composable
fun DetailTopBar(
  title: String,
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier,
  actions: @Composable () -> Unit = {}
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(BestNetSurface)
      .padding(horizontal = 8.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    IconButton(onClick = onBackClick) {
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = "Back",
        tint = BestNetInk
      )
    }
    Text(
      text = title,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = BestNetInk,
      modifier = Modifier.weight(1f)
    )
    actions()
  }
}

@Composable
fun PreviewBanner(
  modifier: Modifier = Modifier,
  text: String = "Preview — shown with sample data, not yet connected to a live system."
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(10.dp))
      .background(BestNetAmberLight.copy(alpha = 0.5f))
      .border(1.dp, BestNetAmber.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
      .padding(horizontal = 14.dp, vertical = 10.dp)
  ) {
    Text(
      text = text,
      color = BestNetAmber,
      fontSize = 12.5.sp,
      fontWeight = FontWeight.Medium,
      lineHeight = 16.sp
    )
  }
}

@Composable
fun StatusBadge(
  status: String,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, icon) = when (status.lowercase()) {
    "pending" -> Triple(BestNetAmberLight, BestNetAmber, Icons.Default.Schedule)
    "in progress" -> Triple(BestNetBlueLight, BestNetBlue, Icons.Default.Autorenew)
    "resolved", "approved", "connected", "active" -> Triple(BestNetGreenLight, BestNetGreenDark, Icons.Default.CheckCircle)
    "denied", "cancelled" -> Triple(BestNetRedLight, BestNetRed, Icons.Default.Close)
    else -> Triple(BestNetSurfaceVariant, BestNetMuted, null)
  }

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(999.dp))
      .background(bgColor)
      .padding(horizontal = 10.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = textColor,
        modifier = Modifier.size(13.dp)
      )
    }
    Text(
      text = status,
      color = textColor,
      fontSize = 11.5.sp,
      fontWeight = FontWeight.Bold
    )
  }
}

/**
 * Interactive Speed Test Dialog with animated speedometer
 */
@Composable
fun SpeedTestDialog(
  onDismiss: () -> Unit
) {
  var testRunning by remember { mutableStateOf(true) }
  var downloadSpeed by remember { mutableFloatStateOf(0f) }
  var uploadSpeed by remember { mutableFloatStateOf(0f) }
  var ping by remember { mutableIntStateOf(14) }

  LaunchedEffect(Unit) {
    // Animate speed test
    for (i in 1..40) {
      downloadSpeed = (i * 2.45f) + (-2..4).random().toFloat()
      delay(50)
    }
    downloadSpeed = 98.6f
    ping = 11
    for (i in 1..25) {
      uploadSpeed = (i * 3.4f) + (-1..3).random().toFloat()
      delay(40)
    }
    uploadSpeed = 87.2f
    testRunning = false
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text(if (testRunning) "Testing..." else "Done", fontWeight = FontWeight.Bold)
      }
    },
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(Icons.Default.Speed, contentDescription = null, tint = BestNetGreen)
        Text("BestNet Speed Test", fontWeight = FontWeight.Bold, fontSize = 18.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(140.dp)
            .clip(CircleShape)
            .background(BestNetGreenLight.copy(alpha = 0.5f))
            .border(3.dp, BestNetGreen, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = String.format(Locale.US, "%.1f", downloadSpeed),
              fontSize = 32.sp,
              fontWeight = FontWeight.Black,
              color = BestNetInk
            )
            Text(
              text = "Mbps Download",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = BestNetMuted
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Upload", fontSize = 12.sp, color = BestNetMuted)
            Text(
              text = if (uploadSpeed > 0) "${String.format(Locale.US, "%.1f", uploadSpeed)} Mbps" else "--",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = BestNetInk
            )
          }
          Box(
            modifier = Modifier
              .width(1.dp)
              .height(28.dp)
              .background(BestNetBorder)
          )
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Ping", fontSize = 12.sp, color = BestNetMuted)
            Text(
              text = "$ping ms",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = BestNetGreen
            )
          }
          Box(
            modifier = Modifier
              .width(1.dp)
              .height(28.dp)
              .background(BestNetBorder)
          )
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Jitter", fontSize = 12.sp, color = BestNetMuted)
            Text(
              text = "2 ms",
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = BestNetInk
            )
          }
        }
      }
    },
    shape = RoundedCornerShape(20.dp),
    containerColor = BestNetSurface
  )
}

/**
 * Interactive In-Call Bottom Sheet for Intercom Dialing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InCallBottomSheet(
  contactName: String,
  contactRole: String,
  onEndCall: () -> Unit
) {
  var callDuration by remember { mutableIntStateOf(0) }
  var isMuted by remember { mutableStateOf(false) }
  var isSpeaker by remember { mutableStateOf(false) }
  val pulseTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by pulseTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.08f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  LaunchedEffect(Unit) {
    while (true) {
      delay(1000)
      callDuration++
    }
  }

  val minutes = callDuration / 60
  val seconds = callDuration % 60
  val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

  ModalBottomSheet(
    onDismissRequest = onEndCall,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    containerColor = BestNetDarkNavy,
    contentColor = Color.White
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "BESTNET INTERCOM",
        color = BestNetGreen,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp
      )
      Spacer(modifier = Modifier.height(16.dp))

      Box(
        modifier = Modifier
          .size(96.dp)
          .scale(pulseScale)
          .clip(CircleShape)
          .background(Color.White.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(BestNetGreen),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = contactName.take(1),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = contactName,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
      Text(
        text = contactRole,
        fontSize = 13.sp,
        color = Color.White.copy(alpha = 0.7f)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = timeFormatted,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = BestNetGreen
      )

      Spacer(modifier = Modifier.height(32.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Mute button
        IconButton(
          onClick = { isMuted = !isMuted },
          modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(if (isMuted) Color.White else Color.White.copy(alpha = 0.15f))
        ) {
          Icon(
            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
            contentDescription = "Mute",
            tint = if (isMuted) BestNetDarkNavy else Color.White
          )
        }

        // End Call button
        IconButton(
          onClick = onEndCall,
          modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(BestNetRed)
        ) {
          Icon(
            imageVector = Icons.Default.CallEnd,
            contentDescription = "End Call",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
          )
        }

        // Speaker button
        IconButton(
          onClick = { isSpeaker = !isSpeaker },
          modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(if (isSpeaker) Color.White else Color.White.copy(alpha = 0.15f))
        ) {
          Icon(
            imageVector = Icons.Default.VolumeUp,
            contentDescription = "Speaker",
            tint = if (isSpeaker) BestNetDarkNavy else Color.White
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

/**
 * Pre-Approve Visitor Dialog
 */
@Composable
fun PreApproveVisitorDialog(
  onDismiss: () -> Unit,
  onPreApprove: (name: String, type: String, hoursFromNow: Long) -> Unit,
  submitting: Boolean = false,
  errorMessage: String? = null,
) {
  var name by remember { mutableStateOf("") }
  var type by remember { mutableStateOf("Guest") }
  // The server requires a concrete future timestamp. Coarse choices are offered
  // rather than a date/time picker: a resident saying "in a few hours" is more
  // truthful than a picker implying minute precision they don't have.
  var hours by remember { mutableStateOf(1L) }
  val types = listOf("Guest", "Delivery", "Service", "Cab")
  val whenOptions = listOf("In 1 hour" to 1L, "In 3 hours" to 3L, "Tomorrow" to 24L)

  AlertDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onPreApprove(name.trim(), type, hours)
          }
        },
        enabled = name.trim().length >= 2 && !submitting,
        colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
        shape = RoundedCornerShape(12.dp)
      ) {
        // Not "Generate Pass": no pass or passcode is issued by the server.
        Text(if (submitting) "Sending…" else "Pre-approve", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = BestNetMuted)
      }
    },
    title = {
      Text("Pre-Approve Visitor", fontWeight = FontWeight.Bold, fontSize = 18.sp)
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // There is no gate OTP or passcode in the product. This says what
        // actually happens: the visit appears on the gate's expected list.
        Text(
          "Tell the gate you're expecting someone. They'll appear on the guard's expected-arrivals list.",
          fontSize = 13.sp,
          color = BestNetMuted
        )

        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Visitor / Company Name") },
          placeholder = { Text("e.g. Ramesh Verma or Amazon") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = BestNetGreen,
            focusedLabelColor = BestNetGreen
          )
        )

        Text("Visitor Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BestNetInk)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          types.forEach { t ->
            val isSelected = type == t
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) BestNetGreen else BestNetSurfaceVariant)
                .clickable { type = t }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = t,
                color = if (isSelected) Color.White else BestNetInk,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }

        Text("Expected", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BestNetInk)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          whenOptions.forEach { (label, h) ->
            val isSelected = hours == h
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSelected) BestNetGreen else BestNetSurfaceVariant)
                .clickable { hours = h }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = label,
                color = if (isSelected) Color.White else BestNetInk,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }

        // A pre-approval that didn't reach the server must say so here, rather
        // than closing the dialog as though it had worked.
        if (errorMessage != null) {
          Text(errorMessage, color = Color(0xFFDC2626), fontSize = 12.sp)
        }
      }
    },
    shape = RoundedCornerShape(20.dp),
    containerColor = BestNetSurface
  )
}

/**
 * Switch Home Dialog / Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchHomeBottomSheet(
  residents: List<Resident>,
  currentResidentId: Long,
  onSelectResident: (Long) -> Unit,
  onDismiss: () -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(),
    containerColor = BestNetSurface
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
      Text(
        text = "Choose a home",
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        color = BestNetInk
      )
      Text(
        text = "Select your current residence or managed property unit.",
        fontSize = 13.sp,
        color = BestNetMuted,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
      )

      residents.forEach { resident ->
        val isSelected = resident.id == currentResidentId
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) BestNetGreenLight else BestNetSurfaceVariant)
            .border(
              1.dp,
              if (isSelected) BestNetGreen else Color.Transparent,
              RoundedCornerShape(14.dp)
            )
            .clickable {
              onSelectResident(resident.id)
              onDismiss()
            }
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = resident.communityName,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = BestNetInk
            )
            Text(
              text = "${resident.unit} · Owner / Resident",
              fontSize = 13.sp,
              color = BestNetMuted
            )
          }

          if (isSelected) {
            Box(
              modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(BestNetGreen),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}
