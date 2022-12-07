package com.yama.marshal.tool

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.yama.marshal.ui.theme.YamaColor.pace_ahead_color
import com.yama.marshal.ui.theme.YamaColor.pace_behind_color
import kotlin.math.abs
import kotlin.math.roundToInt

internal object PaceValueFormatter {

    enum class PaceType() {
        Full,
        Short
    }

    private fun getMinutes(pace: Int): Int {

        val h = pace / 3600
        val m = (pace % 3600) / 60
        val s = (pace % 3600) % 60

        var retVal = h * 60 + m

        if (s >= 30) {
            retVal += 1
        }

        return retVal
    }

    @Composable
    fun getString(pace: Int, type: PaceType): String {
        val minutes = getMinutes(pace)

        if (minutes == 0) {
            return stringResource("on_pace")
        } else {
            val builder = StringBuilder()
            builder.append(abs(minutes))
            builder.append(" ")
            builder.append(stringResource(if (abs(minutes) > 1) "mins" else "min"))
            builder.append(" ")
            builder.append(if (minutes > 0) stringResource("behind") else stringResource("ahead"))

            if (type == PaceType.Full) {
                builder.append(" ")
                builder.append(if (minutes > 0) stringResource("pace") else stringResource("of_pace"))
            }

            return builder.toString()
        }
    }

    @Composable
    fun getStringForCurrentPace(pace: Double): String {
        val minutes = getMinutes(pace.roundToInt())
        val builder = StringBuilder()
        builder.append(abs(minutes).toString())
        builder.append(" ")
        builder.append(if (abs(minutes) == 1) stringResource("min") else stringResource("mins"))
        builder.append(" ")
        builder.append(stringResource("current_pace"))
        return builder.toString()

    }

    @Composable
    fun getString(pace: Double, type: PaceType): String {
        return getString(pace.roundToInt(), type)
    }

    fun getColor(pace: Int): Color {
        return if (getMinutes(pace) <= 0)
            pace_ahead_color
        else pace_behind_color
    }

    fun getColor(pace: Double): Color {
        return getColor(pace.roundToInt())
    }


}
