package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Appointment
import com.example.data.model.Doctor
import com.example.data.model.Patient
import com.example.ui.components.BookingTypeBadge
import com.example.ui.components.DoctorBadge
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.AppStrings
import com.example.ui.theme.DoctorHayaColor
import com.example.ui.theme.DoctorJafarColor
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalizedStrings
import com.example.ui.viewmodel.ClinicViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientsAndAppointmentsScreen(
  viewModel: ClinicViewModel,
  initialSubTab: Int = 0,
  modifier: Modifier = Modifier
) {
  val strings = LocalizedStrings.current()
  val appLanguage = LocalAppLanguage.current
  val isArabic = appLanguage == AppLanguage.ARABIC

  var selectedSubTab by remember { mutableIntStateOf(initialSubTab) }
  var showNewPatientDialog by remember { mutableStateOf(false) }
  var showNewAppointmentDialog by remember { mutableStateOf(false) }

  val patients by viewModel.allPatients.collectAsStateWithLifecycle()
  val appointments by viewModel.allAppointments.collectAsStateWithLifecycle()
  val selectedPatient by viewModel.selectedPatient.collectAsStateWithLifecycle()
  val selectedPatientVisits by viewModel.selectedPatientVisits.collectAsStateWithLifecycle()
  val selectedPatientInvoices by viewModel.selectedPatientInvoices.collectAsStateWithLifecycle()
  val selectedPatientInstallments by viewModel.selectedPatientInstallments.collectAsStateWithLifecycle()
  val selectedPatientAppointments by viewModel.selectedPatientAppointments.collectAsStateWithLifecycle()
  val globalExchangeRate by viewModel.exchangeRateSyp.collectAsStateWithLifecycle()

  var searchQuery by remember { mutableStateOf("") }
  var doctorFilter by remember { mutableStateOf<String?>(null) } // Used for patient directory

  // Requirement 3: Advanced & Strict Appointment Filtering System
  // Applied filters (committed only after clicking Apply / Confirm)
  var appliedDoctorFilter by remember { mutableStateOf<String?>(null) }
  var appliedIsTodayOnly by remember { mutableStateOf(false) }
  var appliedFromDateMillis by remember { mutableStateOf<Long?>(null) }
  var appliedToDateMillis by remember { mutableStateOf<Long?>(null) }

  var showAppointmentFilterDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          if (selectedSubTab == 0) {
            showNewPatientDialog = true
          } else {
            showNewAppointmentDialog = true
          }
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("fab_add_item")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Sub-Tabs
      TabRow(
        selectedTabIndex = selectedSubTab,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        Tab(
          selected = selectedSubTab == 0,
          onClick = { selectedSubTab = 0 },
          text = {
            Text(
              text = "${strings.navPatients} (${patients.size})",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        )
        Tab(
          selected = selectedSubTab == 1,
          onClick = { selectedSubTab = 1 },
          text = {
            Text(
              text = "${strings.appointmentScheduler} (${appointments.size})",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp
            )
          }
        )
      }

      // Filter & Search Controls
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        // Search Input
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text(strings.searchPlaceholder) },
          leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("patients_search_bar"),
          shape = RoundedCornerShape(12.dp),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Patient Directory: Quick Doctor Filter Chips
        if (selectedSubTab == 0) {
          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
              FilterChipItem(
                label = strings.allDoctors,
                isSelected = doctorFilter == null,
                onClick = { doctorFilter = null }
              )
            }
            item {
              FilterChipItem(
                label = strings.drHayaName,
                isSelected = doctorFilter == Doctor.HAYA_HASAN.displayName,
                color = DoctorHayaColor,
                onClick = { doctorFilter = Doctor.HAYA_HASAN.displayName }
              )
            }
            item {
              FilterChipItem(
                label = strings.drJafarName,
                isSelected = doctorFilter == Doctor.JAFAR_ATEYH.displayName,
                color = DoctorJafarColor,
                onClick = { doctorFilter = Doctor.JAFAR_ATEYH.displayName }
              )
            }
          }
        }

        // SubTab 1: Advanced & Strict Appointment Filtering Button & Status
        if (selectedSubTab == 1) {
          Spacer(modifier = Modifier.height(8.dp))
          val activeFilterCount = (if (appliedDoctorFilter != null) 1 else 0) +
            (if (appliedIsTodayOnly) 1 else 0) +
            (if (appliedFromDateMillis != null || appliedToDateMillis != null) 1 else 0)

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            FilledTonalButton(
              onClick = { showAppointmentFilterDialog = true },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.testTag("btn_open_appointment_filter")
            ) {
              Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isArabic) "تصفية المواعيد" else "Filter Appointments",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
              )
              if (activeFilterCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primary,
                  contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                  Text(
                    text = "$activeFilterCount",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }

            if (activeFilterCount > 0) {
              TextButton(
                onClick = {
                  appliedDoctorFilter = null
                  appliedIsTodayOnly = false
                  appliedFromDateMillis = null
                  appliedToDateMillis = null
                }
              ) {
                Text(
                  text = if (isArabic) "إعادة تعيين (${activeFilterCount})" else "Reset Filters (${activeFilterCount})",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.error,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // Tab Content
      if (selectedSubTab == 0) {
        // Patients Directory
        val filteredPatients = patients.filter { patient ->
          val matchesSearch = patient.fullName.contains(searchQuery, ignoreCase = true) ||
                              patient.phoneNumber.contains(searchQuery, ignoreCase = true)
          val matchesDoctor = doctorFilter == null || patient.assignedDoctor.equals(doctorFilter, ignoreCase = true)
          matchesSearch && matchesDoctor
        }

        if (filteredPatients.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (isArabic) "لم يتم العثور على أي مريض مطابق." else "No patients found matching the criteria.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(filteredPatients) { patient ->
              PatientListItemCard(
                patient = patient,
                strings = strings,
                onClick = { viewModel.selectPatient(patient) }
              )
            }
            item { Spacer(modifier = Modifier.height(70.dp)) }
          }
        }
      } else {
        // Appointments Scheduler with strict filtering applied on confirm
        val filteredAppointments = appointments.filter { appt ->
          val matchesSearch = searchQuery.isBlank() ||
                              appt.patientName.contains(searchQuery, ignoreCase = true) ||
                              appt.procedureNotes.contains(searchQuery, ignoreCase = true) ||
                              appt.phoneNumber.contains(searchQuery, ignoreCase = true)
          val matchesDoctor = appliedDoctorFilter == null || appt.doctor.equals(appliedDoctorFilter, ignoreCase = true)

          val matchesToday = if (appliedIsTodayOnly) {
            val cal = Calendar.getInstance()
            val apptCal = Calendar.getInstance().apply { timeInMillis = appt.appointmentDate }
            cal.get(Calendar.YEAR) == apptCal.get(Calendar.YEAR) &&
            cal.get(Calendar.DAY_OF_YEAR) == apptCal.get(Calendar.DAY_OF_YEAR)
          } else {
            true
          }

          val matchesDateRange = if (appliedFromDateMillis != null || appliedToDateMillis != null) {
            val fromMatch = appliedFromDateMillis == null || appt.appointmentDate >= appliedFromDateMillis!!
            val toMatch = appliedToDateMillis == null || appt.appointmentDate <= appliedToDateMillis!!
            fromMatch && toMatch
          } else {
            true
          }

          matchesSearch && matchesDoctor && matchesToday && matchesDateRange
        }

        if (filteredAppointments.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
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
                text = if (isArabic) "لا توجد مواعيد مطابقة للتصفية المحددة." else "No appointments found matching filters.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(filteredAppointments) { appt ->
              EnhancedAppointmentListItemCard(
                appointment = appt,
                strings = strings,
                isArabic = isArabic,
                onStatusChange = { newStatus ->
                  viewModel.updateAppointmentStatus(appt.appointmentId, newStatus)
                },
                onSelectPatient = {
                  val matchedPatient = patients.find { it.patientId == appt.patientId }
                  if (matchedPatient != null) {
                    viewModel.selectPatient(matchedPatient)
                  }
                }
              )
            }
            item { Spacer(modifier = Modifier.height(70.dp)) }
          }
        }
      }
    }
  }

  // Active Patient Profile Dialog (Unified Centralized EHR)
  if (selectedPatient != null) {
    PatientDetailDialog(
      patient = selectedPatient!!,
      visits = selectedPatientVisits,
      invoices = selectedPatientInvoices,
      installments = selectedPatientInstallments,
      appointments = selectedPatientAppointments,
      exchangeRateSyp = globalExchangeRate,
      onDismiss = { viewModel.selectPatient(null) },
      onUpdatePatient = { updatedPatient ->
        viewModel.updatePatient(updatedPatient)
      },
      onAddVisit = { newVisit ->
        viewModel.addClinicalVisit(newVisit)
      },
      onRecordInstallment = { invoiceId, amount, rate, method, notes ->
        viewModel.recordInstallmentPayment(invoiceId, amount, rate, method, notes)
      }
    )
  }

  // Add New Patient Dialog
  if (showNewPatientDialog) {
    AddNewPatientDialog(
      strings = strings,
      isArabic = isArabic,
      onDismiss = { showNewPatientDialog = false },
      onConfirm = { newPatient ->
        viewModel.addPatient(newPatient)
        showNewPatientDialog = false
      }
    )
  }

  // Add New Appointment Dialog
  if (showNewAppointmentDialog) {
    AddNewAppointmentDialog(
      patients = patients,
      strings = strings,
      onDismiss = { showNewAppointmentDialog = false },
      onConfirm = { newAppt ->
        viewModel.addAppointment(newAppt)
        showNewAppointmentDialog = false
      }
    )
  }

  // Requirement 3: Advanced & Strict Appointment Filtering Dialog (Committed on Confirm)
  if (showAppointmentFilterDialog) {
    AppointmentFilterDialog(
      initialDoctor = appliedDoctorFilter,
      initialIsTodayOnly = appliedIsTodayOnly,
      initialFromDateMillis = appliedFromDateMillis,
      initialToDateMillis = appliedToDateMillis,
      isArabic = isArabic,
      strings = strings,
      onDismiss = { showAppointmentFilterDialog = false },
      onApply = { doc, today, from, to ->
        appliedDoctorFilter = doc
        appliedIsTodayOnly = today
        appliedFromDateMillis = from
        appliedToDateMillis = to
        showAppointmentFilterDialog = false
      },
      onReset = {
        appliedDoctorFilter = null
        appliedIsTodayOnly = false
        appliedFromDateMillis = null
        appliedToDateMillis = null
        showAppointmentFilterDialog = false
      }
    )
  }
}

