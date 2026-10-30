package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Appointment
import com.example.data.model.ClinicalVisit
import com.example.data.model.DentalService
import com.example.data.model.InstallmentPayment
import com.example.data.model.InventoryItem
import com.example.data.model.Invoice
import com.example.data.model.Patient
import kotlinx.coroutines.flow.Flow

@Dao
interface PatientDao {
  @Query("SELECT * FROM patients ORDER BY full_name ASC")
  fun getAllPatients(): Flow<List<Patient>>

  @Query("SELECT * FROM patients WHERE assigned_doctor = :doctor ORDER BY full_name ASC")
  fun getPatientsByDoctor(doctor: String): Flow<List<Patient>>

  @Query("SELECT * FROM patients WHERE patient_id = :id")
  fun getPatientById(id: Long): Flow<Patient?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPatient(patient: Patient): Long

  @Update
  suspend fun updatePatient(patient: Patient)

  @Delete
  suspend fun deletePatient(patient: Patient)

  @Query("UPDATE patients SET total_visits = total_visits + 1 WHERE patient_id = :id")
  suspend fun incrementVisits(id: Long)
}

@Dao
interface ClinicalVisitDao {
  @Query("SELECT * FROM clinical_visits WHERE patient_id = :patientId ORDER BY visit_date DESC")
  fun getVisitsForPatient(patientId: Long): Flow<List<ClinicalVisit>>

  @Query("SELECT * FROM clinical_visits ORDER BY visit_date DESC")
  fun getAllVisits(): Flow<List<ClinicalVisit>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVisit(visit: ClinicalVisit): Long

  @Delete
  suspend fun deleteVisit(visit: ClinicalVisit)
}

@Dao
interface InventoryDao {
  @Query("SELECT * FROM inventory_items ORDER BY item_name ASC")
  fun getAllItems(): Flow<List<InventoryItem>>

  @Query("SELECT * FROM inventory_items WHERE current_quantity <= minimum_threshold ORDER BY current_quantity ASC")
  fun getLowStockItems(): Flow<List<InventoryItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(item: InventoryItem): Long

  @Update
  suspend fun updateItem(item: InventoryItem)

  @Delete
  suspend fun deleteItem(item: InventoryItem)

  @Query("UPDATE inventory_items SET current_quantity = :quantity WHERE item_id = :id")
  suspend fun updateQuantity(id: Long, quantity: Double)
}

@Dao
interface DentalServiceDao {
  @Query("SELECT * FROM treatment_services ORDER BY category ASC, service_name ASC")
  fun getAllServices(): Flow<List<DentalService>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertService(service: DentalService): Long

  @Update
  suspend fun updateService(service: DentalService)

  @Delete
  suspend fun deleteService(service: DentalService)
}

@Dao
interface InvoiceDao {
  @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
  fun getAllInvoices(): Flow<List<Invoice>>

  @Query("SELECT * FROM invoices WHERE assigned_doctor = :doctor ORDER BY timestamp DESC")
  fun getInvoicesForDoctor(doctor: String): Flow<List<Invoice>>

  @Query("SELECT * FROM invoices WHERE patient_id = :patientId ORDER BY timestamp DESC")
  fun getInvoicesForPatient(patientId: Long): Flow<List<Invoice>>

  @Query("SELECT * FROM invoices WHERE remaining_balance > 0.01 ORDER BY timestamp DESC")
  fun getUnpaidInvoices(): Flow<List<Invoice>>

  @Query("SELECT * FROM invoices WHERE invoice_id = :id")
  suspend fun getInvoiceById(id: Long): Invoice?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertInvoice(invoice: Invoice): Long

  @Update
  suspend fun updateInvoice(invoice: Invoice)

  @Delete
  suspend fun deleteInvoice(invoice: Invoice)
}

@Dao
interface InstallmentPaymentDao {
  @Query("SELECT * FROM installment_payments ORDER BY payment_date DESC")
  fun getAllPayments(): Flow<List<InstallmentPayment>>

  @Query("SELECT * FROM installment_payments WHERE invoice_id = :invoiceId ORDER BY payment_date DESC")
  fun getPaymentsForInvoice(invoiceId: Long): Flow<List<InstallmentPayment>>

  @Query("SELECT * FROM installment_payments WHERE patient_id = :patientId ORDER BY payment_date DESC")
  fun getPaymentsForPatient(patientId: Long): Flow<List<InstallmentPayment>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayment(payment: InstallmentPayment): Long
}

@Dao
interface AppointmentDao {
  @Query("SELECT * FROM appointments ORDER BY appointment_date ASC, time_slot ASC")
  fun getAllAppointments(): Flow<List<Appointment>>

  @Query("SELECT * FROM appointments WHERE appointment_date >= :startTime AND appointment_date <= :endTime ORDER BY time_slot ASC")
  fun getAppointmentsForDateRange(startTime: Long, endTime: Long): Flow<List<Appointment>>

  @Query("SELECT * FROM appointments WHERE patient_id = :patientId ORDER BY appointment_date DESC")
  fun getAppointmentsForPatient(patientId: Long): Flow<List<Appointment>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAppointment(appointment: Appointment): Long

  @Query("UPDATE appointments SET status = :status WHERE appointment_id = :id")
  suspend fun updateStatus(id: Long, status: String)

  @Delete
  suspend fun deleteAppointment(appointment: Appointment)
}
