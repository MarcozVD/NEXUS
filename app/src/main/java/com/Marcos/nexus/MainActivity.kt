package com.Marcos.nexus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()

                // Verificar si hay usuario autenticado al iniciar
                val startDestination = if (Firebase.auth.currentUser != null) {
                    "home"
                } else {
                    "login"
                }

                NavHost(navController = navController, startDestination = startDestination) {
                    composable("login") {
                        BackHandler {
                            finish()
                        }
                        NexusLoginScreen(navController)
                    }

                    composable("register") {
                        BackHandler {
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                        NexusRegisterScreen(navController)
                    }

                    composable("home") {
                        BackHandler {
                            finish()
                        }
                        NexusHomeScreen(
                            onNavigateToSendMoney = {
                                navController.navigate("send_money")
                            },
                            onNavigateToProducts = {
                                navController.navigate("products")
                            },
                            onNavigateToNotifications = {
                                navController.navigate("notifications")
                            },
                            onNavigateToSettings = {
                                navController.navigate("user_info")
                            },
                            onNavigateToSavings = {
                                navController.navigate("savings")
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
                                navController.navigate("savings")
                            },
                            onNavigateToInversiones = {
                                navController.navigate("investments")
                            },
                            onNavigateToNotifications = {
                                navController.navigate("notifications")
                            },
                            onNavigateToSettings = {
                                navController.navigate("user_info")
                            },
                            onNavigateToNexusCard = {
                                navController.navigate("nexus_card")
                            }
                        )
                    }

                    composable("nexus_card") {
                        NexusCard(
                            onBack = {
                                navController.popBackStack()
                            },
                            onNavigateToNotifications = {
                                navController.navigate("notifications")
                            },
                            onNavigateToSettings = {
                                navController.navigate("user_info")
                            }
                        )
                    }

                    // 🔥 RUTA DE INVERSIONES
                    composable("investments") {
                        InvestmentsScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onNavigateToNotifications = {
                                navController.navigate("notifications")
                            },
                            onNavigateToSettings = {
                                navController.navigate("user_info")
                            }
                        )
                    }

                    // 🔥 RUTA DE AHORROS/CARTERA
                    composable("savings") {
                        SavingsScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onNavigateToNotifications = {
                                navController.navigate("notifications")
                            },
                            onNavigateToSettings = {
                                navController.navigate("user_info")
                            }
                        )
                    }

                    composable("notifications") {
                        NotificationsScreen(
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("user_info") {
                        UserInfoScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onLogout = {
                                Firebase.auth.signOut()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}