package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.InventoryItem
import com.example.ui.components.StockAlertBadge
import com.example.ui.theme.CurrencyFormatter
import com.example.ui.theme.DentalTealPrimary
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalCurrencyMode
import com.example.ui.theme.LocalizedStrings
import com.example.ui.theme.MedicalAlert
import com.example.ui.theme.MedicalAlertContainer
import com.example.ui.viewmodel.ClinicViewModel

@Composable
fun InventoryScreen(
  viewModel: ClinicViewModel,
  modifier: Modifier = Modifier
) {
  val strings = LocalizedStrings.current()
  val currencyMode = LocalCurrencyMode.current
  val isArabic = strings.isArabic

  val inventory by viewModel.allInventory.collectAsStateWithLifecycle()
  val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
  val exchangeRateSyp by viewModel.exchangeRateSyp.collectAsStateWithLifecycle()
  val lowStockOnly by viewModel.inventoryLowStockOnly.collectAsStateWithLifecycle()

  var searchQuery by remember { mutableStateOf("") }
  var showAddItemDialog by remember { mutableStateOf(false) }
  var selectedItemForActions by remember { mutableStateOf<InventoryItem?>(null) }
  var transactionTarget by remember { mutableStateOf<Pair<InventoryItem, Boolean>?>(null) }
  var itemToEdit by remember { mutableStateOf<InventoryItem?>(null) }
  var itemToDelete by remember { mutableStateOf<InventoryItem?>(null) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddItemDialog = true },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("fab_add_inventory")
      ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = strings.addSupplyItem)
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // 1. Header Container - sized exactly to its content with clean, separate lines
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
          // Line 1: Page Title
          Text(
            text = strings.warehouseTitle,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          // Line 2: Real-time Exchange Rate (Independent line directly below title)
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFE0F2F1)
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
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "1$ = ${exchangeRateSyp.toInt()} ${if (isArabic) "ل.س" else "SYP"}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00796B)
              )
            }
          }

          // Line 2: Full-width Search Bar (Vertically centered text with full visibility)
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

          // Line 3: Status Summary & Low-Stock Toggle
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // All items counter badge
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (!lowStockOnly) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
              modifier = Modifier
                .clickable { viewModel.setInventoryLowStockFilterOnly(false) }
            ) {
              Text(
                text = "${if (isArabic) "جميع المواد" else "All Items"}: ${inventory.size}",
                fontSize = 11.sp,
                fontWeight = if (!lowStockOnly) FontWeight.Bold else FontWeight.Medium,
                color = if (!lowStockOnly) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }

            // Low stock items badge / toggle
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (lowStockOnly) MedicalAlert else (if (lowStockItems.isNotEmpty()) MedicalAlertContainer else MaterialTheme.colorScheme.surfaceVariant),
              modifier = Modifier
                .clickable { viewModel.setInventoryLowStockFilterOnly(!lowStockOnly) }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (lowStockItems.isNotEmpty()) {
                  Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (lowStockOnly) Color.White else MedicalAlert,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                  text = "${if (isArabic) "مواد النقص" else "Low Stock"}: ${lowStockItems.size}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (lowStockOnly) Color.White else (if (lowStockItems.isNotEmpty()) MedicalAlert else MaterialTheme.colorScheme.onSurfaceVariant)
                )
              }
            }
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

      // Inventory Items List (Filter by search query & low stock filter)
      val filteredItems = inventory.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                            item.itemName.contains(searchQuery, ignoreCase = true) ||
                            item.category.contains(searchQuery, ignoreCase = true)
        val matchesLowStock = !lowStockOnly || item.isLowStock
        matchesSearch && matchesLowStock
      }

      if (filteredItems.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Inventory2,
              contentDescription = null,
              modifier = Modifier.size(48.dp),
              tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = if (isArabic) "المستودع فارغ حالياً أو لا توجد مواد مطابقة للبحث." else "No inventory items found matching criteria.",
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
          items(filteredItems) { item ->
            // Requirement 4: Streamlined Item Cards displaying ONLY core essentials, no inline action buttons
            InventoryItemCard(
              item = item,
              exchangeRateSyp = exchangeRateSyp,
              currencyMode = currencyMode,
              isArabic = isArabic,
              onClick = { selectedItemForActions = item }
            )
          }
          item { Spacer(modifier = Modifier.height(96.dp)) }
        }
      }
    }
  }

  // Requirement 4: Dedicated Action Modal on tapping item
  if (selectedItemForActions != null) {
    InventoryItemActionModal(
      item = selectedItemForActions!!,
      exchangeRateSyp = exchangeRateSyp,
      currencyMode = currencyMode,
      isArabic = isArabic,
      strings = strings,
      onDismiss = { selectedItemForActions = null },
      onAddStock = {
        val target = selectedItemForActions!!
        selectedItemForActions = null
        transactionTarget = Pair(target, true)
      },
      onDeductStock = {
        val target = selectedItemForActions!!
        selectedItemForActions = null
        transactionTarget = Pair(target, false)
      },
      onEdit = {
        val target = selectedItemForActions!!
        selectedItemForActions = null
        itemToEdit = target
      },
      onDelete = {
        val target = selectedItemForActions!!
        selectedItemForActions = null
        itemToDelete = target
      }
    )
  }

  // Add New Inventory Item Dialog (all fields start completely blank)
  if (showAddItemDialog) {
    AddNewInventoryItemDialog(
      strings = strings,
      onDismiss = { showAddItemDialog = false },
      onConfirm = { newItem ->
        viewModel.addInventoryItem(newItem)
        showAddItemDialog = false
      }
    )
  }

  // Edit Existing Inventory Item Dialog
  if (itemToEdit != null) {
    EditInventoryItemDialog(
      item = itemToEdit!!,
      strings = strings,
      onDismiss = { itemToEdit = null },
      onConfirm = { updatedItem ->
        viewModel.updateInventoryItem(updatedItem)
        itemToEdit = null
      }
    )
  }

  // Accurate Stock Transaction Dialog (Add or Deduct Stock)
  if (transactionTarget != null) {
    val (targetItem, isImport) = transactionTarget!!
    StockTransactionDialog(
      item = targetItem,
      initialIsImport = isImport,
      strings = strings,
      onDismiss = { transactionTarget = null },
      onConfirm = { delta, importMode ->
        viewModel.adjustInventoryTransaction(targetItem.itemId, delta, importMode)
        transactionTarget = null
      }
    )
  }

  // Delete Confirmation Dialog
  if (itemToDelete != null) {
    AlertDialog(
      onDismissRequest = { itemToDelete = null },
      title = { Text(if (isArabic) "حذف مادة من المستودع" else "Delete Warehouse Item") },
      text = {
        Text(
          if (isArabic)
            "هل أنت متأكد من حذف المادة \"${itemToDelete!!.itemName}\" نهائياً من المستودع؟"
          else
            "Are you sure you want to permanently delete \"${itemToDelete!!.itemName}\" from inventory?"
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteInventoryItem(itemToDelete!!)
            itemToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text(if (isArabic) "حذف" else "Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { itemToDelete = null }) {
          Text(strings.cancel)
        }
      }
    )
  }
}

