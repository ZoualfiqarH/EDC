package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A. Patients (Patient File / إضبارة مريض)
 */
@Entity(tableName = "patients")
data class Patient(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "patient_id")
  val patientId: Long = 0,

  @ColumnInfo(name = "full_name")
  val fullName: String,

  @ColumnInfo(name = "age")
  val age: Int,

  @ColumnInfo(name = "gender")
  val gender: String, // Male / Female

  @ColumnInfo(name = "marital_status")
  val maritalStatus: String = "Single",

  @ColumnInfo(name = "occupation")
  val occupation: String = "",

  @ColumnInfo(name = "address")
  val address: String = "",

  @ColumnInfo(name = "phone_number")
  val phoneNumber: String,

  @ColumnInfo(name = "assigned_doctor")
  val assignedDoctor: String, // Dr. Haya Hasan or Dr. Jafar Ateyh

  @ColumnInfo(name = "chronic_diseases")
  val chronicDiseases: String = "None",

  @ColumnInfo(name = "chronic_medications")
  val chronicMedications: String = "None",

  @ColumnInfo(name = "previous_surgeries")
  val previousSurgeries: String = "None",

  @ColumnInfo(name = "previous_dental_procedures")
  val previousDentalProcedures: String = "None",

  @ColumnInfo(name = "total_visits")
  val totalVisits: Int = 0,

  @ColumnInfo(name = "created_at")
  val createdAt: Long = System.currentTimeMillis()
)

/**
 * B. Patient Clinical Records & Visits (الإجراءات السنية السابقة والخاصة بالمريض)
 */
@Entity(tableName = "clinical_visits")
data class ClinicalVisit(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "visit_id")
  val visitId: Long = 0,

  @ColumnInfo(name = "patient_id")
  val patientId: Long,

  @ColumnInfo(name = "procedure_type")
  val procedureType: String,

  @ColumnInfo(name = "visit_date")
  val visitDate: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "cost")
  val cost: Double,

  @ColumnInfo(name = "amount_paid")
  val amountPaid: Double,

  @ColumnInfo(name = "remaining_balance")
  val remainingBalance: Double,

  @ColumnInfo(name = "clinic_share")
  val clinicShare: Double,

  @ColumnInfo(name = "lab_share")
  val labShare: Double = 0.0,

  @ColumnInfo(name = "associated_doctor")
  val associatedDoctor: String,

  @ColumnInfo(name = "radiographs_paths")
  val radiographsPaths: String = "",

  @ColumnInfo(name = "clinical_photos_before_after")
  val clinicalPhotosBeforeAfter: String = "",

  @ColumnInfo(name = "clinical_notes")
  val clinicalNotes: String = ""
)

/**
 * C. Clinic Inventory & Warehouse (المستودع والمواد الطبية)
 */
@Entity(tableName = "inventory_items")
data class InventoryItem(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "item_id")
  val itemId: Long = 0,

  @ColumnInfo(name = "item_name")
  val itemName: String,

  @ColumnInfo(name = "category")
  val category: String, // Anesthesia, Restorative, Surgical, Endodontics, Disposables

  @ColumnInfo(name = "current_quantity")
  val currentQuantity: Double,

  @ColumnInfo(name = "minimum_threshold")
  val minimumThreshold: Int, // Triggers low stock alert when current_quantity <= minimum_threshold

  @ColumnInfo(name = "unit_price")
  val unitPrice: Double,

  @ColumnInfo(name = "unit_measure")
  val unitMeasure: String = "Units"
) {
  val isLowStock: Boolean
    get() = currentQuantity <= minimumThreshold
}

/**
 * D. Treatment Price List (تسعيرة المعالجات)
 */
@Entity(tableName = "treatment_services")
data class DentalService(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "service_id")
  val serviceId: Long = 0,

  @ColumnInfo(name = "service_name")
  val serviceName: String,

  @ColumnInfo(name = "category")
  val category: String,

  @ColumnInfo(name = "standard_price")
  val standardPrice: Double,

  @ColumnInfo(name = "description")
  val description: String = ""
)

/**
 * E. Financials & Invoices (الفواتير والصندوق)
 */
