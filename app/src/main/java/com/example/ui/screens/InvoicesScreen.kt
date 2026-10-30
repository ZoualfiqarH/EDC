package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DentalService
import com.example.data.model.Doctor
import com.example.data.model.FinancialDistributionCalculator
import com.example.data.model.Invoice
import com.example.data.model.Patient
import com.example.ui.components.DoctorBadge
import com.example.ui.components.FinancialBreakdownCard
import com.example.ui.theme.CurrencyDisplayMode
import com.example.ui.theme.CurrencyFormatter
import com.example.ui.theme.DentalMintSecondary
import com.example.ui.theme.DentalTealPrimary
import com.example.ui.theme.DoctorHayaColor
import com.example.ui.theme.DoctorJafarColor
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalCurrencyMode
import com.example.ui.theme.LocalizedStrings
import com.example.ui.theme.MedicalAlert
import com.example.ui.viewmodel.ClinicViewModel
import com.example.util.PdfInvoiceGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoicesScreen(
  viewModel: ClinicViewModel,
  initialOpenWizard: Boolean = false,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val strings = LocalizedStrings.current()
  val currencyMode = LocalCurrencyMode.current
  val isArabic = strings.isArabic

  val invoices by viewModel.allInvoices.collectAsStateWithLifecycle()
  val patients by viewModel.allPatients.collectAsStateWithLifecycle()
  val services by viewModel.allServices.collectAsStateWithLifecycle()
  val financialSummary by viewModel.financialSummary.collectAsStateWithLifecycle()
  val globalExchangeRate by viewModel.exchangeRateSyp.collectAsStateWithLifecycle()

  var showInvoiceWizard by remember { mutableStateOf(initialOpenWizard) }
  var invoiceToCollectInstallment by remember { mutableStateOf<Invoice?>(null) }
  var selectedInvoiceForDetail by remember { mutableStateOf<Invoice?>(null) }
  var showExchangeRateDialog by remember { mutableStateOf(false) }
  var doctorFilter by remember { mutableStateOf<String?>(null) }
  var filterOnlyUnpaid by remember { mutableStateOf(false) }

  var searchQuery by remember { mutableStateOf("") }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showInvoiceWizard = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("fab_generate_invoice")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = strings.createInvoiceWizard)
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // 1. Invoices Header Container - snug fit, distinct stacked rows
      Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Line 1: Page Title & Invoices Count Badge
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = strings.invoicesTitle,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = "${invoices.size}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          // Line 2: Real-time Exchange Rate (Independent horizontal line directly below title)
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFE0F2F1),
            modifier = Modifier.clickable { showExchangeRateDialog = true }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.CurrencyExchange,
                contentDescription = null,
                tint = Color(0xFF00796B),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "1$ = ${String.format(Locale.US, "%,.0f", globalExchangeRate)} ${if (isArabic) "ل.س" else "SYP"}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = strings.exchangeRateSettings,
                tint = Color(0xFF00796B),
                modifier = Modifier.size(11.dp)
              )
            }
          }

          // Line 2: 4 Financial Metrics horizontal strip
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Total Invoiced
            item {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "${strings.totalRevenue}: ",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                  Text(
                    text = CurrencyFormatter.format(financialSummary.totalRevenue, globalExchangeRate, currencyMode, isArabic),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                }
              }
            }

            // Total Collected
            item {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE8F5E9)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "${strings.totalCollected}: ",
                    fontSize = 10.sp,
                    color = Color(0xFF2E7D32)
                  )
                  Text(
                    text = CurrencyFormatter.format(financialSummary.totalCollected, globalExchangeRate, currencyMode, isArabic),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                  )
                }
              }
            }

            // Outstanding Balance
            item {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (financialSummary.totalOutstandingBalance > 0.01) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "${strings.outstandingBalance}: ",
                    fontSize = 10.sp,
                    color = if (financialSummary.totalOutstandingBalance > 0.01) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = CurrencyFormatter.format(financialSummary.totalOutstandingBalance, globalExchangeRate, currencyMode, isArabic),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (financialSummary.totalOutstandingBalance > 0.01) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            // Clinic Share 30%
            item {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE0F7FA)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "${strings.clinicShare30}: ",
                    fontSize = 10.sp,
                    color = DentalTealPrimary
                  )
                  Text(
                    text = CurrencyFormatter.format(financialSummary.totalClinicOverhead, globalExchangeRate, currencyMode, isArabic),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DentalTealPrimary
                  )
                }
              }
            }
          }

          // Line 3: Full-width Search Bar (Vertically centered text with full visibility)
          Surface(
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
              .fillMaxWidth()
              .height(46.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.CenterStart
              ) {
                if (searchQuery.isEmpty()) {
                  Text(
                    text = strings.searchPlaceholder,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                  )
                }
                BasicTextField(
                  value = searchQuery,
                  onValueChange = { searchQuery = it },
                  singleLine = true,
                  textStyle = TextStyle(
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                  ),
                  cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                  modifier = Modifier.fillMaxWidth()
                )
              }
              if (searchQuery.isNotEmpty()) {
                IconButton(
                  onClick = { searchQuery = "" },
                  modifier = Modifier.size(24.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                  )
                }
              }
            }
          }

          // Line 4: Filter Chips Row (All Doctors / Dr. Haya / Dr. Jafar / Unpaid)
          LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            item {
              FilterChipItem(
                label = strings.allDoctors,
                isSelected = doctorFilter == null,
                onClick = { doctorFilter = null }
              )
            }
            item {
              FilterChipItem(
                label = strings.drHayaName,
                isSelected = doctorFilter == Doctor.HAYA_HASAN.displayName,
                color = DoctorHayaColor,
                onClick = { doctorFilter = Doctor.HAYA_HASAN.displayName }
              )
            }
            item {
              FilterChipItem(
                label = strings.drJafarName,
                isSelected = doctorFilter == Doctor.JAFAR_ATEYH.displayName,
                color = DoctorJafarColor,
                onClick = { doctorFilter = Doctor.JAFAR_ATEYH.displayName }
              )
            }
            item {
              FilterChipItem(
                label = strings.statusHasBalance,
                isSelected = filterOnlyUnpaid,
                color = Color(0xFFC62828),
                onClick = { filterOnlyUnpaid = !filterOnlyUnpaid }
              )
            }
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      // 4. Data-First Invoices List: Immediately accessible with maximized vertical real estate
      val filteredInvoices = invoices.filter { inv ->
        val matchesDoctor = doctorFilter == null || inv.assignedDoctor.equals(doctorFilter, ignoreCase = true)
        val matchesUnpaid = !filterOnlyUnpaid || !inv.isFullyPaid
        val matchesSearch = searchQuery.isBlank() ||
                            inv.patientName.contains(searchQuery, ignoreCase = true) ||
                            inv.serviceName.contains(searchQuery, ignoreCase = true) ||
                            inv.invoiceId.toString().contains(searchQuery)
        matchesDoctor && matchesUnpaid && matchesSearch
      }

      if (filteredInvoices.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Receipt,
              contentDescription = null,
              modifier = Modifier.size(48.dp),
              tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (isArabic) "لا توجد فواتير مسجلة حالياً." else "No invoices recorded yet.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredInvoices) { inv ->
            // Requirement 5: Sequential, clean horizontal ledger rows with locked rate and one-tap PDF export
            InvoiceCard(
              invoice = inv,
              strings = strings,
              currencyMode = currencyMode,
              isArabic = isArabic,
              onClick = {
                selectedInvoiceForDetail = inv
              },
              onExportPdf = {
                PdfInvoiceGenerator.generateAndShareInvoice(
                  context = context,
                  invoice = inv,
                  currencyMode = currencyMode,
                  isArabic = isArabic
                )
              },
              onCollectInstallment = {
                invoiceToCollectInstallment = inv
              }
            )
          }
          item { Spacer(modifier = Modifier.height(96.dp)) }
        }
      }
    }
  }

  // Requirement 5: Create Invoice Wizard Modal with horizontal field pairs (no vertical clutter)
  if (showInvoiceWizard) {
    InvoiceWizardDialog(
      patients = patients,
      services = services,
      defaultExchangeRate = globalExchangeRate,
      strings = strings,
      currencyMode = currencyMode,
      isArabic = isArabic,
      onDismiss = { showInvoiceWizard = false },
      onConfirm = { patient, serviceNames, totalServices, doctorFees, exchangeRate, amountPaid, method, notes ->
        viewModel.generateInvoice(
          patient = patient,
          serviceNames = serviceNames,
          totalServicePrice = totalServices,
          doctorFees = doctorFees,
          exchangeRateSyp = exchangeRate,
          amountPaidToday = amountPaid,
          paymentMethod = method,
          notes = notes
        )
        showInvoiceWizard = false
      }
    )
  }

  // Requirement 5: Responsive Invoice Detail Receipt Dialog
  if (selectedInvoiceForDetail != null) {
    InvoiceDetailDialog(
      invoice = selectedInvoiceForDetail!!,
      strings = strings,
      currencyMode = currencyMode,
      isArabic = isArabic,
      onDismiss = { selectedInvoiceForDetail = null },
      onExportPdf = {
        PdfInvoiceGenerator.generateAndShareInvoice(
          context = context,
          invoice = selectedInvoiceForDetail!!,
          currencyMode = currencyMode,
          isArabic = isArabic
        )
      },
      onCollectInstallment = {
        val inv = selectedInvoiceForDetail!!
        selectedInvoiceForDetail = null
        invoiceToCollectInstallment = inv
      }
    )
  }

  // Record Installment Payment Dialog
  if (invoiceToCollectInstallment != null) {
    RecordInstallmentDialog(
      invoice = invoiceToCollectInstallment!!,
      defaultExchangeRate = globalExchangeRate,
      strings = strings,
      currencyMode = currencyMode,
      isArabic = isArabic,
      onDismiss = { invoiceToCollectInstallment = null },
      onConfirm = { amountUsd, exchangeRate, method, notes ->
        viewModel.recordInstallmentPayment(
          invoiceId = invoiceToCollectInstallment!!.invoiceId,
          amountPaidUsd = amountUsd,
          exchangeRateSyp = exchangeRate,
          paymentMethod = method,
          notes = notes
        )
        invoiceToCollectInstallment = null
      }
    )
  }

  // Global Exchange Rate Edit Dialog
  if (showExchangeRateDialog) {
    ExchangeRateEditDialog(
      currentRate = globalExchangeRate,
      strings = strings,
      onDismiss = { showExchangeRateDialog = false },
      onSave = { newRate ->
        viewModel.setExchangeRateSyp(newRate)
        showExchangeRateDialog = false
      }
    )
  }
}

