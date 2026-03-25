package cc.webbiii.app.openplural

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import com.google.gson.FormattingStyle
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.Instant
import java.time.OffsetDateTime
import java.util.Date
import kotlin.math.abs
import kotlin.math.absoluteValue

const val DEFAULT_RESOURCE_COLOR = 16777215
val GSON: Gson = GsonBuilder().setFormattingStyle(FormattingStyle.COMPACT).create()

fun Color.brighter(d: Int): Color {
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()

    val newR = (r + d) / 255f
    val newG = (g + d) / 255f
    val newB = (b + d) / 255f

    return Color(newR, newG, newB)
}

fun Color.toRgb(): Int {
    return toArgb() and 16777215
}

fun Color.toHtml(): String {
    return "#" + toRgb().toString(16)
}

fun htmlColor(color: String): Color {
    var color = color
    if (color.startsWith("#")) {
        color = color.substring(1);
    }

    val rgb = color.toInt(16)
    return opaque(rgb)
}

fun opaque(color: Int): Color {
    val r = (color shr 16) and 0xFF
    val g = (color shr 8) and 0xFF
    val b = color and 0xFF
    return Color(r, g, b)
}

fun Modifier.drawOneSideBorder(
    width: Dp,
    color: Color,
    shape: Shape = RectangleShape
) = this.clip(shape)
    .drawWithContent {
        val widthPx = width.toPx()
        drawContent()
        drawLine(
            color = color,
            start = Offset(widthPx / 2, 0f),
            end = Offset(widthPx / 2, size.height),
            strokeWidth = widthPx
        )
    }

fun Modifier.underline(
    thickness: Dp,
    color: Color,
    shape: Shape = RectangleShape
) = this.clip(shape)
    .drawWithContent {
        val thicknessPx = thickness.toPx()
        drawContent()
        drawLine(
            color = color,
            start = Offset(0f, size.height - thicknessPx / 2),
            end = Offset(size.width, size.height - thicknessPx / 2),
            strokeWidth = thicknessPx
        )
    }

fun Date.toIso8601(): String {
    return toInstant().toString()
}

@SuppressLint("DefaultLocale")
fun Date.duration(to: Date? = null): String {
    val to = to?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis()
    val from = toInstant().toEpochMilli()
    val delta = abs(to - from)
    var seconds = delta / 1000
    var minutes = seconds / 60
    var hours = minutes / 60
    val days = hours / 24

    seconds %= 60
    minutes %= 60
    hours %= 24

    val hoursFormatted = String.format("%02d", hours)
    val minutesFormatted = String.format("%02d", minutes)
    val secondsFormatted = String.format("%02d", seconds)

    var result = ""
    if (days > 0) {
        val daysFormatted = String.format("%02d", days)
        result += "$daysFormatted:"
    }

    return "$result$hoursFormatted:$minutesFormatted:$secondsFormatted"
}

fun dateFromIso8601(input: String): Date {
    return Date.from(OffsetDateTime.parse(input).toInstant())
}

fun nowIso8601(): String {
    return Instant.now().toString()
}

inline fun <reified T> concat(array: Array<T>?, element: T): Array<T> {
    return array?.plus(element) ?: arrayOf(element)
}

fun getDeviceName(): String {
    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    return if (model.startsWith(manufacturer)) {
        capitalize(model)
    } else {
        capitalize(manufacturer) + " " + model
    }
}

fun capitalize(s: String): String {
    if (s.isEmpty()) return s
    return s.substring(0, 1).uppercase() + s.substring(1)
}

fun appVersion(ctx: Context?): PackageInfo? {
    return ctx?.packageName?.let {
        ctx.packageManager?.getPackageInfo(it, PackageManager.GET_ACTIVITIES)
    }
}

class Either<L, R> private constructor(
    val left: L?,
    val right: R?
) {
    companion object {
        fun <L, R> left(value: L): Either<L, R> = Either(value, null)
        fun <L, R> right(value: R): Either<L, R> = Either(null, value)
    }

    fun isLeft(): Boolean = left != null
    fun isRight(): Boolean = right != null
}