package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Appointment
import com.example.data.model.ClinicalVisit
import com.example.data.model.DentalService
import com.example.data.model.Doctor
import com.example.data.model.FinancialBreakdown
import com.example.data.model.FinancialDistributionCalculator
import com.example.data.model.InstallmentPayment
import com.example.data.model.InventoryItem
import com.example.data.model.Invoice
import com.example.data.model.Patient
import com.example.data.repository.ClinicRepository
import com.example.ui.theme.AppLanguage
import com.example.ui.theme.CurrencyDisplayMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FinancialSummary(
  val totalRevenue: Double = 0.0,
  val totalCollected: Double = 0.0,
  val totalOutstandingBalance: Double = 0.0,
  val totalClinicOverhead: Double = 0.0,
  val totalDoctorPool: Double = 0.0,
  val totalHayaShare: Double = 0.0,
  val totalJafarShare: Double = 0.0,
  val invoiceCount: Int = 0,
  val unpaidInvoiceCount: Int = 0,
  val globalExchangeRateSyp: Double = 14500.0
) {
  val totalRevenueSyp: Double get() = totalRevenue * globalExchangeRateSyp
  val totalCollectedSyp: Double get() = totalCollected * globalExchangeRateSyp
  val totalOutstandingBalanceSyp: Double get() = totalOutstandingBalance * globalExchangeRateSyp
  val totalClinicOverheadSyp: Double get() = totalClinicOverhead * globalExchangeRateSyp
  val totalHayaShareSyp: Double get() = totalHayaShare * globalExchangeRateSyp
  val totalJafarShareSyp: Double get() = totalJafarShare * globalExchangeRateSyp
}

data class AppointmentStats(
  val totalToday: Int = 0,
  val walkInCount: Int = 0,
  val phoneCount: Int = 0,
  val scheduledCount: Int = 0,
  val completedCount: Int = 0
)

class ClinicViewModel(private val repository: ClinicRepository) : ViewModel() {

  // Navigation tab (0: Dashboard, 1: Patients & Appointments, 2: Inventory, 3: Invoices, 4: Services)
  private val _currentTab = MutableStateFlow(0)
  val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

  // Global default USD to SYP exchange rate
  private val _exchangeRateSyp = MutableStateFlow(14500.0)
  val exchangeRateSyp: StateFlow<Double> = _exchangeRateSyp.asStateFlow()

  // Search queries
  private val _patientSearchQuery = MutableStateFlow("")
  val patientSearchQuery: StateFlow<String> = _patientSearchQuery.asStateFlow()

  private val _inventorySearchQuery = MutableStateFlow("")
  val inventorySearchQuery: StateFlow<String> = _inventorySearchQuery.asStateFlow()

  // Low-stock automatic filter for inventory
  private val _inventoryLowStockOnly = MutableStateFlow(false)
  val inventoryLowStockOnly: StateFlow<Boolean> = _inventoryLowStockOnly.asStateFlow()

  // Selected Doctor filter for patients/appointments: null means All
  private val _doctorFilter = MutableStateFlow<String?>(null)
  val doctorFilter: StateFlow<String?> = _doctorFilter.asStateFlow()

  // Localization language (ENGLISH or ARABIC)
  private val _appLanguage = MutableStateFlow(AppLanguage.ENGLISH)
  val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

  // Currency display mode (Strict USD or Strict SYP)
  private val _currencyMode = MutableStateFlow(CurrencyDisplayMode.USD)
  val currencyMode: StateFlow<CurrencyDisplayMode> = _currencyMode.asStateFlow()

  // Active Patient for EHR Profile view
  private val _selectedPatient = MutableStateFlow<Patient?>(null)
  val selectedPatient: StateFlow<Patient?> = _selectedPatient.asStateFlow()

  // Clinical visits for the selected patient
  private val _selectedPatientVisits = MutableStateFlow<List<ClinicalVisit>>(emptyList())
  val selectedPatientVisits: StateFlow<List<ClinicalVisit>> = _selectedPatientVisits.asStateFlow()

  // Invoices for the selected patient
  private val _selectedPatientInvoices = MutableStateFlow<List<Invoice>>(emptyList())
  val selectedPatientInvoices: StateFlow<List<Invoice>> = _selectedPatientInvoices.asStateFlow()

  // Installments for the selected patient
  private val _selectedPatientInstallments = MutableStateFlow<List<InstallmentPayment>>(emptyList())
  val selectedPatientInstallments: StateFlow<List<InstallmentPayment>> = _selectedPatientInstallments.asStateFlow()

  // Appointments for the selected patient (Unified Profile integration)
  private val _selectedPatientAppointments = MutableStateFlow<List<Appointment>>(emptyList())
  val selectedPatientAppointments: StateFlow<List<Appointment>> = _selectedPatientAppointments.asStateFlow()

