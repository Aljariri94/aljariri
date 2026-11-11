package com.example.util

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.cosh
import kotlin.math.sin
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.tanh

object CalculatorEngine {

    private val mathContext = MathContext(34, RoundingMode.HALF_UP)
    private const val PI_STRING = "3.14159265358979323846264338327950288"
    private const val E_STRING = "2.71828182845904523536028747135266249"

    private val KNOWN_FUNCTIONS = listOf(
        "asin", "acos", "atan",
        "sinh", "cosh", "tanh",
        "sin", "cos", "tan",
        "log", "ln",
        "³√", "√", "cbrt", "sqrt",
        "abs"
    )

    fun appendDoubleZero(current: String): String {
        val lastNumber = getLastNumber(current)
        return if (lastNumber == "0") {
            current
        } else if (lastNumber.isEmpty()) {
            if (current.isEmpty() || isOperator(current.last()) || current.last() == '(') {
                current + "0"
            } else {
                current + "00"
            }
        } else {
            current + "00"
        }
    }

    fun appendTripleZero(current: String): String {
        val lastNumber = getLastNumber(current)
        return if (lastNumber == "0") {
            current
        } else if (lastNumber.isEmpty()) {
            if (current.isEmpty() || isOperator(current.last()) || current.last() == '(') {
                current + "0"
            } else {
                current + "000"
            }
        } else {
            current + "000"
        }
    }

    fun appendDigit(current: String, digit: String): String {
        val lastNumber = getLastNumber(current)
        return if (lastNumber == "0") {
            current.dropLast(1) + digit
        } else {
            current + digit
        }
    }

    fun appendDot(current: String): String {
        val lastNumber = getLastNumber(current)
        return if (lastNumber.contains(".")) {
            current
        } else if (lastNumber.isEmpty()) {
            current + "0."
        } else {
            current + "."
        }
    }

    fun appendOperator(current: String, op: String): String {
        val normalizedOp = when (op) {
            "*" -> "×"
            "/" -> "÷"
            "-" -> "−"
            else -> op
        }
        if (current.isEmpty()) {
            if (normalizedOp == "−") return "−"
            return ""
        }

        val lastChar = current.last()
        return if (isOperator(lastChar) || lastChar == '.') {
            current.dropLast(1) + normalizedOp
        } else {
            current + normalizedOp
        }
    }

    fun appendSmartParentheses(current: String): String {
        if (current.isEmpty()) return "("

        val openCount = current.count { it == '(' }
        val closeCount = current.count { it == ')' }
        val lastChar = current.last()

        return if (openCount > closeCount && (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == '!')) {
            current + ")"
        } else if (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == '!') {
            current + "×("
        } else {
            current + "("
        }
    }

    fun appendScientificFunction(current: String, fn: String): String {
        val fnCall = when (fn) {
            "sqrt", "√" -> "√("
            "cbrt", "³√" -> "³√("
            else -> "$fn("
        }
        if (current.isEmpty()) return fnCall
        val lastChar = current.last()
        return if (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == '!') {
            current + "×" + fnCall
        } else {
            current + fnCall
        }
    }

    fun appendConstant(current: String, constSymbol: String): String {
        if (current.isEmpty()) return constSymbol
        val lastChar = current.last()
        return if (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == '!') {
            current + "×" + constSymbol
        } else {
            current + constSymbol
        }
    }

    fun appendFactorial(current: String): String {
        if (current.isEmpty()) return ""
        val lastChar = current.last()
        return if (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e') {
            current + "!"
        } else {
            current
        }
    }

    fun appendPower(current: String, powerText: String = "^"): String {
        if (current.isEmpty()) return ""
        val lastChar = current.last()
        return if (lastChar.isDigit() || lastChar == ')' || lastChar == 'π' || lastChar == 'e' || lastChar == '!') {
            current + powerText
        } else {
            current
        }
    }