/**
 * Responsive, clear horizontal ledger row UI with logo-only badge, clear symbols, and one-tap PDF export.
 */
@Composable
private fun InvoiceCard(
  invoice: Invoice,
  strings: com.example.ui.theme.AppStrings,
  currencyMode: CurrencyDisplayMode,
  isArabic: Boolean,
  onClick: () -> Unit,
  onExportPdf: () -> Unit,
  onCollectInstallment: () -> Unit
) {
  val dateFormatted = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.US).format(Date(invoice.timestamp))
  val totalFormatted = CurrencyFormatter.format(invoice.totalAmount, invoice.exchangeRateSyp, currencyMode, isArabic)
  val lockedRateFormatted = "1$ = ${String.format(Locale.US, "%,.0f", invoice.exchangeRateSyp)} ${if (isArabic) "ل.س" else "SYP"}"

  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("invoice_card_${invoice.invoiceId}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Line 1: Header Row with Logo (NO NAME), Invoice Number, Status Badge, and PDF Action
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Clinic Logo Badge - satisfies rule: "في حال كان لدينا شعار لا حاجة لاسمه نكتفي بالشعار فقط"
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, DentalTealPrimary.copy(alpha = 0.35f)),
            modifier = Modifier.size(34.dp)
          ) {
            Image(
              painter = painterResource(id = R.drawable.clinic_logo),
              contentDescription = null,
              modifier = Modifier
                .fillMaxSize()
                .padding(2.dp)
            )
          }

          Text(
            text = "${if (isArabic) "فاتورة" else "Invoice"} #${invoice.invoiceId}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          // Status Badge with icon
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (invoice.isFullyPaid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = if (invoice.isFullyPaid) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (invoice.isFullyPaid) Color(0xFF2E7D32) else Color(0xFFC62828),
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = if (invoice.isFullyPaid) strings.statusFullyPaid else strings.statusHasBalance,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (invoice.isFullyPaid) Color(0xFF2E7D32) else Color(0xFFC62828)
              )
            }
          }
        }

        // PDF Action Button
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
          modifier = Modifier
            .clickable { onExportPdf() }
            .testTag("export_pdf_button_${invoice.invoiceId}")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "PDF",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

      // Line 2: Patient & Date in a clear, uncluttered horizontal row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f, fill = false)
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(15.dp)
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = invoice.patientName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Event,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = dateFormatted,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Line 3: Doctor & Dental Procedure in a dedicated horizontal row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        DoctorBadge(doctorName = invoice.assignedDoctor, compact = true)

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Icon(
            imageVector = Icons.Default.MedicalServices,
            contentDescription = null,
            tint = DentalTealPrimary,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = invoice.serviceName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
          )
        }
      }

      // Line 4: Locked Exchange Rate & Grand Total in a dedicated horizontal container
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = Color(0xFF00695C),
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${if (isArabic) "سعر الصرف" else "Locked Rate"}: $lockedRateFormatted",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF00695C)
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "${if (isArabic) "الإجمالي" else "Total"}: ",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = totalFormatted,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      // Line 5: Installment & Remaining Balance Bar (only if not fully paid)
      if (!invoice.isFullyPaid) {
        val remainingFormatted = CurrencyFormatter.format(invoice.remainingBalance, invoice.exchangeRateSyp, currencyMode, isArabic)
        val paidFormatted = CurrencyFormatter.format(invoice.amountPaidToday, invoice.exchangeRateSyp, currencyMode, isArabic)

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFFFF3E0),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCC80)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = null,
                tint = Color(0xFFC62828),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${strings.remainingBalanceLabel}: $remainingFormatted",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFC62828)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "(${strings.paidTodayOrDownPayment}: $paidFormatted)",
                fontSize = 10.sp,
                color = Color(0xFFE65100)
              )
            }

            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color(0xFFE65100),
              modifier = Modifier.clickable { onCollectInstallment() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = strings.recordInstallmentPayment,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              }
            }
          }
        }
      }

      // Line 6: Financial Revenue Split in a clean horizontal strip with icons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0xFFE0F7FA)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.AccountBalance,
              contentDescription = null,
              tint = DentalTealPrimary,
              modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = "30%: ${CurrencyFormatter.format(invoice.clinicOverhead, invoice.exchangeRateSyp, currencyMode, isArabic)}",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = DentalTealPrimary
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = DoctorHayaColor.copy(alpha = 0.12f)
        ) {
          Text(
            text = "${strings.drHayaShort}: ${CurrencyFormatter.format(invoice.hayaShare, invoice.exchangeRateSyp, currencyMode, isArabic)}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = DoctorHayaColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = DoctorJafarColor.copy(alpha = 0.12f)
        ) {
          Text(
            text = "${strings.drJafarShort}: ${CurrencyFormatter.format(invoice.jafarShare, invoice.exchangeRateSyp, currencyMode, isArabic)}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = DoctorJafarColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
          )
        }
      }
    }
  }
}

