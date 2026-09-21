package com.example.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

/**
 * High-precision monetary utility for FINITYARC.
 *
 * All amounts are stored and calculated as integer minor units (Long) where 1 unit = 1/100 of the currency.
 * For example:
 *   50.00 SAR -> 5000L
 *   12,450.75 SAR -> 1245075L
 *
 * This guarantees strict zero floating-point accumulation drift across offline ledgers.
 */
object CurrencyFormatter {

    /**
     * Formats a Long minor units amount into a human-readable currency string.
     * E.g. 5000L, "SAR" -> "50.00 SAR"
     */
    fun format(minorUnits: Long, currencyCode: String = "SAR"): String {
        val isNegative = minorUnits < 0L
        val absoluteValue = abs(minorUnits)
        val majorUnits = absoluteValue / 100L
        val minorRemainder = absoluteValue % 100L

        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ','
        }
        val majorFormatter = DecimalFormat("#,##0", symbols)
        val formattedMajor = majorFormatter.format(majorUnits)
        val formattedMinor = String.format(Locale.US, "%02d", minorRemainder)

        val signPrefix = if (isNegative) "-" else ""
        return "$signPrefix$formattedMajor.$formattedMinor $currencyCode"
    }

    /**
     * Formats with an explicit plus (+) or minus (-) sign.
     * Useful for transactions and cash flow cards.
     */
    fun formatSigned(minorUnits: Long, isExpense: Boolean, currencyCode: String = "SAR"): String {
        val prefix = if (isExpense) "- " else "+ "
        val absoluteMinorUnits = abs(minorUnits)
        return prefix + format(absoluteMinorUnits, currencyCode)
    }

    /**
     * Converts major decimal units string input into Long minor units safely.
     * E.g. "50.00" -> 5000L, "50" -> 5000L, "0.5" -> 50L.
     */
    fun parseToMinorUnits(input: String): Long? {
        val sanitized = input.trim().replace(",", "")
        if (sanitized.isEmpty()) return null

        val parts = sanitized.split(".")
        if (parts.size > 2) return null

        val majorPart = parts[0].toLongOrNull() ?: return null

        val minorPart = if (parts.size == 2) {
            val decimalStr = parts[1]
            when {
                decimalStr.isEmpty() -> 0L
                decimalStr.length == 1 -> (decimalStr + "0").toLongOrNull() ?: return null
                decimalStr.length == 2 -> decimalStr.toLongOrNull() ?: return null
                else -> decimalStr.substring(0, 2).toLongOrNull() ?: return null
            }
        } else {
            0L
        }

        val sign = if (majorPart < 0L || sanitized.startsWith("-")) -1L else 1L
        return (abs(majorPart) * 100L + minorPart) * sign
    }
}