    fun togglePlusMinus(current: String): String {
        if (current.isEmpty()) return "-"
        val lastNum = getLastNumber(current)
        if (lastNum.isNotEmpty()) {
            val prefix = current.substring(0, current.length - lastNum.length)
            return if (prefix.endsWith("-(")) {
                prefix.dropLast(2) + lastNum
            } else if (prefix.endsWith("-")) {
                prefix.dropLast(1) + "+" + lastNum
            } else if (prefix.endsWith("+")) {
                prefix.dropLast(1) + "-" + lastNum
            } else {
                prefix + "(-" + lastNum + ")"
            }
        }
        return "(-$current)"
    }

    fun applyReciprocal(current: String): String {
        if (current.isEmpty()) return "1/("
        val lastNum = getLastNumber(current)
        if (lastNum.isNotEmpty()) {
            val prefix = current.substring(0, current.length - lastNum.length)
            return prefix + "1/(" + lastNum + ")"
        }
        return "1/($current)"
    }

    fun backspace(current: String): String {
        if (current.isEmpty()) return ""
        // Check if ends with a function name like "sin(" or "cos("
        for (fn in KNOWN_FUNCTIONS) {
            if (current.endsWith("$fn(")) {
                return current.dropLast(fn.length + 1)
            }
        }
        return current.dropLast(1)
    }

    fun getLastNumber(expression: String): String {
        var idx = expression.length - 1
        while (idx >= 0 && (expression[idx].isDigit() || expression[idx] == '.')) {
            idx--
        }
        return expression.substring(idx + 1)
    }

    fun isOperator(c: Char): Boolean =
        c == '+' || c == '-' || c == '−' || c == '×' || c == '÷' || c == '%' || c == '^' || c == '*' || c == '/'

    fun evaluate(expression: String, isDegreeMode: Boolean = true): BigDecimal? {
        if (expression.isBlank()) return null
        val sanitized = sanitize(expression)
        if (sanitized.isBlank()) return null

        return try {
            val tokens = tokenize(sanitized)
            if (tokens.isEmpty()) return null
            evaluateTokens(tokens, isDegreeMode)
        } catch (_: Exception) {
            null
        }
    }

    private fun sanitize(expr: String): String {
        var s = NumberFormatter.toWesternNumerals(expr)
            .replace(",", "")
            .replace("،", "")
            .replace(" ", "")
            .replace("\u2060", "")
            .replace("\u200B", "")
            .trim()
            .replace("*", "×")
            .replace("/", "÷")
            .replace("−", "-")

        while (s.isNotEmpty() && ((isOperator(s.last()) || s.last() == '.') && s.last() != '%')) {
            s = s.dropLast(1)
        }
        // Auto-close missing parentheses
        val open = s.count { it == '(' }
        val close = s.count { it == ')' }
        if (open > close) {
            s += ")".repeat(open - close)
        }
        return s
    }

    private fun isFunctionToken(token: String): Boolean = KNOWN_FUNCTIONS.contains(token)

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        val buffer = StringBuilder()

        var i = 0
        while (i < expr.length) {
            val c = expr[i]

            // Check for known multi-character functions
            var matchedFn: String? = null
            for (fn in KNOWN_FUNCTIONS) {
                if (expr.startsWith(fn, i)) {
                    matchedFn = fn
                    break
                }
            }

            if (matchedFn != null) {
                if (buffer.isNotEmpty()) {
                    tokens.add(buffer.toString())
                    buffer.clear()
                    // Implicit multiplication before function
                    tokens.add("×")
                } else if (tokens.isNotEmpty()) {
                    val prev = tokens.last()
                    if (prev != "(" && !isOperator(prev[0])) {
                        tokens.add("×")
                    }
                }
                tokens.add(matchedFn)
                i += matchedFn.length
                continue
            }

            if (c == 'π' || c == 'e') {
                if (buffer.isNotEmpty()) {
                    tokens.add(buffer.toString())
                    buffer.clear()
                    tokens.add("×")
                } else if (tokens.isNotEmpty()) {
                    val prev = tokens.last()
                    if (prev != "(" && !isOperator(prev[0])) {
                        tokens.add("×")
                    }
                }
                tokens.add(c.toString())
                i++
                continue
            }

            if (c.isDigit() || c == '.') {
                buffer.append(c)
                i++
                continue
            }

            if (c == '!') {
                if (buffer.isNotEmpty()) {
                    tokens.add(buffer.toString())
                    buffer.clear()
                }
                tokens.add("!")
                i++
                continue
            }

            if (isOperator(c) || c == '(' || c == ')') {
                if (c == '-' && (i == 0 || expr[i - 1] == '(' || isOperator(expr[i - 1]))) {
                    // Unary minus
                    buffer.append(c)
                } else {
                    if (buffer.isNotEmpty()) {
                        tokens.add(buffer.toString())
                        buffer.clear()
                    }
                    if (c == '(' && tokens.isNotEmpty()) {
                        val prev = tokens.last()
                        if (!isFunctionToken(prev) && prev != "(" && !isOperator(prev[0])) {
                            tokens.add("×")
                        }
                    }
                    tokens.add(c.toString())
                }
                i++
                continue
            }

            i++
        }