/**
 * Responsive Full Invoice Detail Dialog:
 * Shows a clean digital invoice receipt modal with logo badge (no name text),
 * clear symbols, and horizontal line-by-line structure.
 */
@Composable
private fun InvoiceDetailDialog(
  invoice: Invoice,
  strings: com.example.ui.theme.AppStrings,
  currencyMode: CurrencyDisplayMode,
  isArabic: Boolean,
  onDismiss: () -> Unit,
  onExportPdf: () -> Unit,
  onCollectInstallment: () -> Unit
) {
  val dateFormatted = SimpleDateFormat("EEEE, MMMM dd, yyyy • hh:mm a", Locale.US).format(Date(invoice.timestamp))
  val totalFormatted = CurrencyFormatter.format(invoice.totalAmount, invoice.exchangeRateSyp, currencyMode, isArabic)
  val lockedRateFormatted = "1$ = ${String.format(Locale.US, "%,.0f", invoice.exchangeRateSyp)} ${if (isArabic) "ل.س" else "SYP"}"

  AlertDialog(
    onDismissRequest = onDismiss,
    title = null,
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Clinic Logo Avatar - pure logo without name text
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color.White,
          shadowElevation = 2.dp,
          border = androidx.compose.foundation.BorderStroke(1.dp, DentalTealPrimary.copy(alpha = 0.3f)),
          modifier = Modifier.size(52.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.clinic_logo),
            contentDescription = null,
            modifier = Modifier
              .fillMaxSize()
              .padding(4.dp)
          )
        }

        // Invoice ID and Status Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${if (isArabic) "فاتورة رسمية" else "Official Receipt"} #${invoice.invoiceId}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.width(8.dp))
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (invoice.isFullyPaid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
          ) {
            Text(
              text = if (invoice.isFullyPaid) strings.statusFullyPaid else strings.statusHasBalance,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (invoice.isFullyPaid) Color(0xFF2E7D32) else Color(0xFFC62828),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

        // Horizontal Line 1: Patient
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = if (isArabic) "المريض:" else "Patient:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Text(text = invoice.patientName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        // Horizontal Line 2: Date
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Event, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = if (isArabic) "التاريخ:" else "Date:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Text(text = dateFormatted, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Horizontal Line 3: Doctor
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = if (isArabic) "الطبيب المعالج:" else "Treating Doctor:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          DoctorBadge(doctorName = invoice.assignedDoctor, compact = true)
        }

        // Horizontal Line 4: Dental Procedure
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.MedicalServices, contentDescription = null, tint = DentalTealPrimary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = if (isArabic) "المعالجة السنية:" else "Procedure:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          Text(text = invoice.serviceName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        // Horizontal Line 5: Locked Exchange Rate
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF00695C), modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = if (isArabic) "سعر الصرف المثبت:" else "Locked Rate:", fontSize = 12.sp, color = Color(0xFF00695C))
          }
          Text(text = lockedRateFormatted, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00695C))
        }

        // Horizontal Line 6: Financial Box
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            // Total
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = strings.totalRevenue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
              Text(text = totalFormatted, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }

            // Paid
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = strings.paidTodayOrDownPayment, fontSize = 12.sp, color = Color(0xFF2E7D32))
              Text(
                text = CurrencyFormatter.format(invoice.amountPaidToday, invoice.exchangeRateSyp, currencyMode, isArabic),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
              )
            }

            // Remaining
            if (!invoice.isFullyPaid) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(text = strings.remainingBalanceLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                Text(
                  text = CurrencyFormatter.format(invoice.remainingBalance, invoice.exchangeRateSyp, currencyMode, isArabic),
                  fontSize = 14.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = Color(0xFFC62828)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!invoice.isFullyPaid) {
          Button(
            onClick = onCollectInstallment,
            shape = RoundedCornerShape(8.dp)
          ) {
            Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(strings.recordInstallmentPayment, fontSize = 12.sp)
          }
        }

        FilledTonalButton(
          onClick = onExportPdf,
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(if (isArabic) "PDF تصدير" else "PDF", fontSize = 12.sp)
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.actionClose)
      }
    }
  )
}

