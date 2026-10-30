package com.example.data.repository

import com.example.data.local.EliteDentalDatabase
import com.example.data.model.Appointment
import com.example.data.model.ClinicalVisit
import com.example.data.model.DentalService
import com.example.data.model.Doctor
import com.example.data.model.FinancialDistributionCalculator
import com.example.data.model.InstallmentPayment
import com.example.data.model.InventoryItem
import com.example.data.model.Invoice
import com.example.data.model.Patient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ClinicRepository(private val database: EliteDentalDatabase) {

  private val patientDao = database.patientDao()
  private val clinicalVisitDao = database.clinicalVisitDao()
  private val inventoryDao = database.inventoryDao()
  private val dentalServiceDao = database.dentalServiceDao()
  private val invoiceDao = database.invoiceDao()
  private val appointmentDao = database.appointmentDao()
  private val installmentPaymentDao = database.installmentPaymentDao()

  val allPatients: Flow<List<Patient>> = patientDao.getAllPatients()
  val allAppointments: Flow<List<Appointment>> = appointmentDao.getAllAppointments()
  val allInventory: Flow<List<InventoryItem>> = inventoryDao.getAllItems()
  val lowStockItems: Flow<List<InventoryItem>> = inventoryDao.getLowStockItems()
  val allServices: Flow<List<DentalService>> = dentalServiceDao.getAllServices()
  val allInvoices: Flow<List<Invoice>> = invoiceDao.getAllInvoices()
  val allInstallments: Flow<List<InstallmentPayment>> = installmentPaymentDao.getAllPayments()

  fun getVisitsForPatient(patientId: Long): Flow<List<ClinicalVisit>> =
    clinicalVisitDao.getVisitsForPatient(patientId)

  fun getInvoicesForPatient(patientId: Long): Flow<List<Invoice>> =
    invoiceDao.getInvoicesForPatient(patientId)

  fun getInstallmentsForPatient(patientId: Long): Flow<List<InstallmentPayment>> =
    installmentPaymentDao.getPaymentsForPatient(patientId)

  fun getInstallmentsForInvoice(invoiceId: Long): Flow<List<InstallmentPayment>> =
    installmentPaymentDao.getPaymentsForInvoice(invoiceId)

  fun getAppointmentsForPatient(patientId: Long): Flow<List<Appointment>> =
    appointmentDao.getAppointmentsForPatient(patientId)

  suspend fun insertPatient(patient: Patient): Long = withContext(Dispatchers.IO) {
    patientDao.insertPatient(patient)
  }

  suspend fun updatePatient(patient: Patient) = withContext(Dispatchers.IO) {
    patientDao.updatePatient(patient)
  }

  suspend fun deletePatient(patient: Patient) = withContext(Dispatchers.IO) {
    patientDao.deletePatient(patient)
  }

  suspend fun insertAppointment(appointment: Appointment): Long = withContext(Dispatchers.IO) {
    appointmentDao.insertAppointment(appointment)
  }

  suspend fun updateAppointmentStatus(appointmentId: Long, status: String) = withContext(Dispatchers.IO) {
    appointmentDao.updateStatus(appointmentId, status)
  }

  suspend fun deleteAppointment(appointment: Appointment) = withContext(Dispatchers.IO) {
    appointmentDao.deleteAppointment(appointment)
  }

  suspend fun insertInventoryItem(item: InventoryItem): Long = withContext(Dispatchers.IO) {
    inventoryDao.insertItem(item)
  }

  suspend fun updateInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) {
    inventoryDao.updateItem(item)
  }

  suspend fun updateInventoryQuantity(itemId: Long, newQuantity: Double) = withContext(Dispatchers.IO) {
    val sanitized = newQuantity.coerceAtLeast(0.0)
    inventoryDao.updateQuantity(itemId, sanitized)
  }

  /**
   * Adjusts stock based on explicit transaction type:
   * isImport = true -> Stock In / Supply / Purchase (+delta)
   * isImport = false -> Stock Out / Usage / Procedure / Sale (-delta)
   */
  suspend fun adjustInventoryTransaction(itemId: Long, delta: Double, isImport: Boolean) = withContext(Dispatchers.IO) {
    val currentItems = inventoryDao.getAllItems().first()
    val item = currentItems.find { it.itemId == itemId } ?: return@withContext
    val newQuantity = if (isImport) {
      item.currentQuantity + delta
    } else {
      (item.currentQuantity - delta).coerceAtLeast(0.0)
    }
    inventoryDao.updateQuantity(itemId, newQuantity)
  }

  suspend fun deleteInventoryItem(item: InventoryItem) = withContext(Dispatchers.IO) {
    inventoryDao.deleteItem(item)
  }

  suspend fun insertDentalService(service: DentalService): Long = withContext(Dispatchers.IO) {
    dentalServiceDao.insertService(service)
  }

  suspend fun updateDentalService(service: DentalService) = withContext(Dispatchers.IO) {
    dentalServiceDao.updateService(service)
  }

  suspend fun deleteDentalService(service: DentalService) = withContext(Dispatchers.IO) {
    dentalServiceDao.deleteService(service)
  }

  /**
   * Generates and persists an Invoice with multi-services, multi-currency USD & SYP,
   * down payment / installments, and automated doctor split (30% Clinic / 42% Primary / 28% Secondary).
   */
  suspend fun generateInvoice(
    patient: Patient,
    serviceNames: List<String>,
    totalServicePrice: Double,
    doctorFees: Double,
    exchangeRateSyp: Double = 14500.0,
    amountPaidToday: Double = totalServicePrice + doctorFees,
    paymentMethod: String = "Cash",
    notes: String = ""
  ): Long = withContext(Dispatchers.IO) {
    val totalAmount = totalServicePrice + doctorFees
    val sanitizedPaid = amountPaidToday.coerceIn(0.0, totalAmount)
    val remainingBalance = (totalAmount - sanitizedPaid).coerceAtLeast(0.0)
    val totalAmountSyp = totalAmount * exchangeRateSyp

    val doctor = Doctor.fromName(patient.assignedDoctor)
    val breakdown = FinancialDistributionCalculator.calculate(
      totalAmount = totalAmount,
      assignedDoctor = doctor,
      exchangeRateSyp = exchangeRateSyp,
      amountPaidToday = sanitizedPaid
    )

    val combinedServiceName = if (serviceNames.isEmpty()) "General Dental Procedure" else serviceNames.joinToString(" + ")

    val invoice = Invoice(
      patientId = patient.patientId,
      patientName = patient.fullName,
      assignedDoctor = patient.assignedDoctor,
      serviceName = combinedServiceName,
      servicePrice = totalServicePrice,
      doctorFees = doctorFees,
      totalAmount = totalAmount,
      exchangeRateSyp = exchangeRateSyp,
      totalAmountSyp = totalAmountSyp,
      amountPaidToday = sanitizedPaid,
      remainingBalance = remainingBalance,
      clinicOverhead = breakdown.clinicOverhead,
      doctorPool = breakdown.netDoctorPool,
      hayaShare = breakdown.hayaShare,
      jafarShare = breakdown.jafarShare,
      paymentMethod = paymentMethod,
      timestamp = System.currentTimeMillis(),
      notes = notes
    )

    val invoiceId = invoiceDao.insertInvoice(invoice)

    // Increment patient visit count
    patientDao.incrementVisits(patient.patientId)

    // Record down-payment / initial installment in payment ledger if amount > 0
    if (sanitizedPaid > 0.0) {
      installmentPaymentDao.insertPayment(
        InstallmentPayment(
          invoiceId = invoiceId,
          patientId = patient.patientId,
          patientName = patient.fullName,
          amountPaidUsd = sanitizedPaid,
          amountPaidSyp = sanitizedPaid * exchangeRateSyp,
          exchangeRateSyp = exchangeRateSyp,
          paymentDate = System.currentTimeMillis(),
          paymentMethod = paymentMethod,
          notes = if (remainingBalance > 0.01) "Initial Down Payment" else "Full Payment"
        )
      )
    }

    // Auto-record a clinical visit entry
    clinicalVisitDao.insertVisit(
      ClinicalVisit(
        patientId = patient.patientId,
        procedureType = combinedServiceName,
        visitDate = System.currentTimeMillis(),
        cost = totalAmount,
        amountPaid = sanitizedPaid,
        remainingBalance = remainingBalance,
        clinicShare = breakdown.clinicOverhead,
        associatedDoctor = patient.assignedDoctor,
        clinicalNotes = "Invoice #$invoiceId: $combinedServiceName. Paid: $$sanitizedPaid, Balance: $$remainingBalance. $notes"
      )
    )

    invoiceId
  }

  suspend fun recordInstallmentPayment(
    invoiceId: Long,
    paymentAmountUsd: Double,
    exchangeRateSyp: Double = 14500.0,
    paymentMethod: String = "Cash",
    notes: String = ""
  ): Boolean = withContext(Dispatchers.IO) {
    val invoice = invoiceDao.getInvoiceById(invoiceId) ?: return@withContext false
    val sanitizedPayment = paymentAmountUsd.coerceIn(0.0, invoice.remainingBalance)
    if (sanitizedPayment <= 0.0) return@withContext false

    val newPaidTotal = invoice.amountPaidToday + sanitizedPayment
    val newRemaining = (invoice.totalAmount - newPaidTotal).coerceAtLeast(0.0)

    val updatedInvoice = invoice.copy(
      amountPaidToday = newPaidTotal,
      remainingBalance = newRemaining
    )
    invoiceDao.updateInvoice(updatedInvoice)

    installmentPaymentDao.insertPayment(
      InstallmentPayment(
        invoiceId = invoiceId,
        patientId = invoice.patientId,
        patientName = invoice.patientName,
        amountPaidUsd = sanitizedPayment,
        amountPaidSyp = sanitizedPayment * exchangeRateSyp,
        exchangeRateSyp = exchangeRateSyp,
        paymentDate = System.currentTimeMillis(),
        paymentMethod = paymentMethod,
        notes = notes.ifBlank { "Installment Payment" }
      )
    )

    true
  }

  suspend fun insertClinicalVisit(visit: ClinicalVisit): Long = withContext(Dispatchers.IO) {
    patientDao.incrementVisits(visit.patientId)
    clinicalVisitDao.insertVisit(visit)
  }

  /**
   * Pre-populates the standard treatment price list with USD services (as requested).
   * Strips out dummy mock patients, dummy visits, dummy appointments, dummy inventory,
   * and dummy invoices so all clinic forms start clean for doctor manual entry.
   */
  suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
    val existingServices = dentalServiceDao.getAllServices().first()
    if (existingServices.isNotEmpty()) return@withContext

    // Treatment Price List in USD (Pre-populated services list)
    val defaultServices = listOf(
      // Composite / Aesthetic Fillings (حشوات تجميلية)
      DentalService(serviceName = "Composite Filling - Type A (حشوة تجميلية نوع A)", category = "Aesthetic Fillings", standardPrice = 250.0, description = "Premium multi-layer nano-hybrid aesthetic filling"),
      DentalService(serviceName = "Composite Filling - Type B (حشوة تجميلية نوع B)", category = "Aesthetic Fillings", standardPrice = 200.0, description = "Class II posterior/anterior aesthetic restoration"),
      DentalService(serviceName = "Composite Filling - Type C (حشوة تجميلية نوع C)", category = "Aesthetic Fillings", standardPrice = 175.0, description = "Class I direct aesthetic composite restoration"),
      DentalService(serviceName = "Composite Filling - Type D (حشوة تجميلية نوع D)", category = "Aesthetic Fillings", standardPrice = 150.0, description = "Standard single-surface composite restoration"),

      // Root Canal Treatment (سحب عصب)
      DentalService(serviceName = "Root Canal - Anterior Tooth (سحب عصب سن أمامي)", category = "Root Canal Treatment", standardPrice = 300.0, description = "Complete single-canal rotary endodontic instrumentation and obturation"),
      DentalService(serviceName = "Root Canal - Posterior Tooth (سحب عصب سن خلفي)", category = "Root Canal Treatment", standardPrice = 400.0, description = "Multi-canal molar endodontic treatment with apex locator & bioceramic sealer"),
      DentalService(serviceName = "Endodontic Retreatment (إعادة معالجة سحب عصب)", category = "Root Canal Treatment", standardPrice = 500.0, description = "Removal of previous obturation, negotiation of calcified canals & disinfection"),

      // Crowns & Veneers (التلبيس)
      DentalService(serviceName = "PFM Crown - Porcelain Fused to Metal (خزف معدن)", category = "Crowns & Veneers", standardPrice = 350.0, description = "High-strength porcelain fused to metal medical coping"),
      DentalService(serviceName = "Full Zirconia Crown (فول زيركون)", category = "Crowns & Veneers", standardPrice = 500.0, description = "Monolithic high-translucency CAD/CAM full contour zirconia crown"),
      DentalService(serviceName = "Zirconia 600GC Crown (زيركون 600GC)", category = "Crowns & Veneers", standardPrice = 600.0, description = "Ultra-aesthetic multilayered gradient zirconia GC crown"),
      DentalService(serviceName = "Zirconia E-max Crown / Veneer (زيركون إيماكس)", category = "Crowns & Veneers", standardPrice = 800.0, description = "Lithium disilicate IPS E-max ultra-aesthetic veneer/crown with natural fluorescence"),

      // Preventive & Cleanings (تنظيف وتقليح)
      DentalService(serviceName = "Dental Scaling & Polishing (تنظيف وتقليح)", category = "Preventive & Cleanings", standardPrice = 100.0, description = "Ultrasonic supra & sub-gingival calculus debridement with prophylactic polishing"),

      // Laser Whitening (تبييض ليزري)
      DentalService(serviceName = "Laser Teeth Whitening - Type A (تبييض ليزري نوع A)", category = "Laser Whitening", standardPrice = 900.0, description = "Full in-office diode laser teeth whitening package including home maintenance kit"),
      DentalService(serviceName = "Laser Teeth Whitening - Type B (تبييض ليزري نوع B)", category = "Laser Whitening", standardPrice = 600.0, description = "Standard in-office photothermal laser-activated bleaching session"),

      // Extractions (القلع)
      DentalService(serviceName = "Simple Dental Extraction (قلع عادي)", category = "Extractions", standardPrice = 150.0, description = "Routine forceps tooth extraction with local anesthesia"),
      DentalService(serviceName = "Surgical Dental Extraction (قلع جراحي)", category = "Extractions", standardPrice = 250.0, description = "Surgical flap elevation, bone relief, tooth sectioning, and suturing")
    )
    defaultServices.forEach { dentalServiceDao.insertService(it) }
  }
}
