package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Initialize System Notification Channel alerts
        IslamicNotificationManager.initNotificationChannels(this)
        
        // 2. Set up periodic background notifications every 20 minutes (and show one immediately for delight)
        IslamicNotificationManager.showPeriodicDuaNotification(this)
        IslamicAlarmReceiver.schedulePeriodicDua(this)

        enableEdgeToEdge()
        
        setContent {
            val viewModel: IslamicViewModel by viewModels()
            val settingsState by viewModel.settingsState.collectAsState()
            val settings = settingsState ?: SettingsEntity()
            
            // Apply theme selected by user (falling back to dark mode if configured)
                MyApplicationTheme(darkTheme = settings.useDarkTheme) {
                val navController = rememberNavController()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "splash",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        // Interactive dynamic Islamic Splash Screen
                        composable("splash") {
                            SplashView(
                                viewModel = viewModel,
                                onLoadingFinished = {
                                    navController.navigate("home") {
                                        popUpTo("splash") { inclusive = true }
                                    }
                                }
                            )
                        }

                        // Main home dashboard
                        composable("home") {
                            HomeView(
                                viewModel = viewModel,
                                settings = settings,
                                onNavigate = { route ->
                                    navController.navigate(route)
                                }
                            )
                        }
                        
                        // Quran indexed reader & recitation
                        composable("quran") {
                            QuranView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        // Prayer schedule timelist
                        composable("prayers") {
                            PrayersView(
                                viewModel = viewModel,
                                settings = settings,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        // Azkar & Duas section
                        composable("azkar") {
                            AzkarView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        // Re-route dues to azkar
                        composable("duas") {
                            AzkarView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        // Live Web Islamic Radio player screen
                        composable("radio") {
                            RadioView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        // Electronic Tasbih count circle
                        composable("sebha") {
                            SebhaView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        // 99 Names of Allah grid list
                        composable("names") {
                            NamesView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        // Mecca sensory compass finder
                        composable("qibla") {
                            QiblaView(
                                viewModel = viewModel,
                                settings = settings,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        // IslamAI full intelligent dialogue console
                        composable("chat") {
                            ChatView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        
                        // Profiles & Coordinates Settings configuration page
                        composable("settings") {
                            SettingsView(
                                viewModel = viewModel,
                                settings = settings,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        // -- NEW CHANNELS & SECTIONS --
                        composable("calendar") {
                            HijriCalendarView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("tajweed") {
                            TajweedView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("fiqh") {
                            FiqhView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("sermons") {
                            SermonsView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("hajj") {
                            HajjView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("khatma") {
                            KhatmaView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("ruqyah") {
                            RuqyahView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("quiz") {
                            IslamicQuizView(
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
