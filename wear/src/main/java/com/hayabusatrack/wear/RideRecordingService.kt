package com.hayabusatrack.wear

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.abs

class RideRecordingService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelSensor: Sensor? = null
    private var rotationSensor: Sensor? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val tripId = UUID.randomUUID().toString()
    private val startTime = System.currentTimeMillis()
    private val points = mutableListOf<TripPointEntity>()

    private var lastAccel = 0f
    private var lastLeanAngle = 0f
    private var maxSpeed = 0f
    private var maxAccel = 0f
    private var maxDecel = 0f
    private var maxLeanAngle = 0f
    private var distanceMeters = 0f
    private var lastLocation: Location? = null

    private var lowSpeedSince: Long? = null
    private val stopThresholdMs = 3 * 60 * 1000L
    private val lowSpeedKmh = 3f

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        accelSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        rotationSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startForeground(NOTIF_ID, buildNotification("Enregistrement du trajet en cours"))
        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { onNewLocation(it) }
            }
        }
        try {
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper())
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun onNewLocation(location: Location) {
        val speedKmh = location.speed * 3.6f

        lastLocation?.let { distanceMeters += it.distanceTo(location) }
        lastLocation = location

        if (speedKmh > maxSpeed) maxSpeed = speedKmh

        if (speedKmh < lowSpeedKmh) {
            val since = lowSpeedSince ?: System.currentTimeMillis().also { lowSpeedSince = it }
            if (System.currentTimeMillis() - since > stopThresholdMs) {
                finishTrip()
                return
            }
        } else {
            lowSpeedSince = null
        }

        points.add(
            TripPointEntity(
                tripId = tripId,
                timestamp = System.currentTimeMillis(),
                speedKmh = speedKmh,
                accelMs2 = lastAccel,
                leanAngleDeg = lastLeanAngle,
                lat = location.latitude,
                lon = location.longitude
            )
        )
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> {
                val forwardAccel = event.values[1]
                lastAccel = forwardAccel
                if (forwardAccel > maxAccel) maxAccel = forwardAccel
                if (forwardAccel < maxDecel) maxDecel = forwardAccel
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                val orientation = FloatArray(3)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
                lastLeanAngle = roll
                if (abs(roll) > abs(maxLeanAngle)) maxLeanAngle = roll
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun finishTrip() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        sensorManager.unregisterListener(this)

        val trip = TripEntity(
            id = tripId,
            startTime = startTime,
            endTime = System.currentTimeMillis(),
            maxSpeedKmh = maxSpeed,
            maxAccelMs2 = maxAccel,
            maxDecelMs2 = maxDecel,
            maxLeanAngleDeg = maxLeanAngle,
            distanceMeters = distanceMeters
        )

        serviceScope.launch {
            val db = AppDatabase.getInstance(applicationContext)
            db.tripDao().insertTripWithPoints(trip, points)
            TripSyncSender.send(applicationContext, trip, points)

            startForegroundService(Intent(this@RideRecordingService, MotionDetectionService::class.java))
            stopSelf()
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) { }
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(text: String): Notification {
        val channelId = "recording_channel"
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(channelId) == null) {
            manager.createNotificationChannel(
                NotificationChannel(channelId, "Enregistrement", NotificationManager.IMPORTANCE_LOW)
            )
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("HayabusaTrack")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIF_ID = 2
    }
}