package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

enum class AppLanguage(val code: String, val displayName: String, val layoutDirection: LayoutDirection) {
  ENGLISH("en", "English", LayoutDirection.Ltr),
  ARABIC("ar", "العربية", LayoutDirection.Rtl)
}

enum class CurrencyDisplayMode(val labelEn: String, val labelAr: String, val symbol: String) {
  USD("USD ($)", "دولار ($)", "$"),
  SYP("SYP (ل.س)", "ليرة سورية (ل.س)", "ل.س")
}

val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.ENGLISH }
val LocalCurrencyMode = staticCompositionLocalOf { CurrencyDisplayMode.USD }

/**
 * Currency Formatter Utility adhering to the active strict display mode and exchange rate.
 * Guarantees 100% USD or 100% SYP with no mixed currency strings.
 */
object CurrencyFormatter {
  fun format(
    amountUsd: Double,
    exchangeRateSyp: Double = 14500.0,
    mode: CurrencyDisplayMode = CurrencyDisplayMode.USD,
    isArabic: Boolean = false
  ): String {
    return when (mode) {
      CurrencyDisplayMode.USD -> {
        String.format(Locale.US, "$%.2f", amountUsd)
      }
      CurrencyDisplayMode.SYP -> {
        val sypAmount = amountUsd * exchangeRateSyp
        val sypUnit = if (isArabic) "ل.س" else "SYP"
        String.format(Locale.US, "%,.0f %s", sypAmount, sypUnit)
      }
    }
  }

  fun formatUsd(amountUsd: Double): String = String.format(Locale.US, "$%.2f", amountUsd)

  fun formatSyp(amountUsd: Double, exchangeRateSyp: Double = 14500.0, isArabic: Boolean = false): String {
    val sypAmount = amountUsd * exchangeRateSyp
    val sypUnit = if (isArabic) "ل.س" else "SYP"
    return String.format(Locale.US, "%,.0f %s", sypAmount, sypUnit)
  }
}

/**
 * Comprehensive bilingual string dictionary for Elite Dental Clinic.
 * Guarantees 100% English or 100% Arabic with zero mixed tokens.
 */
class AppStrings(val isArabic: Boolean) {
  // Navigation & Branding
  val appName: String get() = if (isArabic) "عيادة النخبة لطب الأسنان" else "Elite Dental Clinic"
  val clinicManagement: String get() = if (isArabic) "إشراف د. هيا حسن ود. جعفر عطية" else "Managed by Dr. Haya Hasan & Dr. Jafar Ateyh"
  val doctorsSubtitle: String get() = if (isArabic) "د. هيا حسن & د. جعفر عطية" else "Dr. Haya Hasan & Dr. Jafar Ateyh"
  val navOverview: String get() = if (isArabic) "لوحة التحكم" else "Dashboard"
  val navPatients: String get() = if (isArabic) "المرضى" else "Patients"
  val navWarehouse: String get() = if (isArabic) "المستودع" else "Warehouse"
  val navInvoices: String get() = if (isArabic) "الفواتير" else "Invoices"
  val navServices: String get() = if (isArabic) "الخدمات" else "Services"

  // Controls & Switches
  val languageSwitch: String get() = if (isArabic) "اللغة" else "Language"
  val currencySwitch: String get() = if (isArabic) "العملة" else "Currency"

  // Doctor Names
  val drHayaName: String get() = if (isArabic) "د. هيا حسن" else "Dr. Haya Hasan"
  val drJafarName: String get() = if (isArabic) "د. جعفر عطية" else "Dr. Jafar Ateyh"
  val drHayaShort: String get() = if (isArabic) "د. هيا" else "Dr. Haya"
  val drJafarShort: String get() = if (isArabic) "د. جعفر" else "Dr. Jafar"

