package com.hayabusatrack.mobile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripListScreen(onTripClick: (String) -> Unit) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val trips by db.tripDao().getAllTrips().collectAsState(initial = emptyList())

    Scaffold(topBar = { TopAppBar(title = { Text("Trajets HayabusaTrack") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(trips) { trip ->
                val sdf = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
                ListItem(
                    headlineContent = { Text(sdf.format(Date(trip.startTime))) },
                    supportingContent = {
                        Text("Vmax ${trip.maxSpeedKmh.toInt()} km/h · %.1f km".format(trip.distanceMeters / 1000))
                    },
                    modifier = Modifier.clickable { onTripClick(trip.id) }
                )
                Divider()
            }
        }
    }
}