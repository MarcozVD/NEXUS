package com.Marcos.nexus

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()

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
                                navController.navigate("wallet")
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

                    composable("wallet") {
                        WalletScreen(
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

                                // NO limpiar las credenciales biométricas
                                // Esto permite que el usuario pueda usar la huella después de cerrar sesión
                                // BiometricHelper.clearCredentials(this@MainActivity)

                                // Navegar al login
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