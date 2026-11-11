package com.example

import com.example.util.ArabicTafqeet
import com.example.util.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class ExampleUnitTest {

    @Test
    fun testDoubleZeroHandling() {
        assertEquals("0", CalculatorEngine.appendDoubleZero("0"))
        assertEquals("500", CalculatorEngine.appendDoubleZero("5"))
        assertEquals("1200", CalculatorEngine.appendDoubleZero("12"))
        assertEquals("5+0", CalculatorEngine.appendDoubleZero("5+"))
    }

    @Test
    fun testTripleZeroHandling() {
        assertEquals("0", CalculatorEngine.appendTripleZero("0"))
        assertEquals("5000", CalculatorEngine.appendTripleZero("5"))
        val oneMillion = CalculatorEngine.appendTripleZero("1000")
        assertEquals("1000000", oneMillion)
    }

    @Test
    fun testSmartParentheses() {
        assertEquals("(", CalculatorEngine.appendSmartParentheses(""))
        assertEquals("5×(", CalculatorEngine.appendSmartParentheses("5"))
        assertEquals("(5+3)", CalculatorEngine.appendSmartParentheses("(5+3"))
    }

    @Test
    fun testPrecisionEvaluation() {
        // Multi-operator with precedence: 10 + 20 × 3 = 70 (not 90)
        val result1 = CalculatorEngine.evaluate("10+20×3")
        assertEquals("70", CalculatorEngine.toCleanString(result1!!))

        // Parentheses: (10 + 20) × 3 = 90
        val result2 = CalculatorEngine.evaluate("(10+20)×3")
        assertEquals("90", CalculatorEngine.toCleanString(result2!!))

        // High precision decimal math without floating point drift: 0.1 + 0.2 = 0.3
        val result3 = CalculatorEngine.evaluate("0.1+0.2")
        assertEquals("0.3", CalculatorEngine.toCleanString(result3!!))

        // Large commercial sums with thousands/millions
        val result4 = CalculatorEngine.evaluate("5000000+2500000")
        assertEquals("7500000", CalculatorEngine.toCleanString(result4!!))

        val userTest = CalculatorEngine.evaluate("36842.34+854689.6+975357.8+37803257+9875")
        println("User test result: $userTest")
        assertEquals("39680021.74", CalculatorEngine.toCleanString(userTest!!))
    }

    @Test
    fun testPhysicalCalculatorDisplayModes() {
        val sample = BigDecimal("6478.64000060")

        // FIX 2 mode pads/rounds to exactly 2 decimals
        val fix2 = com.example.util.NumberFormatter.formatWithPhysicalMode(
            number = sample,
            mode = com.example.model.DisplayFormatMode.FIX,
            fixPlaces = 2
        )
        assertEquals("6,478.64", fix2)

        // Integer in FIX 2 mode: 5 -> 5.00
        val fix2Int = com.example.util.NumberFormatter.formatWithPhysicalMode(
            number = BigDecimal("5"),
            mode = com.example.model.DisplayFormatMode.FIX,
            fixPlaces = 2
        )
        assertEquals("5.00", fix2Int)

        // NORM mode: does not pad trailing zeros
        val norm = com.example.util.NumberFormatter.formatWithPhysicalMode(
            number = sample,
            mode = com.example.model.DisplayFormatMode.NORM
        )
        assertEquals("6,478.6400006", norm)

        // SCI 2 mode: 6478.64 -> 6.48×10³
        val sci = com.example.util.NumberFormatter.formatWithPhysicalMode(
            number = sample,
            mode = com.example.model.DisplayFormatMode.SCI,
            sciDigits = 2
        )
        assertEquals("6.48×10³", sci)
    }

    @Test
    fun testTaxCalculations() {
        val amount = BigDecimal("100")
        val taxRate = BigDecimal("15")

        // TAX+ (100 + 15% = 115)
        val (totalWithTax, taxAmount) = CalculatorEngine.calculateTaxPlus(amount, taxRate)
        assertEquals("115", CalculatorEngine.toCleanString(totalWithTax))
        assertEquals("15", CalculatorEngine.toCleanString(taxAmount))

        // TAX- (115 with 15% tax extracted = 100 base, 15 tax)
        val (baseAmount, extractedTax) = CalculatorEngine.calculateTaxMinus(BigDecimal("115"), taxRate)
        assertEquals("100", CalculatorEngine.toCleanString(baseAmount))
        assertEquals("15", CalculatorEngine.toCleanString(extractedTax))
    }

    @Test
    fun testDiscountCalculations() {
        val amount = BigDecimal("200")
        val discountRate = BigDecimal("10")

        val (finalPrice, discountAmount) = CalculatorEngine.calculateDiscount(amount, discountRate)
        assertEquals("180", CalculatorEngine.toCleanString(finalPrice))
        assertEquals("20", CalculatorEngine.toCleanString(discountAmount))
    }

    @Test
    fun testArabicTafqeet() {
        val word1000 = ArabicTafqeet.convert(BigDecimal("1000"), "ريال", "هللة")
        assertTrue(word1000.contains("ألف") && word1000.contains("ريال"))

        val wordMillion = ArabicTafqeet.convert(BigDecimal("1500000"), "ريال", "هللة")
        assertTrue(wordMillion.contains("مليون") && wordMillion.contains("خمسمائة ألف") && wordMillion.contains("ريال"))
    }

    @Test
    fun testInvoiceEntryCountingAndDuplicates() {
        // User example: 1 + 41 + 3 -> 3 entries
        val entries = CalculatorEngine.extractInvoiceEntries("1+41+3")
        assertEquals(3, entries.size)
        assertEquals("1", entries[0].amount)
        assertEquals(1, entries[0].index)
        assertEquals("41", entries[1].amount)
        assertEquals(2, entries[1].index)
        assertEquals("3", entries[2].amount)
        assertEquals(3, entries[2].index)
        assertTrue(entries.none { it.isDuplicate })

        // Duplicate test: 100 + 41 + 100 -> entries 0 and 2 are duplicates
        val dupEntries = CalculatorEngine.extractInvoiceEntries("100+41+100")
        assertEquals(3, dupEntries.size)
        assertTrue(dupEntries[0].isDuplicate)
        assertTrue(dupEntries[2].isDuplicate)
        assertEquals(listOf(3), dupEntries[0].duplicateWithIndices)
        assertEquals(listOf(1), dupEntries[2].duplicateWithIndices)
        assertEquals(false, dupEntries[1].isDuplicate)
    }

    @Test
    fun testScientificFunctionsEvaluation() {
        // sin(30) in degree mode = 0.5
        val sin30 = CalculatorEngine.evaluate("sin(30)", isDegreeMode = true)
        assertEquals("0.5", CalculatorEngine.toCleanString(sin30!!))

        // cos(60) in degree mode = 0.5
        val cos60 = CalculatorEngine.evaluate("cos(60)", isDegreeMode = true)
        assertEquals("0.5", CalculatorEngine.toCleanString(cos60!!))

        // tan(45) in degree mode = 1
        val tan45 = CalculatorEngine.evaluate("tan(45)", isDegreeMode = true)
        assertEquals("1", CalculatorEngine.toCleanString(tan45!!))

        // log(100) = 2
        val log100 = CalculatorEngine.evaluate("log(100)")
        assertEquals("2", CalculatorEngine.toCleanString(log100!!))

        // sqrt(16) = 4
        val sqrt16 = CalculatorEngine.evaluate("√(16)")
        assertEquals("4", CalculatorEngine.toCleanString(sqrt16!!))

        // Power 2^3 = 8
        val pow = CalculatorEngine.evaluate("2^3")
        assertEquals("8", CalculatorEngine.toCleanString(pow!!))

        // Factorial 5! = 120
        val fact5 = CalculatorEngine.evaluate("5!")
        assertEquals("120", CalculatorEngine.toCleanString(fact5!!))

        // Combined: 10 + sin(30) × 4 = 10 + 0.5 × 4 = 12
        val combined = CalculatorEngine.evaluate("10+sin(30)×4", isDegreeMode = true)
        assertEquals("12", CalculatorEngine.toCleanString(combined!!))
    }

    @Test
    fun testDigitCountLimitAndSuperscriptFormatting() {
        val expr = "999,999,999,999,999+123"
        val countFirst = CalculatorEngine.getDigitCountOfCurrentNumber(expr, 5)
        assertEquals(15, countFirst)

        val countSecond = CalculatorEngine.getDigitCountOfCurrentNumber(expr, 21)
        assertEquals(3, countSecond)

        // Test scientific superscript format for large number (matching user screenshot 1×10^105)
        val bigNum = BigDecimal("1E105")
        val formatted = com.example.util.NumberFormatter.formatWithPhysicalMode(bigNum)
        assertEquals("1×10¹⁰⁵", formatted)
    }

    @Test
    fun testCommaGroupingThreeDigits() {
        val raw = "123456789012345"
        val formatted = com.example.util.NumberFormatter.formatDisplay(raw)
        assertEquals("123,456,789,012,345", com.example.util.NumberFormatter.toWesternNumerals(formatted))

        // Multiple numbers with operator
        val expr = "1000+2500000×50"
        val formattedExpr = com.example.util.NumberFormatter.formatDisplay(expr)
        assertEquals("1,000+2,500,000×50", com.example.util.NumberFormatter.toWesternNumerals(formattedExpr))
    }

    @Test
    fun testPastedNumberSanitizationAndClamping() {
        // Arabic numerals pasted with commas
        val rawArabic = "١,٢٣٤,٥٦٧"
        val converted = com.example.util.NumberFormatter.toWesternNumerals(rawArabic)
            .replace(",", "")
            .replace("،", "")
        assertEquals("1234567", converted)
        val formatted = com.example.util.NumberFormatter.formatDisplay(converted)
        assertEquals("1,234,567", com.example.util.NumberFormatter.toWesternNumerals(formatted))

        // Deleting from formatted text with deleteAt
        val (afterDelete, _) = CalculatorEngine.deleteAt(formatted, formatted.length, formatted.length)
        val reformatted = com.example.util.NumberFormatter.toWesternNumerals(afterDelete)
        assertEquals("1,234,56", reformatted)
    }

    @Test
    fun testNumberWrappingNonBreakingGlue() {
        val expr = "2539.8+612.72"
        val formatted = com.example.util.NumberFormatter.formatDisplay(expr)
        
        // Assert Word Joiner (\u2060) is present around dot to prevent splitting inside numbers (Image 1 fix)
        assertTrue(formatted.contains("\u2060.\u2060"))
        // Assert Zero-Width Space (\u200B) is placed before the operator for clean wrapping before operators (Image 2)
        assertTrue(formatted.contains("\u200B+"))

        // When cleaned with toWesternNumerals, zero-width characters are removed
        val cleaned = com.example.util.NumberFormatter.toWesternNumerals(formatted)
        assertEquals("2,539.8+612.72", cleaned)
    }

    @Test
    fun testSmartInputOperatorReplacement() {
        // Repeated operator does not repeat: 5+ then + => 5+
        val (res1, _) = CalculatorEngine.smartInsert("5+", 2, "+", isOperator = true)
        assertEquals("5+", res1)

        // Operator replaces previous operator: 5+ then - => 5− (Task 4)
        val (res2, _) = CalculatorEngine.smartInsert("5+", 2, "-", isOperator = true)
        assertEquals("5−", res2)

        // Operator replaces previous operator: 5× then ÷ => 5÷
        val (res3, _) = CalculatorEngine.smartInsert("5×", 2, "÷", isOperator = true)
        assertEquals("5÷", res3)

        // No operator at start except '-' or '−'
        val (resStartPlus, _) = CalculatorEngine.smartInsert("", 0, "+", isOperator = true)
        assertEquals("", resStartPlus)

        val (resStartMinus, _) = CalculatorEngine.smartInsert("", 0, "-", isOperator = true)
        assertEquals("−", resStartMinus)
    }

    @Test
    fun testSmartInputDotPrevention() {
        // Dot at start becomes "0."
        val (res1, _) = CalculatorEngine.smartInsert("", 0, ".")
        assertEquals("0.", res1)

        // Pressing dot again when number already has dot is rejected (No "..")
        val (res2, _) = CalculatorEngine.smartInsert(res1, res1.length, ".")
        assertEquals("0.", res2)

        // Dot after operator becomes "0."
        val (res3, _) = CalculatorEngine.smartInsert("5+", 2, ".")
        assertEquals("5+0.", res3)

        // Pressing dot inside 123.45 is rejected
        val (res4, _) = CalculatorEngine.smartInsert("123.45", 3, ".")
        assertEquals("123.45", res4)
    }

    @Test
    fun testHistoryGroupCountBadge() {
        // Example from prompt: 201,200 + 24,400 => count = 2
        val count1 = CalculatorEngine.getNumberGroupCount("201,200+24,400")
        assertEquals(2, count1)

        // Multi-group operation: 1+2+3+4+5 => 5
        val count2 = CalculatorEngine.getNumberGroupCount("1+2+3+4+5")
        assertEquals(5, count2)

        // Single number => 1
        assertEquals(1, CalculatorEngine.getNumberGroupCount("24425"))

        // Empty => 0
        assertEquals(0, CalculatorEngine.getNumberGroupCount(""))
    }

    @Test
    fun testDivisionByZeroHandledGracefully() {
        val result = CalculatorEngine.evaluate("5÷0")
        // Does not crash, returns null
        assertEquals(null, result)
    }
}


