package com.hayabusatrack.wear

import org.json.JSONArray
import org.json.JSONObject

object TripJsonMapper {

    fun toJson(trip: TripEntity, points: List<TripPointEntity>): String {
        val root = JSONObject()
        root.put("id", trip.id)
        root.put("startTime", trip.startTime)
        root.put("endTime", trip.endTime)
        root.put("maxSpeedKmh", trip.maxSpeedKmh)
        root.put("maxAccelMs2", trip.maxAccelMs2)
        root.put("maxDecelMs2", trip.maxDecelMs2)
        root.put("maxLeanAngleDeg", trip.maxLeanAngleDeg)
        root.put("distanceMeters", trip.distanceMeters)

        val pointsArray = JSONArray()
        for (p in points) {
            val po = JSONObject()
            po.put("timestamp", p.timestamp)
            po.put("speedKmh", p.speedKmh)
            po.put("accelMs2", p.accelMs2)
            po.put("leanAngleDeg", p.leanAngleDeg)
            po.put("lat", p.lat)
            po.put("lon", p.lon)
            pointsArray.put(po)
        }
        root.put("points", pointsArray)
        return root.toString()
    }
}