  // Appointment filters: Date Range (start, end in millis) and Status (All, Upcoming, Past, etc.)
  private val _appointmentDateRange = MutableStateFlow<Pair<Long?, Long?>>(Pair(null, null))
  val appointmentDateRange: StateFlow<Pair<Long?, Long?>> = _appointmentDateRange.asStateFlow()

  private val _appointmentStatusFilter = MutableStateFlow<String?>("All")
  val appointmentStatusFilter: StateFlow<String?> = _appointmentStatusFilter.asStateFlow()

  // All database streams
  val allPatients: StateFlow<List<Patient>> = repository.allPatients
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allAppointments: StateFlow<List<Appointment>> = repository.allAppointments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allInventory: StateFlow<List<InventoryItem>> = repository.allInventory
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val lowStockItems: StateFlow<List<InventoryItem>> = repository.lowStockItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allServices: StateFlow<List<DentalService>> = repository.allServices
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allInvoices: StateFlow<List<Invoice>> = repository.allInvoices
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allInstallments: StateFlow<List<InstallmentPayment>> = repository.allInstallments
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Financial Summary derived state
  val financialSummary: StateFlow<FinancialSummary> = combine(allInvoices, _exchangeRateSyp) { invoices, rate ->
    var revenue = 0.0
    var collected = 0.0
    var remaining = 0.0
    var overhead = 0.0
    var pool = 0.0
    var haya = 0.0
    var jafar = 0.0
    var unpaidCount = 0

    invoices.forEach { inv ->
      revenue += inv.totalAmount
      collected += inv.amountPaidToday
      remaining += inv.remainingBalance
      overhead += inv.clinicOverhead
      pool += inv.doctorPool
      haya += inv.hayaShare
      jafar += inv.jafarShare
      if (!inv.isFullyPaid) {
        unpaidCount++
      }
    }

    FinancialSummary(
      totalRevenue = revenue,
      totalCollected = collected,
      totalOutstandingBalance = remaining,
      totalClinicOverhead = overhead,
      totalDoctorPool = pool,
      totalHayaShare = haya,
      totalJafarShare = jafar,
      invoiceCount = invoices.size,
      unpaidInvoiceCount = unpaidCount,
      globalExchangeRateSyp = rate
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FinancialSummary())

  // Appointment Stats derived state
  val appointmentStats: StateFlow<AppointmentStats> = allAppointments.combine(_doctorFilter) { appts, _ ->
    val walkIns = appts.count { it.bookingType.equals("Walk-in", ignoreCase = true) }
    val phones = appts.count { it.bookingType.equals("Phone", ignoreCase = true) }
    val scheduled = appts.count { it.status.equals("Scheduled", ignoreCase = true) || it.status.equals("Confirmed", ignoreCase = true) }
    val completed = appts.count { it.status.equals("Completed", ignoreCase = true) }

    AppointmentStats(
      totalToday = appts.size,
      walkInCount = walkIns,
      phoneCount = phones,
      scheduledCount = scheduled,
      completedCount = completed
    )
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppointmentStats())

  init {
    viewModelScope.launch {
      repository.seedInitialDataIfEmpty()
    }
  }

  fun setTab(index: Int) {
    _currentTab.value = index
  }

  fun setExchangeRateSyp(rate: Double) {
    if (rate > 0.0) {
      _exchangeRateSyp.value = rate
    }
  }

  fun setPatientSearchQuery(query: String) {
    _patientSearchQuery.value = query
  }

  fun setInventorySearchQuery(query: String) {
    _inventorySearchQuery.value = query
  }

  fun setInventoryLowStockFilterOnly(active: Boolean) {
    _inventoryLowStockOnly.value = active
  }

  fun setLanguage(lang: AppLanguage) {
    _appLanguage.value = lang
  }

  fun setCurrencyMode(mode: CurrencyDisplayMode) {
    _currencyMode.value = mode
  }

  fun setAppointmentDateRange(start: Long?, end: Long?) {
    _appointmentDateRange.value = Pair(start, end)
  }

  fun setAppointmentStatusFilter(filter: String?) {
    _appointmentStatusFilter.value = filter
  }

  fun selectPatient(patient: Patient?) {
    _selectedPatient.value = patient
    if (patient != null) {
      viewModelScope.launch {
        repository.getVisitsForPatient(patient.patientId).collect { visits ->
          _selectedPatientVisits.value = visits
        }
      }
      viewModelScope.launch {
        repository.getInvoicesForPatient(patient.patientId).collect { invoices ->
          _selectedPatientInvoices.value = invoices
        }
      }
      viewModelScope.launch {
        repository.getInstallmentsForPatient(patient.patientId).collect { installments ->
          _selectedPatientInstallments.value = installments
        }
      }
      viewModelScope.launch {
        repository.getAppointmentsForPatient(patient.patientId).collect { appts ->
          _selectedPatientAppointments.value = appts
        }
      }
    } else {
      _selectedPatientVisits.value = emptyList()
      _selectedPatientInvoices.value = emptyList()
      _selectedPatientInstallments.value = emptyList()
      _selectedPatientAppointments.value = emptyList()
    }
  }

  fun addPatient(patient: Patient) {
    viewModelScope.launch {
      repository.insertPatient(patient)
    }
  }

  fun updatePatient(patient: Patient) {
    viewModelScope.launch {
      repository.updatePatient(patient)
      if (_selectedPatient.value?.patientId == patient.patientId) {
        _selectedPatient.value = patient
      }
    }
  }

  fun deletePatient(patient: Patient) {
    viewModelScope.launch {
      repository.deletePatient(patient)
      if (_selectedPatient.value?.patientId == patient.patientId) {
        _selectedPatient.value = null
      }
    }
  }

  fun addAppointment(appointment: Appointment) {
    viewModelScope.launch {
      repository.insertAppointment(appointment)
    }
  }

  fun updateAppointmentStatus(appointmentId: Long, status: String) {
    viewModelScope.launch {
      repository.updateAppointmentStatus(appointmentId, status)
    }
  }

  fun deleteAppointment(appointment: Appointment) {
    viewModelScope.launch {
      repository.deleteAppointment(appointment)
    }
  }

  fun addInventoryItem(item: InventoryItem) {
    viewModelScope.launch {
      repository.insertInventoryItem(item)
    }
  }

  fun updateInventoryItem(item: InventoryItem) {
    viewModelScope.launch {
      repository.updateInventoryItem(item)
    }
  }

  fun adjustInventoryQuantity(itemId: Long, newQuantity: Double) {
    viewModelScope.launch {
      repository.updateInventoryQuantity(itemId, newQuantity)
    }
  }

  fun adjustInventoryTransaction(itemId: Long, delta: Double, isImport: Boolean) {
    viewModelScope.launch {
      repository.adjustInventoryTransaction(itemId, delta, isImport)
    }
  }

  fun deleteInventoryItem(item: InventoryItem) {
    viewModelScope.launch {
      repository.deleteInventoryItem(item)
    }
  }

  fun addDentalService(service: DentalService) {
    viewModelScope.launch {
      repository.insertDentalService(service)
    }
  }

  fun updateDentalService(service: DentalService) {
    viewModelScope.launch {
      repository.updateDentalService(service)
    }
  }

  fun deleteDentalService(service: DentalService) {
    viewModelScope.launch {
      repository.deleteDentalService(service)
    }
  }

  fun addClinicalVisit(visit: ClinicalVisit) {
    viewModelScope.launch {
      repository.insertClinicalVisit(visit)
    }
  }

  fun generateInvoice(
    patient: Patient,
    serviceNames: List<String>,
    totalServicePrice: Double,
    doctorFees: Double,
    exchangeRateSyp: Double,
    amountPaidToday: Double,
    paymentMethod: String,
    notes: String
  ) {
    viewModelScope.launch {
      repository.generateInvoice(
        patient = patient,
        serviceNames = serviceNames,
        totalServicePrice = totalServicePrice,
        doctorFees = doctorFees,
        exchangeRateSyp = exchangeRateSyp,
        amountPaidToday = amountPaidToday,
        paymentMethod = paymentMethod,
        notes = notes
      )
    }
  }

  fun recordInstallmentPayment(
    invoiceId: Long,
    amountPaidUsd: Double,
    exchangeRateSyp: Double,
    paymentMethod: String,
    notes: String
  ) {
    viewModelScope.launch {
      repository.recordInstallmentPayment(
        invoiceId = invoiceId,
        paymentAmountUsd = amountPaidUsd,
        exchangeRateSyp = exchangeRateSyp,
        paymentMethod = paymentMethod,
        notes = notes
      )
    }
  }

  fun calculateBreakdownPreview(
    totalAmount: Double,
    doctorName: String,
    exchangeRateSyp: Double = _exchangeRateSyp.value,
    amountPaidToday: Double = totalAmount
  ): FinancialBreakdown {
    val doctor = Doctor.fromName(doctorName)
    return FinancialDistributionCalculator.calculate(
      totalAmount = totalAmount,
      assignedDoctor = doctor,
      exchangeRateSyp = exchangeRateSyp,
      amountPaidToday = amountPaidToday
    )
  }
}

class ClinicViewModelFactory(private val repository: ClinicRepository) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(ClinicViewModel::class.java)) {
      return ClinicViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
