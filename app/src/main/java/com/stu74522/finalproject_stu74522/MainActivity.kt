package com.stu74522.finalproject_stu74522

import LoginScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.stu74522.finalproject_stu74522.ui.theme.FinalProject_stu74522Theme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FinalProject_stu74522Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val auth = FirebaseAuth.getInstance()
                    val startDestination = if (auth.currentUser != null) {
                        Screen.Screen0.route
                    } else {
                        Screen.Login.route
                    }

                    AppNavigation(startDestination)
                }
            }
        }
    }
}





