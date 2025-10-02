package com.example.alarmsetter

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

object AlarmScheduler {
	fun scheduleExact(context: Context, triggerAtMillis: Long, extras: Intent) {
		val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
		val receiverIntent = Intent(context, AlarmReceiver::class.java).apply {
			putExtras(extras)
		}
		val requestCode = (triggerAtMillis % Int.MAX_VALUE).toInt()
		val pending = PendingIntent.getBroadcast(
			context,
			requestCode,
			receiverIntent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
		val showIntent = PendingIntent.getActivity(
			context,
			requestCode + 1,
			Intent(context, MainActivity::class.java),
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)
		val info = AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			alarmManager.setAlarmClock(info, pending)
		} else {
			alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pending)
		}
	}

	fun scheduleSnooze(context: Context, minutes: Int, baseExtras: Intent) {
		val trigger = System.currentTimeMillis() + minutes * 60_000L
		scheduleExact(context, trigger, baseExtras)
	}
}