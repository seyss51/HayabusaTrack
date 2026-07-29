package com.hayabusatrack.wear

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Transaction

@Dao
interface TripDao {
    @Insert
    suspend fun insertTrip(trip: TripEntity)

    @Insert
    suspend fun insertPoints(points: List<TripPointEntity>)

    @Transaction
    suspend fun insertTripWithPoints(trip: TripEntity, points: List<TripPointEntity>) {
        insertTrip(trip)
        insertPoints(points)
    }
}