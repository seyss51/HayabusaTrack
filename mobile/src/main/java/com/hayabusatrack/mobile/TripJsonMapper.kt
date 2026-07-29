package com.hayabusatrack.mobile

import org.json.JSONObject

object TripJsonMapper {

    data class ParsedTrip(val trip: TripEntity, val points: List<TripPointEntity>)

    fun fromJson(json: String): ParsedTrip {
        val root = JSONObject(json)
        val tripId = root.getString("id")

        val trip = TripEntity(
            id = tripId,
            startTime = root.getLong("startTime"),
            endTime = root.getLong("endTime"),
            maxSpeedKmh = root.getDouble("maxSpeedKmh").toFloat(),
            maxAccelMs2 = root.getDouble("maxAccelMs2").toFloat(),
            maxDecelMs2 = root.getDouble("maxDecelMs2").toFloat(),
            maxLeanAngleDeg = root.getDouble("maxLeanAngleDeg").toFloat(),
            distanceMeters = root.getDouble("distanceMeters").toFloat()
        )

        val pointsArray = root.getJSONArray("points")
        val points = mutableListOf<TripPointEntity>()
        for (i in 0 until pointsArray.length()) {
            val po = pointsArray.getJSONObject(i)
            points.add(
                TripPointEntity(
                    tripId = tripId,
                    timestamp = po.getLong("timestamp"),
                    speedKmh = po.getDouble("speedKmh").toFloat(),
                    accelMs2 = po.getDouble("accelMs2").toFloat(),
                    leanAngleDeg = po.getDouble("leanAngleDeg").toFloat(),
                    lat = po.getDouble("lat"),
                    lon = po.getDouble("lon")
                )
            )
        }
        return ParsedTrip(trip, points)
    }
}