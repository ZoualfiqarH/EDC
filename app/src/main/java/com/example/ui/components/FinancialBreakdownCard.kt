package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import com.example.data.model.FinancialBreakdown
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.CurrencyFormatter
import com.example.ui.theme.DentalMintSecondary
import com.example.ui.theme.DentalTealPrimary
import com.example.ui.theme.DoctorHayaColor
import com.example.ui.theme.DoctorJafarColor
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalCurrencyMode
import com.example.ui.theme.LocalizedStrings

@Composable
fun FinancialBreakdownCard(
  breakdown: FinancialBreakdown,
  modifier: Modifier = Modifier
) {
  val strings = LocalizedStrings.current()
  val appLanguage = LocalAppLanguage.current
  val currencyMode = LocalCurrencyMode.current
  val isArabic = appLanguage == AppLanguage.ARABIC

  Card(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header: Multi-currency Totals
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = if (isArabic) "توزيع الإيراد المعتمد (30% / 70%)" else "Revenue Distribution (30% / 70%)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "${strings.assignedDoctor}: ${breakdown.assignedDoctor.getLocalizedName(isArabic)} • 1$ = ${String.format("%,.0f", breakdown.exchangeRateSyp)} ${if (isArabic) "ل.س" else "SYP"}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = CurrencyFormatter.format(breakdown.totalAmount, breakdown.exchangeRateSyp, currencyMode, isArabic),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(10.dp))

      // Installment Breakdown: Amount Paid Today vs Remaining Balance
      if (breakdown.remainingBalance > 0.01) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFFFF3E0))
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "${strings.paidTodayOrDownPayment}:",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFE65100)
            )
            Text(
              text = CurrencyFormatter.format(breakdown.amountPaidToday, breakdown.exchangeRateSyp, currencyMode, isArabic),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFE65100)
            )
          }

          Column(horizontalAlignment = Alignment.End) {
            Text(
              text = "${strings.remainingBalanceLabel}:",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFFC62828)
            )
            Text(
              text = CurrencyFormatter.format(breakdown.remainingBalance, breakdown.exchangeRateSyp, currencyMode, isArabic),
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFFC62828)
            )
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
      }

      // Clinic Share (30%)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .width(4.dp)
              .height(16.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(DentalTealPrimary)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = strings.clinicShare30,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = CurrencyFormatter.format(breakdown.clinicOverhead, breakdown.exchangeRateSyp, currencyMode, isArabic),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = DentalTealPrimary
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Net Doctor Pool (70%)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .width(4.dp)
              .height(16.dp)
              .clip(RoundedCornerShape(2.dp))
              .background(DentalMintSecondary)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = strings.doctorPool70,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
          )
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = CurrencyFormatter.format(breakdown.netDoctorPool, breakdown.exchangeRateSyp, currencyMode, isArabic),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = DentalMintSecondary
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Doctor-specific split cards (42% Primary Treating / 28% Secondary)
      val isHayaPrimary = breakdown.assignedDoctor == Doctor.HAYA_HASAN
      val hayaPercent = if (isHayaPrimary) "42% (60% Pool)" else "28% (40% Pool)"
      val jafarPercent = if (isHayaPrimary) "28% (40% Pool)" else "42% (60% Pool)"

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Dr. Haya Box
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(DoctorHayaColor.copy(alpha = 0.12f))
            .padding(10.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = strings.drHayaName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DoctorHayaColor
              )
              Text(
                text = if (isHayaPrimary) "42%" else "28%",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DoctorHayaColor
              )
            }
            Text(
              text = hayaPercent,
              fontSize = 9.sp,
              color = DoctorHayaColor.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = CurrencyFormatter.format(breakdown.hayaShare, breakdown.exchangeRateSyp, currencyMode, isArabic),
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = DoctorHayaColor
            )
            Text(
              text = if (isHayaPrimary) (if (isArabic) "طبيب معالج (أساسي)" else "Primary Treating Doctor") else (if (isArabic) "طبيب شريك" else "Secondary Doctor"),
              fontSize = 9.sp,
              color = DoctorHayaColor.copy(alpha = 0.7f)
            )
          }
        }

        // Dr. Jafar Box
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(DoctorJafarColor.copy(alpha = 0.12f))
            .padding(10.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = strings.drJafarName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DoctorJafarColor
              )
              Text(
                text = if (!isHayaPrimary) "42%" else "28%",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DoctorJafarColor
              )
            }
            Text(
              text = jafarPercent,
              fontSize = 9.sp,
              color = DoctorJafarColor.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = CurrencyFormatter.format(breakdown.jafarShare, breakdown.exchangeRateSyp, currencyMode, isArabic),
              fontSize = 13.sp,
              fontWeight = FontWeight.ExtraBold,
              color = DoctorJafarColor
            )
            Text(
              text = if (!isHayaPrimary) (if (isArabic) "طبيب معالج (أساسي)" else "Primary Treating Doctor") else (if (isArabic) "طبيب شريك" else "Secondary Doctor"),
              fontSize = 9.sp,
              color = DoctorJafarColor.copy(alpha = 0.7f)
            )
          }
        }
      }
    }
  }
}