/**
 * Requirement 4: Streamlined Item Card displaying ONLY core essential details (Name, Stock count, Basic info).
 * No operational action buttons placed directly on the card.
 */
@Composable
private fun InventoryItemCard(
  item: InventoryItem,
  exchangeRateSyp: Double,
  currencyMode: com.example.ui.theme.CurrencyDisplayMode,
  isArabic: Boolean,
  onClick: () -> Unit
) {
  val priceFormatted = CurrencyFormatter.format(item.unitPrice, exchangeRateSyp, currencyMode, isArabic)

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (item.isLowStock) MedicalAlertContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .testTag("inventory_item_${item.itemId}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.itemName,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = "${item.category} • ${item.unitMeasure} • $priceFormatted",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        StockAlertBadge(currentQuantity = item.currentQuantity, threshold = item.minimumThreshold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "${item.currentQuantity.toInt()} ${item.unitMeasure}",
          fontSize = 14.sp,
          fontWeight = FontWeight.ExtraBold,
          color = if (item.isLowStock) MedicalAlert else DentalTealPrimary
        )
      }
    }
  }
}

/**
 * Requirement 4: Dedicated Action Modal/Drawer for inventory items
 * Triggers focused operations: Add Stock, Deduct Stock, Edit Item Details, Delete Item.
 */
