package com.example.data.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

/**
 * Encapsulates the exact financial split algorithm between Elite Dental Clinic
 * and the two attending doctors (Dr. Haya Hasan & Dr. Jafar Ateyh),
 * with dual USD and SYP (Syrian Pound) multi-currency and installment tracking.
 *
 * Specific split model:
 * - Clinic Share: 30% of total invoice
 * - Remaining Balance (70%): Distributed between two doctors:
 *     - Treating Doctor (Primary): 60% of remaining 70% (i.e. 42% of total)
 *     - Secondary Doctor: 40% of remaining 70% (i.e. 28% of total)
 */
data class FinancialBreakdown(
  val totalAmount: Double,
  val clinicOverhead: Double, // 30% of Total Amount -> Clinic Box / Overhead
  val netDoctorPool: Double,  // 70% of Total Amount -> Net Doctor Pool
  val hayaShare: Double,      // 42% if assigned to Haya, 28% if assigned to Jafar
  val jafarShare: Double,     // 42% if assigned to Jafar, 28% if assigned to Haya
  val assignedDoctor: Doctor,
  val exchangeRateSyp: Double = 14500.0,
  val amountPaidToday: Double = totalAmount,
  val remainingBalance: Double = (totalAmount - amountPaidToday).coerceAtLeast(0.0)
) {
  val clinicShare: Double get() = clinicOverhead

  fun formattedTotal(): String = String.format(Locale.US, "$%.2f", totalAmount)
  fun formattedClinicOverhead(): String = String.format(Locale.US, "$%.2f", clinicOverhead)
  fun formattedNetDoctorPool(): String = String.format(Locale.US, "$%.2f", netDoctorPool)
  fun formattedHayaShare(): String = String.format(Locale.US, "$%.2f", hayaShare)
  fun formattedJafarShare(): String = String.format(Locale.US, "$%.2f", jafarShare)
  fun formattedPaidToday(): String = String.format(Locale.US, "$%.2f", amountPaidToday)
  fun formattedRemainingBalance(): String = String.format(Locale.US, "$%.2f", remainingBalance)

  // Syrian Pound (SYP) Formatted Equivalents
  fun formattedTotalSyp(): String = String.format(Locale.US, "%,.0f SYP", totalAmount * exchangeRateSyp)
  fun formattedClinicOverheadSyp(): String = String.format(Locale.US, "%,.0f SYP", clinicOverhead * exchangeRateSyp)
  fun formattedNetDoctorPoolSyp(): String = String.format(Locale.US, "%,.0f SYP", netDoctorPool * exchangeRateSyp)
  fun formattedHayaShareSyp(): String = String.format(Locale.US, "%,.0f SYP", hayaShare * exchangeRateSyp)
  fun formattedJafarShareSyp(): String = String.format(Locale.US, "%,.0f SYP", jafarShare * exchangeRateSyp)
  fun formattedPaidTodaySyp(): String = String.format(Locale.US, "%,.0f SYP", amountPaidToday * exchangeRateSyp)
  fun formattedRemainingBalanceSyp(): String = String.format(Locale.US, "%,.0f SYP", remainingBalance * exchangeRateSyp)
}

object FinancialDistributionCalculator {

  /**
   * Rounds a double value to exactly 2 decimal places with HALF_UP precision.
   */
  fun roundToTwoDecimals(value: Double): Double {
    return BigDecimal(value.toString())
      .setScale(2, RoundingMode.HALF_UP)
      .toDouble()
  }

  /**
   * Automated Financial Distribution Algorithm:
   * 1. Clinic Share Deduction:
   *    Clinic Share = Total Amount * 0.30 (30%)
   *    Doctor Pool = Total Amount * 0.70 (70%)
   *
   * 2. Doctor Distribution (Based on Treating / Primary Doctor):
   *    - Primary Treating Doctor = 60% of Doctor Pool (42% of total invoice)
   *    - Secondary Doctor = 40% of Doctor Pool (28% of total invoice)
   *
   * 3. Decimal Precision & Funds Conservation:
   *    Accurately rounds values to 2 decimal places in USD, ensuring:
   *    clinicShare + primaryShare + secondaryShare == totalAmount
   */
  fun calculate(
    totalAmount: Double,
    assignedDoctor: Doctor,
    exchangeRateSyp: Double = 14500.0,
    amountPaidToday: Double = totalAmount
  ): FinancialBreakdown {
    val sanitizedTotal = if (totalAmount < 0.0) 0.0 else roundToTwoDecimals(totalAmount)
    val sanitizedPaid = if (amountPaidToday < 0.0) 0.0 else roundToTwoDecimals(amountPaidToday).coerceAtMost(sanitizedTotal)
    val remaining = roundToTwoDecimals((sanitizedTotal - sanitizedPaid).coerceAtLeast(0.0))

    val clinicShare = roundToTwoDecimals(sanitizedTotal * 0.30)
    val doctorPool = roundToTwoDecimals(sanitizedTotal * 0.70)

    val primaryDoctorShare = roundToTwoDecimals(doctorPool * 0.60) // 42% of total
    var secondaryDoctorShare = roundToTwoDecimals(doctorPool * 0.40) // 28% of total

    // Ensure exact decimal conservation: clinic + primary + secondary == total
    val allocated = clinicShare + primaryDoctorShare + secondaryDoctorShare
    val discrepancy = roundToTwoDecimals(sanitizedTotal - allocated)
    if (discrepancy != 0.0) {
      secondaryDoctorShare = roundToTwoDecimals(secondaryDoctorShare + discrepancy)
    }

    val (hayaShare, jafarShare) = when (assignedDoctor) {
      Doctor.HAYA_HASAN -> Pair(primaryDoctorShare, secondaryDoctorShare)
      Doctor.JAFAR_ATEYH -> Pair(secondaryDoctorShare, primaryDoctorShare)
    }

    return FinancialBreakdown(
      totalAmount = sanitizedTotal,
      clinicOverhead = clinicShare,
      netDoctorPool = doctorPool,
      hayaShare = hayaShare,
      jafarShare = jafarShare,
      assignedDoctor = assignedDoctor,
      exchangeRateSyp = exchangeRateSyp,
      amountPaidToday = sanitizedPaid,
      remainingBalance = remaining
    )
  }
}
