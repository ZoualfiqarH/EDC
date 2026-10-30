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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.testTag
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Appointment
import com.example.data.model.ClinicalVisit
import com.example.data.model.Doctor
import com.example.data.model.InstallmentPayment
import com.example.data.model.Invoice
import com.example.data.model.Patient
import com.example.ui.components.BookingTypeBadge
import com.example.ui.components.DoctorBadge
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.AppStrings
import com.example.ui.theme.CurrencyFormatter
import com.example.ui.theme.DoctorHayaColor
import com.example.ui.theme.DoctorJafarColor
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalCurrencyMode
import com.example.ui.theme.LocalizedStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientDetailDialog(
  patient: Patient,
  visits: List<ClinicalVisit>,
  invoices: List<Invoice> = emptyList(),
  installments: List<InstallmentPayment> = emptyList(),
  appointments: List<Appointment> = emptyList(),
  exchangeRateSyp: Double = 14500.0,
  onDismiss: () -> Unit,
  onAddVisit: (ClinicalVisit) -> Unit,
  onRecordInstallment: (Long, Double, Double, String, String) -> Unit = { _, _, _, _, _ -> },
  onUpdatePatient: (Patient) -> Unit = {}
) {
  val strings = LocalizedStrings.current()
  val appLanguage = LocalAppLanguage.current
  val currencyMode = LocalCurrencyMode.current
  val isArabic = appLanguage == AppLanguage.ARABIC

  var currentPatient by remember(patient) { mutableStateOf(patient) }
  var showEditPatientDialog by remember { mutableStateOf(false) }

  var selectedTab by remember { mutableIntStateOf(0) }
  var showAddVisitDialog by remember { mutableStateOf(false) }
  var invoiceToCollect by remember { mutableStateOf<Invoice?>(null) }

  val totalBilledUsd = invoices.sumOf { it.totalAmount }
  val totalPaidUsd = invoices.sumOf { it.amountPaidToday }
  val totalOutstandingBalanceUsd = invoices.sumOf { it.remainingBalance }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      shape = RoundedCornerShape(20.dp),
      color = MaterialTheme.colorScheme.background
    ) {
      Column(modifier = Modifier.fillMaxSize()) {
        // Dialog Top Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
              modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = currentPatient.fullName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
              )
              Row(verticalAlignment = Alignment.CenterVertically) {
                DoctorBadge(doctorName = currentPatient.assignedDoctor, compact = true)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "${currentPatient.age} ${if (isArabic) "سنة" else "yrs"} • ${if (currentPatient.gender == "Male") (if (isArabic) "ذكر" else "Male") else (if (isArabic) "أنثى" else "Female")} • ${currentPatient.totalVisits} ${if (isArabic) "زيارة" else "visits"}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { showEditPatientDialog = true },
              modifier = Modifier.testTag("btn_edit_patient")
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = if (isArabic) "تعديل بيانات المريض" else "Edit Patient",
                tint = MaterialTheme.colorScheme.primary
              )
            }
            IconButton(onClick = onDismiss) {
              Icon(imageVector = Icons.Default.Close, contentDescription = strings.cancel)
            }
          }
        }

        // Installment Alert Bar on top of dialog if patient has outstanding balance
        if (totalOutstandingBalanceUsd > 0.01) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Color(0xFFFFF3E0))
              .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "${strings.remainingBalanceLabel}:",
                color = Color(0xFFC62828),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Text(
              text = CurrencyFormatter.format(totalOutstandingBalanceUsd, exchangeRateSyp, currencyMode, isArabic),
              color = Color(0xFFC62828),
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold
            )
          }
        }

        HorizontalDivider()

        // 5 Centralized Patient EHR Tabs
        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          edgePadding = 12.dp
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = { Text(strings.tabPersonalInfo, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = { Text("${strings.tabClinicalHistory} (${visits.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = {
              Text(
                text = if (totalOutstandingBalanceUsd > 0.01) "${strings.tabFinancialLedger} ⚠" else "${strings.tabFinancialLedger} (${invoices.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (totalOutstandingBalanceUsd > 0.01 && selectedTab != 2) Color(0xFFC62828) else Color.Unspecified
              )
            }
          )
          Tab(
            selected = selectedTab == 3,
            onClick = { selectedTab = 3 },
            text = { Text("${strings.tabAppointments} (${appointments.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
          )
          Tab(
            selected = selectedTab == 4,
            onClick = { selectedTab = 4 },
            text = { Text(strings.tabRadiographs, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
          )
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
          when (selectedTab) {
            0 -> PatientEhrTab(
              patient = currentPatient,
              strings = strings,
              isArabic = isArabic,
              onOpenEdit = { showEditPatientDialog = true }
            )
            1 -> PatientVisitsTab(
              visits = visits,
              strings = strings,
              onOpenAddVisit = { showAddVisitDialog = true }
            )
            2 -> PatientInvoicesTab(
              invoices = invoices,
              installments = installments,
              exchangeRateSyp = exchangeRateSyp,
              strings = strings,
              onCollectInstallment = { inv -> invoiceToCollect = inv }
            )
            3 -> PatientAppointmentsTab(
              appointments = appointments,
              strings = strings,
              isArabic = isArabic
            )
            4 -> PatientRadiographsTab(patient = currentPatient, visits = visits, strings = strings)
          }
        }
      }
    }
  }

  if (showEditPatientDialog) {
    EditPatientDialog(
      patient = currentPatient,
      strings = strings,
      isArabic = isArabic,
      onDismiss = { showEditPatientDialog = false },
      onConfirm = { updated ->
        currentPatient = updated
        onUpdatePatient(updated)
        showEditPatientDialog = false
      }
    )
  }

  if (showAddVisitDialog) {
    AddClinicalVisitDialog(
      patient = currentPatient,
      strings = strings,
      onDismiss = { showAddVisitDialog = false },
      onConfirm = { newVisit ->
        onAddVisit(newVisit)
        showAddVisitDialog = false
      }
    )
  }

  if (invoiceToCollect != null) {
    CollectPatientInstallmentDialog(
      invoice = invoiceToCollect!!,
      defaultExchangeRate = exchangeRateSyp,
      strings = strings,
      isArabic = isArabic,
      onDismiss = { invoiceToCollect = null },
      onConfirm = { amountUsd, rate, method, notes ->
        onRecordInstallment(invoiceToCollect!!.invoiceId, amountUsd, rate, method, notes)
        invoiceToCollect = null
      }
    )
  }
}

@Composable
private fun PatientEhrTab(
  patient: Patient,
  strings: AppStrings,
  isArabic: Boolean,
  onOpenEdit: () -> Unit = {}
) {
  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Demographics Card
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (isArabic) "البيانات الشخصية ومعلومات الاتصال" else "Demographics & Contact Information",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleSmall
            )
            TextButton(
              onClick = onOpenEdit,
              modifier = Modifier.testTag("btn_edit_patient_demographics")
            ) {
              Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(text = if (isArabic) "تعديل" else "Edit", fontSize = 12.sp)
            }
          }
          Spacer(modifier = Modifier.height(4.dp))
          DetailRow(label = "${strings.phone}:", value = patient.phoneNumber)
          DetailRow(label = "${strings.address}:", value = patient.address.ifBlank { if (isArabic) "غير محدد" else "Not provided" })
          DetailRow(label = "${strings.occupation}:", value = patient.occupation.ifBlank { if (isArabic) "غير محدد" else "Not specified" })
          DetailRow(label = "${strings.maritalStatus}:", value = patient.maritalStatus)
          DetailRow(label = "${strings.assignedDoctor}:", value = patient.assignedDoctor)
          DetailRow(label = "${strings.totalVisits}:", value = "${patient.totalVisits} ${if (isArabic) "زيارة مسجلة" else "visits logged"}")
        }
      }
    }

    // Medical History Card
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.MedicalInformation,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = strings.chronicDiseases,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleSmall
            )
          }
          Spacer(modifier = Modifier.height(10.dp))

          MedicalAlertBadgeRow(
            label = "${strings.chronicDiseases}:",
            value = patient.chronicDiseases,
            isAlert = !patient.chronicDiseases.equals("None", ignoreCase = true)
          )

          Spacer(modifier = Modifier.height(8.dp))

          MedicalAlertBadgeRow(
            label = "${strings.chronicMedications}:",
            value = patient.chronicMedications,
            isAlert = !patient.chronicMedications.equals("None", ignoreCase = true)
          )

          Spacer(modifier = Modifier.height(8.dp))

          DetailRow(label = "${strings.previousSurgeries}:", value = patient.previousSurgeries)
        }
      }
    }

    // Previous Dental History Card
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = strings.previousDental,
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleSmall
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = patient.previousDentalProcedures.ifBlank { if (isArabic) "لا توجد معالجات سابقة مدونة" else "No prior dental procedures recorded." },
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }
    }
  }
}

