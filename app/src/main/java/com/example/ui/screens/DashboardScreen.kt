package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.Appointment
import com.example.ui.components.BookingTypeBadge
import com.example.ui.components.DoctorBadge
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.CurrencyDisplayMode
import com.example.ui.theme.CurrencyFormatter
import com.example.ui.theme.DentalMintSecondary
import com.example.ui.theme.DentalTealPrimary
import com.example.ui.theme.DoctorHayaColor
import com.example.ui.theme.DoctorJafarColor
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalCurrencyMode
import com.example.ui.theme.LocalizedStrings
import com.example.ui.theme.MedicalAlert
import com.example.ui.theme.MedicalAlertContainer
import com.example.ui.viewmodel.ClinicViewModel

@Composable
fun DashboardScreen(
  viewModel: ClinicViewModel,
  onOpenNewPatientDialog: () -> Unit,
  onOpenNewAppointmentDialog: () -> Unit,
  onOpenNewInvoiceDialog: () -> Unit,
  onNavigateToInventoryWithLowStockFilter: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val strings = LocalizedStrings.current()
  val appLanguage = LocalAppLanguage.current
  val currencyMode = LocalCurrencyMode.current
  val isArabic = appLanguage == AppLanguage.ARABIC

  val patients by viewModel.allPatients.collectAsStateWithLifecycle()
  val appointments by viewModel.allAppointments.collectAsStateWithLifecycle()
  val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
  val financialSummary by viewModel.financialSummary.collectAsStateWithLifecycle()
  val appointmentStats by viewModel.appointmentStats.collectAsStateWithLifecycle()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // 1. Redesigned Overview Header: Clinic Name, Management Details, and Compact Interactive Toggles Sub-line
    item {
      Spacer(modifier = Modifier.height(10.dp))
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(MaterialTheme.colorScheme.surface)
          .padding(vertical = 8.dp)
      ) {
        // Clinic Header with Logo and Management Details
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // Clinic Logo Badge
          Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 2.dp,
            shadowElevation = 3.dp,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
            modifier = Modifier.size(64.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.clinic_logo),
              contentDescription = strings.appName,
              modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
                .clip(RoundedCornerShape(13.dp))
            )
          }

          Column(modifier = Modifier.weight(1f)) {
            // Clinic Name
            Text(
              text = strings.appName,
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.testTag("clinic_title")
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Management details ("Managed by...")
            Text(
              text = strings.clinicManagement,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.testTag("clinic_management_text")
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dedicated Sub-Line: Compact, interactive toggle buttons for Language & Currency switching
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Language Toggle Segmented Bar
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
              .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Language,
              contentDescription = strings.languageSwitch,
              modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(15.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // English Button
            CompactToggleButton(
              text = "English",
              isSelected = appLanguage == AppLanguage.ENGLISH,
              onClick = { viewModel.setLanguage(AppLanguage.ENGLISH) },
              tag = "toggle_lang_en"
            )

            Spacer(modifier = Modifier.width(3.dp))

            // Arabic Button
            CompactToggleButton(
              text = "العربية",
              isSelected = appLanguage == AppLanguage.ARABIC,
              onClick = { viewModel.setLanguage(AppLanguage.ARABIC) },
              tag = "toggle_lang_ar"
            )
          }

          // Currency Toggle Segmented Bar
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
              .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.CurrencyExchange,
              contentDescription = strings.currencySwitch,
              modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(15.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // USD Button
            CompactToggleButton(
              text = if (isArabic) "دولار ($)" else "USD ($)",
              isSelected = currencyMode == CurrencyDisplayMode.USD,
              onClick = { viewModel.setCurrencyMode(CurrencyDisplayMode.USD) },
              tag = "toggle_currency_usd"
            )

            Spacer(modifier = Modifier.width(3.dp))

            // SYP Button
            CompactToggleButton(
              text = if (isArabic) "ل.س" else "SYP",
              isSelected = currencyMode == CurrencyDisplayMode.SYP,
              onClick = { viewModel.setCurrencyMode(CurrencyDisplayMode.SYP) },
              tag = "toggle_currency_syp"
            )
          }
        }
      }
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    }

    // 2. Low-Stock Alert Warning Banner (if breached threshold)
    if (lowStockItems.isNotEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MedicalAlertContainer),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToInventoryWithLowStockFilter() }
            .testTag("low_stock_banner")
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = null,
              tint = MedicalAlert,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = strings.lowStockBannerTitle,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MedicalAlert
              )
              Text(
                text = if (isArabic)
                  "يوجد ${lowStockItems.size} مواد تجاوزت الحد الأدنى للأمان وتحتاج توريد فوري"
                else
                  "${lowStockItems.size} items have breached minimum threshold and require restocking.",
                fontSize = 11.sp,
                color = MedicalAlert.copy(alpha = 0.9f)
              )
            }
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              tint = MedicalAlert,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    // 3. Four Core Metric Summary Cards (Strict Localization & Strict Currency)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Card 1: Active Patients
          MetricCard(
            title = strings.activePatientsCount,
            value = "${patients.size}",
            subtext = strings.doctorsSubtitle,
            icon = Icons.Default.People,
            iconTint = DentalTealPrimary,
            modifier = Modifier
              .weight(1f)
              .clickable { viewModel.setTab(1) }
              .testTag("metric_patients")
          )

          // Card 2: Today's Appointments (Walk-in vs Phone)
          val apptSubtext = if (isArabic)
            "${appointmentStats.walkInCount} مباشر • ${appointmentStats.phoneCount} هاتف"
          else
            "${appointmentStats.walkInCount} Walk-in • ${appointmentStats.phoneCount} Phone"

          MetricCard(
            title = strings.todaysAppointmentsCount,
            value = "${appointmentStats.totalToday}",
            subtext = apptSubtext,
            icon = Icons.Default.CalendarMonth,
            iconTint = Color(0xFFE65100),
            modifier = Modifier
              .weight(1f)
              .clickable { viewModel.setTab(1) }
              .testTag("metric_appointments")
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Card 3: Inventory Low Stock
          MetricCard(
            title = strings.warehouseAlertsCount,
            value = "${lowStockItems.size}",
            subtext = if (lowStockItems.isEmpty()) strings.safeStockLevels else strings.actionNeeded,
            icon = Icons.Default.Inventory2,
            iconTint = if (lowStockItems.isEmpty()) Color(0xFF2E7D32) else MedicalAlert,
            modifier = Modifier
              .weight(1f)
              .clickable { viewModel.setTab(2) }
              .testTag("metric_warehouse")
          )

          // Card 4: Clinic Share (30%)
          val clinicShareFormatted = CurrencyFormatter.format(
            amountUsd = financialSummary.totalClinicOverhead,
            exchangeRateSyp = financialSummary.globalExchangeRateSyp,
            mode = currencyMode,
            isArabic = isArabic
          )
          val totalRevFormatted = CurrencyFormatter.format(
            amountUsd = financialSummary.totalRevenue,
            exchangeRateSyp = financialSummary.globalExchangeRateSyp,
            mode = currencyMode,
            isArabic = isArabic
          )

          MetricCard(
            title = strings.clinicShare30,
            value = clinicShareFormatted,
            subtext = "${strings.totalRevenue}: $totalRevFormatted",
            icon = Icons.Default.PointOfSale,
            iconTint = DentalMintSecondary,
            modifier = Modifier
              .weight(1f)
              .clickable { viewModel.setTab(3) }
              .testTag("metric_clinic_box")
          )
        }
      }
    }

    // 4. Quick Action Shortcuts
    item {
      Text(
        text = strings.quickActions,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        QuickActionButton(
          label = strings.newPatient,
          icon = Icons.Default.Add,
          color = DentalTealPrimary,
          onClick = onOpenNewPatientDialog,
          modifier = Modifier
            .weight(1f)
            .testTag("quick_new_patient")
        )
        QuickActionButton(
          label = strings.newAppointment,
          icon = Icons.Default.EventNote,
          color = Color(0xFFE65100),
          onClick = onOpenNewAppointmentDialog,
          modifier = Modifier
            .weight(1f)
            .testTag("quick_book_appointment")
        )
        QuickActionButton(
          label = strings.newInvoice,
          icon = Icons.Default.ReceiptLong,
          color = DentalMintSecondary,
          onClick = onOpenNewInvoiceDialog,
          modifier = Modifier
            .weight(1f)
            .testTag("quick_create_invoice")
        )
      }
    }

    // 5. Automated Profit-Sharing Breakdown Section (Strict Localization & Strict Currency)
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { viewModel.setTab(3) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = strings.financialSummaryTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = strings.splitFormulaSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Icon(
              imageVector = Icons.Default.PointOfSale,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
          }

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
          Spacer(modifier = Modifier.height(14.dp))

          // Total Invoiced Revenue
          FinancialLineRow(
            label = strings.totalRevenue,
            amountUsd = financialSummary.totalRevenue,
            exchangeRate = financialSummary.globalExchangeRateSyp,
            currencyMode = currencyMode,
            isArabic = isArabic,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Collected Cash
          FinancialLineRow(
            label = strings.totalCollected,
            amountUsd = financialSummary.totalCollected,
            exchangeRate = financialSummary.globalExchangeRateSyp,
            currencyMode = currencyMode,
            isArabic = isArabic,
            color = DentalMintSecondary
          )

          // Remaining Balance if any
          if (financialSummary.totalOutstandingBalance > 0.01) {
            Spacer(modifier = Modifier.height(8.dp))
            FinancialLineRow(
              label = strings.outstandingBalance,
              amountUsd = financialSummary.totalOutstandingBalance,
              exchangeRate = financialSummary.globalExchangeRateSyp,
              currencyMode = currencyMode,
              isArabic = isArabic,
              color = Color(0xFFC62828)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Clinic Share (30%)
          FinancialLineRow(
            label = strings.clinicShare30,
            amountUsd = financialSummary.totalClinicOverhead,
            exchangeRate = financialSummary.globalExchangeRateSyp,
            currencyMode = currencyMode,
            isArabic = isArabic,
            color = DentalTealPrimary
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Net Doctor Pool (70%)
          FinancialLineRow(
            label = strings.doctorPool70,
            amountUsd = financialSummary.totalDoctorPool,
            exchangeRate = financialSummary.globalExchangeRateSyp,
            currencyMode = currencyMode,
            isArabic = isArabic,
            color = DentalMintSecondary
          )

          Spacer(modifier = Modifier.height(14.dp))

          // Two Doctors' Net Earnings (Dr. Haya & Dr. Jafar)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            // Dr. Haya Hasan
            DoctorEarningsCard(
              doctorName = strings.drHayaName,
              amountUsd = financialSummary.totalHayaShare,
              exchangeRate = financialSummary.globalExchangeRateSyp,
              currencyMode = currencyMode,
              isArabic = isArabic,
              themeColor = DoctorHayaColor,
              subtext = strings.netShareLabel,
              modifier = Modifier.weight(1f)
            )

            // Dr. Jafar Ateyh
            DoctorEarningsCard(
              doctorName = strings.drJafarName,
              amountUsd = financialSummary.totalJafarShare,
              exchangeRate = financialSummary.globalExchangeRateSyp,
              currencyMode = currencyMode,
              isArabic = isArabic,
              themeColor = DoctorJafarColor,
              subtext = strings.netShareLabel,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // 6. Today's Appointments Queue
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${strings.todaysQueueTitle} (${appointments.size})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = strings.viewAll,
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clickable { viewModel.setTab(1) }
        )
      }
    }

    if (appointments.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.EventNote,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = strings.noAppointmentsToday,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(appointments.take(5)) { appointment ->
        AppointmentQueueCard(
          appointment = appointment,
          strings = strings,
          onStatusChange = { newStatus ->
            viewModel.updateAppointmentStatus(appointment.appointmentId, newStatus)
          }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

@Composable
private fun CompactToggleButton(
  text: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  tag: String
) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 5.dp)
      .testTag(tag),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      fontSize = 11.sp,
      fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
      color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
private fun FinancialLineRow(
  label: String,
  amountUsd: Double,
  exchangeRate: Double,
  currencyMode: CurrencyDisplayMode,
  isArabic: Boolean,
  color: Color
) {
  val formatted = CurrencyFormatter.format(amountUsd, exchangeRate, currencyMode, isArabic)
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontWeight = FontWeight.SemiBold
    )
    Text(
      text = formatted,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.Bold,
      color = color
    )
  }
}

@Composable
private fun DoctorEarningsCard(
  doctorName: String,
  amountUsd: Double,
  exchangeRate: Double,
  currencyMode: CurrencyDisplayMode,
  isArabic: Boolean,
  themeColor: Color,
  subtext: String,
  modifier: Modifier = Modifier
) {
  val formatted = CurrencyFormatter.format(amountUsd, exchangeRate, currencyMode, isArabic)
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(themeColor.copy(alpha = 0.1f))
      .border(1.dp, themeColor.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    Column {
      Text(
        text = doctorName,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = themeColor
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = formatted,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 15.sp,
        color = themeColor
      )
      Text(
        text = subtext,
        fontSize = 10.sp,
        color = themeColor.copy(alpha = 0.8f)
      )
    }
  }
}

@Composable
private fun MetricCard(
  title: String,
  value: String,
  subtext: String,
  icon: ImageVector,
  iconTint: Color,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.weight(1f)
        )
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(iconTint.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = value,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = subtext,
        style = MaterialTheme.typography.bodySmall,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun QuickActionButton(
  label: String,
  icon: ImageVector,
  color: Color,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
    modifier = modifier.clickable(onClick = onClick)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(CircleShape)
          .background(color),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = Color.White,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = color
      )
    }
  }
}

@Composable
private fun AppointmentQueueCard(
  appointment: Appointment,
  strings: com.example.ui.theme.AppStrings,
  onStatusChange: (String) -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Time pill
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.primaryContainer)
          .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = appointment.timeSlot,
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = appointment.patientName,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Repositioned appointment type tag neatly horizontally directly underneath patient name
        Row(verticalAlignment = Alignment.CenterVertically) {
          BookingTypeBadge(bookingType = appointment.bookingType)
          Spacer(modifier = Modifier.width(6.dp))
          DoctorBadge(doctorName = appointment.doctor, compact = true)
        }

        if (appointment.procedureNotes.isNotBlank()) {
          Spacer(modifier = Modifier.height(3.dp))
          Text(
            text = appointment.procedureNotes,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
          )
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Status pill / action
      val isCompleted = appointment.status.equals("Completed", ignoreCase = true)
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isCompleted) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.clickable {
          val next = if (isCompleted) "Scheduled" else "Completed"
          onStatusChange(next)
        }
      ) {
        Text(
          text = if (isCompleted) strings.markDone else appointment.status,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = if (isCompleted) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
      }
    }
  }
}
