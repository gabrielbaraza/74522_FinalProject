package com.stu74522.finalproject_stu74522

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailsScreen(navController: NavController, categoryId: String) {
    val products = remember { mutableStateListOf<Product>() }
    val db = FirebaseFirestore.getInstance()


    LaunchedEffect(categoryId) {
        db.collection("products")
            .whereEqualTo("categoryId", categoryId)
            .get()
            .addOnSuccessListener { snapshot ->
                products.clear()
                products.addAll(snapshot.documents.mapNotNull { it.toObject(Product::class.java) })
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Category Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn {
                items(products) { product ->
                    ProductItem(navController,product)
                }
            }
        }
    }
}

@Composable
fun ProductItem(navController: NavController, product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Name: ${product.name}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Price: $${product.price}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Quantity: ${product.quantity}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Description: ${product.description}", style = MaterialTheme.typography.bodySmall)
            Button(onClick = { navController.navigate("productDetails/${product.name}") }) {
                Text("Show Details")
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(navController: NavController, productName: String) {
    val product = remember { mutableStateOf<Product?>(null) }
    val selectedQuantity = remember { mutableStateOf(1) }  // Initial quantity
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    LaunchedEffect(productName) {
        db.collection("products")
            .whereEqualTo("name", productName)
            .get()
            .addOnSuccessListener { snapshot ->
                product.value = snapshot.documents.firstOrNull()?.toObject<Product>()
                product.value?.let {
                    Log.d("Firestore", "Product loaded successfully: ${it.name}")
                } ?: Log.d("Firestore", "No product found with name: $productName")
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching product by name", exception)
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            product.value?.let { prod ->
                Text("Name: ${prod.name}", style = MaterialTheme.typography.titleLarge)
                Text("Price: $${prod.price}", style = MaterialTheme.typography.bodyLarge)
                Text("Description: ${prod.description}", style = MaterialTheme.typography.bodyMedium)
                Text("Available: ${prod.quantity} in stock", style = MaterialTheme.typography.bodySmall)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { if (selectedQuantity.value > 1) selectedQuantity.value -= 1 }) {
                        Text("-")
                    }
                    Text("${selectedQuantity.value}", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 8.dp))
                    Button(onClick = { if (selectedQuantity.value < prod.quantity) selectedQuantity.value += 1 }) {
                        Text("+")
                    }
                }

                Button(
                    onClick = {
                        scope.launch {
                            CartUtils.addToCart(prod.name, selectedQuantity.value)
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    enabled = selectedQuantity.value > 0 && product.value != null
                ) {
                    Text("Add to Cart")
                }
            } ?: Text("Loading product details...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}



data class Product(
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val categoryId: String = ""
)
