package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.BestNetLogo
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetInk

@Composable
fun SplashScreen(
  onGetStartedClick: () -> Unit
) {
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
          .padding(horizontal = 24.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Logo & Tagline
        BestNetLogo(
          fontSize = 32.sp,
          showTagline = true,
          taglineText = "Connected Communities, Brighter Lives",
          modifier = Modifier.padding(bottom = 20.dp)
        )

        // Cityscape Illustration
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(18.dp))
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_splash_cityscape),
            contentDescription = "Residential Community",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Headings
        Text(
          text = "A Smarter Community",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = BestNetInk,
          textAlign = TextAlign.Center
        )
        Text(
          text = "A Better Tomorrow",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = BestNetGreen,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Get Started Button
        Button(
          onClick = onGetStartedClick,
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
          shape = RoundedCornerShape(26.dp),
          colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen)
        ) {
          Text(
            text = "Get Started",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }
  }
}
