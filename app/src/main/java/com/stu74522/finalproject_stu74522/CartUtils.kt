import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.stu74522.finalproject_stu74522.Product
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await



data class CartItem(
    val productName: String,
    var quantity: Int,

)

data class Cart(
    var userEmail: String,
    val items: MutableList<CartItem>
)

object CartUtils {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    var cart: Cart by mutableStateOf(Cart("", mutableListOf()))

    init {
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            cart.userEmail = user?.email ?: ""
            if (user != null) {
                loadCart()
            } else {
                clearCart()
            }
        }
    }

    private fun loadCart() {

    }

    fun clearCart() {
        cart.items.clear()
        saveCart()
    }

    fun addToCart(productName: String, quantity: Int) {
        val existingItem = cart.items.find { it.productName == productName }
        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            cart.items.add(CartItem(productName, quantity))
        }
        saveCart()
    }

    fun updateQuantity(productName: String, newQuantity: Int) {
        val item = cart.items.find { it.productName == productName }
        if (item != null && newQuantity > 0) {
            item.quantity = newQuantity
        } else {
            cart.items.removeIf { it.productName == productName }
        }
        saveCart()
    }

    fun removeFromCart(productName: String) {
        cart.items.removeAll { it.productName == productName }
        saveCart()
    }

    fun saveCart() {
        if (cart.userEmail.isNotEmpty()) {
            db.collection("carts").document(cart.userEmail)
                .set(cart)
                .addOnSuccessListener {  }
                .addOnFailureListener {  }
        }
    }

    suspend fun fetchProductByName(name: String): Product? {
        val querySnapshot = db.collection("products")
            .whereEqualTo("name", name)
            .get()
            .await()
        return querySnapshot.documents.firstOrNull()?.toObject(Product::class.java)
    }


    fun calculateTotal(): Double {
        return cart.items.sumOf { cartItem ->
            db.collection("products").document(cartItem.productName).get().result.toObject(Product::class.java)?.price ?: 0.0 * cartItem.quantity
        }
    }



}
