package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * High-performance, zero-drift currency formatting utility for FINITYARC.
 *
 * Ensures all monetary representations in Compose UI stem directly from integer minor units
 * without binary floating-point conversions.
 */
object CurrencyFormatter {

    /**
     * Converts integer minor units (halalas/cents) to presentation string.
     * E.g.:
     *  5000L -> "50.00 SAR"
     *  1250L -> "12.50 SAR"
     *  0L    -> "0.00 SAR"
     */
    fun formatMinorUnits(minorUnits: Long, currency: String = "SAR"): String {
        val isNegative = minorUnits < 0L
        val absValue = kotlin.math.abs(minorUnits)
        val majorUnits = absValue / 100L
        val minorRemainder = absValue % 100L

        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val majorFormatter = DecimalFormat("#,##0", symbols)
        val formattedMajor = majorFormatter.format(majorUnits)
        val formattedMinor = String.format(Locale.US, "%02d", minorRemainder)
        val prefix = if (isNegative) "-" else ""

        return "$prefix$formattedMajor.$formattedMinor $currency"
    }

    /**
     * Converts a raw numeric string buffer typed into the keypad into currency format.
     * E.g., "1250" -> "12.50 SAR", "" -> "0.00 SAR".
     */
    fun formatRawBuffer(rawBuffer: String, currency: String = "SAR"): String {
        val minorUnits = rawBuffer.toLongOrNull() ?: 0L
        return formatMinorUnits(minorUnits, currency)
    }
}
