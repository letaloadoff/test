package com.example.onlyone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			Surface(color = MaterialTheme.colorScheme.background) {
				OnlyOneGame()
			}
		}
	}
}

@Composable
fun OnlyOneGame() {
	var isRunning by remember { mutableStateOf(false) }
	var score by remember { mutableStateOf(0) }
	var best by remember { mutableStateOf(0) }
	val progress = remember { Animatable(0f) }
	var message by remember { mutableStateOf<String?>(null) }

	LaunchedEffect(isRunning) {
		if (isRunning) {
			progress.snapTo(0f)
			progress.animateTo(
				targetValue = 1f,
				animationSpec = tween(durationMillis = 30000, easing = LinearEasing)
			)
			score = 100
			best = maxOf(best, score)
			message = "You win!"
			isRunning = false
		}
	}

	LaunchedEffect(progress.value, isRunning) {
		if (isRunning) {
			score = (progress.value * 100f).toInt()
		}
	}

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color(0xFF0E0E10)),
		contentAlignment = Alignment.Center
	) {
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			Text(
				text = "Only One",
				color = Color.White,
				fontSize = 28.sp,
				fontWeight = FontWeight.Bold,
				modifier = Modifier.padding(bottom = 24.dp)
			)

			Canvas(modifier = Modifier
				.fillMaxSize()
				.weight(1f)
				.padding(horizontal = 24.dp)) {
				val barWidth = size.width
				val barHeight = 18.dp.toPx()
				val y = size.height / 2f
				// Background bar
				drawRoundRect(
					color = Color.DarkGray,
					topLeft = androidx.compose.ui.geometry.Offset((size.width - barWidth) / 2f, y - barHeight / 2f),
					size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
					cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2f, barHeight / 2f)
				)
				// Progress fill
				drawRoundRect(
					color = Color(0xFF00E676),
					topLeft = androidx.compose.ui.geometry.Offset((size.width - barWidth) / 2f, y - barHeight / 2f),
					size = androidx.compose.ui.geometry.Size(barWidth * progress.value, barHeight),
					cornerRadius = androidx.compose.ui.geometry.CornerRadius(barHeight / 2f, barHeight / 2f)
				)
			}


			Text(
				text = if (isRunning) "Don't press it..." else "Press the only one",
				color = Color.White,
				modifier = Modifier.padding(vertical = 16.dp)
			)

			message?.let {
				Text(text = it, color = Color(0xFFFFC107))
			}

			Button(onClick = {
				if (!isRunning) {
					isRunning = true
					message = null
					score = 0
				} else {
					// Player pressed during run -> game over
					isRunning = false
					best = maxOf(best, score)
					message = "Game over!"
				}
			}) {
				Text(if (!isRunning) "Start" else "Only One!")
			}

			Text(
				text = "Score: $score    Best: $best",
				color = Color(0xFFB0BEC5),
				modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
			)
		}
	}
}

