package com.stu74522.finalproject_stu74522


import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInformationScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    var email by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isUpdating by remember { mutableStateOf(false) }

    // Fetch existing user details
    LaunchedEffect(user) {
        user?.let {
            db.collection("users").document(it.uid).get().addOnSuccessListener { document ->
                firstName = document.getString("firstName") ?: ""
                lastName = document.getString("lastName") ?: ""
                email = document.getString("email") ?: ""
                address = document.getString("address") ?: ""
                phone = document.getString("phone") ?: ""
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Information") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                actions = {
                    TextButton(onClick = {
                        navController.navigate("AboutScreen")
                    }) {
                        Text("About this app")
                    }
                }

            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") })
            TextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") })
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, readOnly = true)
            TextField(value = address, onValueChange = { address = it }, label = { Text("Address") })
            TextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
            Button(
                onClick = {
                    if (!isUpdating) {
                        isUpdating = true
                        db.collection("users").document(user!!.uid)
                            .update(mapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "address" to address,
                                "phone" to phone
                            )).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    message = "Update Successful"
                                } else {
                                    message = "Update Failed: ${task.exception?.message}"
                                }
                                isUpdating = false
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Information")
            }
            if (message.isNotEmpty()) {
                Text(message, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About This App") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Go back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("This app was designed by student 74522", style = MaterialTheme.typography.bodyLarge)
            Text("© 2024 74522", style = MaterialTheme.typography.bodyMedium)

        }
    }
}

