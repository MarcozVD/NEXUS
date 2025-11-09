package com.Marcos.nexus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login") {
                    composable("login") { NexusLoginScreen(navController) }
                    composable("register") { NexusRegisterScreen(navController) }
                    composable("home") {
                        NexusHomeScreen(
                            onNavigateToSendMoney = {
                                navController.navigate("send_money")
                            },
                            onNavigateToProducts = {
                                navController.navigate("products")
                            }
                        )
                    }
                    composable("send_money") {
                        SendMoneyScreen(
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable("products") {
                        ProductsScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onNavigateToCartera = {
                                // navController.navigate("cartera")
                            },
                            onNavigateToInversiones = {
                                // navController.navigate("inversiones")
                            },
                            onNavigateToNotifications = {
                                // navController.navigate("notifications")
                            },
                            onNavigateToSettings = {
                                // navController.navigate("settings")
                            }
                        )
                    }
                }
            }
        }
    }
}