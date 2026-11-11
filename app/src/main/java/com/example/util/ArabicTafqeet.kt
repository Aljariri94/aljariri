package com.example.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Converts numbers into Arabic spelled-out words (Tafqeet / تفقيط الأرقام).
 * Essential for commercial transactions, cheques, invoices, and accounting.
 */
object ArabicTafqeet {

    private val ones = arrayOf(
        "", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة",
        "عشرة", "أحد عشر", "اثنا عشر", "ثلاثة عشر", "أربعة عشر", "خمسة عشر", "ستة عشر",
        "سبعة عشر", "ثمانية عشر", "تسعة عشر"
    )

    private val tens = arrayOf(
        "", "", "عشرون", "ثلاثون", "أربعون", "خمسون", "ستون", "سبعون", "ثمانون", "تسعون"
    )

    private val hundreds = arrayOf(
        "", "مائة", "مئتان", "ثلاثمائة", "أربعمائة", "خمسمائة", "ستمائة", "سبعمائة", "ثمانمائة", "تسعمائة"
    )

    private val scales = arrayOf(
        ScaleNames("", "", "", ""),
        ScaleNames("ألف", "ألفان", "آلاف", "ألفاً"),
        ScaleNames("مليون", "مليونان", "ملايين", "مليوناً"),
        ScaleNames("مليار", "ملياران", "مليارات", "ملياراً"),
        ScaleNames("تريليون", "تريليونان", "تريليونات", "تريليوناً")
    )

    private data class ScaleNames(
        val singular: String,
        val dual: String,
        val plural: String,
        val accusative: String
    )

    fun convert(amount: BigDecimal, currencyName: String = "ريال", fractionalName: String = "هللة"): String {
        if (amount.compareTo(BigDecimal.ZERO) == 0) {
            return "صفر $currencyName"
        }

        val isNegative = amount.signum() < 0
        val positiveAmount = amount.abs()

        // Guard against overflow for numbers >= 1e15
        if (positiveAmount >= BigDecimal("1000000000000000")) {
            return ""
        }

        val integerPart = positiveAmount.toBigInteger()
        val fractionalPart = positiveAmount.remainder(BigDecimal.ONE)
            .multiply(BigDecimal(100))
            .setScale(0, RoundingMode.HALF_UP)
            .toInt()

        val intWords = convertInteger(integerPart.toLong())
        val sb = StringBuilder()

        if (isNegative) {
            sb.append("سالب ")
        }

        if (intWords.isNotEmpty()) {
            sb.append(intWords)
            if (currencyName.isNotBlank()) {
                sb.append(" ").append(currencyName)
            }
        }

        if (fractionalPart > 0) {
            val fracWords = convertInteger(fractionalPart.toLong())
            if (sb.isNotEmpty()) {
                sb.append(" و ")
            }
            sb.append(fracWords)
            if (fractionalName.isNotBlank()) {
                sb.append(" ").append(fractionalName)
            }
        }

        return sb.toString().trim()
    }

    private fun convertInteger(number: Long): String {
        if (number == 0L) return ""
        if (number < 0L) return "سالب " + convertInteger(-number)

        val groups = mutableListOf<Int>()
        var temp = number
        while (temp > 0) {
            groups.add((temp % 1000).toInt())
            temp /= 1000
        }

        val parts = mutableListOf<String>()

        for (i in groups.indices.reversed()) {
            val group = groups[i]
            if (group == 0) continue

            val groupText = convertGroup(group)
            val scale = scales.getOrNull(i)

            if (scale == null || scale.singular.isEmpty()) {
                parts.add(groupText)
            } else {
                when {
                    group == 1 -> parts.add(scale.singular)
                    group == 2 -> parts.add(scale.dual)
                    group in 3..10 -> parts.add("$groupText ${scale.plural}")
                    else -> parts.add("$groupText ${scale.accusative}")
                }
            }
        }

        return parts.joinToString(" و ")
    }

    private fun convertGroup(number: Int): String {
        val h = number / 100
        val remainder = number % 100
        val t = remainder / 10
        val o = remainder % 10

        val parts = mutableListOf<String>()

        if (h > 0) {
            parts.add(hundreds[h])
        }

        when {
            remainder in 1..19 -> {
                parts.add(ones[remainder])
            }
            remainder >= 20 -> {
                if (o > 0) {
                    parts.add("${ones[o]} و ${tens[t]}")
                } else {
                    parts.add(tens[t])
                }
            }
        }

        return parts.joinToString(" و ")
    }
}
