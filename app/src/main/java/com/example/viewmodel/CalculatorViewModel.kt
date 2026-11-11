package com.example.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CalculationEntity
import com.example.data.local.PreferencesManager
import com.example.data.repository.CalculationRepository
import com.example.model.CalculatorMode
import com.example.model.KeyType
import com.example.model.NumeralStyle
import com.example.util.ArabicTafqeet
import com.example.util.CalculatorEngine
import com.example.util.NumberFormatter
import com.example.util.SoundHapticManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class CalculatorUiState(
    val expression: String = "",
    val liveResult: String = "",
    val previousCalculation: String = "",
    val tafqeetText: String = "",
    val mode: CalculatorMode = CalculatorMode.CLEVCALC_STYLE,
    val numeralStyle: NumeralStyle = NumeralStyle.WESTERN,
    val memoryValue: BigDecimal = BigDecimal.ZERO,
    val grandTotal: BigDecimal = BigDecimal.ZERO,
    val taxRate: BigDecimal = BigDecimal("15.0"),
    val discountRate: BigDecimal = BigDecimal("10.0"),
    val currencyName: String = "ريال",
    val fractionalName: String = "هللة",
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val isExpandedBarOpen: Boolean = false,
    val statusMessage: String? = null,
    val isEvaluated: Boolean = false,
    val invoiceEntries: List<com.example.model.InvoiceEntry> = emptyList(),
    val evaluatedGroupCount: Int = 0,
    val themeMode: com.example.model.ThemeMode = com.example.model.ThemeMode.SYSTEM,
    val colorTheme: com.example.model.CalculatorColorTheme = com.example.model.CalculatorColorTheme.ROYAL_BLUE,
    val isIosStyle: Boolean = true,
    val isGlass: Boolean = false,
    val cursorPosition: Int = 0,
    val selectionStart: Int = 0,
    val selectionEnd: Int = 0,
    val hasSelection: Boolean = false,
    val selectedText: String = "",
    val isDegreeMode: Boolean = true,
    val isSecondFunction: Boolean = false,
    val displayFormatMode: com.example.model.DisplayFormatMode = com.example.model.DisplayFormatMode.NORM,
    val fixDecimalPlaces: Int = 2,
    val sciDigits: Int = 2,
    val previousCalculations: List<String> = emptyList(),
    val clearPulse: Long = 0L
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val soundHapticManager = SoundHapticManager(application)
    private val prefsManager = PreferencesManager(application)
    private val repository by lazy {
        CalculationRepository(AppDatabase.getDatabase(getApplication()).calculationDao())
    }

    val historyList: StateFlow<List<CalculationEntity>> by lazy {
        repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    private var fullPrecisionResult: BigDecimal? = null

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    init {
        // Restore saved state from PreferencesManager so no data is lost on exit!
        val savedExpr = prefsManager.getLastExpression()
        val savedResult = prefsManager.getLastResult()
        val savedPrev = prefsManager.getLastPreviousCalc()
        val savedTafqeet = prefsManager.getLastTafqeet()
        val savedMemory = prefsManager.getMemory()
        val savedGT = prefsManager.getGrandTotal()
        val savedHaptic = prefsManager.isHapticEnabled()
        val savedSound = prefsManager.isSoundEnabled()
        val savedNumeral = prefsManager.getNumeralStyle()
        val savedMode = prefsManager.getCalculatorMode()
        val savedTax = prefsManager.getTaxRate()
        val savedDisc = prefsManager.getDiscountRate()
        val savedCurrency = prefsManager.getCurrency()
        val savedThemeMode = prefsManager.getThemeMode()
        val savedColorTheme = prefsManager.getColorTheme()
        val savedIosStyle = prefsManager.isIosStyle()
        val savedGlassStyle = prefsManager.isGlassStyle() || savedThemeMode == com.example.model.ThemeMode.GLASS
        val effectiveThemeMode = if (savedThemeMode == com.example.model.ThemeMode.GLASS) com.example.model.ThemeMode.SYSTEM else savedThemeMode
        val savedDisplayFormat = prefsManager.getDisplayFormatMode()
        val savedFixPlaces = prefsManager.getFixDecimalPlaces()
        val savedSciDigits = prefsManager.getSciDigits()

        _uiState.update {
            it.copy(
                expression = savedExpr,
                liveResult = savedResult,
                previousCalculation = savedPrev,
                previousCalculations = if (savedPrev.isNotEmpty()) listOf(savedPrev) else emptyList(),
                tafqeetText = savedTafqeet,
                memoryValue = savedMemory,
                grandTotal = savedGT,
                hapticEnabled = savedHaptic,
                soundEnabled = savedSound,
                numeralStyle = savedNumeral,
                mode = savedMode,
                taxRate = savedTax,
                discountRate = savedDisc,
                currencyName = savedCurrency,
                themeMode = effectiveThemeMode,
                colorTheme = savedColorTheme,
                isIosStyle = savedIosStyle,
                isGlass = savedGlassStyle,
                displayFormatMode = savedDisplayFormat,
                fixDecimalPlaces = savedFixPlaces,
                sciDigits = savedSciDigits,
                cursorPosition = savedExpr.length,
                selectionStart = savedExpr.length,
                selectionEnd = savedExpr.length
            )
        }

        if (savedExpr.isNotEmpty()) {
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
                updateLiveEvaluation(savedExpr)
            }
        }
    }

    private fun feedback(keyType: KeyType) {
        soundHapticManager.triggerFeedback(
            keyType,
            _uiState.value.hapticEnabled,
            _uiState.value.soundEnabled
        )
    }

    private fun persistState() {
        val s = _uiState.value
        prefsManager.saveCalculatorState(
            expression = s.expression,
            result = s.liveResult,
            previousCalc = s.previousCalculation,
            tafqeet = s.tafqeetText,
            memory = s.memoryValue,
            grandTotal = s.grandTotal
        )
    }

    /**
     * Inserts text at the current cursor position or replaces the selected range.
     * Provides complete freedom in navigation and inserting numbers anywhere.
     */
    private fun insertTextAtCursor(text: String, isOperator: Boolean = false) {
        val state = _uiState.value

        var textToInsert = text
        if (!isOperator && !state.isEvaluated && text.any { it.isDigit() }) {
            val selectedDigits = if (state.hasSelection) state.selectedText.count { it.isDigit() } else 0
            val currentDigits = (CalculatorEngine.getDigitCountOfCurrentNumber(state.expression, state.cursorPosition) - selectedDigits).coerceAtLeast(0)
            val available = (15 - currentDigits).coerceAtLeast(0)
            if (available <= 0) {
                // Limit to 15 digits per number as strictly requested
                return
            }
            if (textToInsert.count { it.isDigit() } > available) {
                var keptDigits = 0
                val sb = StringBuilder()
                for (ch in textToInsert) {
                    if (ch.isDigit()) {
                        if (keptDigits < available) {
                            sb.append(ch)
                            keptDigits++
                        }
                    } else {
                        sb.append(ch)
                    }
                }
                textToInsert = sb.toString()
            }
            if (textToInsert.isEmpty()) return
        }

        val (rawExpr, rawCursor) = if (state.isEvaluated) {
            val base = if (isOperator) {
                fullPrecisionResult?.let { CalculatorEngine.toCleanString(it) } ?: state.liveResult
            } else ""
            if (base.isNotEmpty()) {
                CalculatorEngine.smartInsert(base, base.length, textToInsert, isOperator = true)
            } else {
                CalculatorEngine.smartInsert("", 0, textToInsert, isOperator = isOperator)
            }
        } else if (state.hasSelection) {
            CalculatorEngine.replaceRange(state.expression, state.selectionStart, state.selectionEnd, textToInsert, isOperator = isOperator)
        } else {
            CalculatorEngine.smartInsert(state.expression, state.cursorPosition, textToInsert, isOperator = isOperator)
        }

        val formatted = NumberFormatter.formatDisplay(rawExpr, state.numeralStyle)
        val safeCursor = NumberFormatter.mapCursorPosition(rawExpr, rawCursor, formatted)

        _uiState.update {
            it.copy(
                expression = formatted,
                cursorPosition = safeCursor,
                selectionStart = safeCursor,
                selectionEnd = safeCursor,
                hasSelection = false,
                selectedText = "",
                isEvaluated = false
            )
        }
        updateLiveEvaluation(formatted)
        persistState()
    }

    fun onCursorOrSelectionChanged(cursor: Int, start: Int, end: Int) {
        val expr = _uiState.value.expression
        val s = start.coerceIn(0, expr.length)
        val e = end.coerceIn(s, expr.length)
        val hasSel = s < e
        val selText = if (hasSel) expr.substring(s, e) else ""
        _uiState.update {
            it.copy(
                cursorPosition = cursor.coerceIn(0, expr.length),
                selectionStart = s,
                selectionEnd = e,
                hasSelection = hasSel,
                selectedText = selText
            )
        }
    }

    fun onDigit(digit: String) {
        feedback(KeyType.DIGIT)
        insertTextAtCursor(digit)
    }

    /**
     * Appends double zero (00) at the cursor position.
     */
    fun onDoubleZero() {
        feedback(KeyType.DOUBLE_ZERO)
        insertTextAtCursor("00")
    }

    /**
     * Appends triple zero (000) at the cursor position.
     */
    fun onTripleZero() {
        feedback(KeyType.TRIPLE_ZERO)
        insertTextAtCursor("000")
    }

    fun onDot() {
        feedback(KeyType.DIGIT)
        insertTextAtCursor(".")
    }

    fun onParentheses() {
        feedback(KeyType.PARENTHESES)
        val state = _uiState.value
        val paren = if (state.expression.count { it == '(' } > state.expression.count { it == ')' }) ")" else "("
        insertTextAtCursor(paren)
    }

    fun onOperator(op: String) {
        feedback(KeyType.OPERATOR)
        insertTextAtCursor(op, isOperator = true)
    }

    fun onEquals() {
        feedback(KeyType.EQUALS)
        val state = _uiState.value
        val expr = state.expression
        if (expr.isBlank()) return

        val cleanExpr = NumberFormatter.toWesternNumerals(expr)
            .replace(",", "")
            .replace("،", "")
            .replace("\u2060", "")
            .replace("\u200B", "")
            .replace(" ", "")
        if (cleanExpr.contains("÷0") || cleanExpr.contains("/0")) {
            _uiState.update { it.copy(statusMessage = "لا يمكن القسمة على صفر") }
            return
        }

        val resultValue = CalculatorEngine.evaluate(expr)
        if (resultValue == null) {
            if (cleanExpr.contains("÷") || cleanExpr.contains("/")) {
                _uiState.update { it.copy(statusMessage = "لا يمكن القسمة على صفر") }
            }
            return
        }
        fullPrecisionResult = resultValue
        val resultString = CalculatorEngine.toCleanString(resultValue)
        val formattedResult = NumberFormatter.formatWithPhysicalMode(
            resultValue,
            state.displayFormatMode,
            state.fixDecimalPlaces,
            state.sciDigits,
            state.numeralStyle
        )

        val tafqeet = ArabicTafqeet.convert(
            resultValue,
            state.currencyName,
            state.fractionalName
        )

        val newGT = state.grandTotal.add(resultValue)

        val entryCount = state.invoiceEntries.size
        val noteText = if (entryCount > 1) "($entryCount مبالغ/فواتير)" else ""

        val lineHistory = "$expr = $formattedResult"
        val updatedHistory = (state.previousCalculations + lineHistory).takeLast(50)

        // Store into Room SQLite database asynchronously
        viewModelScope.launch {
            try {
                repository.insertCalculation(
                    expression = expr,
                    result = resultString,
                    note = noteText,
                    tafqeet = tafqeet
                )
            } catch (_: Exception) {}
        }

        _uiState.update {
            it.copy(
                previousCalculation = lineHistory,
                previousCalculations = updatedHistory,
                expression = resultString,
                liveResult = formattedResult,
                tafqeetText = tafqeet,
                grandTotal = newGT,
                isEvaluated = true,
                evaluatedGroupCount = entryCount,
                cursorPosition = resultString.length,
                selectionStart = resultString.length,
                selectionEnd = resultString.length,
                hasSelection = false,
                selectedText = ""
            )
        }
        persistState()
    }

    fun onBackspace() {
        feedback(KeyType.BACKSPACE)
        val state = _uiState.value
        if (state.isEvaluated) {
            _uiState.update {
                it.copy(
                    expression = "",
                    liveResult = "",
                    tafqeetText = "",
                    isEvaluated = false,
                    evaluatedGroupCount = 0,
                    cursorPosition = 0,
                    selectionStart = 0,
                    selectionEnd = 0,
                    hasSelection = false,
                    selectedText = "",
                    clearPulse = System.currentTimeMillis()
                )
            }
        } else {
            val expr = state.expression
            val (rawExpr, rawCursor) = if (state.hasSelection) {
                CalculatorEngine.deleteAt(expr, state.selectionStart, state.selectionEnd)
            } else {
                CalculatorEngine.deleteAt(expr, state.cursorPosition, state.cursorPosition)
            }
            val formatted = if (rawExpr.isNotEmpty()) NumberFormatter.formatDisplay(rawExpr, state.numeralStyle) else ""
            val safeCursor = NumberFormatter.mapCursorPosition(rawExpr, rawCursor, formatted)
            _uiState.update {
                it.copy(
                    expression = formatted,
                    cursorPosition = safeCursor,
                    selectionStart = safeCursor,
                    selectionEnd = safeCursor,
                    hasSelection = false,
                    selectedText = "",
                    evaluatedGroupCount = 0,
                    clearPulse = if (formatted.isEmpty()) System.currentTimeMillis() else it.clearPulse
                )
            }
            updateLiveEvaluation(formatted)
            persistState()
        }
    }

    fun onClear() {
        feedback(KeyType.CLEAR)
        _uiState.update {
            it.copy(
                expression = "",
                liveResult = "",
                tafqeetText = "",
                isEvaluated = false,
                evaluatedGroupCount = 0,
                cursorPosition = 0,
                selectionStart = 0,
                selectionEnd = 0,
                hasSelection = false,
                selectedText = "",
                clearPulse = System.currentTimeMillis()
            )
        }
        persistState()
    }

    fun onAllClear() {
        feedback(KeyType.ALL_CLEAR)
        _uiState.update {
            it.copy(
                expression = "",
                liveResult = "",
                previousCalculation = "",
                tafqeetText = "",
                isEvaluated = false,
                grandTotal = BigDecimal.ZERO,
                evaluatedGroupCount = 0,
                invoiceEntries = emptyList(),
                cursorPosition = 0,
                selectionStart = 0,
                selectionEnd = 0,
                hasSelection = false,
                selectedText = "",
                clearPulse = System.currentTimeMillis(),
                statusMessage = "تم تصفير الشاشة بالكامل"
            )
        }
        persistState()
    }

    // Commercial & Memory Features
    fun onTaxPlus() {
        feedback(KeyType.FUNCTION)
        val currentAmount = getCurrentAmount() ?: return
        val (total, taxAmount) = CalculatorEngine.calculateTaxPlus(currentAmount, _uiState.value.taxRate)
        val totalStr = CalculatorEngine.toCleanString(total)
        val taxStr = CalculatorEngine.toCleanString(taxAmount)
        val expr = _uiState.value.expression
        val desc = "$expr + ${_uiState.value.taxRate}% ضريبة (${taxStr})"

        viewModelScope.launch {
            repository.insertCalculation(
                expression = desc,
                result = totalStr,
                note = "ضريبة مضافة +$taxStr",
                tafqeet = ArabicTafqeet.convert(total, _uiState.value.currencyName, _uiState.value.fractionalName)
            )
        }

        _uiState.update { state ->
            state.copy(
                previousCalculation = "$desc = $totalStr",
                expression = totalStr,
                liveResult = totalStr,
                tafqeetText = ArabicTafqeet.convert(total, state.currencyName, state.fractionalName),
                isEvaluated = true,
                statusMessage = "تم إضافة الضريبة: +$taxStr ${state.currencyName}"
            )
        }
        persistState()
    }

    fun onTaxMinus() {
        feedback(KeyType.FUNCTION)
        val currentAmount = getCurrentAmount() ?: return
        val (baseAmount, taxAmount) = CalculatorEngine.calculateTaxMinus(currentAmount, _uiState.value.taxRate)
        val baseStr = CalculatorEngine.toCleanString(baseAmount)
        val taxStr = CalculatorEngine.toCleanString(taxAmount)
        val expr = _uiState.value.expression
        val desc = "$expr - ${_uiState.value.taxRate}% ضريبة (${taxStr})"

        viewModelScope.launch {
            repository.insertCalculation(
                expression = desc,
                result = baseStr,
                note = "استقطاع ضريبة -$taxStr",
                tafqeet = ArabicTafqeet.convert(baseAmount, _uiState.value.currencyName, _uiState.value.fractionalName)
            )
        }

        _uiState.update { state ->
            state.copy(
                previousCalculation = "$desc = $baseStr",
                expression = baseStr,
                liveResult = baseStr,
                tafqeetText = ArabicTafqeet.convert(baseAmount, state.currencyName, state.fractionalName),
                isEvaluated = true,
                statusMessage = "المبلغ بدون ضريبة: $baseStr (الضريبة: $taxStr)"
            )
        }
        persistState()
    }

    fun onDiscount() {
        feedback(KeyType.FUNCTION)
        val currentAmount = getCurrentAmount() ?: return
        val (finalAmount, discAmount) = CalculatorEngine.calculateDiscount(currentAmount, _uiState.value.discountRate)
        val finalStr = CalculatorEngine.toCleanString(finalAmount)
        val discStr = CalculatorEngine.toCleanString(discAmount)
        val desc = "${_uiState.value.expression} - ${_uiState.value.discountRate}% خصم"

        viewModelScope.launch {
            repository.insertCalculation(
                expression = desc,
                result = finalStr,
                note = "خصم -$discStr",
                tafqeet = ArabicTafqeet.convert(finalAmount, _uiState.value.currencyName, _uiState.value.fractionalName)
            )
        }

        _uiState.update { state ->
            state.copy(
                previousCalculation = "$desc = $finalStr",
                expression = finalStr,
                liveResult = finalStr,
                tafqeetText = ArabicTafqeet.convert(finalAmount, state.currencyName, state.fractionalName),
                isEvaluated = true,
                statusMessage = "قيمة الخصم: -$discStr ${state.currencyName}"
            )
        }
        persistState()
    }

    fun onMemoryAdd() {
        feedback(KeyType.MEMORY)
        val currentAmount = getCurrentAmount() ?: return
        val newMem = _uiState.value.memoryValue.add(currentAmount)
        _uiState.update {
            it.copy(
                memoryValue = newMem,
                statusMessage = "تمت الإضافة للذاكرة: M = ${CalculatorEngine.toCleanString(newMem)}"
            )
        }
        persistState()
    }

    fun onMemorySub() {
        feedback(KeyType.MEMORY)
        val currentAmount = getCurrentAmount() ?: return
        val newMem = _uiState.value.memoryValue.subtract(currentAmount)
        _uiState.update {
            it.copy(
                memoryValue = newMem,
                statusMessage = "تم الطرح من الذاكرة: M = ${CalculatorEngine.toCleanString(newMem)}"
            )
        }
        persistState()
    }

    fun onMemoryRecall() {
        feedback(KeyType.MEMORY)
        val memStr = CalculatorEngine.toCleanString(_uiState.value.memoryValue)
        _uiState.update {
            it.copy(
                expression = memStr,
                liveResult = memStr,
                isEvaluated = true,
                statusMessage = "استرجاع الذاكرة: $memStr"
            )
        }
        updateLiveEvaluation(memStr)
        persistState()
    }

    fun onMemoryClear() {
        feedback(KeyType.MEMORY)
        _uiState.update {
            it.copy(
                memoryValue = BigDecimal.ZERO,
                statusMessage = "تم مسح الذاكرة (MC)"
            )
        }
        persistState()
    }

    fun onGrandTotalRecall() {
        feedback(KeyType.FUNCTION)
        val gtStr = CalculatorEngine.toCleanString(_uiState.value.grandTotal)
        _uiState.update {
            it.copy(
                expression = gtStr,
                liveResult = gtStr,
                tafqeetText = ArabicTafqeet.convert(_uiState.value.grandTotal, it.currencyName, it.fractionalName),
                isEvaluated = true,
                statusMessage = "المجموع الإجمالي (GT): $gtStr"
            )
        }
        persistState()
    }

    fun onToggleDegreeRad() {
        feedback(KeyType.FUNCTION)
        val newDeg = !_uiState.value.isDegreeMode
        _uiState.update { it.copy(isDegreeMode = newDeg) }
        if (_uiState.value.expression.isNotEmpty()) {
            updateLiveEvaluation(_uiState.value.expression)
        }
    }

    fun onToggleSecondFunction() {
        feedback(KeyType.FUNCTION)
        _uiState.update { it.copy(isSecondFunction = !it.isSecondFunction) }
    }

    fun onScientificFunction(fn: String) {
        feedback(KeyType.FUNCTION)
        when (fn) {
            "sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh", "log", "ln", "√", "³√" -> {
                insertTextAtCursor(CalculatorEngine.appendScientificFunction("", fn))
            }
            "π", "e" -> {
                insertTextAtCursor(CalculatorEngine.appendConstant("", fn))
            }
            "x²" -> {
                insertTextAtCursor("^2", isOperator = true)
            }
            "x³" -> {
                insertTextAtCursor("^3", isOperator = true)
            }
            "xʸ", "^" -> {
                insertTextAtCursor("^", isOperator = true)
            }
            "eˣ" -> {
                insertTextAtCursor("e^", isOperator = true)
            }
            "10ˣ" -> {
                insertTextAtCursor("10^", isOperator = true)
            }
            "1/x" -> {
                val current = _uiState.value.expression
                val newExpr = CalculatorEngine.applyReciprocal(current)
                _uiState.update {
                    it.copy(
                        expression = newExpr,
                        cursorPosition = newExpr.length,
                        selectionStart = newExpr.length,
                        selectionEnd = newExpr.length,
                        hasSelection = false,
                        selectedText = "",
                        isEvaluated = false
                    )
                }
                updateLiveEvaluation(newExpr)
                persistState()
            }
            "x!" -> {
                insertTextAtCursor("!", isOperator = true)
            }
            "±" -> {
                val current = _uiState.value.expression
                val newExpr = CalculatorEngine.togglePlusMinus(current)
                _uiState.update {
                    it.copy(
                        expression = newExpr,
                        cursorPosition = newExpr.length,
                        selectionStart = newExpr.length,
                        selectionEnd = newExpr.length,
                        hasSelection = false,
                        selectedText = "",
                        isEvaluated = false
                    )
                }
                updateLiveEvaluation(newExpr)
                persistState()
            }
            "Rand" -> {
                val randomNum = String.format(java.util.Locale.US, "%.4f", java.lang.Math.random())
                insertTextAtCursor(randomNum)
            }
            "EE" -> {
                insertTextAtCursor("×10^", isOperator = true)
            }
            else -> {
                insertTextAtCursor(fn)
            }
        }
    }

    fun toggleExpandedBar() {
        _uiState.update { it.copy(isExpandedBarOpen = !it.isExpandedBarOpen) }
    }

    fun onToggleMode(newMode: CalculatorMode) {
        prefsManager.setCalculatorMode(newMode)
        _uiState.update { it.copy(mode = newMode) }
    }

    fun setNumeralStyle(style: NumeralStyle) {
        prefsManager.setNumeralStyle(style)
        _uiState.update { it.copy(numeralStyle = style) }
    }

    fun setThemeMode(mode: com.example.model.ThemeMode) {
        prefsManager.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun setColorTheme(theme: com.example.model.CalculatorColorTheme) {
        prefsManager.setColorTheme(theme)
        _uiState.update { it.copy(colorTheme = theme) }
    }

    fun toggleHaptics(enabled: Boolean) {
        prefsManager.setHapticEnabled(enabled)
        _uiState.update { it.copy(hapticEnabled = enabled) }
    }

    fun toggleSound(enabled: Boolean) {
        prefsManager.setSoundEnabled(enabled)
        _uiState.update { it.copy(soundEnabled = enabled) }
    }

    fun updateTaxRate(rate: BigDecimal) {
        prefsManager.setTaxRate(rate)
        _uiState.update { it.copy(taxRate = rate) }
    }

    fun updateDiscountRate(rate: BigDecimal) {
        prefsManager.setDiscountRate(rate)
        _uiState.update { it.copy(discountRate = rate) }
    }

    fun updateCurrency(currency: String, fractional: String) {
        prefsManager.setCurrency(currency)
        _uiState.update {
            it.copy(
                currencyName = currency,
                fractionalName = fractional,
                tafqeetText = if (it.liveResult.isNotEmpty()) {
                    val num = try { BigDecimal(it.liveResult) } catch (_: Exception) { BigDecimal.ZERO }
                    ArabicTafqeet.convert(num, currency, fractional)
                } else ""
            )
        }
        persistState()
    }

    fun selectHistoryItem(item: CalculationEntity) {
        onSelectHistoryExpression(item.expression)
    }

    fun onSelectHistoryExpression(expr: String) {
        val clean = NumberFormatter.toWesternNumerals(expr)
            .replace(",", "")
            .replace("،", "")
            .replace("\u2060", "")
            .replace("\u200B", "")
            .trim()
        if (clean.isEmpty()) return

        val current = _uiState.value.expression
        if (current.isEmpty() || current == "0" || _uiState.value.isEvaluated) {
            val formatted = NumberFormatter.formatDisplay(clean, _uiState.value.numeralStyle)
            _uiState.update {
                it.copy(
                    expression = formatted,
                    cursorPosition = formatted.length,
                    selectionStart = formatted.length,
                    selectionEnd = formatted.length,
                    hasSelection = false,
                    selectedText = "",
                    isEvaluated = false
                )
            }
            updateLiveEvaluation(formatted)
            persistState()
        } else {
            insertTextAtCursor(clean)
        }
    }

    fun onSelectHistoryResult(result: String) {
        val clean = NumberFormatter.toWesternNumerals(result)
            .replace(",", "")
            .replace("،", "")
            .replace("\u2060", "")
            .replace("\u200B", "")
            .trim()
        if (clean.isEmpty()) return

        val current = _uiState.value.expression
        if (current.isEmpty() || current == "0" || _uiState.value.isEvaluated) {
            val formatted = NumberFormatter.formatDisplay(clean, _uiState.value.numeralStyle)
            _uiState.update {
                it.copy(
                    expression = formatted,
                    cursorPosition = formatted.length,
                    selectionStart = formatted.length,
                    selectionEnd = formatted.length,
                    hasSelection = false,
                    selectedText = "",
                    isEvaluated = false
                )
            }
            updateLiveEvaluation(formatted)
            persistState()
        } else {
            insertTextAtCursor(clean)
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
        _uiState.update { it.copy(statusMessage = "تم مسح سجل العمليات") }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    fun toggleIosStyle(enabled: Boolean? = null) {
        val current = _uiState.value.isIosStyle
        val target = enabled ?: !current
        prefsManager.setIosStyle(target)
        _uiState.update { it.copy(isIosStyle = target) }
        feedback(KeyType.FUNCTION)
    }

    /**
     * Toggles Liquid Glass independently across all themes (Light, Dark, System).
     */
    fun toggleGlass(enabled: Boolean? = null) {
        val current = _uiState.value.isGlass
        val target = enabled ?: !current
        prefsManager.setGlassStyle(target)
        _uiState.update { it.copy(isGlass = target) }
        feedback(KeyType.FUNCTION)
    }

    fun onPercent() {
        onOperator("%")
    }

    fun copyToClipboard(context: Context, fallbackText: String) {
        val state = _uiState.value
        val textToCopy = if (state.hasSelection) state.selectedText else fallbackText
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Calculator", textToCopy)
        clipboard.setPrimaryClip(clip)
        val msg = if (state.hasSelection) "تم نسخ الجزء المحدد: $textToCopy" else "تم النسخ: $textToCopy"
        _uiState.update { it.copy(statusMessage = msg) }
    }

    fun cutToClipboard(context: Context, fallbackText: String) {
        val state = _uiState.value
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (state.hasSelection) {
            val textToCut = state.selectedText
            clipboard.setPrimaryClip(ClipData.newPlainText("Calculator", textToCut))
            val (newExpr, newCursor) = CalculatorEngine.deleteAt(state.expression, state.selectionStart, state.selectionEnd)
            _uiState.update {
                it.copy(
                    expression = newExpr,
                    cursorPosition = newCursor,
                    selectionStart = newCursor,
                    selectionEnd = newCursor,
                    hasSelection = false,
                    selectedText = "",
                    statusMessage = "تم قص الجزء المحدد: $textToCut"
                )
            }
            updateLiveEvaluation(newExpr)
        } else {
            val textToCut = if (state.liveResult.isNotEmpty()) state.liveResult else state.expression
            clipboard.setPrimaryClip(ClipData.newPlainText("Calculator", textToCut))
            _uiState.update {
                it.copy(
                    expression = "",
                    liveResult = "",
                    tafqeetText = "",
                    cursorPosition = 0,
                    selectionStart = 0,
                    selectionEnd = 0,
                    hasSelection = false,
                    selectedText = "",
                    isEvaluated = false,
                    evaluatedGroupCount = 0,
                    statusMessage = "تم القص: $textToCut"
                )
            }
        }
        persistState()
    }

    fun pasteFromClipboard(context: Context) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = clipboard.primaryClip
        if (clip != null && clip.itemCount > 0) {
            val raw = clip.getItemAt(0).coerceToText(context).toString()
            onUserPastedText(raw)
        } else {
            _uiState.update { it.copy(statusMessage = "الحافظة فارغة") }
        }
    }

    fun onUserPastedText(rawText: String) {
        if (rawText.isBlank()) {
            _uiState.update { it.copy(statusMessage = "لا يوجد نص صالح للصق") }
            return
        }

        val converted = NumberFormatter.toWesternNumerals(rawText)
            .replace(",", "")
            .replace("،", "")
            .replace(" ", "")
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .filter { it.isDigit() || it in "+-*/.()" }
            .replace("*", "×")
            .replace("/", "÷")
            .replace("-", "−")

        if (converted.isEmpty()) {
            _uiState.update { it.copy(statusMessage = "لا توجد أرقام صالحة للصق") }
            return
        }

        insertTextAtCursor(converted)
        _uiState.update { it.copy(statusMessage = "تم اللصق: $converted") }
    }

    fun onUserTextChanged(rawText: String, cursor: Int) {
        val converted = NumberFormatter.toWesternNumerals(rawText)
            .replace(",", "")
            .replace("،", "")
            .replace("\u2060", "")
            .replace("\u200B", "")
            .replace(" ", "")
        val formatted = if (converted.isNotEmpty() && converted != "0") {
            NumberFormatter.formatDisplay(converted, _uiState.value.numeralStyle)
        } else {
            ""
        }
        val rawCursor = NumberFormatter.unmapCursorPosition(rawText, cursor)
        val safeCursor = NumberFormatter.mapCursorPosition(converted, rawCursor, formatted)
        _uiState.update {
            it.copy(
                expression = formatted,
                cursorPosition = safeCursor,
                selectionStart = safeCursor,
                selectionEnd = safeCursor,
                hasSelection = false,
                selectedText = "",
                isEvaluated = false,
                clearPulse = if (formatted.isEmpty()) System.currentTimeMillis() else it.clearPulse
            )
        }
        updateLiveEvaluation(formatted)
        persistState()
    }

    fun shareCalculation(context: Context, text: String) {
        val sendIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            putExtra(android.content.Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = android.content.Intent.createChooser(sendIntent, "مشاركة")
        shareIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    private fun getCurrentAmount(): BigDecimal? {
        val state = _uiState.value
        return if (state.isEvaluated && state.liveResult.isNotEmpty()) {
            try { BigDecimal(state.liveResult) } catch (_: Exception) { null }
        } else if (state.expression.isNotEmpty()) {
            CalculatorEngine.evaluate(state.expression, state.isDegreeMode)
        } else null
    }

    private fun updateLiveEvaluation(expr: String) {
        val entries = CalculatorEngine.extractInvoiceEntries(expr)
        if (expr.isBlank()) {
            _uiState.update { it.copy(liveResult = "", tafqeetText = "", invoiceEntries = emptyList()) }
            return
        }

        val evalResult = CalculatorEngine.evaluate(expr, _uiState.value.isDegreeMode)
        if (evalResult != null) {
            fullPrecisionResult = evalResult
        }
        val resultStr = if (evalResult != null) {
            NumberFormatter.formatWithPhysicalMode(
                evalResult,
                _uiState.value.displayFormatMode,
                _uiState.value.fixDecimalPlaces,
                _uiState.value.sciDigits,
                _uiState.value.numeralStyle
            )
        } else ""
        val tafqeet = if (evalResult != null) {
            ArabicTafqeet.convert(
                evalResult,
                _uiState.value.currencyName,
                _uiState.value.fractionalName
            )
        } else ""

        _uiState.update {
            it.copy(
                liveResult = resultStr,
                tafqeetText = tafqeet,
                invoiceEntries = entries
            )
        }
    }

    fun updateDisplayFormat(mode: com.example.model.DisplayFormatMode, fixPlaces: Int, sciDigits: Int) {
        prefsManager.setDisplayFormatMode(mode)
        prefsManager.setFixDecimalPlaces(fixPlaces)
        prefsManager.setSciDigits(sciDigits)
        _uiState.update {
            it.copy(
                displayFormatMode = mode,
                fixDecimalPlaces = fixPlaces,
                sciDigits = sciDigits
            )
        }
        if (_uiState.value.expression.isNotEmpty()) {
            updateLiveEvaluation(_uiState.value.expression)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundHapticManager.release()
    }
}
