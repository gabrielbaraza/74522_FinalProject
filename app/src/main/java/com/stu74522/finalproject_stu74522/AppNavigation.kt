package com.stu74522.finalproject_stu74522

import LoginScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination =  startDestination) {
        composable(Screen.SignUp.route) { SignUpScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Screen0.route) { Screen0(navController) }
        composable(Screen.MainScreen.route) { MainScreen(navController) }
        composable(Screen.CartScreen.route) {CartScreen(navController)}
        composable(Screen.SuccessfulTransactionScreen.route) { SuccessfulTransactionScreen(navController) }
        composable(Screen.UserInformationScreen.route){UserInformationScreen(navController)}
        composable(Screen.UserHistoricScreen.route){UserHistoricScreen(navController)}
        composable(Screen.AboutScreen.route){AboutScreen(navController)}

        composable("categoryDetails/{categoryId}") { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: throw IllegalStateException("Category ID is required")
            CategoryDetailsScreen(navController, categoryId)
        }

        composable("productDetails/{productName}") { backStackEntry ->

            ProductDetailsScreen(navController, backStackEntry.arguments?.getString("productName") ?: "")
        }
    }
}