        if (buffer.isNotEmpty()) {
            tokens.add(buffer.toString())
        }
        return tokens
    }

    private fun evaluateTokens(tokens: List<String>, isDegreeMode: Boolean): BigDecimal {
        val outputQueue = mutableListOf<String>()
        val opStack = mutableListOf<String>()

        fun precedence(op: String): Int = when (op) {
            "+", "-" -> 1
            "×", "÷", "%" -> 2
            "^" -> 3
            "!" -> 4
            else -> if (isFunctionToken(op)) 5 else 0
        }

        for (token in tokens) {
            if (isFunctionToken(token)) {
                opStack.add(token)
            } else if (token == "!") {
                outputQueue.add("!")
            } else if (token.length == 1 && isOperator(token[0])) {
                while (opStack.isNotEmpty() && precedence(opStack.last()) >= precedence(token)) {
                    outputQueue.add(opStack.removeAt(opStack.lastIndex))
                }
                opStack.add(token)
            } else if (token == "(") {
                opStack.add(token)
            } else if (token == ")") {
                while (opStack.isNotEmpty() && opStack.last() != "(") {
                    outputQueue.add(opStack.removeAt(opStack.lastIndex))
                }
                if (opStack.isNotEmpty() && opStack.last() == "(") {
                    opStack.removeAt(opStack.lastIndex)
                }
                if (opStack.isNotEmpty() && isFunctionToken(opStack.last())) {
                    outputQueue.add(opStack.removeAt(opStack.lastIndex))
                }
            } else {
                outputQueue.add(token)
            }
        }

        while (opStack.isNotEmpty()) {
            val top = opStack.removeAt(opStack.lastIndex)
            if (top != "(" && top != ")") {
                outputQueue.add(top)
            }
        }

        // Evaluate RPN with BigDecimal
        val evalStack = mutableListOf<BigDecimal>()

        for (token in outputQueue) {
            if (isFunctionToken(token)) {
                if (evalStack.isEmpty()) return BigDecimal.ZERO
                val arg = evalStack.removeAt(evalStack.lastIndex)
                val resDouble = evalScientificFunction(token, arg.toDouble(), isDegreeMode)
                evalStack.add(cleanDoubleToBigDecimal(resDouble))
            } else if (token == "!") {
                if (evalStack.isEmpty()) return BigDecimal.ZERO
                val n = evalStack.removeAt(evalStack.lastIndex)
                evalStack.add(factorialBigDecimal(n))
            } else if (token.length == 1 && isOperator(token[0])) {
                if (evalStack.size < 2) return BigDecimal.ZERO
                val b = evalStack.removeAt(evalStack.lastIndex)
                val a = evalStack.removeAt(evalStack.lastIndex)
                val res = when (token) {
                    "+" -> a.add(b, mathContext)
                    "-" -> a.subtract(b, mathContext)
                    "×" -> a.multiply(b, mathContext)
                    "÷" -> {
                        if (b.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Division by zero")
                        a.divide(b, mathContext)
                    }
                    "%" -> a.multiply(b.divide(BigDecimal(100), mathContext), mathContext)
                    "^" -> {
                        val bScale = try { b.stripTrailingZeros().scale() } catch (_: Exception) { 0 }
                        if (bScale <= 0 && b.compareTo(BigDecimal.valueOf(9999)) <= 0 && b.compareTo(BigDecimal.valueOf(-9999)) >= 0) {
                            val intPow = b.toInt()
                            if (intPow >= 0) a.pow(intPow, mathContext)
                            else BigDecimal.ONE.divide(a.pow(-intPow, mathContext), mathContext)
                        } else {
                            BigDecimal(Math.pow(a.toDouble(), b.toDouble()), mathContext)
                        }
                    }
                    else -> BigDecimal.ZERO
                }
                evalStack.add(res)
            } else {
                val value = when (token) {
                    "π" -> BigDecimal(PI_STRING, mathContext)
                    "e" -> BigDecimal(E_STRING, mathContext)
                    else -> try {
                        BigDecimal(token)
                    } catch (_: Exception) {
                        BigDecimal.ZERO
                    }
                }
                evalStack.add(value)
            }
        }

        if (evalStack.isEmpty()) return BigDecimal.ZERO
        return evalStack.last().stripTrailingZeros()
    }

    private fun evalScientificFunction(fn: String, arg: Double, isDegreeMode: Boolean): Double {
        return when (fn) {
            "sin" -> {
                if (isDegreeMode) {
                    val mod = (arg % 360.0 + 360.0) % 360.0
                    if (abs(mod) < 1e-9 || abs(mod - 180.0) < 1e-9 || abs(mod - 360.0) < 1e-9) 0.0
                    else if (abs(mod - 90.0) < 1e-9) 1.0
                    else if (abs(mod - 270.0) < 1e-9) -1.0
                    else sin(Math.toRadians(arg))
                } else sin(arg)
            }
            "cos" -> {
                if (isDegreeMode) {
                    val mod = (arg % 360.0 + 360.0) % 360.0
                    if (abs(mod - 90.0) < 1e-9 || abs(mod - 270.0) < 1e-9) 0.0
                    else if (abs(mod) < 1e-9 || abs(mod - 360.0) < 1e-9) 1.0
                    else if (abs(mod - 180.0) < 1e-9) -1.0
                    else cos(Math.toRadians(arg))
                } else cos(arg)
            }
            "tan" -> {
                if (isDegreeMode) {
                    val mod = (arg % 180.0 + 180.0) % 180.0
                    if (abs(mod - 90.0) < 1e-9) throw ArithmeticException("Undefined")
                    else if (abs(mod) < 1e-9 || abs(mod - 180.0) < 1e-9) 0.0
                    else if (abs(mod - 45.0) < 1e-9) 1.0
                    else if (abs(mod - 135.0) < 1e-9) -1.0
                    else tan(Math.toRadians(arg))
                } else tan(arg)
            }
            "asin" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error")
                val r = asin(arg)
                if (isDegreeMode) Math.toDegrees(r) else r
            }
            "acos" -> {
                if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error")
                val r = acos(arg)
                if (isDegreeMode) Math.toDegrees(r) else r
            }
            "atan" -> {
                val r = atan(arg)
                if (isDegreeMode) Math.toDegrees(r) else r
            }
            "sinh" -> sinh(arg)
            "cosh" -> cosh(arg)
            "tanh" -> tanh(arg)
            "log" -> {
                if (arg <= 0.0) throw ArithmeticException("Log of non-positive")
                kotlin.math.log10(arg)
            }
            "ln" -> {
                if (arg <= 0.0) throw ArithmeticException("Ln of non-positive")
                kotlin.math.ln(arg)
            }
            "√", "sqrt" -> {
                if (arg < 0.0) throw ArithmeticException("Sqrt of negative")
                sqrt(arg)
            }
            "³√", "cbrt" -> cbrt(arg)
            "abs" -> abs(arg)
            else -> arg
        }
    }

    private fun factorialBigDecimal(n: BigDecimal): BigDecimal {
        if (n.compareTo(BigDecimal.ZERO) < 0) throw ArithmeticException("Factorial of negative")
        val stripped = n.stripTrailingZeros()
        if (stripped.scale() > 0) throw ArithmeticException("Factorial of non-integer")
        val count = n.toInt()
        if (count > 2000) throw ArithmeticException("Factorial overflow")
        var result = BigDecimal.ONE
        for (i in 2..count) {
            result = result.multiply(BigDecimal.valueOf(i.toLong()), mathContext)
        }
        return result
    }

    private fun cleanDoubleToBigDecimal(v: Double): BigDecimal {
        if (v.isNaN() || v.isInfinite()) throw ArithmeticException("Invalid number")
        val roundedInt = Math.round(v)
        if (abs(v - roundedInt) < 1e-12) {
            return BigDecimal(roundedInt)
        }
        return BigDecimal(v, mathContext).setScale(12, RoundingMode.HALF_UP).stripTrailingZeros()
    }

    fun toCleanString(value: BigDecimal): String {
        if (value.compareTo(BigDecimal.ZERO) == 0) return "0"
        val stripped = value.stripTrailingZeros()
        return if (stripped.scale() <= 0) {
            stripped.toBigInteger().toString()
        } else {
            stripped.toPlainString()
        }
    }

    fun calculateTaxPlus(amount: BigDecimal, taxRatePercent: BigDecimal): Pair<BigDecimal, BigDecimal> {
        val taxMultiplier = taxRatePercent.divide(BigDecimal(100), mathContext)
        val taxAmount = amount.multiply(taxMultiplier, mathContext)
        val total = amount.add(taxAmount, mathContext).stripTrailingZeros()
        return Pair(total, taxAmount.stripTrailingZeros())
    }

    fun calculateTaxMinus(amountWithTax: BigDecimal, taxRatePercent: BigDecimal): Pair<BigDecimal, BigDecimal> {
        val taxFactor = BigDecimal.ONE.add(taxRatePercent.divide(BigDecimal(100), mathContext), mathContext)
        val baseAmount = amountWithTax.divide(taxFactor, mathContext).stripTrailingZeros()
        val taxAmount = amountWithTax.subtract(baseAmount, mathContext).stripTrailingZeros()
        return Pair(baseAmount, taxAmount)
    }

    fun calculateDiscount(amount: BigDecimal, discountPercent: BigDecimal): Pair<BigDecimal, BigDecimal> {
        val discFactor = discountPercent.divide(BigDecimal(100), mathContext)
        val discAmount = amount.multiply(discFactor, mathContext)
        val finalAmount = amount.subtract(discAmount, mathContext).stripTrailingZeros()
        return Pair(finalAmount, discAmount.stripTrailingZeros())
    }

    fun extractInvoiceEntries(expression: String): List<com.example.model.InvoiceEntry> {
        if (expression.isBlank()) return emptyList()

        val entries = mutableListOf<com.example.model.InvoiceEntry>()
        val currentNum = StringBuilder()
        var currentOp: String? = null
        var indexCounter = 1

        var i = 0
        while (i < expression.length) {
            val c = expression[i]
            if (c.isDigit() || c == '.') {
                currentNum.append(c)
            } else if (isOperator(c) || c == '(' || c == ')') {
                if (c == '-' && (i == 0 || isOperator(expression[i - 1]) || expression[i - 1] == '(')) {
                    currentNum.append(c)
                } else {
                    if (currentNum.isNotEmpty()) {
                        entries.add(
                            com.example.model.InvoiceEntry(
                                index = indexCounter++,
                                amount = currentNum.toString(),
                                operatorBefore = currentOp
                            )
                        )
                        currentNum.clear()
                    }
                    currentOp = c.toString()
                }
            }
            i++
        }

        if (currentNum.isNotEmpty()) {
            entries.add(
                com.example.model.InvoiceEntry(
                    index = indexCounter++,
                    amount = currentNum.toString(),
                    operatorBefore = currentOp
                )
            )
        }

        val amountToIndices = mutableMapOf<String, MutableList<Int>>()
        for (entry in entries) {
            amountToIndices.getOrPut(entry.amount) { mutableListOf() }.add(entry.index)
        }

        return entries.map { entry ->
            val allIndices = amountToIndices[entry.amount] ?: emptyList()
            val isDup = allIndices.size > 1
            val others = allIndices.filter { it != entry.index }
            entry.copy(
                isDuplicate = isDup,
                duplicateWithIndices = others
            )
        }
    }

    fun getDigitCountOfCurrentNumber(expression: String, cursor: Int): Int {
        if (expression.isEmpty()) return 0
        val safeCursor = cursor.coerceIn(0, expression.length)

        var start = safeCursor
        while (start > 0) {
            val c = expression[start - 1]
            if (isOperator(c) || c == '(' || c == ')' || c == '\n') {
                break
            }
            start--
        }

        var end = safeCursor
        while (end < expression.length) {
            val c = expression[end]
            if (isOperator(c) || c == '(' || c == ')' || c == '\n') {
                break
            }
            end++
        }

        val token = expression.substring(start, end)
        return token.count { it.isDigit() }
    }

    fun getNumberGroupCount(expression: String): Int {
        if (expression.isBlank()) return 0
        val clean = NumberFormatter.toWesternNumerals(expression)
            .replace(",", "")
            .replace("،", "")
            .replace("\u2060", "")
            .replace("\u200B", "")
            .trim()
        if (clean.isEmpty() || clean == "0") return 0

        var count = 0
        var inNumber = false
        var i = 0
        while (i < clean.length) {
            val c = clean[i]
            if (c.isDigit() || c == '.') {
                if (!inNumber) {
                    count++
                    inNumber = true
                }
            } else {
                inNumber = false
            }
            i++
        }
        return count
    }

    fun smartInsert(current: String, cursor: Int, text: String, isOperator: Boolean = false): Pair<String, Int> {
        if (text.isEmpty()) return Pair(current, cursor)
        val safeCursor = cursor.coerceIn(0, current.length)

        // Normalize operator symbols
        val normalizedText = when (text) {
            "*" -> "×"
            "/" -> "÷"
            "-" -> "−"
            else -> text
        }
        val isOp = isOperator || (normalizedText.length == 1 && isOperator(normalizedText[0]))

        // 1. Operator insertion rules
        if (isOp) {
            // Rule: No operator at start except '-' or '−' for negative number
            if (current.isEmpty() || safeCursor == 0) {
                return if (normalizedText == "−" || normalizedText == "-") {
                    Pair("−" + current, 1)
                } else {
                    Pair(current, safeCursor)
                }
            }

            // Look backwards from cursor to find previous meaningful character
            var lastIdx = safeCursor - 1
            while (lastIdx >= 0 && (current[lastIdx] == '\u200B' || current[lastIdx] == '\u2060' || current[lastIdx] == ' ')) {
                lastIdx--
            }

            if (lastIdx < 0) {
                return if (normalizedText == "−" || normalizedText == "-") {
                    Pair("−" + current.substring(safeCursor), 1)
                } else {
                    Pair(current, safeCursor)
                }
            }

            val prevChar = current[lastIdx]

            // If preceded by '(': allow negative sign only
            if (prevChar == '(') {
                return if (normalizedText == "−" || normalizedText == "-") {
                    val newExpr = current.substring(0, safeCursor) + "−" + current.substring(safeCursor)
                    Pair(newExpr, safeCursor + 1)
                } else {
                    Pair(current, safeCursor)
                }
            }

            // Rule: Operator after operator REPLACES previous operator (e.g., 5 + and press - => 5 -)
            if (isOperator(prevChar) || prevChar == '.') {
                val prefix = current.substring(0, lastIdx)
                val suffix = current.substring(safeCursor)
                val newExpr = prefix + normalizedText + suffix
                return Pair(newExpr, prefix.length + normalizedText.length)
            }

            // Normal operator appending/insertion
            val prefix = current.substring(0, safeCursor)
            val suffix = current.substring(safeCursor)
            val newExpr = prefix + normalizedText + suffix
            return Pair(newExpr, safeCursor + normalizedText.length)
        }

        // 2. Decimal dot rules: Do NOT allow ".." in one number
        if (normalizedText == ".") {
            // Find boundaries of the number token surrounding safeCursor
            var start = safeCursor
            while (start > 0) {
                val c = current[start - 1]
                if (isOperator(c) || c == '(' || c == ')') break
                start--
            }
            var end = safeCursor
            while (end < current.length) {
                val c = current[end]
                if (isOperator(c) || c == '(' || c == ')') break
                end++
            }
            val token = current.substring(start, end)
                .replace("\u2060", "")
                .replace("\u200B", "")
                .replace(",", "")
                .replace("،", "")

            // Reject if token already contains a dot
            if (token.contains(".")) {
                return Pair(current, safeCursor)
            }

            // If token is empty (at start or after operator/paren), insert "0."
            if (token.isEmpty()) {
                val prefix = current.substring(0, safeCursor)
                val suffix = current.substring(safeCursor)
                val newExpr = prefix + "0." + suffix
                return Pair(newExpr, safeCursor + 2)
            }

            // Otherwise insert standard dot
            val prefix = current.substring(0, safeCursor)
            val suffix = current.substring(safeCursor)
            val newExpr = prefix + "." + suffix
            return Pair(newExpr, safeCursor + 1)
        }

        // 3. Leading zero rules (e.g. 0 then 5 => 5, 0 then 00 => 0)
        if (normalizedText.all { it.isDigit() }) {
            var start = safeCursor
            while (start > 0) {
                val c = current[start - 1]
                if (isOperator(c) || c == '(' || c == ')') break
                start--
            }
            var end = safeCursor
            while (end < current.length) {
                val c = current[end]
                if (isOperator(c) || c == '(' || c == ')') break
                end++
            }
            val rawToken = current.substring(start, end)
                .replace("\u2060", "")
                .replace("\u200B", "")
                .replace(",", "")
                .replace("،", "")

            if (rawToken == "0") {
                if (normalizedText == "0" || normalizedText == "00" || normalizedText == "000") {
                    return Pair(current, safeCursor)
                }
                // Replace "0" with the new non-zero digit
                val prefix = current.substring(0, start)
                val suffix = current.substring(end)
                val newExpr = prefix + normalizedText + suffix
                return Pair(newExpr, prefix.length + normalizedText.length)
            }
        }

        // 4. Default insertion
        val prefix = current.substring(0, safeCursor)
        val suffix = current.substring(safeCursor)
        val newExpr = prefix + normalizedText + suffix
        return Pair(newExpr, safeCursor + normalizedText.length)
    }

    fun insertAt(current: String, cursor: Int, text: String, isOperator: Boolean = false): Pair<String, Int> {
        return smartInsert(current, cursor, text, isOperator)
    }

    fun replaceRange(current: String, start: Int, end: Int, text: String, isOperator: Boolean = false): Pair<String, Int> {
        val s = start.coerceIn(0, current.length)
        val e = end.coerceIn(s, current.length)
        val base = current.substring(0, s) + current.substring(e)
        return smartInsert(base, s, text, isOperator)
    }

    fun deleteAt(current: String, start: Int, end: Int): Pair<String, Int> {
        val s = start.coerceIn(0, current.length)
        val e = end.coerceIn(s, current.length)
        if (s < e) {
            val newExpr = current.substring(0, s) + current.substring(e)
            return Pair(newExpr, s)
        } else if (s > 0) {
            var delIdx = s - 1
            // Skip over zero-width non-breaking characters
            while (delIdx > 0 && (current[delIdx] == '\u2060' || current[delIdx] == '\u200B')) {
                delIdx--
            }
            val prefix = current.substring(0, delIdx + 1)
            for (fn in KNOWN_FUNCTIONS) {
                if (prefix.endsWith("$fn(")) {
                    val dropCount = fn.length + 1
                    val newExpr = current.substring(0, delIdx + 1 - dropCount) + current.substring(s)
                    return Pair(newExpr, delIdx + 1 - dropCount)
                }
            }
            // If deleting a comma, drop the comma and the digit before it
            if (current[delIdx] == ',' || current[delIdx] == '،') {
                val dropCount = if (delIdx >= 1) 2 else 1
                val newExpr = current.substring(0, delIdx + 1 - dropCount) + current.substring(s)
                return Pair(newExpr, (delIdx + 1 - dropCount).coerceAtLeast(0))
            }
            val newExpr = current.substring(0, delIdx) + current.substring(s)
            return Pair(newExpr, delIdx)
        }
        return Pair(current, 0)
    }
}
