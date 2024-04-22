package com.stu74522.finalproject_stu74522

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val categories = remember { mutableStateListOf<Category>() }
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(key1 = true) {
        db.collection("categories").get().addOnSuccessListener { snapshot ->
            categories.clear()
            categories.addAll(snapshot.documents.mapNotNull { it.toObject<Category>() })
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Categories") }) },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        CategoriesContent(padding = innerPadding, categories = categories, navController = navController)
    }
}


@Composable
fun CategoriesContent(padding: PaddingValues, categories: List<Category>, navController: NavController) {
    LazyColumn(modifier = Modifier.padding(padding)) {
        items(categories) { category ->
            CategoryListItem(category, onClick = {
                navController.navigate("categoryDetails/${category.categoryId
                }")
            })
        }
    }
}
@Composable
fun CategoryListItem(category: Category, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = category.name, style = MaterialTheme.typography.titleMedium)
            Text(text = category.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}@Composable
fun getCurrentRoute(navController: NavController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

data class Category(val categoryId: String = "", val name: String = "", val description: String = "")