/**
 * Requirement 5: Create Invoice Wizard Dialog with clean horizontal field pairings,
 * permanent exchange rate locking, and minimal vertical height.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceWizardDialog(
  patients: List<Patient>,
  services: List<DentalService>,
  defaultExchangeRate: Double,
  strings: com.example.ui.theme.AppStrings,
  currencyMode: CurrencyDisplayMode,
  isArabic: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (Patient, List<String>, Double, Double, Double, Double, String, String) -> Unit
) {
  var selectedPatient by remember { mutableStateOf(patients.firstOrNull()) }
  var patientDropdownExpanded by remember { mutableStateOf(false) }

  val selectedServices = remember { mutableStateListOf<DentalService>() }

  var doctorFeesText by remember { mutableStateOf("") }
  var exchangeRateText by remember { mutableStateOf(defaultExchangeRate.toInt().toString()) }
  var isInstallmentPlan by remember { mutableStateOf(false) }
  var downPaymentText by remember { mutableStateOf("") }
  var paymentMethod by remember { mutableStateOf("Cash") }
  var notes by remember { mutableStateOf("") }

  var serviceSearchQuery by remember { mutableStateOf("") }

  val servicesBaseTotalUsd = selectedServices.sumOf { it.standardPrice }
  val doctorFeesUsd = doctorFeesText.toDoubleOrNull() ?: 0.0
  val totalInvoiceUsd = servicesBaseTotalUsd + doctorFeesUsd
  val activeRate = exchangeRateText.toDoubleOrNull() ?: defaultExchangeRate

  val amountPaidTodayUsd = if (isInstallmentPlan) {
    (downPaymentText.toDoubleOrNull() ?: 0.0).coerceIn(0.0, totalInvoiceUsd)
  } else {
    totalInvoiceUsd
  }

  val previewBreakdown = selectedPatient?.let { p ->
    FinancialDistributionCalculator.calculate(
      totalAmount = totalInvoiceUsd,
      assignedDoctor = Doctor.fromName(p.assignedDoctor),
      exchangeRateSyp = activeRate,
      amountPaidToday = amountPaidTodayUsd
    )
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.createInvoiceWizard, fontWeight = FontWeight.Bold) },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Horizontal Pair 1: Patient Selector & Locked Exchange Rate Field
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(modifier = Modifier.weight(1.3f)) {
              ExposedDropdownMenuBox(
                expanded = patientDropdownExpanded,
                onExpandedChange = { patientDropdownExpanded = it }
              ) {
                OutlinedTextField(
                  value = selectedPatient?.fullName ?: (if (isArabic) "اختر المريض" else "Select Patient"),
                  onValueChange = {},
                  readOnly = true,
                  label = { Text(strings.navPatients) },
                  trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = patientDropdownExpanded) },
                  modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                  singleLine = true
                )
                ExposedDropdownMenu(
                  expanded = patientDropdownExpanded,
                  onDismissRequest = { patientDropdownExpanded = false }
                ) {
                  patients.forEach { p ->
                    DropdownMenuItem(
                      text = { Text("${p.fullName} (${Doctor.fromName(p.assignedDoctor).getLocalizedName(isArabic)})") },
                      onClick = {
                        selectedPatient = p
                        patientDropdownExpanded = false
                      }
                    )
                  }
                }
              }
            }

            OutlinedTextField(
              value = exchangeRateText,
              onValueChange = { exchangeRateText = it },
              label = { Text(if (isArabic) "🔒 سعر الصرف (ل.س)" else "🔒 Rate (SYP)") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
          }
        }

        // Services Picker Section
        item {
          Text(
            text = "${strings.navServices} (${selectedServices.size} ${if (isArabic) "مختارة" else "selected"})",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
          )

          OutlinedTextField(
            value = serviceSearchQuery,
            onValueChange = { serviceSearchQuery = it },
            placeholder = { Text(strings.searchPlaceholder) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }

        val filteredServices = services.filter {
          serviceSearchQuery.isBlank() ||
          it.serviceName.contains(serviceSearchQuery, ignoreCase = true) ||
          it.category.contains(serviceSearchQuery, ignoreCase = true)
        }

        items(filteredServices.take(6)) { service ->
          val isSelected = selectedServices.contains(service)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent)
              .clickable {
                if (isSelected) selectedServices.remove(service) else selectedServices.add(service)
              }
              .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Checkbox(
              checked = isSelected,
              onCheckedChange = {
                if (isSelected) selectedServices.remove(service) else selectedServices.add(service)
              }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(text = service.serviceName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              Text(
                text = CurrencyFormatter.format(service.standardPrice, activeRate, currencyMode, isArabic),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Horizontal Pair 2: Doctor's Fees & Payment Method
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedTextField(
              value = doctorFeesText,
              onValueChange = { doctorFeesText = it },
              label = { Text("${strings.doctorFeesLabel} ($)") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )

            OutlinedTextField(
              value = paymentMethod,
              onValueChange = { paymentMethod = it },
              label = { Text(if (isArabic) "طريقة الدفع (كاش/تحويل)" else "Payment Method") },
              modifier = Modifier.weight(1f),
              singleLine = true
            )
          }
        }

        // Horizontal Pair 3: Installment Plan Option & Down Payment
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .weight(1f)
                .clickable { isInstallmentPlan = !isInstallmentPlan }
            ) {
              Checkbox(
                checked = isInstallmentPlan,
                onCheckedChange = { isInstallmentPlan = it }
              )
              Text(
                text = if (isArabic) "تقسيط / دفعة أولى" else "Installment Plan",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }

            if (isInstallmentPlan) {
              OutlinedTextField(
                value = downPaymentText,
                onValueChange = { downPaymentText = it },
                label = { Text("${strings.paidTodayOrDownPayment} ($)") },
                modifier = Modifier.weight(1f),
                singleLine = true
              )
            }
          }
        }

        // Total Calculations & Locked Split Card
        item {
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = strings.totalAmountLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Text(
                  text = CurrencyFormatter.format(totalInvoiceUsd, activeRate, currencyMode, isArabic),
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 16.sp,
                  color = MaterialTheme.colorScheme.primary
                )
              }
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFFE0F2F1)
              ) {
                Text(
                  text = "🔒 1$ = ${activeRate.toInt()} SYP",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color(0xFF00695C),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
              }
            }
          }
        }

        // Notes
        item {
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(strings.notes) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (selectedPatient != null && totalInvoiceUsd > 0.0) {
            val serviceNames = if (selectedServices.isEmpty()) listOf("Consultation") else selectedServices.map { it.serviceName }
            onConfirm(
              selectedPatient!!,
              serviceNames,
              servicesBaseTotalUsd,
              doctorFeesUsd,
              activeRate,
              amountPaidTodayUsd,
              paymentMethod,
              notes.trim()
            )
          }
        },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("btn_save_invoice")
      ) {
        Text(strings.confirm)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancel) }
    }
  )
}

/**
 * Record Installment Payment Dialog
 */
