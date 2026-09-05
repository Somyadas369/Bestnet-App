package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BestNetBorder
import com.example.ui.theme.BestNetGreen
import com.example.ui.theme.BestNetInk
import com.example.ui.theme.BestNetMuted

/**
 * BestNet Brand Wordmark: "Best" in Emerald Green, "Net" in Dark Navy,
 * with styled Wifi signal waves arched above the 'N' and 't'.
 */
@Composable
fun BestNetLogo(
  modifier: Modifier = Modifier,
  fontSize: TextUnit = 24.sp,
  showTagline: Boolean = false,
  taglineText: String = "Connected Communities, Brighter Lives"
) {
  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.Start
  ) {
    Box(contentAlignment = Alignment.TopEnd) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = "Best",
          color = BestNetGreen,
          fontSize = fontSize,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = (-0.5).sp
        )
        Text(
          text = "Net",
          color = BestNetInk,
          fontSize = fontSize,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = (-0.5).sp
        )
      }

      // Wifi Waves curved over Net
      WifiArcMark(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .size(width = (fontSize.value * 0.85).dp, height = (fontSize.value * 0.5).dp),
        color = BestNetGreen
      )
    }

    if (showTagline) {
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = taglineText,
        color = BestNetMuted,
        fontSize = (fontSize.value * 0.38).sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.2.sp
      )
    }
  }
}

@Composable
fun WifiArcMark(
  modifier: Modifier = Modifier,
  color: Color = BestNetGreen
) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height

    // Outer arc
    drawArc(
      color = color,
      startAngle = 205f,
      sweepAngle = 130f,
      useCenter = false,
      topLeft = Offset(w * 0.05f, h * 0.05f),
      size = Size(w * 0.9f, h * 1.5f),
      style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
    )

    // Middle arc
    drawArc(
      color = color,
      startAngle = 215f,
      sweepAngle = 110f,
      useCenter = false,
      topLeft = Offset(w * 0.22f, h * 0.42f),
      size = Size(w * 0.56f, h * 1.05f),
      style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round)
    )

    // Inner dot
    drawCircle(
      color = color,
      radius = 1.8.dp.toPx(),
      center = Offset(w * 0.5f, h * 0.92f)
    )
  }
}

/**
 * Service Pillars: INTERNET | INTERCOM | COMMUNITY | SUPPORT
 */
@Composable
fun ServicePillarsHeader(
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    val items = listOf("Internet", "Intercom", "Community", "Support")
    items.forEachIndexed { index, item ->
      if (index > 0) {
        Box(
          modifier = Modifier
            .padding(horizontal = 8.dp)
            .width(1.dp)
            .height(11.dp)
            .background(BestNetBorder)
        )
      }
      Text(
        text = item,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = BestNetMuted
      )
    }
  }
}

/**
 * Indian Flag Icon for Mobile Number input
 */
@Composable
fun IndiaFlagIcon(
  modifier: Modifier = Modifier,
  width: Dp = 24.dp,
  height: Dp = 16.dp
) {
  Box(
    modifier = modifier
      .size(width, height)
      .background(Color.White, RoundedCornerShape(2.dp))
  ) {
    Column(modifier = Modifier.matchParentSize()) {
      Box(
        modifier = Modifier
          .weight(1f)
          .fillParentMaxWidth()
          .background(Color(0xFFFF9933))
      )
      Box(
        modifier = Modifier
          .weight(1f)
          .fillParentMaxWidth()
          .background(Color.White),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.size(height * 0.45f)) {
          drawCircle(
            color = Color(0xFF000080),
            radius = size.minDimension / 2f,
            style = Stroke(width = 0.8.dp.toPx())
          )
        }
      }
      Box(
        modifier = Modifier
          .weight(1f)
          .fillParentMaxWidth()
          .background(Color(0xFF138808))
      )
    }
  }
}

// Helper extension for flag
private fun Modifier.fillParentMaxWidth(): Modifier = this.then(Modifier.width(28.dp))
