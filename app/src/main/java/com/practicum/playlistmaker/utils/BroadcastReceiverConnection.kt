package com.practicum.playlistmaker.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast

class BroadcastReceiverConnection: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == CONNECTIVITY_CHANGE) {
            val connectivityManager = context?.getSystemService(ConnectivityManager::class.java)
            val currentNetwork = connectivityManager?.activeNetwork
            val caps = connectivityManager?.getNetworkCapabilities(currentNetwork)
            val hasInternet =
                caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

            if (!hasInternet) {
                Toast.makeText(context, "Отсутствует подключение к интернету", Toast.LENGTH_LONG).show()
            }
        }
    }

    private companion object {
        const val CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE"
    }
}