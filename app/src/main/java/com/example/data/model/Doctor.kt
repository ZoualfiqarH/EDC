package com.example.data.model

enum class Doctor(
  val displayNameEn: String,
  val displayNameAr: String,
  val specialtyEn: String,
  val specialtyAr: String
) {
  HAYA_HASAN(
    displayNameEn = "Dr. Haya Hasan",
    displayNameAr = "د. هيا حسن",
    specialtyEn = "Orthodontics & Aesthetic Dentistry",
    specialtyAr = "تقويم وتجميل الأسنان"
  ),
  JAFAR_ATEYH(
    displayNameEn = "Dr. Jafar Ateyh",
    displayNameAr = "د. جعفر عطية",
    specialtyEn = "Oral Surgery & Implantology",
    specialtyAr = "جراحة وزراعة الأسنان"
  );

  val displayName: String get() = displayNameEn

  fun getLocalizedName(isArabic: Boolean): String = if (isArabic) displayNameAr else displayNameEn

  fun getLocalizedSpecialty(isArabic: Boolean): String = if (isArabic) specialtyAr else specialtyEn

  companion object {
    fun fromName(name: String?): Doctor {
      if (name == null) return HAYA_HASAN
      val trimmed = name.trim()
      return entries.find { doctor ->
        doctor.displayNameEn.equals(trimmed, ignoreCase = true) ||
        doctor.displayNameAr.equals(trimmed, ignoreCase = true) ||
        doctor.name.equals(trimmed, ignoreCase = true) ||
        trimmed.contains("Haya", ignoreCase = true) ||
        trimmed.contains("هيا", ignoreCase = true)
      } ?: if (trimmed.contains("Jafar", ignoreCase = true) || trimmed.contains("جعفر", ignoreCase = true)) {
        JAFAR_ATEYH
      } else {
        HAYA_HASAN
      }
    }
  }
}
