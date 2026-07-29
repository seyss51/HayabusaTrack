package com.hayabusatrack.wear

import android.content.Context
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

object TripSyncSender {

    fun send(context: Context, trip: TripEntity, points: List<TripPointEntity>) {
        val json = TripJsonMapper.toJson(trip, points)
        val asset = Asset.createFromBytes(json.toByteArray(Charsets.UTF_8))

        val request = PutDataMapRequest.create("/trip_sync/${trip.id}").apply {
            dataMap.putAsset("trip_data", asset)
            dataMap.putLong("sentAt", System.currentTimeMillis())
        }
        val putRequest = request.asPutDataRequest().setUrgent()
        Wearable.getDataClient(context).putDataItem(putRequest)
    }
}