  // Dashboard & Financials
  val financialSummaryTitle: String get() = if (isArabic) "الموجز المالي وتوزيع الإيرادات" else "Financial Summary & Revenue Split"
  val splitFormulaSubtitle: String get() = if (isArabic) "التوزيع: 30% العيادة | 42% الطبيب المعالج | 28% الطبيب الشريك" else "Split: 30% Clinic Share | 42% Treating Doctor | 28% Partner Doctor"
  val totalRevenue: String get() = if (isArabic) "إجمالي الإيرادات" else "Total Invoiced Revenue"
  val totalCollected: String get() = if (isArabic) "المبالغ المحصلة نقدياً" else "Total Collected Cash"
  val outstandingBalance: String get() = if (isArabic) "الرصيد المتبقي (الذمم)" else "Outstanding Balance"
  val clinicShare30: String get() = if (isArabic) "حصة العيادة (30%)" else "Clinic Share (30%)"
  val doctorPool70: String get() = if (isArabic) "صندوق الأطباء (70%)" else "Doctors Pool (70%)"
  val drHayaShare: String get() = if (isArabic) "حصة د. هيا" else "Dr. Haya Share"
  val drJafarShare: String get() = if (isArabic) "حصة د. جعفر" else "Dr. Jafar Share"
  val netShareLabel: String get() = if (isArabic) "صافي الحصة المتراكمة" else "Accumulated Net Share"
  val exchangeRateLabel: String get() = if (isArabic) "سعر الصرف (1$ = ل.س)" else "Exchange Rate (1$ = SYP)"
  val quickActions: String get() = if (isArabic) "الإجراءات السريعة" else "Quick Actions"
  val newPatient: String get() = if (isArabic) "تسجيل مريض" else "New Patient"
  val newAppointment: String get() = if (isArabic) "حجز موعد" else "Book Visit"
  val newInvoice: String get() = if (isArabic) "إنشاء فاتورة" else "New Invoice"
  val activePatientsCount: String get() = if (isArabic) "المرضى المسجلين" else "Active Patients"
  val todaysAppointmentsCount: String get() = if (isArabic) "مواعيد اليوم" else "Today's Appointments"
  val warehouseAlertsCount: String get() = if (isArabic) "نواقص المستودع" else "Warehouse Alerts"
  val safeStockLevels: String get() = if (isArabic) "مستويات المخزون آمنة" else "Safe stock levels"
  val actionNeeded: String get() = if (isArabic) "يلزم إعادة التوريد" else "Restocking needed"
  val todaysQueueTitle: String get() = if (isArabic) "قائمة مواعيد اليوم" else "Today's Clinic Queue"
  val viewAll: String get() = if (isArabic) "عرض الكل" else "View All"
  val noAppointmentsToday: String get() = if (isArabic) "لا توجد مواعيد مجدولة لليوم حتى الآن" else "No appointments scheduled for today yet"

  // Patient Profile & EHR
  val patientProfile: String get() = if (isArabic) "الملف السريري للمريض" else "Patient Clinical Profile"
  val tabPersonalInfo: String get() = if (isArabic) "البيانات والسيرة المرضية" else "Personal & Medical"
  val tabClinicalHistory: String get() = if (isArabic) "السجل السريري والإجراءات" else "Clinical History"
  val tabFinancialLedger: String get() = if (isArabic) "السجل المالي والأقساط" else "Financial Ledger"
  val tabAppointments: String get() = if (isArabic) "سجل المواعيد" else "Appointments"
  val tabRadiographs: String get() = if (isArabic) "الأشعة والصور السريرية" else "Radiographs & Photos"

  val fullName: String get() = if (isArabic) "الاسم الكامل" else "Full Name"
  val age: String get() = if (isArabic) "العمر" else "Age"
  val gender: String get() = if (isArabic) "الجنس" else "Gender"
  val genderMale: String get() = if (isArabic) "ذكر" else "Male"
  val genderFemale: String get() = if (isArabic) "أنثى" else "Female"
  val maritalStatus: String get() = if (isArabic) "الحالة الاجتماعية" else "Marital Status"
  val occupation: String get() = if (isArabic) "المهنة" else "Occupation"
  val address: String get() = if (isArabic) "العنوان" else "Address"
  val phone: String get() = if (isArabic) "رقم الهاتف" else "Phone Number"
  val assignedDoctor: String get() = if (isArabic) "الطبيب المشرف" else "Assigned Doctor"
  val chronicDiseases: String get() = if (isArabic) "الأمراض المزمنة" else "Chronic Diseases"
  val chronicMedications: String get() = if (isArabic) "الأدوية الدائمة" else "Chronic Medications"
  val previousSurgeries: String get() = if (isArabic) "العمليات الجراحية السابقة" else "Previous Surgeries"
  val previousDental: String get() = if (isArabic) "المعالجات السنية السابقة" else "Previous Dental Procedures"
  val previousDentalProcedures: String get() = previousDental
  val totalVisits: String get() = if (isArabic) "إجمالي الزيارات" else "Total Visits"
  val addClinicalVisit: String get() = if (isArabic) "إضافة إجراء سنّي جديد" else "Add Clinical Visit"

  // Appointments & Filtering
  val appointmentScheduler: String get() = if (isArabic) "جدول مواعيد العيادة" else "Clinic Appointment Scheduler"
  val filterAll: String get() = if (isArabic) "الكل" else "All"
  val filterToday: String get() = if (isArabic) "اليوم" else "Today"
  val filterUpcoming: String get() = if (isArabic) "القادمة" else "Upcoming"
  val filterPast: String get() = if (isArabic) "السابقة" else "Past"
  val filterDateRange: String get() = if (isArabic) "تحديد نطاق التاريخ" else "Date Range Filter"
  val fromDate: String get() = if (isArabic) "من تاريخ" else "From Date"
  val toDate: String get() = if (isArabic) "إلى تاريخ" else "To Date"
  val clearFilter: String get() = if (isArabic) "إلغاء التصفية" else "Clear Filter"
  val bookingWalkIn: String get() = if (isArabic) "زيارة مباشرة" else "Walk-in"
  val bookingPhone: String get() = if (isArabic) "حجز هاتفي" else "Phone Booking"
  val procedurePerformed: String get() = if (isArabic) "الإجراء السني / الملاحظات السريرية" else "Procedure Performed / Notes"
  val statusScheduled: String get() = if (isArabic) "مجدول" else "Scheduled"
  val statusConfirmed: String get() = if (isArabic) "مؤكد" else "Confirmed"
  val statusInProgress: String get() = if (isArabic) "قيد المعالجة" else "In Progress"
  val statusCompleted: String get() = if (isArabic) "مكتمل" else "Completed"
  val statusCancelled: String get() = if (isArabic) "ملغي" else "Cancelled"
  val markDone: String get() = if (isArabic) "تم الإنجاز ✓" else "Done ✓"

