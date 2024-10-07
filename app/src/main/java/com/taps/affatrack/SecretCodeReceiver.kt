package com.taps.affatrack

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import android.content.BroadcastReceiver
import android.os.Bundle
class SecretCodeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Si el intent corresponde al código secreto
        val action = intent?.action
        val uri = intent?.data

        if (action == "android.provider.Telephony.SECRET_CODE" && uri?.host == "7012") {
            // Habilitar la actividad principal
            val packageManager = context?.packageManager
            val componentName = ComponentName(context!!, MainActivity::class.java)
            packageManager?.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // Iniciar MainActivity
            val mainActivityIntent = Intent(context, MainActivity::class.java)
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(mainActivityIntent)

            // Opcional: Mostrar un mensaje de Toast para confirmar que se ha detectado el código
            Toast.makeText(context, "Código secreto detectado", Toast.LENGTH_SHORT).show()
        }
    }
}