@Composable
private fun PatientVisitsTab(
  visits: List<ClinicalVisit>,
  strings: AppStrings,
  onOpenAddVisit: () -> Unit
) {
  val currencyMode = LocalCurrencyMode.current
  val appLanguage = LocalAppLanguage.current
  val isArabic = appLanguage == AppLanguage.ARABIC

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "${strings.tabClinicalHistory} (${visits.size})",
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
      )
      Button(
        onClick = onOpenAddVisit,
        shape = RoundedCornerShape(8.dp)
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(strings.addClinicalVisit, fontSize = 11.sp)
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    if (visits.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = if (isArabic) "لا توجد إجراءات سريرية مدونة للمريض بعد." else "No clinical visits recorded yet.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(visits) { visit ->
          ClinicalVisitCard(visit = visit, isArabic = isArabic)
        }
      }
    }
  }
}

@Composable
private fun ClinicalVisitCard(visit: ClinicalVisit, isArabic: Boolean) {
  val dateFormatted = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(visit.visitDate))
  val currencyMode = LocalCurrencyMode.current

  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = visit.procedureType,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.weight(1f)
        )
        DoctorBadge(doctorName = visit.associatedDoctor, compact = true)
      }

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = dateFormatted,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = CurrencyFormatter.format(visit.cost, mode = currencyMode, isArabic = isArabic),
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold
        )
      }

      if (visit.clinicalNotes.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = visit.clinicalNotes,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      if (visit.radiographsPaths.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(text = visit.radiographsPaths, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
        }
      }
    }
  }
}

