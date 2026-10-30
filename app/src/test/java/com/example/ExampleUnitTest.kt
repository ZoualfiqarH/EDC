package com.example

import com.example.data.model.Doctor
import com.example.data.model.FinancialDistributionCalculator
import com.example.data.model.InventoryItem
import com.example.ui.theme.CurrencyDisplayMode
import com.example.ui.theme.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun testFinancialSplit_WhenPatientBelongsToHaya() {
    val totalAmount = 1000.0
    val result = FinancialDistributionCalculator.calculate(totalAmount, Doctor.HAYA_HASAN)

    // 1. Clinic Share = 30% of Total Amount
    assertEquals(300.0, result.clinicOverhead, 0.001)

    // 2. Doctor Pool = 70% of Total Amount
    assertEquals(700.0, result.netDoctorPool, 0.001)

    // 3. Since patient belongs to Dr. Haya (Primary / Treating):
    // Primary Haya takes 60% of Doctor Pool (0.60 * 700 = 420.0, 42% of total)
    // Secondary Jafar takes 40% of Doctor Pool (0.40 * 700 = 280.0, 28% of total)
    assertEquals(420.0, result.hayaShare, 0.001)
    assertEquals(280.0, result.jafarShare, 0.001)

    // Verification of exact conservation of funds
    assertEquals(totalAmount, result.clinicOverhead + result.hayaShare + result.jafarShare, 0.001)
  }

  @Test
  fun testFinancialSplit_WhenPatientBelongsToJafar() {
    val totalAmount = 1000.0
    val result = FinancialDistributionCalculator.calculate(totalAmount, Doctor.JAFAR_ATEYH)

    // 1. Clinic Share = 30% of Total Amount
    assertEquals(300.0, result.clinicOverhead, 0.001)

    // 2. Doctor Pool = 70% of Total Amount
    assertEquals(700.0, result.netDoctorPool, 0.001)

    // 3. Since patient belongs to Dr. Jafar (Primary / Treating):
    // Primary Jafar takes 60% of Doctor Pool (0.60 * 700 = 420.0, 42% of total)
    // Secondary Haya takes 40% of Doctor Pool (0.40 * 700 = 280.0, 28% of total)
    assertEquals(420.0, result.jafarShare, 0.001)
    assertEquals(280.0, result.hayaShare, 0.001)

    // Verification of exact conservation of funds
    assertEquals(totalAmount, result.clinicOverhead + result.hayaShare + result.jafarShare, 0.001)
  }

  @Test
  fun testMultiCurrencyAndInstallments() {
    val totalAmountUsd = 1200.0
    val exchangeRate = 14500.0
    val downPaymentUsd = 500.0

    val breakdown = FinancialDistributionCalculator.calculate(
      totalAmount = totalAmountUsd,
      assignedDoctor = Doctor.HAYA_HASAN,
      exchangeRateSyp = exchangeRate,
      amountPaidToday = downPaymentUsd
    )

    // Total USD & SYP
    assertEquals(1200.0, breakdown.totalAmount, 0.001)
    assertEquals(17400000.0, breakdown.totalAmount * exchangeRate, 0.001)

    // Down payment & remaining balance
    assertEquals(500.0, breakdown.amountPaidToday, 0.001)
    assertEquals(700.0, breakdown.remainingBalance, 0.001)
    assertEquals(10150000.0, breakdown.remainingBalance * exchangeRate, 0.001)

    // Clinic Share (30%) & Doctor Pool (70%) on Total Amount
    assertEquals(360.0, breakdown.clinicOverhead, 0.001)
    assertEquals(840.0, breakdown.netDoctorPool, 0.001)
    assertEquals(504.0, breakdown.hayaShare, 0.001)
    assertEquals(336.0, breakdown.jafarShare, 0.001)
  }

  @Test
  fun testDecimalPrecisionAndDiscrepancyHandling() {
    val totalAmount = 100.33
    val breakdown = FinancialDistributionCalculator.calculate(totalAmount, Doctor.HAYA_HASAN)
    assertEquals(totalAmount, breakdown.clinicOverhead + breakdown.hayaShare + breakdown.jafarShare, 0.0001)
  }

  @Test
  fun testInventory_LowStockThresholdTrigger() {
    val normalItem = InventoryItem(
      itemId = 1,
      itemName = "Dental Composite A2",
      category = "Restorative",
      currentQuantity = 15.0,
      minimumThreshold = 5,
      unitPrice = 45.0
    )
    assertFalse(normalItem.isLowStock)

    val lowStockItem = InventoryItem(
      itemId = 2,
      itemName = "Lidocaine 2%",
      category = "Anesthesia",
      currentQuantity = 3.0,
      minimumThreshold = 8,
      unitPrice = 35.0
    )
    assertTrue(lowStockItem.isLowStock)
  }

  @Test
  fun testDoctorLocalization() {
    assertEquals("Dr. Haya Hasan", Doctor.HAYA_HASAN.getLocalizedName(false))
    assertEquals("د. هيا حسن", Doctor.HAYA_HASAN.getLocalizedName(true))
    assertEquals("Dr. Jafar Ateyh", Doctor.JAFAR_ATEYH.getLocalizedName(false))
    assertEquals("د. جعفر عطية", Doctor.JAFAR_ATEYH.getLocalizedName(true))

    // Doctor.fromName matching both languages
    assertEquals(Doctor.HAYA_HASAN, Doctor.fromName("د. هيا حسن"))
    assertEquals(Doctor.JAFAR_ATEYH, Doctor.fromName("د. جعفر عطية"))
  }

  @Test
  fun testStrictCurrencyFormatter() {
    val usdOutput = CurrencyFormatter.format(100.0, 14500.0, CurrencyDisplayMode.USD, false)
    assertEquals("$100.00", usdOutput)

    val sypOutputEn = CurrencyFormatter.format(100.0, 14500.0, CurrencyDisplayMode.SYP, false)
    assertEquals("1,450,000 SYP", sypOutputEn)

    val sypOutputAr = CurrencyFormatter.format(100.0, 14500.0, CurrencyDisplayMode.SYP, true)
    assertEquals("1,450,000 ل.س", sypOutputAr)
  }
}
