package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Doctor
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.DoctorHayaColor
import com.example.ui.theme.DoctorHayaLight
import com.example.ui.theme.DoctorJafarColor
import com.example.ui.theme.DoctorJafarLight
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.MedicalAlert
import com.example.ui.theme.MedicalAlertContainer

@Composable
fun DoctorBadge(
  doctorName: String,
  modifier: Modifier = Modifier,
  compact: Boolean = false
) {
  val appLanguage = LocalAppLanguage.current
  val isArabic = appLanguage == AppLanguage.ARABIC
  val doctor = Doctor.fromName(doctorName)
  val isHaya = doctor == Doctor.HAYA_HASAN
  val badgeBg = if (isHaya) DoctorHayaLight else DoctorJafarLight
  val badgeFg = if (isHaya) DoctorHayaColor else DoctorJafarColor

  val label = if (compact) {
    if (isArabic) (if (isHaya) "د. هيا" else "د. جعفر") else (if (isHaya) "Dr. Haya" else "Dr. Jafar")
  } else {
    doctor.getLocalizedName(isArabic)
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(badgeBg)
      .border(1.dp, badgeFg.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
      .padding(horizontal = if (compact) 8.dp else 10.dp, vertical = if (compact) 3.dp else 5.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(badgeFg)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        color = badgeFg,
        fontSize = if (compact) 11.sp else 12.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun BookingTypeBadge(
  bookingType: String,
  modifier: Modifier = Modifier
) {
  val appLanguage = LocalAppLanguage.current
  val isArabic = appLanguage == AppLanguage.ARABIC
  val isWalkIn = bookingType.equals("Walk-in", ignoreCase = true)
  val bg = if (isWalkIn) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
  val fg = if (isWalkIn) Color(0xFFE65100) else Color(0xFF2E7D32)

  val label = if (isWalkIn) {
    if (isArabic) "زيارة مباشرة" else "Walk-in"
  } else {
    if (isArabic) "حجز هاتفي" else "Phone"
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(bg)
      .padding(horizontal = 8.dp, vertical = 3.dp)
  ) {
    Text(
      text = label,
      color = fg,
      fontSize = 11.sp,
      fontWeight = FontWeight.SemiBold
    )
  }
}

@Composable
fun StockAlertBadge(
  currentQuantity: Double,
  threshold: Int,
  modifier: Modifier = Modifier
) {
  val appLanguage = LocalAppLanguage.current
  val isArabic = appLanguage == AppLanguage.ARABIC
  val isLow = currentQuantity <= threshold

  if (isLow) {
    Box(
      modifier = modifier
        .clip(RoundedCornerShape(6.dp))
        .background(MedicalAlertContainer)
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Warning,
          contentDescription = null,
          tint = MedicalAlert,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = if (isArabic) "منخفض (${currentQuantity.toInt()} / $threshold)" else "Low Stock (${currentQuantity.toInt()} / $threshold)",
          color = MedicalAlert,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  } else {
    Box(
      modifier = modifier
        .clip(RoundedCornerShape(6.dp))
        .background(Color(0xFFE8F5E9))
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Text(
        text = if (isArabic) "متوفر (${currentQuantity.toInt()})" else "In Stock (${currentQuantity.toInt()})",
        color = Color(0xFF2E7D32),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}
