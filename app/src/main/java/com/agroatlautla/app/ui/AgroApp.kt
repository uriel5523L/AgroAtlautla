package com.agroatlautla.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.agroatlautla.app.ui.screens.AddActivityScreen
import com.agroatlautla.app.ui.screens.AddCropScreen
import com.agroatlautla.app.ui.screens.AddExpenseScreen
import com.agroatlautla.app.ui.screens.CalendarScreen
import com.agroatlautla.app.ui.screens.CropDetailScreen
import com.agroatlautla.app.ui.screens.CropsScreen
import com.agroatlautla.app.ui.screens.DashboardScreen
import com.agroatlautla.app.ui.screens.ExpensesScreen
import com.agroatlautla.app.ui.screens.LoginScreen
import com.agroatlautla.app.ui.screens.PestDetailScreen
import com.agroatlautla.app.ui.screens.PestsScreen
import com.agroatlautla.app.ui.screens.ProfileScreen
import com.agroatlautla.app.ui.screens.RecoveryScreen
import com.agroatlautla.app.ui.screens.RegisterScreen
import com.agroatlautla.app.ui.screens.ReportsScreen
import com.agroatlautla.app.ui.screens.SplashScreen

private data class BottomItem(
    val route: String,
    val label: String,
    val iconText: String
)

private val bottomItems = listOf(
    BottomItem("home", "Inicio", "I"),
    BottomItem("crops", "Cultivos", "C"),
    BottomItem("calendar", "Calendario", "F"),
    BottomItem("pests", "Plagas", "P"),
    BottomItem("profile", "Perfil", "U")
)

@Composable
fun AgroApp(viewModel: AgroViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                AgroBottomBar(navController = navController, currentRoute = currentRoute)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                SplashScreen(
                    onStart = {
                        val target = if (viewModel.currentUser == null) "login" else "home"
                        navController.navigate(target)
                    }
                )
            }
            composable("login") {
                LoginScreen(
                    viewModel = viewModel,
                    onLogin = { navController.navigateMain("home") },
                    onRegister = { navController.navigate("register") },
                    onRecover = { navController.navigate("recover") }
                )
            }
            composable("register") {
                RegisterScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onRegistered = { navController.navigateMain("home") }
                )
            }
            composable("recover") {
                RecoveryScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("home") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable("crops") {
                CropsScreen(
                    viewModel = viewModel,
                    onAddCrop = { navController.navigate("addCrop") },
                    onCropSelected = { cropId -> navController.navigate("cropDetail/$cropId") }
                )
            }
            composable("addCrop") {
                AddCropScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(
                route = "cropDetail/{cropId}",
                arguments = listOf(navArgument("cropId") { type = NavType.StringType })
            ) { entry ->
                CropDetailScreen(
                    viewModel = viewModel,
                    cropId = entry.arguments?.getString("cropId") ?: "",
                    onBack = { navController.popBackStack() }
                )
            }
            composable("calendar") {
                CalendarScreen(
                    viewModel = viewModel,
                    onAddActivity = { navController.navigate("addActivity") }
                )
            }
            composable("addActivity") {
                AddActivityScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable("pests") {
                PestsScreen(
                    viewModel = viewModel,
                    onPestSelected = { pestId -> navController.navigate("pestDetail/$pestId") }
                )
            }
            composable(
                route = "pestDetail/{pestId}",
                arguments = listOf(navArgument("pestId") { type = NavType.StringType })
            ) { entry ->
                PestDetailScreen(
                    viewModel = viewModel,
                    pestId = entry.arguments?.getString("pestId") ?: "",
                    onBack = { navController.popBackStack() }
                )
            }
            composable("expenses") {
                ExpensesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAddExpense = { navController.navigate("addExpense") }
                )
            }
            composable("addExpense") {
                AddExpenseScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable("reports") {
                ReportsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onLogout = { navController.navigateMain("login") }
                )
            }
        }
    }
}

@Composable
private fun AgroBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        bottomItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) navController.navigate(item.route)
                },
                icon = { Text(item.iconText) },
                label = { Text(item.label) }
            )
        }
    }
}

private fun NavHostController.navigateMain(route: String) {
    navigate(route) {
        popUpTo("splash") { inclusive = true }
        launchSingleTop = true
    }
}