@Composable
private fun PatientInvoicesTab(
  invoices: List<Invoice>,
  installments: List<InstallmentPayment>,
  exchangeRateSyp: Double,
  strings: AppStrings,
  onCollectInstallment: (Invoice) -> Unit
) {
  val currencyMode = LocalCurrencyMode.current
  val appLanguage = LocalAppLanguage.current
  val isArabic = appLanguage == AppLanguage.ARABIC

  val totalBilled = invoices.sumOf { it.totalAmount }
  val totalPaid = invoices.sumOf { it.amountPaidToday }
  val totalRemaining = invoices.sumOf { it.remainingBalance }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Statement Summary Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = if (isArabic) "كشف الحساب المالي للمريض (ذمم وأقساط)" else "Patient Financial Statement & Ledger",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.height(10.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(text = strings.totalRevenue, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
              Text(
                text = CurrencyFormatter.format(totalBilled, exchangeRateSyp, currencyMode, isArabic),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
              )
            }
            Column {
              Text(text = strings.totalCollected, fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
              Text(
                text = CurrencyFormatter.format(totalPaid, exchangeRateSyp, currencyMode, isArabic),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF2E7D32)
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(text = strings.outstandingBalance, fontSize = 11.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
              Text(
                text = CurrencyFormatter.format(totalRemaining, exchangeRateSyp, currencyMode, isArabic),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFC62828)
              )
            }
          }
        }
      }
    }

    item {
      Text(
        text = "${strings.navInvoices} (${invoices.size})",
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )
    }

    if (invoices.isEmpty()) {
      item {
        Text(
          text = if (isArabic) "لا توجد فواتير مصدرة لهذا المريض." else "No invoices issued for this patient yet.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      items(invoices) { inv ->
        val dateFormatted = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(inv.timestamp))
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "${if (isArabic) "فاتورة رقم" else "Invoice #"} ${inv.invoiceId} • $dateFormatted", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              Text(
                text = CurrencyFormatter.format(inv.totalAmount, inv.exchangeRateSyp, currencyMode, isArabic),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = inv.serviceName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Revenue split representation (30% Clinic / 42% Primary / 28% Secondary)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(6.dp),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "${if (isArabic) "عيادة 30%:" else "Clinic 30%:"} ${CurrencyFormatter.format(inv.clinicOverhead, inv.exchangeRateSyp, currencyMode, isArabic)}",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )
              Text(
                text = "${if (isArabic) "د. هيا:" else "Dr. Haya:"} ${CurrencyFormatter.format(inv.hayaShare, inv.exchangeRateSyp, currencyMode, isArabic)}",
                fontSize = 10.sp,
                color = DoctorHayaColor,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "${if (isArabic) "د. جعفر:" else "Dr. Jafar:"} ${CurrencyFormatter.format(inv.jafarShare, inv.exchangeRateSyp, currencyMode, isArabic)}",
                fontSize = 10.sp,
                color = DoctorJafarColor,
                fontWeight = FontWeight.Bold
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (inv.isFullyPaid) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = strings.statusFullyPaid, color = Color(0xFF2E7D32), fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            } else {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "${strings.remainingBalanceLabel}: ${CurrencyFormatter.format(inv.remainingBalance, inv.exchangeRateSyp, currencyMode, isArabic)}",
                    color = Color(0xFFC62828),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                Button(
                  onClick = { onCollectInstallment(inv) },
                  shape = RoundedCornerShape(6.dp)
                ) {
                  Text(strings.recordInstallmentPayment, fontSize = 10.sp)
                }
              }
            }
          }
        }
      }
    }

    // Installment Payments History Ledger
    item {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "${strings.installmentLedger} (${installments.size})",
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )
    }

    if (installments.isEmpty()) {
      item {
        Text(
          text = if (isArabic) "لا توجد دفعات مقبوضة مسجلة بعد." else "No installment payments logged.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      items(installments) { payment ->
        val dateFormatted = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.US).format(Date(payment.paymentDate))
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = CurrencyFormatter.format(payment.amountPaidUsd, payment.exchangeRateSyp, currencyMode, isArabic),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = Color(0xFF2E7D32)
              )
              Text(
                text = "${payment.paymentMethod} • $dateFormatted",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Text(
              text = payment.notes,
              fontSize = 10.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

/**
 * Unified Patient Appointment History Tab (Past & Upcoming visits with procedure performed)
 */
@Composable
private fun PatientAppointmentsTab(
  appointments: List<Appointment>,
  strings: AppStrings,
  isArabic: Boolean
) {
  val now = System.currentTimeMillis()
  val upcomingAppointments = appointments.filter { it.appointmentDate >= now || it.status.equals("Scheduled", ignoreCase = true) || it.status.equals("Confirmed", ignoreCase = true) }
  val pastAppointments = appointments.filter { !upcomingAppointments.contains(it) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    if (appointments.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.CalendarMonth,
              contentDescription = null,
              modifier = Modifier.size(48.dp),
              tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (isArabic) "لا توجد مواعيد مسجلة لهذا المريض." else "No appointments scheduled for this patient.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      // Upcoming Appointments Section
      if (upcomingAppointments.isNotEmpty()) {
        item {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${strings.filterUpcoming} (${upcomingAppointments.size})",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        items(upcomingAppointments) { appt ->
          PatientAppointmentCard(appointment = appt, isUpcoming = true, strings = strings, isArabic = isArabic)
        }
      }

      // Past / Completed Appointments Section
      if (pastAppointments.isNotEmpty()) {
        item {
          Spacer(modifier = Modifier.height(8.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${strings.filterPast} (${pastAppointments.size})",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        items(pastAppointments) { appt ->
          PatientAppointmentCard(appointment = appt, isUpcoming = false, strings = strings, isArabic = isArabic)
        }
      }
    }
  }
}

@Composable
private fun PatientAppointmentCard(
  appointment: Appointment,
  isUpcoming: Boolean,
  strings: AppStrings,
  isArabic: Boolean
) {
  val dateFormatted = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US).format(Date(appointment.appointmentDate))
  val isCompleted = appointment.status.equals("Completed", ignoreCase = true)

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isUpcoming) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isUpcoming) 2.dp else 0.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = if (isUpcoming) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "$dateFormatted • ${appointment.timeSlot}",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
          )
        }

        BookingTypeBadge(bookingType = appointment.bookingType)
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Doctor & Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        DoctorBadge(doctorName = appointment.doctor, compact = true)

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = when {
            isCompleted -> Color(0xFFE8F5E9)
            appointment.status.equals("In Progress", ignoreCase = true) -> Color(0xFFFFF3E0)
            else -> Color(0xFFE3F2FD)
          }
        ) {
          Text(
            text = when (appointment.status.lowercase()) {
              "completed" -> strings.statusCompleted
              "scheduled" -> strings.statusScheduled
              "confirmed" -> strings.statusConfirmed
              "in progress" -> strings.statusInProgress
              "cancelled" -> strings.statusCancelled
              else -> appointment.status
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = when {
              isCompleted -> Color(0xFF2E7D32)
              appointment.status.equals("In Progress", ignoreCase = true) -> Color(0xFFE65100)
              else -> Color(0xFF1565C0)
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      // Explicit Dental Procedure / Action Performed
      if (appointment.procedureNotes.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.MedicalServices,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              text = strings.procedurePerformed,
              fontSize = 9.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = appointment.procedureNotes,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PatientRadiographsTab(patient: Patient, visits: List<ClinicalVisit>, strings: AppStrings) {
  val appLanguage = LocalAppLanguage.current
  val isArabic = appLanguage == AppLanguage.ARABIC

  val scans = visits.filter { it.radiographsPaths.isNotBlank() || it.clinicalPhotosBeforeAfter.isNotBlank() }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text(
        text = if (isArabic) "الأشعة والصور الشعاعية السنية (X-Ray & Scans)" else "Digital Radiographs & Intraoral Imaging",
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )
    }

    if (scans.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = Icons.Default.Image,
              contentDescription = null,
              modifier = Modifier.size(40.dp),
              tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (isArabic) "لا توجد صور أشعة مرفوعة حالياً لهذا المريض." else "No imaging scans attached yet.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    } else {
      items(scans) { scan ->
        val dateFormatted = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(scan.visitDate))
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(text = scan.procedureType, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(text = "$dateFormatted • ${scan.associatedDoctor}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            if (scan.radiographsPaths.isNotBlank()) {
              Text(text = "📸 Radiograph: ${scan.radiographsPaths}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
            if (scan.clinicalPhotosBeforeAfter.isNotBlank()) {
              Text(text = "✨ Before/After: ${scan.clinicalPhotosBeforeAfter}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
private fun MedicalAlertBadgeRow(label: String, value: String, isAlert: Boolean) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Surface(
      shape = RoundedCornerShape(6.dp),
      color = if (isAlert) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
    ) {
      Text(
        text = value,
        fontSize = 11.sp,
        fontWeight = if (isAlert) FontWeight.Bold else FontWeight.Normal,
        color = if (isAlert) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
      )
    }
  }
}

@Composable
private fun AddClinicalVisitDialog(
  patient: Patient,
  strings: AppStrings,
  onDismiss: () -> Unit,
  onConfirm: (ClinicalVisit) -> Unit
) {
  var procedureType by remember { mutableStateOf("") }
  var costText by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.addClinicalVisit) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text("Patient: ${patient.fullName} (${patient.assignedDoctor})", fontSize = 12.sp)

        OutlinedTextField(
          value = procedureType,
          onValueChange = { procedureType = it },
          label = { Text("Procedure Type") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = costText,
          onValueChange = { costText = it },
          label = { Text("Cost ($ USD)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Clinical Notes / Procedure Log") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val cost = costText.toDoubleOrNull() ?: 0.0
          val visit = ClinicalVisit(
            patientId = patient.patientId,
            procedureType = procedureType.ifBlank { "Clinical Dental Procedure" },
            visitDate = System.currentTimeMillis(),
            cost = cost,
            amountPaid = cost,
            remainingBalance = 0.0,
            clinicShare = cost * 0.30, // 30% Clinic Share
            associatedDoctor = patient.assignedDoctor,
            clinicalNotes = notes
          )
          onConfirm(visit)
        }
      ) {
        Text(strings.save)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    }
  )
}

@Composable
private fun CollectPatientInstallmentDialog(
  invoice: Invoice,
  defaultExchangeRate: Double,
  strings: AppStrings,
  isArabic: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (Double, Double, String, String) -> Unit
) {
  var paymentAmountText by remember { mutableStateOf(invoice.remainingBalance.toString()) }
  var exchangeRateText by remember { mutableStateOf(defaultExchangeRate.toInt().toString()) }
  var paymentMethod by remember { mutableStateOf("Cash") }
  var notes by remember { mutableStateOf("Installment Payment on Invoice #${invoice.invoiceId}") }

  val paymentAmountUsd = paymentAmountText.toDoubleOrNull() ?: 0.0
  val exchangeRate = exchangeRateText.toDoubleOrNull() ?: defaultExchangeRate
  val paymentAmountSyp = paymentAmountUsd * exchangeRate

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.recordInstallmentPayment) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Patient: ${invoice.patientName} • Remaining: ${String.format(Locale.US, "$%.2f", invoice.remainingBalance)}",
          fontWeight = FontWeight.Bold,
          fontSize = 12.sp
        )

        OutlinedTextField(
          value = paymentAmountText,
          onValueChange = { paymentAmountText = it },
          label = { Text("Payment Amount ($ USD)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = exchangeRateText,
          onValueChange = { exchangeRateText = it },
          label = { Text("Exchange Rate (SYP)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Text(
          text = "Payment in SYP: ${String.format(Locale.US, "%,.0f SYP", paymentAmountSyp)}",
          fontWeight = FontWeight.Bold,
          color = Color(0xFF00796B),
          fontSize = 12.sp
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Receipt Notes") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (paymentAmountUsd > 0.0) {
            onConfirm(paymentAmountUsd, exchangeRate, paymentMethod, notes)
          }
        }
      ) {
        Text(strings.confirm)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancel) }
    }
  )
}

/**
 * Requirement 2: Dedicated Edit Patient feature allowing updates strictly to
 * core personal identification details only, ensuring clinical records and billing history
 * remain structurally intact and safely linked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPatientDialog(
  patient: Patient,
  strings: AppStrings,
  isArabic: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (Patient) -> Unit
) {
  var fullName by remember { mutableStateOf(patient.fullName) }
  var ageText by remember { mutableStateOf(patient.age.toString()) }
  var gender by remember { mutableStateOf(patient.gender) }
  var phone by remember { mutableStateOf(patient.phoneNumber) }
  var address by remember { mutableStateOf(patient.address) }
  var occupation by remember { mutableStateOf(patient.occupation) }
  var maritalStatus by remember { mutableStateOf(patient.maritalStatus) }
  var assignedDoctor by remember { mutableStateOf(patient.assignedDoctor) }
  var chronicDiseases by remember { mutableStateOf(if (patient.chronicDiseases == "None") "" else patient.chronicDiseases) }
  var chronicMedications by remember { mutableStateOf(if (patient.chronicMedications == "None") "" else patient.chronicMedications) }
  var previousSurgeries by remember { mutableStateOf(patient.previousSurgeries) }
  var previousDental by remember { mutableStateOf(patient.previousDentalProcedures) }

  var doctorDropdownExpanded by remember { mutableStateOf(false) }
  var genderDropdownExpanded by remember { mutableStateOf(false) }

  val genderOptions = listOf(
    Pair(strings.genderMale, "Male"),
    Pair(strings.genderFemale, "Female")
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isArabic) "تعديل الملف الشخصي للمريض" else "Edit Patient Profile",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        item {
          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("${strings.fullName} *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = ageText,
              onValueChange = { ageText = it },
              label = { Text("${strings.age} *") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )

            // Gender Dropdown
            ExposedDropdownMenuBox(
              expanded = genderDropdownExpanded,
              onExpandedChange = { genderDropdownExpanded = it },
              modifier = Modifier.weight(1f)
            ) {
              OutlinedTextField(
                value = if (gender == "Male") strings.genderMale else if (gender == "Female") strings.genderFemale else gender,
                onValueChange = {},
                readOnly = true,
                label = { Text(strings.gender) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderDropdownExpanded) },
                modifier = Modifier
                  .fillMaxWidth()
                  .menuAnchor(MenuAnchorType.PrimaryNotEditable)
              )
              ExposedDropdownMenu(
                expanded = genderDropdownExpanded,
                onDismissRequest = { genderDropdownExpanded = false }
              ) {
                genderOptions.forEach { (label, value) ->
                  DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                      gender = value
                      genderDropdownExpanded = false
                    }
                  )
                }
              }
            }
          }
        }

        item {
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("${strings.phone} *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        item {
          // Supervising Doctor Dropdown
          ExposedDropdownMenuBox(
            expanded = doctorDropdownExpanded,
            onExpandedChange = { doctorDropdownExpanded = it }
          ) {
            val currentDoc = Doctor.fromName(assignedDoctor)
            OutlinedTextField(
              value = currentDoc.getLocalizedName(isArabic),
              onValueChange = {},
              readOnly = true,
              label = { Text(strings.assignedDoctor) },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = doctorDropdownExpanded) },
              modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
              expanded = doctorDropdownExpanded,
              onDismissRequest = { doctorDropdownExpanded = false }
            ) {
              Doctor.entries.forEach { doc ->
                DropdownMenuItem(
                  text = { Text(doc.getLocalizedName(isArabic)) },
                  onClick = {
                    assignedDoctor = doc.displayName
                    doctorDropdownExpanded = false
                  }
                )
              }
            }
          }
        }

        item {
          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text(strings.address) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = occupation,
              onValueChange = { occupation = it },
              label = { Text(strings.occupation) },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
            OutlinedTextField(
              value = maritalStatus,
              onValueChange = { maritalStatus = it },
              label = { Text(strings.maritalStatus) },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
          }
        }

        item {
          OutlinedTextField(
            value = chronicDiseases,
            onValueChange = { chronicDiseases = it },
            label = { Text(strings.chronicDiseases) },
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = chronicMedications,
            onValueChange = { chronicMedications = it },
            label = { Text(strings.chronicMedications) },
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = previousSurgeries,
            onValueChange = { previousSurgeries = it },
            label = { Text(strings.previousSurgeries) },
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = previousDental,
            onValueChange = { previousDental = it },
            label = { Text(strings.previousDentalProcedures) },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (fullName.isNotBlank()) {
            val updated = patient.copy(
              fullName = fullName.trim(),
              age = ageText.toIntOrNull() ?: patient.age,
              gender = if (gender.isNotBlank()) gender else patient.gender,
              phoneNumber = phone.trim(),
              address = address.trim(),
              occupation = occupation.trim(),
              maritalStatus = maritalStatus.trim(),
              assignedDoctor = assignedDoctor,
              chronicDiseases = chronicDiseases.trim().ifEmpty { "None" },
              chronicMedications = chronicMedications.trim().ifEmpty { "None" },
              previousSurgeries = previousSurgeries.trim(),
              previousDentalProcedures = previousDental.trim()
            )
            onConfirm(updated)
          }
        },
        enabled = fullName.isNotBlank()
      ) {
        Text(strings.save)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    }
  )
}

