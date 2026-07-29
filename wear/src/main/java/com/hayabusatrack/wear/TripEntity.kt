package com.hayabusatrack.wear

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val id: String,
    val startTime: Long,
    val endTime: Long,
    val maxSpeedKmh: Float,
    val maxAccelMs2: Float,
    val maxDecelMs2: Float,
    val maxLeanAngleDeg: Float,
    val distanceMeters: Float
)