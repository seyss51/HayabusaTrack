package com.hayabusatrack.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    AppNav()
                }
            }
        }
    }
}

@Composable
fun AppNav() {
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            TripListScreen(onTripClick = { tripId -> navController.navigate("detail/$tripId") })
        }
        composable("detail/{tripId}") { backStackEntry ->
            val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
            TripDetailScreen(tripId = tripId)
        }
    }
}