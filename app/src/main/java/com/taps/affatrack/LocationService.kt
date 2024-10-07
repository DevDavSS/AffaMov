package com.taps.affatrack

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class LocationService : Service() {
    private lateinit var locationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForegroundService() // Inicia el servicio en primer plano con la notificación

        locationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.let {
                    for (location in it.locations) {
                        val message = "Latitud: ${location.latitude}, Longitud: ${location.longitude}"
                        sendCoordinatesToServer(message)
                        Log.d("LocationService", "Latitud: ${location.latitude}, Longitud: ${location.longitude}")
                    }
                }
            }
        }

        startLocationUpdates() // Llamar a startLocationUpdates directamente
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        try {
            locationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        } catch (e: SecurityException) {
            Log.e("LocationService", "Location permission not granted: ${e.message}")
            // Handle the exception, possibly stop the service or notify the user
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        locationClient.removeLocationUpdates(locationCallback)
    }

    private fun sendCoordinatesToServer(message: String) {
        Thread {
            val sharedPreferences: SharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)
            val serverURL = sharedPreferences.getString("server_url", null) ?: return@Thread

            try {
                val fullUrl = "$serverURL/coordenadas"
                val url = URL(fullUrl)

                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "text/plain")

                val outputStream = connection.outputStream
                val writer = OutputStreamWriter(outputStream)
                writer.write(message)
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode
                Log.d("LocationService", "Response Code: $responseCode")

                connection.disconnect()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "location_service_channel"
            val channelName = "Location Service"
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun startForegroundService() {
        val channelId = "location_service_channel"
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("")
            .setContentText("")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(1, notification)

    }
}