@Composable
private fun InventoryItemActionModal(
  item: InventoryItem,
  exchangeRateSyp: Double,
  currencyMode: com.example.ui.theme.CurrencyDisplayMode,
  isArabic: Boolean,
  strings: com.example.ui.theme.AppStrings,
  onDismiss: () -> Unit,
  onAddStock: () -> Unit,
  onDeductStock: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  val priceFormatted = CurrencyFormatter.format(item.unitPrice, exchangeRateSyp, currencyMode, isArabic)

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.itemName,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )
          Text(
            text = "${item.category} • $priceFormatted",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        StockAlertBadge(currentQuantity = item.currentQuantity, threshold = item.minimumThreshold)
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        // Stock summary box
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = strings.currentStockLabel,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${item.currentQuantity.toInt()} ${item.unitMeasure}",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (item.isLowStock) MedicalAlert else DentalTealPrimary
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = strings.minThresholdLabel,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${item.minimumThreshold} ${item.unitMeasure}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }

        Text(
          text = if (isArabic) "العمليات المتاحة على المادة:" else "Available Item Operations:",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        // Action Buttons Grid / Column
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Add Stock (Stock In)
          Button(
            onClick = onAddStock,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            modifier = Modifier.weight(1f)
          ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = if (isArabic) "توريد +" else "Stock In +", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          // Deduct Stock (Stock Out)
          Button(
            onClick = onDeductStock,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
            modifier = Modifier.weight(1f)
          ) {
            Icon(imageVector = Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = if (isArabic) "صرف -" else "Stock Out -", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Edit details
          OutlinedButton(
            onClick = onEdit,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = if (isArabic) "تعديل البيانات" else "Edit Details", fontSize = 12.sp)
          }

          // Delete item
          OutlinedButton(
            onClick = onDelete,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.weight(1f)
          ) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = if (isArabic) "حذف المادة" else "Delete Item", fontSize = 12.sp)
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(strings.cancel)
      }
    }
  )
}

/**
 * Add New Inventory Item Dialog (all fields start completely blank)
 */
