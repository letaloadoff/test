package com.example.alarmsetter.sun

import java.util.Calendar
import kotlin.math.*

object SunTimes {
	data class Result(val sunrise: Calendar, val sunset: Calendar)

	fun calculate(lat: Double, lon: Double, date: Calendar = Calendar.getInstance()): Result {
		val cal = date.clone() as Calendar
		cal.set(Calendar.SECOND, 0)
		cal.set(Calendar.MILLISECOND, 0)
		val tzOffsetHours = cal.timeZone.getOffset(cal.timeInMillis) / 3600000.0
		val n = dayOfYear(cal)
		val lngHour = lon / 15.0
		val sunriseTime = calcSunTime(n, lat, lon, true, tzOffsetHours, lngHour)
		val sunsetTime = calcSunTime(n, lat, lon, false, tzOffsetHours, lngHour)
		val sunriseCal = (date.clone() as Calendar).apply {
			set(Calendar.HOUR_OF_DAY, sunriseTime.first)
			set(Calendar.MINUTE, sunriseTime.second)
		}
		val sunsetCal = (date.clone() as Calendar).apply {
			set(Calendar.HOUR_OF_DAY, sunsetTime.first)
			set(Calendar.MINUTE, sunsetTime.second)
		}
		return Result(sunriseCal, sunsetCal)
	}

	private fun calcSunTime(n: Int, lat: Double, lon: Double, isSunrise: Boolean, tz: Double, lngHour: Double): Pair<Int, Int> {
		val t = if (isSunrise) n + ((6 - lngHour) / 24) else n + ((18 - lngHour) / 24)
		val m = (0.9856 * t) - 3.289
		var l = m + (1.916 * sin(Math.toRadians(m))) + (0.020 * sin(Math.toRadians(2 * m))) + 282.634
		l = (l + 360) % 360
		var ra = Math.toDegrees(atan(0.91764 * tan(Math.toRadians(l))))
		ra = (ra + 360) % 360
		val lQuadrant = (floor(l / 90) * 90)
		val raQuadrant = (floor(ra / 90) * 90)
		ra = ra + (lQuadrant - raQuadrant)
		ra /= 15.0
		val sinDec = 0.39782 * sin(Math.toRadians(l))
		val cosDec = cos(asin(sinDec))
		val cosH = (cos(Math.toRadians(90.833)) - (sinDec * sin(Math.toRadians(lat)))) / (cosDec * cos(Math.toRadians(lat)))
		val h = if (isSunrise) 360 - Math.toDegrees(acos(cosH)) else Math.toDegrees(acos(cosH))
		val hHours = h / 15.0
		var tLocal = hHours + ra - (0.06571 * t) - 6.622
		var localTime = (tLocal - lngHour + tz) % 24
		if (localTime < 0) localTime += 24
		val hour = floor(localTime).toInt()
		val minute = floor((localTime - hour) * 60).toInt()
		return hour to minute
	}

	private fun dayOfYear(cal: Calendar): Int {
		return cal.get(Calendar.DAY_OF_YEAR)
	}
}