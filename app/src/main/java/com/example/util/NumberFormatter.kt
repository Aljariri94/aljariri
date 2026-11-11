package com.example.util

import com.example.model.DisplayFormatMode
import com.example.model.NumeralStyle
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object NumberFormatter {

    private val westernSymbols = DecimalFormatSymbols(Locale.US).apply {
        groupingSeparator = ','
        decimalSeparator = '.'
    }

    private val df = DecimalFormat("#,##0.##########", westernSymbols)

    const val ZWSP = "\u200B" // Zero-Width Space: Allowed line break opportunity before operators
    const val WJ = "\u2060"   // Word Joiner: Prohibits line breaks inside numbers, commas, and dots

    fun toSuperscript(text: String): String {
        val map = mapOf(
            '0' to '⁰',
            '1' to '¹',
            '2' to '²',
            '3' to '³',
            '4' to '⁴',
            '5' to '⁵',
            '6' to '⁶',
            '7' to '⁷',
            '8' to '⁸',
            '9' to '⁹',
            '-' to '⁻',
            '+' to '⁺'
        )
        return text.map { map[it] ?: it }.joinToString("")
    }

    fun formatScientificWithSuperscript(number: BigDecimal, maxDecimals: Int = 10): String {
        val pattern = if (maxDecimals <= 0) "0E0" else "0." + "#".repeat(maxDecimals) + "E0"
        val raw = DecimalFormat(pattern, westernSymbols).format(number)
        val parts = raw.split("E", "e")
        if (parts.size == 2) {
            val mantissa = parts[0]
            val exp = parts[1].replace("+", "")
            return "$mantissa×10${toSuperscript(exp)}"
        }
        return raw
    }

    fun formatWithPhysicalMode(
        number: BigDecimal,
        mode: DisplayFormatMode = DisplayFormatMode.NORM,
        fixPlaces: Int = 2,
        sciDigits: Int = 2,
        style: NumeralStyle = NumeralStyle.WESTERN,
        includeGrouping: Boolean = true
    ): String {
        val formatted = try {
            val abs = number.abs()
            val isExtreme = abs >= BigDecimal("1000000000000000") || (abs > BigDecimal.ZERO && abs < BigDecimal("0.000001"))

            when {
                mode == DisplayFormatMode.SCI -> {
                    formatScientificWithSuperscript(number, sciDigits.coerceIn(0, 10))
                }
                isExtreme -> {
                    // Automatically shorten huge or micro numbers to clean scientific notation like ClevCalc!
                    formatScientificWithSuperscript(number, 10)
                }
                mode == DisplayFormatMode.FIX -> {
                    val places = fixPlaces.coerceIn(0, 9)
                    val pattern = if (places == 0) {
                        if (includeGrouping) "#,##0" else "0"
                    } else {
                        val zeros = "0".repeat(places)
                        if (includeGrouping) "#,##0.$zeros" else "0.$zeros"
                    }
                    val rounded = number.setScale(places, RoundingMode.HALF_UP)
                    DecimalFormat(pattern, westernSymbols).format(rounded)
                }
                else -> {
                    val pattern = if (includeGrouping) "#,##0.##########" else "0.##########"
                    DecimalFormat(pattern, westernSymbols).format(number)
                }
            }
        } catch (_: Exception) {
            number.toPlainString()
        }

        return if (style == NumeralStyle.EASTERN) {
            toEasternNumerals(formatted)
        } else {
            formatted
        }
    }

    fun formatNumber(number: BigDecimal, style: NumeralStyle = NumeralStyle.WESTERN): String {
        return formatWithPhysicalMode(number = number, mode = DisplayFormatMode.NORM, style = style)
    }

    private fun isOperatorChar(c: Char): Boolean =
        c == '+' || c == '-' || c == '−' || c == '×' || c == '÷' || c == '*' || c == '/'

    fun formatDisplay(rawString: String, style: NumeralStyle = NumeralStyle.WESTERN): String {
        if (rawString.isEmpty()) return if (style == NumeralStyle.EASTERN) "٠" else "0"

        val result = StringBuilder()
        var currentNum = StringBuilder()

        for (char in rawString) {
            // CRITICAL: Skip any existing commas or zero-width formatting characters so we never duplicate
            if (char == ',' || char == '،' || char == '\u2060' || char == '\u200B') {
                continue
            }

            if (char.isDigit() || char == '.') {
                currentNum.append(char)
            } else {
                if (currentNum.isNotEmpty()) {
                    result.append(formatNumberPart(currentNum.toString()))
                    currentNum = StringBuilder()
                }
                // Allowed break point before operators when preceded by content
                if (isOperatorChar(char) && result.isNotEmpty()) {
                    result.append(ZWSP)
                }
                result.append(char)
            }
        }

        if (currentNum.isNotEmpty()) {
            result.append(formatNumberPart(currentNum.toString()))
        }

        val out = result.toString()
        return if (style == NumeralStyle.EASTERN) toEasternNumerals(out) else out
    }

    private fun formatNumberPart(numStr: String): String {
        if (numStr.isEmpty()) return ""
        val parts = numStr.split(".")
        val intPart = parts[0]

        // Strict 3-digit grouping from the right: (000,000,000,000,000)
        // Bonded with Word Joiner (WJ) so numbers NEVER split across lines
        val formattedInt = if (intPart.isNotEmpty()) {
            val isNeg = intPart.startsWith("-") || intPart.startsWith("−")
            val digits = if (isNeg) intPart.substring(1) else intPart
            if (digits.isEmpty()) {
                if (isNeg) intPart.take(1) else ""
            } else {
                val sb = java.lang.StringBuilder()
                if (isNeg) sb.append(intPart.take(1))
                val len = digits.length
                for (i in 0 until len) {
                    if (i > 0 && (len - i) % 3 == 0) {
                        sb.append(WJ).append(',').append(WJ)
                    }
                    sb.append(digits[i])
                }
                sb.toString()
            }
        } else {
            ""
        }

        return if (parts.size > 1) {
            "$formattedInt$WJ.$WJ${parts[1]}"
        } else if (numStr.endsWith(".")) {
            "$formattedInt$WJ.$WJ"
        } else {
            formattedInt
        }
    }

    private fun isSignificantChar(c: Char): Boolean =
        c != ',' && c != '،' && c != '\u2060' && c != '\u200B'

    fun mapCursorPosition(original: String, originalCursor: Int, formatted: String): Int {
        val safeOrigCursor = originalCursor.coerceIn(0, original.length)
        var significantCount = 0
        for (i in 0 until safeOrigCursor) {
            val c = original[i]
            if (isSignificantChar(c)) {
                significantCount++
            }
        }
        var newCursor = 0
        var matched = 0
        for (i in formatted.indices) {
            if (matched == significantCount) {
                newCursor = i
                break
            }
            val c = formatted[i]
            if (isSignificantChar(c)) {
                matched++
            }
            newCursor = i + 1
        }
        return newCursor.coerceIn(0, formatted.length)
    }

    fun unmapCursorPosition(formatted: String, formattedCursor: Int): Int {
        val safeCursor = formattedCursor.coerceIn(0, formatted.length)
        var count = 0
        for (i in 0 until safeCursor) {
            val c = formatted[i]
            if (isSignificantChar(c)) {
                count++
            }
        }
        return count
    }

    fun toEasternNumerals(input: String): String {
        val easternDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val chars = input.toCharArray()
        for (i in chars.indices) {
            val c = chars[i]
            if (c in '0'..'9') {
                chars[i] = easternDigits[c - '0']
            }
        }
        return String(chars)
    }

    fun toWesternNumerals(input: String): String {
        val clean = input
            .replace(WJ, "")
            .replace(ZWSP, "")
        val chars = clean.toCharArray()
        for (i in chars.indices) {
            val c = chars[i]
            if (c in '٠'..'٩') {
                chars[i] = ('0' + (c - '٠'))
            }
        }
        return String(chars)
    }
}
