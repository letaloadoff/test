package com.example.alarmsetter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
	override fun onReceive(context: Context, intent: Intent) {
		val ringIntent = Intent(context, AlarmRingingActivity::class.java).apply {
			flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
			putExtras(intent)
		}
		context.startActivity(ringIntent)
	}
}