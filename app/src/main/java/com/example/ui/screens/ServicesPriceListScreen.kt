package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DentalService
import com.example.ui.theme.CurrencyDisplayMode
import com.example.ui.theme.CurrencyFormatter
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalCurrencyMode
import com.example.ui.theme.LocalizedStrings
import com.example.ui.viewmodel.ClinicViewModel

@Composable
fun ServicesPriceListScreen(
  viewModel: ClinicViewModel,
  modifier: Modifier = Modifier
) {
  val strings = LocalizedStrings.current()
  val currencyMode = LocalCurrencyMode.current
  val isArabic = strings.isArabic

  val services by viewModel.allServices.collectAsStateWithLifecycle()
  val exchangeRate by viewModel.exchangeRateSyp.collectAsStateWithLifecycle()

  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf<String?>(null) }
  var showAddServiceDialog by remember { mutableStateOf(false) }
  var serviceToEdit by remember { mutableStateOf<DentalService?>(null) }

  val categories = remember(services) {
    listOf(strings.filterAll) + services.map { it.category }.distinct().sorted()
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddServiceDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("fab_add_service")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = strings.navServices)
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Header Section
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = strings.navServices,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = if (isArabic) "لائحة أسعار المعالجات والخدمات السنية المعتمدة" else "Standard clinical dental fee schedule & procedures",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text(strings.searchPlaceholder) },
          leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
              }
            }
          },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          singleLine = true
        )
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

      // Services List
      val filteredServices = services.filter { s ->
        searchQuery.isBlank() ||
        s.serviceName.contains(searchQuery, ignoreCase = true) ||
        s.category.contains(searchQuery, ignoreCase = true) ||
        s.description.contains(searchQuery, ignoreCase = true)
      }

      if (filteredServices.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (isArabic) "لا توجد خدمات مطابقة." else "No treatments found.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredServices) { service ->
            ServicePriceItemCard(
              service = service,
              exchangeRate = exchangeRate,
              currencyMode = currencyMode,
              isArabic = isArabic,
              onEdit = { serviceToEdit = service },
              onDelete = { viewModel.deleteDentalService(service) }
            )
          }
          item { Spacer(modifier = Modifier.height(70.dp)) }
        }
      }
    }
  }

  // Add Service Dialog
  if (showAddServiceDialog) {
    ServiceEditDialog(
      existingService = null,
      strings = strings,
      onDismiss = { showAddServiceDialog = false },
      onConfirm = { newService ->
        viewModel.addDentalService(newService)
        showAddServiceDialog = false
      }
    )
  }

  // Edit Service Dialog
  if (serviceToEdit != null) {
    ServiceEditDialog(
      existingService = serviceToEdit,
      strings = strings,
      onDismiss = { serviceToEdit = null },
      onConfirm = { updatedService ->
        viewModel.updateDentalService(updatedService)
        serviceToEdit = null
      }
    )
  }
}

@Composable
private fun ServicePriceItemCard(
  service: DentalService,
  exchangeRate: Double,
  currencyMode: CurrencyDisplayMode,
  isArabic: Boolean,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val priceFormatted = CurrencyFormatter.format(service.standardPrice, exchangeRate, currencyMode, isArabic)

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = service.serviceName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp)
          ) {
            Text(
              text = service.category,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = priceFormatted,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      if (service.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = service.description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
      Spacer(modifier = Modifier.height(6.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit Service",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
        }

        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete Service",
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun ServiceEditDialog(
  existingService: DentalService?,
  strings: com.example.ui.theme.AppStrings,
  onDismiss: () -> Unit,
  onConfirm: (DentalService) -> Unit
) {
  var name by remember { mutableStateOf(existingService?.serviceName ?: "") }
  var category by remember { mutableStateOf(existingService?.category ?: "") }
  var priceText by remember { mutableStateOf(existingService?.standardPrice?.toString() ?: "") }
  var description by remember { mutableStateOf(existingService?.description ?: "") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (existingService == null) (if (strings.isArabic) "إضافة خدمة علاجية" else "Add Dental Treatment") else (if (strings.isArabic) "تعديل الخدمة" else "Edit Dental Treatment")) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(if (strings.isArabic) "اسم المعالجة" else "Service Name") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = category,
          onValueChange = { category = it },
          label = { Text(if (strings.isArabic) "التصنيف" else "Category") },
          placeholder = { Text("Aesthetic Fillings, Root Canal, Crowns, etc.") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = priceText,
          onValueChange = { priceText = it },
          label = { Text(if (strings.isArabic) "السعر الأساسي ($ USD)" else "Standard Price ($ USD)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text(if (strings.isArabic) "الوصف والمواصفات السريرية" else "Clinical Description") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 2
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            val price = priceText.toDoubleOrNull() ?: 0.0
            val service = existingService?.copy(
              serviceName = name.trim(),
              category = category.ifBlank { "General" }.trim(),
              standardPrice = price,
              description = description.trim()
            ) ?: DentalService(
              serviceName = name.trim(),
              category = category.ifBlank { "General" }.trim(),
              standardPrice = price,
              description = description.trim()
            )
            onConfirm(service)
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
