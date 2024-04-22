package com.stu74522.finalproject_stu74522

import CartItem
import CartUtils
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController) {
    val cartItems = remember { mutableStateListOf<CartItem>() }
    val scope = rememberCoroutineScope()
    val totalPrice = remember { mutableStateOf(0.0) }

    LaunchedEffect(key1 = Unit) {
        cartItems.clear()
        cartItems.addAll(CartUtils.cart.items)
        scope.launch {
            updateTotalPrice(cartItems, totalPrice)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Cart") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            navController.navigate("UserHistoricScreen")
                        }
                    ) {
                        Text("See Last Orders", color = Color.Black)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        CartContent(cartItems, paddingValues, scope, navController, totalPrice) {
            cartItems.clear()
            scope.launch {
                updateTotalPrice(cartItems, totalPrice)
            }
        }
    }
}


@Composable
fun CartContent(cartItems: MutableList<CartItem>, paddingValues: PaddingValues, scope: CoroutineScope, navController: NavController, totalPrice: MutableState<Double>, onCartCleared: () -> Unit) {
    LazyColumn(
        contentPadding = paddingValues,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(cartItems) { cartItem ->
            ProductCartItemView(cartItem, scope, totalPrice, cartItems)
        }
        item {
            FinalPriceView(totalPrice.value)
            OrderButton(navController, cartItems, onCartCleared)
        }
    }
}

@Composable
fun ProductCartItemView(cartItem: CartItem, scope: CoroutineScope, totalPrice: MutableState<Double>, cartItems: MutableList<CartItem>) {
    Row(
        modifier = Modifier.padding(8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(cartItem.productName, style = MaterialTheme.typography.bodyLarge)
            Text("Quantity: ${cartItem.quantity}", style = MaterialTheme.typography.bodyLarge)
        }
        IconButton(onClick = {
            scope.launch {
                CartUtils.removeFromCart(cartItem.productName)
                cartItems.remove(cartItem)
                updateTotalPrice(cartItems, totalPrice)
            }
        }) {
            Icon(Icons.Filled.Delete, contentDescription = "Remove item")
        }
    }
}
@Composable
fun FinalPriceView(finalPrice: Double) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text("Final Price:", style = MaterialTheme.typography.bodyLarge)
        Text("$${finalPrice}", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun OrderButton(navController: NavController, cartItems: List<CartItem>, onCartCleared: () -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    Button(
        onClick = {
            val itemsDetails = mutableListOf<Map<String, Any>>()
            var totalCost = 0.0

            val productFetchTasks = cartItems.map { cartItem ->
                db.collection("products")
                    .whereEqualTo("name", cartItem.productName)
                    .get()
                    .continueWithTask { task ->
                        if (task.isSuccessful && task.result.documents.isNotEmpty()) {
                            val document = task.result.documents.first()
                            val product = document.toObject(Product::class.java)
                            val price = product?.price ?: 0.0
                            val quantity = cartItem.quantity
                            val subtotal = price * quantity
                            totalCost += subtotal

                            itemsDetails.add(mapOf(
                                "productName" to cartItem.productName,
                                "quantity" to quantity,
                                "price" to price,
                                "subtotal" to subtotal
                            ))

                            // Decrease product stock quantity
                            val newQuantity = document.getLong("quantity")?.minus(quantity) ?: 0
                            db.collection("products").document(document.id)
                                .update("quantity", newQuantity)
                        }
                        task
                    }
            }

            Tasks.whenAllComplete(productFetchTasks).addOnCompleteListener {

                val newOrder = mapOf(
                    "items" to itemsDetails,
                    "orderDate" to System.currentTimeMillis(),
                    "totalCost" to totalCost
                )

                val userDocRef = db.collection("users").document(currentUser?.uid ?: "")
                db.runTransaction { transaction ->
                    val userData = transaction.get(userDocRef)
                    val orders = userData.get("orders") as? MutableList<Map<String, Any>> ?: mutableListOf()
                    orders.add(newOrder)

                    transaction.update(userDocRef, "orders", orders)
                    null
                }.addOnSuccessListener {
                    CartUtils.clearCart()
                    navController.navigate(Screen.SuccessfulTransactionScreen.route)
                    onCartCleared()
                    println("New order added to user's order history.")
                }.addOnFailureListener { e ->
                    println("Failed to update user's order history: $e")
                }
            }
        },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("Order My Cart", color = MaterialTheme.colorScheme.onPrimary)
    }
}



suspend fun updateTotalPrice(cartItems: List<CartItem>, totalPrice: MutableState<Double>) {
    totalPrice.value = cartItems.sumOf { cartItem ->
        val product = CartUtils.fetchProductByName(cartItem.productName)
        (product?.price ?: 0.0) * cartItem.quantity
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessfulTransactionScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction Success") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back to Cart")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Your transaction was successful!", style = MaterialTheme.typography.titleMedium)
        }
    }
}

