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
                            // Cerrar la app cuando presione atrás en login
                            finish()
                        }
                        NexusLoginScreen(navController)
                    }

                    composable("register") {
                        BackHandler {
                            // Volver al login cuando presione atrás en register
                            navController.navigate("login") {
                                popUpTo("register") { inclusive = true }
                            }
                        }
                        NexusRegisterScreen(navController)
                    }

                    composable("home") {
                        BackHandler {
                            // Cerrar la app cuando presione atrás en home
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
                                // Cerrar sesión de Firebase
                                Firebase.auth.signOut()
                                // Navegar al login y limpiar todo el backstack
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