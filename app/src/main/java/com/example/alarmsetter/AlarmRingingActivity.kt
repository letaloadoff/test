package com.example.alarmsetter

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AlarmRingingActivity : AppCompatActivity() {
	private var mediaPlayer: MediaPlayer? = null
	private var fadeTimer: CountDownTimer? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		window.addFlags(
			WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
			WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
			WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
		)
		setContentView(R.layout.activity_ringing)

		val problemView: TextView = findViewById(R.id.textProblem)
		val answerEdit: EditText = findViewById(R.id.editAnswer)
		val dismissBtn: Button = findViewById(R.id.buttonDismiss)
		val snoozeBtn: Button = findViewById(R.id.buttonSnooze)

		val a = (2..9).random()
		val b = (2..9).random()
		problemView.text = "$a × $b = ?"
		val correct = a * b

		startRingtoneWithFade(intent.getStringExtra("tone")?.let { Uri.parse(it) })

		dismissBtn.setOnClickListener {
			if (answerEdit.text.toString().toIntOrNull() == correct) {
				stopRinging()
				finish()
			} else {
				answerEdit.error = "Try again"
			}
		}

		snoozeBtn.setOnClickListener {
			stopRinging()
			finish()
		}
	}

	private fun startRingtoneWithFade(uri: Uri?) {
		val toneUri = uri ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
		mediaPlayer = MediaPlayer().apply {
			setAudioAttributes(
				AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_ALARM)
					.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
					.build()
			)
			setDataSource(this@AlarmRingingActivity, toneUri)
			isLooping = true
			prepare()
			setVolume(0f, 0f)
			start()
		}
		fadeTimer = object : CountDownTimer(15000, 500) {
			override fun onTick(millisUntilFinished: Long) {
				val progress = 1f - (millisUntilFinished / 15000f)
				mediaPlayer?.setVolume(progress, progress)
			}
			override fun onFinish() {
				mediaPlayer?.setVolume(1f, 1f)
			}
		}.start()
	}

	private fun stopRinging() {
		fadeTimer?.cancel()
		mediaPlayer?.stop()
		mediaPlayer?.release()
		mediaPlayer = null
	}

	override fun onDestroy() {
		super.onDestroy()
		stopRinging()
	}
}