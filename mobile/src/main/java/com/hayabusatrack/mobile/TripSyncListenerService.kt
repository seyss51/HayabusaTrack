package com.hayabusatrack.mobile

import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TripSyncListenerService : WearableListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        val pendingResult = goAsync()
        val events = dataEvents.map { it.freeze() }
        dataEvents.release()

        serviceScope.launch {
            try {
                for (event in events) {
                    if (event.type == DataEvent.TYPE_CHANGED) {
                        val item = event.dataItem
                        if (item.uri.path?.startsWith("/trip_sync") == true) {
                            handleTripData(item)
                        }
                    }
                }
            } catch (e: Exception) {
                // log si besoin
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun handleTripData(item: DataItem) {
        val dataMap = DataMapItem.fromDataItem(item).dataMap
        val asset = dataMap.getAsset("trip_data") ?: return
        val fd = Wearable.getDataClient(this).getFdForAsset(asset).await()
        val json = fd.inputStream.bufferedReader().use { it.readText() }
        val parsed = TripJsonMapper.fromJson(json)
        val db = AppDatabase.getInstance(applicationContext)
        db.tripDao().insertTripWithPoints(parsed.trip, parsed.points)
    }
}