@Entity(tableName = "invoices")
data class Invoice(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "invoice_id")
  val invoiceId: Long = 0,

  @ColumnInfo(name = "patient_id")
  val patientId: Long,

  @ColumnInfo(name = "patient_name")
  val patientName: String,

  @ColumnInfo(name = "assigned_doctor")
  val assignedDoctor: String,

  @ColumnInfo(name = "service_name")
  val serviceName: String, // Multi-selected treatments list

  @ColumnInfo(name = "service_price")
  val servicePrice: Double, // Sum of treatments in USD

  @ColumnInfo(name = "doctor_fees")
  val doctorFees: Double = 0.0, // Additional doctor fees in USD

  @ColumnInfo(name = "total_amount")
  val totalAmount: Double, // Total in USD

  @ColumnInfo(name = "exchange_rate_syp")
  val exchangeRateSyp: Double = 14500.0, // 1 USD = X SYP

  @ColumnInfo(name = "total_amount_syp")
  val totalAmountSyp: Double = totalAmount * exchangeRateSyp,

  @ColumnInfo(name = "amount_paid_today")
  val amountPaidToday: Double = totalAmount, // Down payment or paid today in USD

  @ColumnInfo(name = "remaining_balance")
  val remainingBalance: Double = (totalAmount - amountPaidToday).coerceAtLeast(0.0), // Remaining balance in USD

  @ColumnInfo(name = "clinic_overhead")
  val clinicOverhead: Double,

  @ColumnInfo(name = "doctor_pool")
  val doctorPool: Double,

  @ColumnInfo(name = "haya_share")
  val hayaShare: Double,

  @ColumnInfo(name = "jafar_share")
  val jafarShare: Double,

  @ColumnInfo(name = "payment_method")
  val paymentMethod: String = "Cash",

  @ColumnInfo(name = "timestamp")
  val timestamp: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "notes")
  val notes: String = ""
) {
  val isFullyPaid: Boolean
    get() = remainingBalance <= 0.01

  val remainingBalanceSyp: Double
    get() = remainingBalance * exchangeRateSyp

  val amountPaidTodaySyp: Double
    get() = amountPaidToday * exchangeRateSyp
}

/**
 * F. Installment & Partial Payment Ledger (سجل دفعات وأقساط المرضى)
 */
@Entity(tableName = "installment_payments")
data class InstallmentPayment(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "payment_id")
  val paymentId: Long = 0,

  @ColumnInfo(name = "invoice_id")
  val invoiceId: Long,

  @ColumnInfo(name = "patient_id")
  val patientId: Long,

  @ColumnInfo(name = "patient_name")
  val patientName: String,

  @ColumnInfo(name = "amount_paid_usd")
  val amountPaidUsd: Double,

  @ColumnInfo(name = "amount_paid_syp")
  val amountPaidSyp: Double,

  @ColumnInfo(name = "exchange_rate_syp")
  val exchangeRateSyp: Double,

  @ColumnInfo(name = "payment_date")
  val paymentDate: Long = System.currentTimeMillis(),

  @ColumnInfo(name = "payment_method")
  val paymentMethod: String = "Cash",

  @ColumnInfo(name = "notes")
  val notes: String = ""
)

/**
 * Appointments Entity (مواعيد العيادة)
 */
@Entity(tableName = "appointments")
data class Appointment(
  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "appointment_id")
  val appointmentId: Long = 0,

  @ColumnInfo(name = "patient_id")
  val patientId: Long,

  @ColumnInfo(name = "patient_name")
  val patientName: String,

  @ColumnInfo(name = "phone_number")
  val phoneNumber: String,

  @ColumnInfo(name = "doctor")
  val doctor: String, // Dr. Haya Hasan or Dr. Jafar Ateyh

  @ColumnInfo(name = "appointment_date")
  val appointmentDate: Long,

  @ColumnInfo(name = "time_slot")
  val timeSlot: String, // e.g. "10:30 AM"

  @ColumnInfo(name = "booking_type")
  val bookingType: String, // "Walk-in" or "Phone"

  @ColumnInfo(name = "status")
  val status: String = "Scheduled", // "Scheduled", "Confirmed", "In Progress", "Completed", "Cancelled"

  @ColumnInfo(name = "procedure_notes")
  val procedureNotes: String = ""
)
