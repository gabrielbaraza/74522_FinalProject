package com.stu74522.finalproject_stu74522

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHistoricScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser
    var orders by remember { mutableStateOf<List<Map<String, Any>>>(listOf()) }


    LaunchedEffect(key1 = currentUser) {
        currentUser?.let { user ->
            db.collection("users").document(user.uid).get().addOnSuccessListener { document ->
                val userOrders = document.data?.get("orders") as? List<Map<String, Any>> ?: listOf()
                orders = userOrders
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order History") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("Your Orders", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            LazyColumn {
                items(orders) { order ->
                    OrderCard(order)
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: Map<String, Any>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val items = order["items"] as List<Map<String, Any>>
            val orderDate = order["orderDate"] as Long
            val totalCost = order["totalCost"] as Double

            Text("Order Date: ${java.text.DateFormat.getDateInstance().format(java.util.Date(orderDate))}", style = MaterialTheme.typography.bodyLarge)
            Text("Total Cost: $${String.format("%.2f", totalCost)}", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(10.dp))
            items.forEach { item ->
                Text("${item["productName"]} - Quantity: ${item["quantity"]} at \$${item["price"]}")
            }
            Divider(Modifier.padding(vertical = 8.dp))
        }
    }
}
