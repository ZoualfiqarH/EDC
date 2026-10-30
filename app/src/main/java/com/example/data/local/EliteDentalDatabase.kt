package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Appointment
import com.example.data.model.ClinicalVisit
import com.example.data.model.DentalService
import com.example.data.model.InventoryItem
import com.example.data.model.Invoice
import com.example.data.model.Patient

import com.example.data.model.InstallmentPayment

@Database(
  entities = [
    Patient::class,
    ClinicalVisit::class,
    InventoryItem::class,
    DentalService::class,
    Invoice::class,
    Appointment::class,
    InstallmentPayment::class
  ],
  version = 2,
  exportSchema = false
)
abstract class EliteDentalDatabase : RoomDatabase() {
  abstract fun patientDao(): PatientDao
  abstract fun clinicalVisitDao(): ClinicalVisitDao
  abstract fun inventoryDao(): InventoryDao
  abstract fun dentalServiceDao(): DentalServiceDao
  abstract fun invoiceDao(): InvoiceDao
  abstract fun appointmentDao(): AppointmentDao
  abstract fun installmentPaymentDao(): InstallmentPaymentDao

  companion object {
    @Volatile
    private var INSTANCE: EliteDentalDatabase? = null

    fun getDatabase(context: Context): EliteDentalDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          EliteDentalDatabase::class.java,
          "elite_dental_clinic.db"
        )
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
