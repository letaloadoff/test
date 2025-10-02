package com.example.alarmsetter

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val timePicker: TimePicker = findViewById(R.id.timePicker)
		val labelEdit: EditText = findViewById(R.id.editLabel)
		val vibrateCheck: CheckBox = findViewById(R.id.checkVibrate)
		val skipUiCheck: CheckBox = findViewById(R.id.checkSkipUi)
		val setButton: Button = findViewById(R.id.buttonSetAlarm)

		setButton.setOnClickListener {
			val hour: Int
			val minute: Int
			if (Build.VERSION.SDK_INT >= 23) {
				hour = timePicker.hour
				minute = timePicker.minute
			} else {
				hour = timePicker.currentHour
				minute = timePicker.currentMinute
			}

			val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
				putExtra(AlarmClock.EXTRA_HOUR, hour)
				putExtra(AlarmClock.EXTRA_MINUTES, minute)
				putExtra(AlarmClock.EXTRA_MESSAGE, labelEdit.text?.toString() ?: "")
				putExtra(AlarmClock.EXTRA_VIBRATE, vibrateCheck.isChecked)
				putExtra(AlarmClock.EXTRA_SKIP_UI, skipUiCheck.isChecked)
			}
			if (intent.resolveActivity(packageManager) != null) {
				startActivity(intent)
			}
		}
	}
}