@Composable
private fun FilterChipItem(
  label: String,
  isSelected: Boolean,
  color: Color = MaterialTheme.colorScheme.primary,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null,
    modifier = Modifier.clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isSelected) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
      }
      Text(
        text = label,
        fontSize = 11.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

/**
 * Requirement 5: On the Patients list screen, display the assigned doctor's name
 * on a clean, dedicated line underneath the patient's name for neat formatting.
 */
@Composable
private fun PatientListItemCard(
  patient: Patient,
  strings: AppStrings,
  onClick: () -> Unit
) {
  val isArabic = strings.isArabic

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("patient_card_${patient.patientId}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
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
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        // Patient Name on Top Line
        Text(
          text = patient.fullName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          maxLines = 1
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Requirement 5: Clean, dedicated line for Assigned Doctor underneath patient name
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${strings.assignedDoctor}: ",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
          )
          DoctorBadge(doctorName = patient.assignedDoctor, compact = true)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Phone & Age Info
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (patient.phoneNumber.isNotBlank()) {
            Icon(
              imageVector = Icons.Default.Phone,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = patient.phoneNumber,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
          }
          if (patient.age > 0) {
            Text(
              text = "${patient.age} ${if (isArabic) "سنة" else "yrs"}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Chronic Diseases tag & Visits count
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (patient.chronicDiseases.isNotBlank() && !patient.chronicDiseases.equals("None", ignoreCase = true)) {
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = Color(0xFFFFEBEE)
            ) {
              Text(
                text = "⚠ ${patient.chronicDiseases}",
                color = Color(0xFFC62828),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                maxLines = 1
              )
            }
          } else {
            Spacer(modifier = Modifier.width(1.dp))
          }

          Text(
            text = "${patient.totalVisits} ${if (isArabic) "زيارة" else "visits"}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}

@Composable
private fun EnhancedAppointmentListItemCard(
  appointment: Appointment,
  strings: AppStrings,
  isArabic: Boolean,
  onStatusChange: (String) -> Unit,
  onSelectPatient: () -> Unit
) {
  val isCompleted = appointment.status.equals("Completed", ignoreCase = true)
  val dateFormatted = SimpleDateFormat("EEE, MMM dd, yyyy", Locale.US).format(Date(appointment.appointmentDate))

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.clickable { onSelectPatient() }) {
          Text(
            text = appointment.patientName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          if (appointment.phoneNumber.isNotBlank()) {
            Text(
              text = appointment.phoneNumber,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
        BookingTypeBadge(bookingType = appointment.bookingType)
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "$dateFormatted • ${appointment.timeSlot}",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        DoctorBadge(doctorName = appointment.doctor, compact = true)
      }

      // Explicit Dental Procedure / Action Performed
      if (appointment.procedureNotes.isNotBlank()) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.MedicalServices,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = strings.procedurePerformed,
              fontSize = 9.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = appointment.procedureNotes,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Status Management Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!isCompleted) {
          Button(
            onClick = { onStatusChange("Completed") },
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(strings.statusCompleted, fontSize = 11.sp)
          }
        } else {
          TextButton(onClick = { onStatusChange("Scheduled") }) {
            Text(if (isArabic) "إعادة جدولة" else "Reopen", fontSize = 11.sp)
          }
        }
      }
    }
  }
}

/**
 * Requirements 3 & 5:
 * - All input fields start completely blank for manual entry by the doctor.
 * - Gender field provides explicit dropdown options (Male / Female) styled
 *   consistently with the supervising doctor selector dropdown.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNewPatientDialog(
  strings: AppStrings,
  isArabic: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (Patient) -> Unit
) {
  // All fields start completely blank
  var fullName by remember { mutableStateOf("") }
  var ageText by remember { mutableStateOf("") }
  var gender by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("") }
  var address by remember { mutableStateOf("") }
  var occupation by remember { mutableStateOf("") }
  var maritalStatus by remember { mutableStateOf("") }
  var assignedDoctor by remember { mutableStateOf(Doctor.HAYA_HASAN.displayName) }
  var chronicDiseases by remember { mutableStateOf("") }
  var chronicMedications by remember { mutableStateOf("") }
  var previousSurgeries by remember { mutableStateOf("") }
  var previousDental by remember { mutableStateOf("") }

  var doctorDropdownExpanded by remember { mutableStateOf(false) }
  var genderDropdownExpanded by remember { mutableStateOf(false) }

  val genderOptions = listOf(
    Pair(strings.genderMale, "Male"),
    Pair(strings.genderFemale, "Female")
  )

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.newPatient) },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        item {
          OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text(strings.fullName) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }
        item {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = ageText,
              onValueChange = { ageText = it },
              label = { Text(strings.age) },
              modifier = Modifier.weight(1f),
              singleLine = true
            )

            // Requirement 5: Explicit Gender dropdown options styled consistently with doctor selector
            ExposedDropdownMenuBox(
              expanded = genderDropdownExpanded,
              onExpandedChange = { genderDropdownExpanded = it },
              modifier = Modifier.weight(1f)
            ) {
              OutlinedTextField(
                value = when (gender) {
                  "Male" -> strings.genderMale
                  "Female" -> strings.genderFemale
                  else -> ""
                },
                onValueChange = {},
                readOnly = true,
                label = { Text(strings.gender) },
                placeholder = { Text(strings.gender) },
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
            label = { Text(strings.phone) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
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
          // Supervising Doctor Dropdown Selector
          ExposedDropdownMenuBox(
            expanded = doctorDropdownExpanded,
            onExpandedChange = { doctorDropdownExpanded = it }
          ) {
            val displayDoctorName = Doctor.fromName(assignedDoctor).getLocalizedName(isArabic)
            OutlinedTextField(
              value = displayDoctorName,
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
              DropdownMenuItem(
                text = { Text(Doctor.HAYA_HASAN.getLocalizedName(isArabic)) },
                onClick = {
                  assignedDoctor = Doctor.HAYA_HASAN.displayName
                  doctorDropdownExpanded = false
                }
              )
              DropdownMenuItem(
                text = { Text(Doctor.JAFAR_ATEYH.getLocalizedName(isArabic)) },
                onClick = {
                  assignedDoctor = Doctor.JAFAR_ATEYH.displayName
                  doctorDropdownExpanded = false
                }
              )
            }
          }
        }
        item {
          OutlinedTextField(
            value = occupation,
            onValueChange = { occupation = it },
            label = { Text(strings.occupation) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
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
            value = previousDental,
            onValueChange = { previousDental = it },
            label = { Text(strings.previousDental) },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (fullName.isNotBlank()) {
            val patient = Patient(
              fullName = fullName.trim(),
              age = ageText.toIntOrNull() ?: 0,
              gender = gender.ifBlank { "Unspecified" },
              maritalStatus = maritalStatus,
              address = address.trim(),
              phoneNumber = phone.trim(),
              occupation = occupation.trim(),
              assignedDoctor = assignedDoctor,
              chronicDiseases = chronicDiseases.trim(),
              chronicMedications = chronicMedications.trim(),
              previousSurgeries = previousSurgeries.trim(),
              previousDentalProcedures = previousDental.trim()
            )
            onConfirm(patient)
          }
        }
      ) {
        Text(strings.save)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancel) }
    }
  )
}

/**
 * Requirement 3: AddNewAppointmentDialog fields start completely blank
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNewAppointmentDialog(
  patients: List<Patient>,
  strings: AppStrings,
  onDismiss: () -> Unit,
  onConfirm: (Appointment) -> Unit
) {
  var selectedPatient by remember { mutableStateOf(patients.firstOrNull()) }
  var patientDropdownExpanded by remember { mutableStateOf(false) }

  // Starts completely blank for doctor/receptionist entry
  var timeSlot by remember { mutableStateOf("") }
  var bookingType by remember { mutableStateOf("Walk-in") }
  var procedureNotes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.newAppointment) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Patient Selector
        ExposedDropdownMenuBox(
          expanded = patientDropdownExpanded,
          onExpandedChange = { patientDropdownExpanded = it }
        ) {
          OutlinedTextField(
            value = selectedPatient?.fullName ?: (if (strings.isArabic) "اختر المريض" else "Select Patient"),
            onValueChange = {},
            readOnly = true,
            label = { Text(strings.navPatients) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientDropdownExpanded) },
            modifier = Modifier
              .fillMaxWidth()
              .menuAnchor(MenuAnchorType.PrimaryNotEditable)
          )
          ExposedDropdownMenu(
            expanded = patientDropdownExpanded,
            onDismissRequest = { patientDropdownExpanded = false }
          ) {
            patients.forEach { p ->
              DropdownMenuItem(
                text = { Text("${p.fullName} (${Doctor.fromName(p.assignedDoctor).getLocalizedName(strings.isArabic)})") },
                onClick = {
                  selectedPatient = p
                  patientDropdownExpanded = false
                }
              )
            }
          }
        }

        // Booking Type
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (bookingType == "Walk-in") Color(0xFFE65100) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .weight(1f)
              .clickable { bookingType = "Walk-in" }
          ) {
            Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
              Text(
                text = strings.bookingWalkIn,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (bookingType == "Walk-in") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (bookingType == "Phone") Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .weight(1f)
              .clickable { bookingType = "Phone" }
          ) {
            Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
              Text(
                text = strings.bookingPhone,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = if (bookingType == "Phone") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        OutlinedTextField(
          value = timeSlot,
          onValueChange = { timeSlot = it },
          label = { Text(if (strings.isArabic) "وقت الموعد (مثال: 10:30 صباحاً)" else "Time Slot (e.g., 10:30 AM)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = procedureNotes,
          onValueChange = { procedureNotes = it },
          label = { Text(strings.procedurePerformed) },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (selectedPatient != null && timeSlot.isNotBlank()) {
            val appt = Appointment(
              patientId = selectedPatient!!.patientId,
              patientName = selectedPatient!!.fullName,
              phoneNumber = selectedPatient!!.phoneNumber,
              doctor = selectedPatient!!.assignedDoctor,
              appointmentDate = System.currentTimeMillis(),
              timeSlot = timeSlot.trim(),
              bookingType = bookingType,
              status = "Scheduled",
              procedureNotes = procedureNotes.trim()
            )
            onConfirm(appt)
          }
        }
      ) {
        Text(strings.save)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancel) }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentFilterDialog(
  initialDoctor: String?,
  initialIsTodayOnly: Boolean,
  initialFromDateMillis: Long?,
  initialToDateMillis: Long?,
  isArabic: Boolean,
  strings: AppStrings,
  onDismiss: () -> Unit,
  onApply: (doctor: String?, isTodayOnly: Boolean, fromDate: Long?, toDate: Long?) -> Unit,
  onReset: () -> Unit
) {
  var draftDoctor by remember { mutableStateOf(initialDoctor) }
  var draftIsTodayOnly by remember { mutableStateOf(initialIsTodayOnly) }
  var draftFromDateMillis by remember { mutableStateOf(initialFromDateMillis) }
  var draftToDateMillis by remember { mutableStateOf(initialToDateMillis) }

  var activeDatePickerTarget by remember { mutableStateOf<String?>(null) }
  val datePickerState = rememberDatePickerState()
  val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.FilterList,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = if (isArabic) "تصفية جدول المواعيد" else "Filter Appointments Schedule",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // 1. Doctor Filter
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = if (isArabic) "1. الطبيب المعالج" else "1. Treating Doctor",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // All
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (draftDoctor == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .weight(1f)
                .clickable { draftDoctor = null }
            ) {
              Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text(
                  text = strings.allDoctors,
                  fontSize = 11.sp,
                  fontWeight = if (draftDoctor == null) FontWeight.Bold else FontWeight.Normal,
                  color = if (draftDoctor == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            // Dr. Haya
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (draftDoctor == Doctor.HAYA_HASAN.displayName) DoctorHayaColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
              border = if (draftDoctor == Doctor.HAYA_HASAN.displayName) androidx.compose.foundation.BorderStroke(1.5.dp, DoctorHayaColor) else null,
              modifier = Modifier
                .weight(1f)
                .clickable { draftDoctor = Doctor.HAYA_HASAN.displayName }
            ) {
              Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text(
                  text = strings.drHayaName,
                  fontSize = 11.sp,
                  fontWeight = if (draftDoctor == Doctor.HAYA_HASAN.displayName) FontWeight.Bold else FontWeight.Normal,
                  color = if (draftDoctor == Doctor.HAYA_HASAN.displayName) DoctorHayaColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            // Dr. Jafar
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (draftDoctor == Doctor.JAFAR_ATEYH.displayName) DoctorJafarColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
              border = if (draftDoctor == Doctor.JAFAR_ATEYH.displayName) androidx.compose.foundation.BorderStroke(1.5.dp, DoctorJafarColor) else null,
              modifier = Modifier
                .weight(1f)
                .clickable { draftDoctor = Doctor.JAFAR_ATEYH.displayName }
            ) {
              Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                Text(
                  text = strings.drJafarName,
                  fontSize = 11.sp,
                  fontWeight = if (draftDoctor == Doctor.JAFAR_ATEYH.displayName) FontWeight.Bold else FontWeight.Normal,
                  color = if (draftDoctor == Doctor.JAFAR_ATEYH.displayName) DoctorJafarColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // 2. Today Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (isArabic) "2. مواعيد اليوم فقط" else "2. Today's Schedule Only",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (isArabic) "حصر العرض بمواعيد هذا اليوم فقط" else "Isolate appointments for the current day",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Switch(
            checked = draftIsTodayOnly,
            onCheckedChange = { checked ->
              draftIsTodayOnly = checked
              if (checked) {
                draftFromDateMillis = null
                draftToDateMillis = null
              }
            }
          )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        // 3. Date Range
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = if (isArabic) "3. نطاق زمني مخصص (من تاريخ - إلى تاريخ)" else "3. Custom Date Range (From - To)",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (draftIsTodayOnly) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedButton(
              onClick = { activeDatePickerTarget = "FROM" },
              enabled = !draftIsTodayOnly,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = strings.fromDate,
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = draftFromDateMillis?.let { dateFormat.format(Date(it)) } ?: (if (isArabic) "غير محدد" else "Not set"),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }

            OutlinedButton(
              onClick = { activeDatePickerTarget = "TO" },
              enabled = !draftIsTodayOnly,
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = strings.toDate,
                  fontSize = 10.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = draftToDateMillis?.let { dateFormat.format(Date(it)) } ?: (if (isArabic) "غير محدد" else "Not set"),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }

          if (draftFromDateMillis != null || draftToDateMillis != null) {
            TextButton(
              onClick = {
                draftFromDateMillis = null
                draftToDateMillis = null
              },
              modifier = Modifier.align(Alignment.End)
            ) {
              Text(
                text = if (isArabic) "مسح النطاق الزمني" else "Clear Date Range",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onApply(draftDoctor, draftIsTodayOnly, draftFromDateMillis, draftToDateMillis)
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("btn_apply_appointment_filter")
      ) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = if (isArabic) "تطبيق التصفية" else "Apply Filter", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        TextButton(
          onClick = {
            draftDoctor = null
            draftIsTodayOnly = false
            draftFromDateMillis = null
            draftToDateMillis = null
            onReset()
          }
        ) {
          Text(text = if (isArabic) "إلغاء التصفية" else "Reset", color = MaterialTheme.colorScheme.error)
        }
        TextButton(onClick = onDismiss) {
          Text(text = strings.cancel)
        }
      }
    }
  )

  if (activeDatePickerTarget != null) {
    DatePickerDialog(
      onDismissRequest = { activeDatePickerTarget = null },
      confirmButton = {
        Button(
          onClick = {
            val selectedDate = datePickerState.selectedDateMillis
            if (activeDatePickerTarget == "FROM") {
              draftFromDateMillis = selectedDate
            } else if (activeDatePickerTarget == "TO") {
              draftToDateMillis = selectedDate?.let { it + 86399999L }
            }
            activeDatePickerTarget = null
          }
        ) {
          Text(strings.confirm)
        }
      },
      dismissButton = {
        TextButton(onClick = { activeDatePickerTarget = null }) { Text(strings.cancel) }
      }
    ) {
      DatePicker(
        state = datePickerState,
        title = {
          Text(
            text = if (activeDatePickerTarget == "FROM") strings.fromDate else strings.toDate,
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.Bold
          )
        }
      )
    }
  }
}