@Composable
private fun RecordInstallmentDialog(
  invoice: Invoice,
  defaultExchangeRate: Double,
  strings: com.example.ui.theme.AppStrings,
  currencyMode: CurrencyDisplayMode,
  isArabic: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (amountUsd: Double, exchangeRate: Double, method: String, notes: String) -> Unit
) {
  var paymentAmountText by remember { mutableStateOf("") }
  var paymentMethod by remember { mutableStateOf("Cash") }
  var notes by remember { mutableStateOf("") }
  var exchangeRateText by remember { mutableStateOf(invoice.exchangeRateSyp.toInt().toString()) }

  val maxRemainingUsd = invoice.remainingBalance

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.recordInstallmentPayment, fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Patient & Balance Info
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFFFF3E0),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = invoice.patientName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
              Text(
                text = "${if (isArabic) "فاتورة" else "Invoice"} #${invoice.invoiceId}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(text = strings.remainingBalanceLabel, fontSize = 10.sp, color = Color(0xFFC62828))
              Text(
                text = CurrencyFormatter.format(maxRemainingUsd, invoice.exchangeRateSyp, currencyMode, isArabic),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFC62828)
              )
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = paymentAmountText,
            onValueChange = { paymentAmountText = it },
            label = { Text("${if (isArabic) "المبلغ المقبوض" else "Amount"} ($)") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )

          OutlinedTextField(
            value = exchangeRateText,
            onValueChange = { exchangeRateText = it },
            label = { Text(if (isArabic) "سعر الصرف" else "Rate") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        OutlinedTextField(
          value = paymentMethod,
          onValueChange = { paymentMethod = it },
          label = { Text(if (isArabic) "طريقة القبض" else "Payment Method") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text(strings.notes) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val amount = paymentAmountText.toDoubleOrNull() ?: 0.0
          val rate = exchangeRateText.toDoubleOrNull() ?: invoice.exchangeRateSyp
          if (amount > 0.0) {
            onConfirm(amount, rate, paymentMethod, notes.trim())
          }
        },
        shape = RoundedCornerShape(8.dp)
      ) {
        Text(strings.confirm)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancel) }
    }
  )
}

