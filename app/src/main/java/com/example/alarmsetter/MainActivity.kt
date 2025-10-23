package com.example.alarmsetter

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.provider.Settings
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.alarmsetter.sun.SunTimes
import com.google.android.gms.location.LocationServices
import java.util.Calendar

class MainActivity : AppCompatActivity() {
	private var pickedTone: Uri? = null
	private var lastLocation: Location? = null

	private val tonePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
		if (result.resultCode == Activity.RESULT_OK) {
			pickedTone = result.data?.data
		}
	}

	private val requestLocationPermission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { map ->
		val granted = map[Manifest.permission.ACCESS_FINE_LOCATION] == true || map[Manifest.permission.ACCESS_COARSE_LOCATION] == true
		if (granted) fetchLastLocation()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val timePicker: TimePicker = findViewById(R.id.timePicker)
		val labelEdit: EditText = findViewById(R.id.editLabel)
		val vibrateCheck: CheckBox = findViewById(R.id.checkVibrate)
		val skipUiCheck: CheckBox = findViewById(R.id.checkSkipUi)
		val setButton: Button = findViewById(R.id.buttonSetAlarm)
		val pickTone: Button = findViewById(R.id.buttonPickTone)
		val radio: RadioGroup = findViewById(R.id.radioSunChoice)
		val editOffset: EditText = findViewById(R.id.editOffsetMinutes)
		val editSnooze: EditText = findViewById(R.id.editSnoozeMinutes)
		val spinnerDifficulty: Spinner = findViewById(R.id.spinnerDifficulty)

		spinnerDifficulty.adapter = ArrayAdapter(
			this,
			android.R.layout.simple_spinner_dropdown_item,
			listOf("Easy", "Medium", "Hard")
		)

		pickTone.setOnClickListener {
			val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
				putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
				putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
				putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, pickedTone)
			}
			tonePicker.launch(intent)
		}

		setButton.setOnClickListener {
			val useSunrise = radio.checkedRadioButtonId == R.id.radioSunrise
			val useSunset = radio.checkedRadioButtonId == R.id.radioSunset
			val offsetMin = editOffset.text.toString().toIntOrNull() ?: 0
			val snoozeMin = editSnooze.text.toString().toIntOrNull() ?: 10
			val label = labelEdit.text?.toString() ?: ""

			if (!useSunrise && !useSunset) {
				val hour: Int
				val minute: Int
				if (Build.VERSION.SDK_INT >= 23) {
					hour = timePicker.hour
					minute = timePicker.minute
				} else {
					hour = timePicker.currentHour
					minute = timePicker.currentMinute
				}
				val cal = Calendar.getInstance().apply {
					set(Calendar.SECOND, 0)
					set(Calendar.MILLISECOND, 0)
					set(Calendar.HOUR_OF_DAY, hour)
					set(Calendar.MINUTE, minute)
					if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
				}
				schedule(cal.timeInMillis, label, vibrateCheck.isChecked, snoozeMin)
			} else {
				ensureLocationThenCompute { location ->
					val times = SunTimes.calculate(location.latitude, location.longitude)
					val base = if (useSunrise) times.sunrise else times.sunset
					base.add(Calendar.MINUTE, offsetMin)
					if (base.timeInMillis <= System.currentTimeMillis()) base.add(Calendar.DAY_OF_YEAR, 1)
					schedule(base.timeInMillis, label, vibrateCheck.isChecked, snoozeMin)
				}
			}
		}
	}

	private fun schedule(triggerAt: Long, label: String, vibrate: Boolean, snoozeMin: Int) {
		val extras = Intent("com.example.alarmsetter.ACTION_RING_ALARM").apply {
			putExtra("label", label)
			putExtra("vibrate", vibrate)
			putExtra("snooze", snoozeMin)
			putExtra("tone", pickedTone?.toString())
		}
		AlarmScheduler.scheduleExact(this, triggerAt, extras)
		Toast.makeText(this, "Alarm scheduled", Toast.LENGTH_SHORT).show()
	}

	private fun ensureLocationThenCompute(onReady: (Location) -> Unit) {
		val fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
		val coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
		if (!(fine || coarse)) {
			requestLocationPermission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
			Toast.makeText(this, "Location permission needed for sun times", Toast.LENGTH_SHORT).show()
			return
		}
		fetchLastLocation(onReady)
	}

	private fun fetchLastLocation(onReady: ((Location) -> Unit)? = null) {
		val client = LocationServices.getFusedLocationProviderClient(this)
		client.lastLocation.addOnSuccessListener { location ->
			if (location != null) {
				lastLocation = location
				onReady?.invoke(location)
			} else {
				Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
			}
		}
	}
}