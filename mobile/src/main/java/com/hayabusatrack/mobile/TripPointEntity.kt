package com.hayabusatrack.mobile

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_points")
data class TripPointEntity(
    @PrimaryKey(autoGenerate = true) val pointId: Long = 0,
    val tripId: String,
    val timestamp: Long,
    val speedKmh: Float,
    val accelMs2: Float,
    val leanAngleDeg: Float,
    val lat: Double,
    val lon: Double
)