package com.stu74522.finalproject_stu74522

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.NavController

@Composable
fun SignUpScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 30.sp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.trim() },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                if (!isProcessing) {
                    if (password == confirmPassword) {
                        isProcessing = true
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    val userData = hashMapOf(
                                        "firstName" to firstName,
                                        "lastName" to lastName,
                                        "email" to email
                                    )
                                    user?.let { firebaseUser ->
                                        db.collection("users").document(firebaseUser.uid)
                                            .set(userData)
                                            .addOnSuccessListener {

                                                val cartData = hashMapOf(
                                                    "userEmail" to email,
                                                    "products" to listOf<Pair<String, Int>>()
                                                )
                                                db.collection("carts").document(firebaseUser.uid)
                                                    .set(cartData)
                                                    .addOnSuccessListener {
                                                        navController.navigate(Screen.Screen0.route) {
                                                            popUpTo("SignUp") { inclusive = true }
                                                        }
                                                        isProcessing = false
                                                    }
                                                    .addOnFailureListener { e ->
                                                        message = "Error creating cart: ${e.message}"
                                                        isProcessing = false
                                                    }
                                            }
                                            .addOnFailureListener { e ->
                                                message = "Error writing to Firestore: ${e.message}"
                                                isProcessing = false
                                            }
                                    }
                                } else {
                                    message = "Sign Up Failed: ${task.exception?.message}"
                                    isProcessing = false
                                }
                            }
                    } else {
                        message = "Passwords do not match"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isProcessing
        ) {
            Text("Sign Up")
        }
        if (message.isNotEmpty()) {
            Text(message, color = MaterialTheme.colorScheme.error)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Already have an account? Log in",
            Modifier.clickable {
                if (!isProcessing) {
                    navController.navigate(Screen.Login.route)
                }
            },
            color = MaterialTheme.colorScheme.primary
        )
    }
}
