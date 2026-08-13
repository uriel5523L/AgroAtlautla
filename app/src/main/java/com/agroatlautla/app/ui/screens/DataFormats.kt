package com.agroatlautla.app.ui.screens

import com.agroatlautla.app.data.local.CropEntity
import java.util.Locale

private val HectaresRegex = Regex("([0-9]+(?:\\.[0-9]+)?)\\s*ha", RegexOption.IGNORE_CASE)

fun parseHectares(area: String): Double {
    return HectaresRegex.find(area)?.groupValues?.get(1)?.toDoubleOrNull() ?: 0.0
}

fun formatHectares(value: Double): String {
    return if (value % 1.0 == 0.0) value.toInt().toString() else String.format(Locale.US, "%.1f", value)
}

fun cropIconText(crop: CropEntity): String = when (crop.icon) {
    "corn" -> "M"
    "bean" -> "F"
    "wheat" -> "A"
    else -> "H"
}