@Composable
private fun AddNewInventoryItemDialog(
  strings: com.example.ui.theme.AppStrings,
  onDismiss: () -> Unit,
  onConfirm: (InventoryItem) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("") }
  var quantityText by remember { mutableStateOf("") }
  var thresholdText by remember { mutableStateOf("") }
  var priceText by remember { mutableStateOf("") }
  var unitMeasure by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.addSupplyItem) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(if (strings.isArabic) "اسم المادة السنية" else "Item Name") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = category,
          onValueChange = { category = it },
          label = { Text(if (strings.isArabic) "التصنيف (مثال: تخدير، حشوات، جراحة)" else "Category (e.g., Anesthesia, Restorative)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = quantityText,
            onValueChange = { quantityText = it },
            label = { Text(strings.currentStockLabel) },
            modifier = Modifier.weight(1f),
            singleLine = true
          )

          OutlinedTextField(
            value = thresholdText,
            onValueChange = { thresholdText = it },
            label = { Text(strings.minThresholdLabel) },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = priceText,
            onValueChange = { priceText = it },
            label = { Text(if (strings.isArabic) "سعر الوحدة ($)" else "Unit Price ($)") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )

          OutlinedTextField(
            value = unitMeasure,
            onValueChange = { unitMeasure = it },
            label = { Text(if (strings.isArabic) "الوحدة (علبة، محقنة، حبة)" else "Unit Measure (Box, Pack)") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            val item = InventoryItem(
              itemName = name.trim(),
              category = category.ifBlank { "General" }.trim(),
              currentQuantity = quantityText.toDoubleOrNull() ?: 0.0,
              minimumThreshold = thresholdText.toIntOrNull() ?: 0,
              unitPrice = priceText.toDoubleOrNull() ?: 0.0,
              unitMeasure = unitMeasure.ifBlank { "Units" }.trim()
            )
            onConfirm(item)
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

/**
 * Edit Existing Inventory Item Dialog
 */
@Composable
private fun EditInventoryItemDialog(
  item: InventoryItem,
  strings: com.example.ui.theme.AppStrings,
  onDismiss: () -> Unit,
  onConfirm: (InventoryItem) -> Unit
) {
  var name by remember { mutableStateOf(item.itemName) }
  var category by remember { mutableStateOf(item.category) }
  var thresholdText by remember { mutableStateOf(item.minimumThreshold.toString()) }
  var priceText by remember { mutableStateOf(item.unitPrice.toString()) }
  var unitMeasure by remember { mutableStateOf(item.unitMeasure) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (strings.isArabic) "تعديل بيانات المادة" else "Edit Item Details") },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(if (strings.isArabic) "اسم المادة السنية" else "Item Name") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        OutlinedTextField(
          value = category,
          onValueChange = { category = it },
          label = { Text(if (strings.isArabic) "التصنيف" else "Category") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = priceText,
            onValueChange = { priceText = it },
            label = { Text(if (strings.isArabic) "سعر الوحدة ($)" else "Unit Price ($)") },
            modifier = Modifier.weight(1f),
            singleLine = true
          )

          OutlinedTextField(
            value = thresholdText,
            onValueChange = { thresholdText = it },
            label = { Text(strings.minThresholdLabel) },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }

        OutlinedTextField(
          value = unitMeasure,
          onValueChange = { unitMeasure = it },
          label = { Text(if (strings.isArabic) "الوحدة (علبة، محقنة، حبة)" else "Unit Measure") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            val updated = item.copy(
              itemName = name.trim(),
              category = category.ifBlank { "General" }.trim(),
              minimumThreshold = thresholdText.toIntOrNull() ?: item.minimumThreshold,
              unitPrice = priceText.toDoubleOrNull() ?: item.unitPrice,
              unitMeasure = unitMeasure.ifBlank { item.unitMeasure }.trim()
            )
            onConfirm(updated)
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

/**
 * Accurate Inventory Stock Transaction Dialog:
 * Handles explicit Stock In (supply import/purchase) and Stock Out (usage/export/procedure).
 */
@Composable
private fun StockTransactionDialog(
  item: InventoryItem,
  initialIsImport: Boolean = true,
  strings: com.example.ui.theme.AppStrings,
  onDismiss: () -> Unit,
  onConfirm: (delta: Double, isImport: Boolean) -> Unit
) {
  var isImport by remember { mutableStateOf(initialIsImport) }
  var quantityText by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(strings.adjustStockTitle) },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "${item.itemName} (${strings.currentStockLabel}: ${item.currentQuantity.toInt()} ${item.unitMeasure})",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        // Transaction Type Selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isImport) Color(0xFF2E7D32) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .weight(1f)
              .clickable { isImport = true }
          ) {
            Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
              Text(
                text = strings.stockInImport,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isImport) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (!isImport) Color(0xFFC62828) else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .weight(1f)
              .clickable { isImport = false }
          ) {
            Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
              Text(
                text = strings.stockOutExport,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (!isImport) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        OutlinedTextField(
          value = quantityText,
          onValueChange = { quantityText = it },
          label = { Text("${strings.quantityToAdjust} (${item.unitMeasure})") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val delta = quantityText.toDoubleOrNull() ?: 0.0
          if (delta > 0.0) {
            onConfirm(delta, isImport)
          }
        }
      ) {
        Text(strings.confirm)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text(strings.cancel) }
    }
  )
}
