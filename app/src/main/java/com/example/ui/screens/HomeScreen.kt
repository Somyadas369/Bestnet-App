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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Resident
import com.example.ui.components.BestNetLogo
import com.example.ui.theme.BestNetAmber
import com.example.ui.theme.BestNetAmberLight
import com.example.ui.theme.BestNetBackground
import com.example.ui.theme.BestNetBlue
import com.example.ui.theme.BestNetBlueLight
import com.example.ui.theme.BestNetBorder
import com.example.ui.theme.BestNetCyan
import com.example.ui.theme.BestNetCyanLight
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetGreenDark
import com.example.ui.theme.BestNetGreenLight
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted
import com.example.ui.theme.BestNetPurple
import com.example.ui.theme.BestNetPurpleLight
import com.example.ui.theme.BestNetRed
import com.example.ui.theme.BestNetRedLight
import com.example.ui.theme.BestNetSurface

@Composable
fun HomeScreen(
  resident: Resident?,
  onNavigateToIntercom: () -> Unit,
  onNavigateToComplaint: () -> Unit,
  onNavigateToVisitors: () -> Unit,
  onNavigateToNotices: () -> Unit,
  onSwitchToServicesTab: () -> Unit,
  onSwitchToCommunityTab: () -> Unit,
  onNavigateToProfile: () -> Unit,
  onOpenSpeedTest: () -> Unit,
  onOpenSwitchHome: () -> Unit
) {
  var showPromoCard by remember { mutableStateOf(true) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(BestNetBackground)
  ) {
    // Top Bar with Logo and Avatar
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(BestNetSurface)
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        BestNetLogo(fontSize = 22.sp, showTagline = false)

        // Resident Avatar
        Box(
          modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .border(1.5.dp, BestNetGreenLight, CircleShape)
            .clickable { onNavigateToProfile() }
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_user_avatar),
            contentDescription = "Profile",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        }
      }
    }

    // Greeting Block
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(BestNetSurface)
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Text(
          text = "Hi, ${resident?.name ?: "Rahul Sharma"}",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          color = BestNetInk
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clickable { onOpenSwitchHome() }
            .padding(top = 2.dp, bottom = 12.dp)
        ) {
          Text(
            text = "${resident?.unit ?: "A-1201"}, ${resident?.communityName ?: "Sunrise Apartments"}",
            fontSize = 13.sp,
            color = BestNetMuted
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "▾",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = BestNetGreen
          )
        }
      }
    }

    // Main Content
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {

        // Promo Card
        if (showPromoCard) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .background(
                  Brush.linearGradient(
                    colors = listOf(Color(0xFF17356B), Color(0xFF1F4F9E), Color(0xFF0284C7))
                  )
                )
                .padding(16.dp)
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Top
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Fast. Reliable. Always for You",
                      color = Color.White.copy(alpha = 0.9f),
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = "High-Speed Internet\nfor a Smarter Home",
                      color = Color.White,
                      fontSize = 17.sp,
                      fontWeight = FontWeight.Bold,
                      lineHeight = 22.sp
                    )
                  }

                  IconButton(
                    onClick = { showPromoCard = false },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.Close,
                      contentDescription = "Dismiss",
                      tint = Color.White.copy(alpha = 0.8f),
                      modifier = Modifier.size(18.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.Bottom
                ) {
                  // Pagination dots
                  Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Box(
                      modifier = Modifier
                        .size(width = 16.dp, height = 5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White)
                    )
                    Box(
                      modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.4f))
                    )
                    Box(
                      modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.4f))
                    )
                  }

                  // Router Icon graphic
                  Box(
                    modifier = Modifier
                      .size(38.dp)
                      .clip(CircleShape)
                      .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      imageVector = Icons.Default.Router,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(22.dp)
                    )
                  }
                }
              }
            }
          }
        }

        // Quick Action Grid (3 x 2)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            QuickActionTile(
              title = "Intercom",
              icon = Icons.Default.Phone,
              iconBgColor = BestNetGreenLight,
              iconTintColor = BestNetGreen,
              onClick = onNavigateToIntercom,
              modifier = Modifier.weight(1f)
            )
            QuickActionTile(
              title = "Raise\nComplaint",
              icon = Icons.Default.Campaign,
              iconBgColor = BestNetRedLight,
              iconTintColor = BestNetRed,
              onClick = onNavigateToComplaint,
              modifier = Modifier.weight(1f)
            )
            QuickActionTile(
              title = "My Services",
              icon = Icons.Default.GridView,
              iconBgColor = BestNetBlueLight,
              iconTintColor = BestNetBlue,
              onClick = onSwitchToServicesTab,
              modifier = Modifier.weight(1f)
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            QuickActionTile(
              title = "Visitors",
              icon = Icons.Default.Person,
              iconBgColor = BestNetCyanLight,
              iconTintColor = BestNetCyan,
              onClick = onNavigateToVisitors,
              modifier = Modifier.weight(1f)
            )
            QuickActionTile(
              title = "Notices",
              icon = Icons.Default.Notifications,
              iconBgColor = BestNetAmberLight,
              iconTintColor = BestNetAmber,
              onClick = onNavigateToNotices,
              modifier = Modifier.weight(1f)
            )
            QuickActionTile(
              title = "Community",
              icon = Icons.Default.People,
              iconBgColor = BestNetPurpleLight,
              iconTintColor = BestNetPurple,
              onClick = onSwitchToCommunityTab,
              modifier = Modifier.weight(1f)
            )
          }
        }

        // Internet Status Card
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = BestNetSurface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, BestNetBorder, RoundedCornerShape(18.dp))
              .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(38.dp)
                  .clip(CircleShape)
                  .background(BestNetGreenLight),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = BestNetGreen,
                  modifier = Modifier.size(24.dp)
                )
              }

              Column {
                Text(
                  text = "Internet Status",
                  fontSize = 11.5.sp,
                  color = BestNetMuted,
                  fontWeight = FontWeight.Medium
                )
                Text(
                  text = "Connected",
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = BestNetGreen
                )
                Text(
                  text = "Uptime: 5d 12h 30m",
                  fontSize = 11.sp,
                  color = BestNetMuted
                )
              }
            }

            Button(
              onClick = onOpenSpeedTest,
              shape = RoundedCornerShape(10.dp),
              colors = ButtonDefaults.buttonColors(containerColor = BestNetGreen),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
              Text(
                text = "Speed Test",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))
      }
    }
  }
}

@Composable
fun QuickActionTile(
  title: String,
  icon: ImageVector,
  iconBgColor: Color,
  iconTintColor: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable { onClick() },
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = BestNetSurface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .border(1.dp, BestNetBorder, RoundedCornerShape(16.dp))
        .padding(vertical = 16.dp, horizontal = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(iconBgColor),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconTintColor,
          modifier = Modifier.size(22.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = BestNetInk,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        lineHeight = 15.sp
      )
    }
  }
}