/**
 * Exchange Rate Edit Dialog
 */
@Composable
private fun ExchangeRateEditDialog(
  currentRate: Double,
  strings: com.example.ui.theme.AppStrings,
  onDismiss: () -> Unit,
  onSave: (Double) -> Unit
) {
  var rateText by remember { mutableStateOf(currentRate.toInt().toString()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.exchangeRateSettings) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = if (strings.isArabic) "تحديد سعر صرف الدولار مقابل الليرة السورية المعتمد في العيادة:" else "Set current USD to Syrian Pound (SYP) exchange rate:",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(
          value = rateText,
          onValueChange = { rateText = it },
          label = { Text(strings.exchangeRateLabel) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val newRate = rateText.toDoubleOrNull()
          if (newRate != null && newRate > 0.0) {
            onSave(newRate)
          }
        }
      ) {
        Text(strings.save)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancel) }
    }
  )
}

@Composable
private fun FilterChipItem(
  label: String,
  isSelected: Boolean,
  color: Color = MaterialTheme.colorScheme.primary,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, color) else null,
    modifier = Modifier.clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (isSelected) {
        Surface(
          shape = RoundedCornerShape(4.dp),
          color = color,
          modifier = Modifier.size(6.dp)
        ) {}
        Spacer(modifier = Modifier.width(6.dp))
      }
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
