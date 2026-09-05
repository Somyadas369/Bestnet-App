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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BestNetLogo
import com.example.ui.components.IndiaFlagIcon
import com.example.ui.theme.BestNetBorder
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted
import com.example.ui.theme.BestNetSurface

@Composable
fun LoginScreen(
  onLoginSuccess: () -> Unit,
  onShowComingSoon: (String) -> Unit
) {
  var phoneNumber by remember { mutableStateOf("9876543210") }
  var otpStep by remember { mutableStateOf(false) }
  var otpCode by remember { mutableStateOf("") }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            Color(0xFFE7EDFD),
            Color(0xFFF2F5FD),
            Color(0xFFFFFFFF)
          )
        )
      )
      .statusBarsPadding()
      .padding(20.dp),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        // Logo
        BestNetLogo(
          fontSize = 28.sp,
          showTagline = false,
          modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
          text = if (otpStep) "Verify OTP" else "Welcome Back!",
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = BestNetInk
        )
        Text(
          text = if (otpStep) "Enter the 6-digit code sent to +91 $phoneNumber" else "Login to your BestNet account",
          fontSize = 13.5.sp,
          color = BestNetMuted,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        if (!otpStep) {
          // Phone Input Pill
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, BestNetBorder, RoundedCornerShape(12.dp))
              .background(BestNetSurface, RoundedCornerShape(12.dp))
              .padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            IndiaFlagIcon(width = 24.dp, height = 16.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "+91 ▾",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = BestNetInk
            )
            Box(
              modifier = Modifier
                .padding(horizontal = 10.dp)
                .width(1.dp)
                .height(24.dp)
                .background(BestNetBorder)
            )
            OutlinedTextField(
              value = phoneNumber,
              onValueChange = { if (it.length <= 10) phoneNumber = it },
              placeholder = { Text("Mobile Number", fontSize = 14.sp, color = BestNetMuted) },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent
              ),
              modifier = Modifier.weight(1f)
            )
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Send OTP button
          Button(
            onClick = {
              if (phoneNumber.length >= 10) {
                otpStep = true
              }
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen)
          ) {
            Text("Send OTP", fontSize = 15.sp, fontWeight = FontWeight.Bold)
          }
        } else {
          // OTP code input
          OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6) otpCode = it },
            placeholder = { Text("Enter 6-digit OTP (e.g. 123456)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = BestNetGreen,
              focusedLabelColor = BestNetGreen
            )
          )

          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = onLoginSuccess,
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp),
            shape = RoundedCornerShape(25.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen)
          ) {
            Text("Verify & sign in", fontSize = 15.sp, fontWeight = FontWeight.Bold)
          }

          TextButton(onClick = { otpStep = false }) {
            Text("Use a different number", color = BestNetGreen, fontSize = 13.sp)
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Divider
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .height(1.dp)
              .background(BestNetBorder)
          )
          Text(
            text = "Or continue with",
            fontSize = 12.sp,
            color = BestNetMuted,
            modifier = Modifier.padding(horizontal = 10.dp)
          )
          Box(
            modifier = Modifier
              .weight(1f)
              .height(1.dp)
              .background(BestNetBorder)
          )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Social Buttons (Google & Apple)
        Row(
          horizontalArrangement = Arrangement.spacedBy(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Google
          Box(
            modifier = Modifier
              .size(50.dp)
              .clip(CircleShape)
              .border(1.dp, BestNetBorder, CircleShape)
              .clickable { onShowComingSoon("Google Sign-In") }
              .background(Color.White),
            contentAlignment = Alignment.Center
          ) {
            Text("G", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
          }

          // Apple
          Box(
            modifier = Modifier
              .size(50.dp)
              .clip(CircleShape)
              .border(1.dp, BestNetBorder, CircleShape)
              .clickable { onShowComingSoon("Apple Sign-In") }
              .background(Color.White),
            contentAlignment = Alignment.Center
          ) {
            Text("", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = BestNetInk)
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Register link
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("New to BestNet? ", fontSize = 13.sp, color = BestNetMuted)
          Text(
            text = "Register",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BestNetGreen,
            modifier = Modifier.clickable { onShowComingSoon("Self-Registration") }
          )
        }
      }
    }
  }
}
