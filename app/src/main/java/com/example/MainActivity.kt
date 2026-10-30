package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.EliteDentalDatabase
import com.example.data.repository.ClinicRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.PatientsAndAppointmentsScreen
import com.example.ui.screens.ServicesPriceListScreen
import com.example.ui.theme.LocalAppLanguage
import com.example.ui.theme.LocalCurrencyMode
import com.example.ui.theme.LocalizedStrings
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ClinicViewModel
import com.example.ui.viewmodel.ClinicViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        EliteDentalApp()
      }
    }
  }
}

enum class NavSection(val icon: ImageVector, val tag: String) {
  DASHBOARD(Icons.Default.Dashboard, "nav_dashboard"),
  PATIENTS(Icons.Default.People, "nav_patients"),
  INVENTORY(Icons.Default.Inventory2, "nav_inventory"),
  INVOICES(Icons.Default.ReceiptLong, "nav_invoices"),
  SERVICES(Icons.Default.MedicalServices, "nav_services")
}

@Composable
fun EliteDentalApp() {
  val context = LocalContext.current
  val database = remember { EliteDentalDatabase.getDatabase(context) }
  val repository = remember { ClinicRepository(database) }
  val viewModel: ClinicViewModel = viewModel(factory = ClinicViewModelFactory(repository))

  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
  val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
  val currencyMode by viewModel.currencyMode.collectAsStateWithLifecycle()

  // Quick Action navigation helpers
  var openNewPatientDialogFromQuickAction by remember { mutableStateOf(false) }
  var openNewAppointmentDialogFromQuickAction by remember { mutableStateOf(false) }
  var openNewInvoiceDialogFromQuickAction by remember { mutableStateOf(false) }

  // Handle system back navigation: Return to Dashboard if on another tab
  BackHandler(enabled = currentTab != 0) {
    viewModel.setTab(0)
  }

  CompositionLocalProvider(
    LocalLayoutDirection provides appLanguage.layoutDirection,
    LocalAppLanguage provides appLanguage,
    LocalCurrencyMode provides currencyMode
  ) {
    val strings = LocalizedStrings.current()

    val navItems = listOf(
      Pair(NavSection.DASHBOARD, strings.navOverview),
      Pair(NavSection.PATIENTS, strings.navPatients),
      Pair(NavSection.INVENTORY, strings.navWarehouse),
      Pair(NavSection.INVOICES, strings.navInvoices),
      Pair(NavSection.SERVICES, strings.navServices)
    )

    // Requirement 1: Completely remove top options bar from all application screens
    Scaffold(
      contentWindowInsets = WindowInsets.safeDrawing,
      bottomBar = {
        NavigationBar(
          modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("bottom_nav_bar")
        ) {
          navItems.forEachIndexed { index, (section, title) ->
            val selected = currentTab == index
            NavigationBarItem(
              selected = selected,
              onClick = { viewModel.setTab(index) },
              icon = {
                if (section == NavSection.INVENTORY && lowStockItems.isNotEmpty()) {
                  BadgedBox(
                    badge = {
                      Badge {
                        Text("${lowStockItems.size}")
                      }
                    }
                  ) {
                    Icon(imageVector = section.icon, contentDescription = title)
                  }
                } else {
                  Icon(imageVector = section.icon, contentDescription = title)
                }
              },
              label = {
                Text(
                  text = title,
                  fontSize = 11.sp,
                  fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                  maxLines = 1
                )
              },
              modifier = Modifier.testTag(section.tag)
            )
          }
        }
      }
    ) { paddingValues ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
      ) {
        when (currentTab) {
          0 -> DashboardScreen(
            viewModel = viewModel,
            onOpenNewPatientDialog = {
              viewModel.setTab(1)
              openNewPatientDialogFromQuickAction = true
            },
            onOpenNewAppointmentDialog = {
              viewModel.setTab(1)
              openNewAppointmentDialogFromQuickAction = true
            },
            onOpenNewInvoiceDialog = {
              viewModel.setTab(3)
              openNewInvoiceDialogFromQuickAction = true
            },
            onNavigateToInventoryWithLowStockFilter = {
              viewModel.setInventoryLowStockFilterOnly(true)
              viewModel.setTab(2)
            }
          )
          1 -> PatientsAndAppointmentsScreen(
            viewModel = viewModel,
            initialSubTab = if (openNewAppointmentDialogFromQuickAction) 1 else 0
          )
          2 -> InventoryScreen(viewModel = viewModel)
          3 -> InvoicesScreen(
            viewModel = viewModel,
            initialOpenWizard = openNewInvoiceDialogFromQuickAction
          )
          4 -> ServicesPriceListScreen(viewModel = viewModel)
        }
      }
    }
  }
}
