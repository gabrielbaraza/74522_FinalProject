package com.stu74522.finalproject_stu74522

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Screen0(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }


    LaunchedEffect(key1 = user) {
        user?.uid?.let { userId ->
            firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
                firstName = document.getString("firstName") ?: ""
                lastName = document.getString("lastName") ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home Page") },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.UserInformationScreen.route)
                    }) {
                        Icon(Icons.Filled.AccountCircle, contentDescription = "User Profile")
                    }
                    IconButton(onClick = {
                        auth.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Screen0.route) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        BodyContent(paddingValues, "Hello $firstName $lastName!",navController)
    }
}

@Composable
fun BodyContent(paddingValues: PaddingValues, greeting: String, navController: NavController) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = greeting, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                navController.navigate(Screen.MainScreen.route)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
        ) {
            Text("Start Shopping", color = Color.White)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val currentRoute = getCurrentRoute(navController)

    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == Screen.Screen0.route,
            onClick = {
                navController.navigate(Screen.Screen0.route)
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.List, contentDescription = "Shop") },
            label = { Text("Shop") },
            selected = currentRoute == Screen.MainScreen.route,
            onClick = {
                navController.navigate(Screen.MainScreen.route)
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Cart") },
            label = { Text("Cart") },
            selected = currentRoute == "cart",
            onClick = {
                navController.navigate(Screen.CartScreen.route)
            }
        )
    }
}
