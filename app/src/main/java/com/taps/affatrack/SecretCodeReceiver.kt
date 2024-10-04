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
        val outgoingNumber = intent?.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
        val secretCode = "*#1234#" // El código secreto que quieres que se ingrese

        if (outgoingNumber == secretCode) {
            // Mostrar la app si el código es correcto
            val packageManager = context?.packageManager
            val componentName = ComponentName(context!!, MainActivity::class.java)

            // Habilitar el Launcher
            packageManager?.setComponentEnabledSetting(
                componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            // Iniciar MainActivity
            val mainActivityIntent = Intent(context, MainActivity::class.java)
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Necesario para iniciar una actividad desde un contexto no UI
            context.startActivity(mainActivityIntent)

            // Cancelar la llamada
            setResultData(null)

            // Opcional: Mostrar un mensaje de Toast para confirmar que se ha detectado el código
            Toast.makeText(context, "Código secreto detectado", Toast.LENGTH_SHORT).show()
        }
    }
}
