package com.taps.affatrack

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.content.Context
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.taps.affatrack.ui.theme.AffaTrackTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainActivity : ComponentActivity() {
    private val permissionStateFlow = MutableStateFlow(false)
    val permissionState: StateFlow<Boolean> = permissionStateFlow

    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            permissionStateFlow.value = isGranted
            if (isGranted) {
                startLocationService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AffaTrackTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    mainInterface(permissionState, requestPermissionLauncher)
                }
            }
        }

        checkAndRequestLocationPermission()
    }

    private fun checkAndRequestLocationPermission() {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val backgroundLocationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            PackageManager.PERMISSION_GRANTED
        }

        when {
            fineLocationPermission == PackageManager.PERMISSION_GRANTED -> {
                if (backgroundLocationPermission == PackageManager.PERMISSION_GRANTED) {
                    startLocationService()
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
            else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun startLocationService() {
        // Check if location permissions are granted before starting the service
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            val intent = Intent(this, LocationService::class.java)
            ContextCompat.startForegroundService(this, intent)
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun mainInterface(permissionState: StateFlow<Boolean>, requestPermissionLauncher: ActivityResultLauncher<String>) {
        val hasPermission by permissionState.collectAsState()
        val url = remember { mutableStateOf("") }  // Mutable state for the TextField value

        LaunchedEffect(Unit) {
            if (hasPermission) {
                startLocationService()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF000000), shape = RoundedCornerShape(8.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = url.value,
                onValueChange = { url.value = it },
                label = { Text("Enter the URL") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.White
                )
            )
            Box(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .background(Color.Black, shape = RoundedCornerShape(8.dp))
                    .border(2.dp, Color.Red, shape = RoundedCornerShape(8.dp))
                    .clickable {

                        val sharedPreferences: SharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                        with(sharedPreferences.edit()) {
                            putString("server_url", url.value)
                            apply()
                        }
                        startLocationService()
                    }
                    .padding(16.dp)
            ) {
                Text(
                    text = "Start Tracking",
                    color = Color.White
                )
            }


            Box(
                modifier = Modifier
                    .padding(top = 30.dp)
                    .background(Color.Black, shape = RoundedCornerShape(15.dp))
                    .border(2.dp, Color.Red, shape = RoundedCornerShape(15.dp))
                    .clickable {
                        hideApp()
                    }
                    .padding(16.dp)
            ) {
                Text(
                    text = "Hide",
                    color = Color.White
                )
            }


        }
    }

    private fun hideApp() {
        val packageManager = applicationContext.packageManager
        val componentName = ComponentName(applicationContext, MainActivity::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        } else {

            val intent = Intent(this, DummyActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)


            packageManager.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
        }
    }

}
