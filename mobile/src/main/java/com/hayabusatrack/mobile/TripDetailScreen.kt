package com.hayabusatrack.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun TripDetailScreen(tripId: String) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    var points by remember { mutableStateOf<List<TripPointEntity>>(emptyList()) }
    var trip by remember { mutableStateOf<TripEntity?>(null) }

    LaunchedEffect(tripId) {
        trip = db.tripDao().getTripById(tripId)
        points = db.tripDao().getPointsForTrip(tripId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        trip?.let { t ->
            Text("Récap trajet", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Vitesse max : ${t.maxSpeedKmh.toInt()} km/h")
            Text("Accélération max : %.2f m/s²".format(t.maxAccelMs2))
            Text("Décélération max : %.2f m/s²".format(t.maxDecelMs2))
            Text("Angle max : %.1f°".format(t.maxLeanAngleDeg))
            Text("Distance : %.1f km".format(t.distanceMeters / 1000))
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text("Vitesse (km/h)", style = MaterialTheme.typography.titleMedium)
        LineChart(values = points.map { it.speedKmh }, modifier = Modifier.fillMaxWidth().height(150.dp))

        Spacer(modifier = Modifier.height(16.dp))
        Text("Accélération (m/s²)", style = MaterialTheme.typography.titleMedium)
        LineChart(values = points.map { it.accelMs2 }, modifier = Modifier.fillMaxWidth().height(150.dp))

        Spacer(modifier = Modifier.height(16.dp))
        Text("Angle d'inclinaison (°)", style = MaterialTheme.typography.titleMedium)
        LineChart(values = points.map { it.leanAngleDeg }, modifier = Modifier.fillMaxWidth().height(150.dp))
    }
}