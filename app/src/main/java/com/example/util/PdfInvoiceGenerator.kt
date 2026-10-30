package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Doctor
import com.example.data.model.Invoice
import com.example.ui.theme.CurrencyDisplayMode
import com.example.ui.theme.CurrencyFormatter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfInvoiceGenerator {

  fun generateAndShareInvoice(
    context: Context,
    invoice: Invoice,
    currencyMode: CurrencyDisplayMode = CurrencyDisplayMode.USD,
    isArabic: Boolean = false
  ) {
    try {
      val doc = PdfDocument()
      // Standard A4 page: 595 x 842 points
      val pageWidth = 595
      val pageHeight = 842
      val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
      val page = doc.startPage(pageInfo)
      val canvas: Canvas = page.canvas

      val paint = Paint()
      paint.isAntiAlias = true

      // 1. Top Decorative Header Banner
      paint.color = Color.rgb(0, 121, 107) // Dental Teal
      canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, paint)

      // Draw Logo in top-right of banner
      try {
        val logoBitmap = android.graphics.BitmapFactory.decodeResource(context.resources, com.example.R.drawable.clinic_logo)
        if (logoBitmap != null) {
          val logoBoxPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
          }
          val logoBoxLeft = pageWidth - 105f
          val logoBoxTop = 10f
          val logoBoxRight = pageWidth - 35f
          val logoBoxBottom = 80f
          canvas.drawRoundRect(logoBoxLeft, logoBoxTop, logoBoxRight, logoBoxBottom, 8f, 8f, logoBoxPaint)

          val destRect = android.graphics.RectF(logoBoxLeft + 4f, logoBoxTop + 4f, logoBoxRight - 4f, logoBoxBottom - 4f)
          canvas.drawBitmap(logoBitmap, null, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
        }
      } catch (_: Exception) {
        // Fallback silently if image cannot be decoded
      }

      // Clinic Title in White
      paint.color = Color.WHITE
      paint.textSize = 20f
      paint.isFakeBoldText = true
      val clinicTitle = "ELITE DENTAL CLINIC"
      canvas.drawText(clinicTitle, 36f, 38f, paint)

      if (isArabic) {
        paint.textSize = 13f
        canvas.drawText("عيادة النخبة لطب الأسنان", 36f, 54f, paint)
      }

      // Doctors Subtitle
      paint.textSize = 11f
      paint.isFakeBoldText = false
      val doctorsSub = if (isArabic) "إشراف: د. هيا حسن & د. جعفر عطية" else "Managed by Dr. Haya Hasan & Dr. Jafar Ateyh"
      canvas.drawText(doctorsSub, 36f, if (isArabic) 72f else 62f, paint)

      // 2. Invoice Meta Info Box
      var y = 120f
      paint.color = Color.rgb(240, 244, 248)
      canvas.drawRoundRect(36f, y, pageWidth - 36f, y + 80f, 12f, 12f, paint)

      paint.color = Color.rgb(33, 33, 33)
      paint.textSize = 14f
      paint.isFakeBoldText = true
      val invNumberText = "INVOICE #${invoice.invoiceId}"
      canvas.drawText(invNumberText, 50f, y + 28f, paint)

      paint.textSize = 11f
      paint.isFakeBoldText = false
      paint.color = Color.rgb(100, 116, 139)
      val dateFormatted = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).format(Date(invoice.timestamp))
      canvas.drawText("Issue Date: $dateFormatted", 50f, y + 48f, paint)
      val doctorObj = Doctor.fromName(invoice.assignedDoctor)
      canvas.drawText("Supervising Doctor: ${doctorObj.displayNameEn} (${doctorObj.displayNameAr})", 50f, y + 66f, paint)

      // Right-aligned Patient Details
      paint.color = Color.rgb(33, 33, 33)
      paint.textSize = 13f
      paint.isFakeBoldText = true
      canvas.drawText("Patient: ${invoice.patientName}", 340f, y + 28f, paint)

      paint.textSize = 11f
      paint.isFakeBoldText = false
      paint.color = Color.rgb(100, 116, 139)
      canvas.drawText("Patient ID: #${invoice.patientId}", 340f, y + 48f, paint)
      canvas.drawText("Payment Method: ${invoice.paymentMethod}", 340f, y + 66f, paint)

      y += 110f

      // 3. Treatment Services Table Header
      paint.color = Color.rgb(226, 232, 240)
      canvas.drawRoundRect(36f, y, pageWidth - 36f, y + 30f, 6f, 6f, paint)

      paint.color = Color.rgb(15, 23, 42)
      paint.textSize = 11f
      paint.isFakeBoldText = true
      canvas.drawText("DESCRIPTION / SERVICE", 50f, y + 20f, paint)
      canvas.drawText("AMOUNT (USD)", 360f, y + 20f, paint)
      canvas.drawText("AMOUNT (SYP)", 460f, y + 20f, paint)

      y += 45f

      // Service items
      paint.color = Color.rgb(51, 65, 85)
      paint.textSize = 11f
      paint.isFakeBoldText = false

      // Split multiple services if combined
      val servicesList = invoice.serviceName.split("+").map { it.trim() }
      for (service in servicesList) {
        val safeService = if (service.length > 45) service.substring(0, 42) + "..." else service
        canvas.drawText("• $safeService", 50f, y, paint)
        y += 20f
      }

      // Base Services Price Line
      canvas.drawText("Base Services Total", 50f, y, paint)
      canvas.drawText(String.format(Locale.US, "$%.2f", invoice.servicePrice), 360f, y, paint)
      canvas.drawText(String.format(Locale.US, "%,.0f SYP", invoice.servicePrice * invoice.exchangeRateSyp), 460f, y, paint)
      y += 20f

      // Doctor's Fees Line
      if (invoice.doctorFees > 0.0) {
        canvas.drawText("Additional Doctor Fees (أتعاب الطبيب)", 50f, y, paint)
        canvas.drawText(String.format(Locale.US, "$%.2f", invoice.doctorFees), 360f, y, paint)
        canvas.drawText(String.format(Locale.US, "%,.0f SYP", invoice.doctorFees * invoice.exchangeRateSyp), 460f, y, paint)
        y += 20f
      }

      // Divider line
      y += 10f
      paint.color = Color.rgb(203, 213, 225)
      canvas.drawLine(36f, y, pageWidth - 36f, y, paint)
      y += 20f

      // 4. Financial Totals & Installment Balance Summary Box
      paint.color = Color.rgb(248, 250, 252)
      canvas.drawRoundRect(280f, y, pageWidth - 36f, y + 130f, 10f, 10f, paint)

      paint.color = Color.rgb(15, 23, 42)
      paint.textSize = 13f
      paint.isFakeBoldText = true
      canvas.drawText("Total Invoice Amount:", 300f, y + 28f, paint)
      canvas.drawText(String.format(Locale.US, "$%.2f", invoice.totalAmount), 460f, y + 28f, paint)

      paint.textSize = 11f
      paint.isFakeBoldText = false
      paint.color = Color.rgb(0, 121, 107)
      canvas.drawText("= " + String.format(Locale.US, "%,.0f SYP", invoice.totalAmountSyp), 460f, y + 46f, paint)

      paint.color = Color.rgb(46, 125, 50)
      paint.textSize = 12f
      paint.isFakeBoldText = true
      canvas.drawText("Amount Paid Today:", 300f, y + 74f, paint)
      canvas.drawText(String.format(Locale.US, "$%.2f", invoice.amountPaidToday), 460f, y + 74f, paint)

      paint.color = if (invoice.remainingBalance > 0.01) Color.rgb(198, 40, 40) else Color.rgb(46, 125, 50)
      paint.textSize = 13f
      paint.isFakeBoldText = true
      val balanceLabel = if (invoice.remainingBalance > 0.01) "Remaining Balance Due:" else "Status: Fully Settled ✓"
      canvas.drawText(balanceLabel, 300f, y + 104f, paint)
      if (invoice.remainingBalance > 0.01) {
        canvas.drawText(String.format(Locale.US, "$%.2f", invoice.remainingBalance), 460f, y + 104f, paint)
        paint.textSize = 10f
        canvas.drawText("(${String.format(Locale.US, "%,.0f SYP", invoice.remainingBalanceSyp)})", 460f, y + 120f, paint)
      }

      y += 150f

      // 5. Notes section if present
      if (invoice.notes.isNotBlank()) {
        paint.color = Color.rgb(71, 85, 105)
        paint.textSize = 10f
        paint.isFakeBoldText = true
        canvas.drawText("Notes & Clinical Remarks:", 36f, y, paint)
        paint.isFakeBoldText = false
        val safeNotes = if (invoice.notes.length > 80) invoice.notes.substring(0, 77) + "..." else invoice.notes
        canvas.drawText(safeNotes, 36f, y + 16f, paint)
        y += 40f
      }

      // 6. Official Clinic Footer
      paint.color = Color.rgb(148, 163, 184)
      canvas.drawLine(36f, pageHeight - 60f, pageWidth - 36f, pageHeight - 60f, paint)

      paint.textSize = 10f
      paint.color = Color.rgb(71, 85, 105)
      paint.isFakeBoldText = false
      val footerText = "Syria,Latakia,Jableh,Al-Amara Square Tel: 0997220888"
      val textWidth = paint.measureText(footerText)
      val xPos = (pageWidth - textWidth) / 2f
      canvas.drawText(footerText, xPos, pageHeight - 40f, paint)

      doc.finishPage(page)

      // Save to cacheDir / invoices
      val invoicesDir = File(context.cacheDir, "invoices")
      if (!invoicesDir.exists()) {
        invoicesDir.mkdirs()
      }
      val pdfFile = File(invoicesDir, "Invoice_${invoice.invoiceId}.pdf")
      val outputStream = FileOutputStream(pdfFile)
      doc.writeTo(outputStream)
      outputStream.close()
      doc.close()

      // Share via Android FileProvider
      val contentUri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        pdfFile
      )

      val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, contentUri)
        putExtra(Intent.EXTRA_SUBJECT, "Invoice #${invoice.invoiceId} - ${invoice.patientName}")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(shareIntent, if (isArabic) "مشاركة / طباعة الفاتورة" else "Share / Print Invoice")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)

    } catch (e: Exception) {
      e.printStackTrace()
      Toast.makeText(context, "Error generating PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
  }
}