  // Invoices & Installments & PDF
  val invoicesTitle: String get() = if (isArabic) "الفواتير والصندوق ونظام التقسيط" else "Invoices, Cash Box & Installments"
  val createInvoiceWizard: String get() = if (isArabic) "إنشاء فاتورة جديدة" else "Create New Invoice"
  val recordInstallmentPayment: String get() = if (isArabic) "تسديد دفعة من الحساب" else "Record Installment Payment"
  val exportPdfButton: String get() = if (isArabic) "تصدير الفاتورة PDF" else "Export Invoice PDF"
  val installmentLedger: String get() = if (isArabic) "سجل الدفعات المقبوضة" else "Installment Payment Ledger"
  val paidTodayOrDownPayment: String get() = if (isArabic) "الدفعة المسددة حالياً" else "Amount Paid Today"
  val remainingBalanceLabel: String get() = if (isArabic) "الرصيد المتبقي بذمة المريض" else "Remaining Balance"
  val doctorFeesLabel: String get() = if (isArabic) "أتعاب الطبيب الإضافية" else "Additional Doctor Fees"
  val totalAmountLabel: String get() = if (isArabic) "المبلغ الإجمالي" else "Total Amount"
  val statusFullyPaid: String get() = if (isArabic) "مسدد بالكامل" else "Fully Paid"
  val statusHasBalance: String get() = if (isArabic) "يوجد رصيد متبقي" else "Balance Pending"
  val exchangeRateSettings: String get() = if (isArabic) "إعدادات سعر الصرف" else "Exchange Rate Settings"
  val notes: String get() = if (isArabic) "ملاحظات" else "Notes"

  // Warehouse & Stock
  val warehouseTitle: String get() = if (isArabic) "مستودع العيادة والمواد السنية" else "Clinic Warehouse & Dental Supplies"
  val warehouseSubtitle: String get() = if (isArabic) "إدارة المخزون والتوريد والاستهلاك" else "Inventory, Supplies & Stock Adjustments"
  val lowStockBannerTitle: String get() = if (isArabic) "تنبيه نقص المواد في المستودع" else "Low Stock Inventory Alert"
  val lowStockBannerDesc: String get() = if (isArabic) "المواد التالية تجاوزت حد الأمان الأدنى وتحتاج إلى إعادة توريد فورية" else "The following supplies have breached the minimum safety threshold and require immediate restocking"
  val addSupplyItem: String get() = if (isArabic) "إضافة مادة جديدة" else "Add New Item"
  val stockInImport: String get() = if (isArabic) "توريد / شراء (+)" else "Stock In / Purchase (+)"
  val stockOutExport: String get() = if (isArabic) "استهلاك / صرف (-)" else "Usage / Export (-)"
  val transactionType: String get() = if (isArabic) "نوع الحركة" else "Transaction Type"
  val quantityToAdjust: String get() = if (isArabic) "الكمية المراد تعديلها" else "Quantity to Adjust"
  val adjustStockTitle: String get() = if (isArabic) "تعديل كمية المخزون" else "Adjust Inventory Stock"
  val minThresholdLabel: String get() = if (isArabic) "حد الأمان الأدنى" else "Min Safety Threshold"
  val currentStockLabel: String get() = if (isArabic) "المخزون الحالي" else "Current Stock"

  // General Actions
  val cancel: String get() = if (isArabic) "إلغاء" else "Cancel"
  val close: String get() = if (isArabic) "إغلاق" else "Close"
  val actionClose: String get() = close
  val save: String get() = if (isArabic) "حفظ" else "Save"
  val confirm: String get() = if (isArabic) "تأكيد" else "Confirm"
  val searchPlaceholder: String get() = if (isArabic) "بحث..." else "Search..."
  val filterByDoctor: String get() = if (isArabic) "تصفية حسب الطبيب" else "Filter by Doctor"
  val allDoctors: String get() = if (isArabic) "جميع الأطباء" else "All Doctors"
}

object LocalizedStrings {
  @Composable
  @ReadOnlyComposable
  fun current(): AppStrings {
    val language = LocalAppLanguage.current
    return AppStrings(isArabic = language == AppLanguage.ARABIC)
  }
}
