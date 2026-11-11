package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.components.ClevDisplay
import com.example.ui.components.ClevKeypad
import com.example.ui.components.HistoryDrawer
import com.example.ui.components.SettingsDialog
import com.example.viewmodel.CalculatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showHistory by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val historyList = if (showHistory) {
        viewModel.historyList.collectAsState().value
    } else {
        emptyList()
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkTheme = when (uiState.themeMode) {
        com.example.model.ThemeMode.DARK -> true
        com.example.model.ThemeMode.LIGHT -> false
        else -> isSystemDark
    }
    val isGlass = uiState.isGlass || (uiState.themeMode == com.example.model.ThemeMode.GLASS)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (isGlass) {
                    val glassGradient = if (isDarkTheme) {
                        listOf(
                            androidx.compose.ui.graphics.Color(0xFF0F172A),
                            androidx.compose.ui.graphics.Color(uiState.colorTheme.primaryHex).copy(alpha = 0.38f),
                            androidx.compose.ui.graphics.Color(0xFF050811)
                        )
                    } else {
                        listOf(
                            androidx.compose.ui.graphics.Color(0xFFF1F5F9),
                            androidx.compose.ui.graphics.Color(uiState.colorTheme.primaryHex).copy(alpha = 0.22f),
                            androidx.compose.ui.graphics.Color(0xFFE2E8F0)
                        )
                    }
                    Modifier.background(androidx.compose.ui.graphics.Brush.verticalGradient(glassGradient))
                } else {
                    Modifier.background(MaterialTheme.colorScheme.background)
                }
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .align(Alignment.Center)
        ) {
            // Upper Section: Large Organized Display (Result & Invoice Counter)
            ClevDisplay(
                expression = uiState.expression,
                liveResult = uiState.liveResult,
                previousCalculation = uiState.previousCalculation,
                previousCalculations = uiState.previousCalculations,
                tafqeetText = uiState.tafqeetText,
                mode = uiState.mode,
                numeralStyle = uiState.numeralStyle,
                memoryValue = uiState.memoryValue,
                grandTotal = uiState.grandTotal,
                invoiceEntries = uiState.invoiceEntries,
                evaluatedGroupCount = uiState.evaluatedGroupCount,
                isExpanded = uiState.isExpandedBarOpen,
                themeMode = uiState.themeMode,
                isIosStyle = uiState.isIosStyle,
                isGlass = isGlass,
                cursorPosition = uiState.cursorPosition,
                selectionStart = uiState.selectionStart,
                selectionEnd = uiState.selectionEnd,
                hasSelection = uiState.hasSelection,
                selectedText = uiState.selectedText,
                clearPulse = uiState.clearPulse,
                onCursorOrSelectionChanged = { cursor, start, end ->
                    viewModel.onCursorOrSelectionChanged(cursor, start, end)
                },
                onUserTextChanged = { raw, cur ->
                    viewModel.onUserTextChanged(raw, cur)
                },
                onToggleIosStyle = { viewModel.toggleIosStyle() },
                onToggleGlass = { viewModel.toggleGlass() },
                onToggleDegreeRad = { viewModel.onToggleDegreeRad() },
                onPercent = { viewModel.onPercent() },
                onToggleExpand = { viewModel.toggleExpandedBar() },
                onBackspace = { viewModel.onBackspace() },
                onOpenHistory = { showHistory = true },
                onOpenSettings = { showSettings = true },
                onToggleMode = { viewModel.onToggleMode(it) },
                onCopyResult = { viewModel.copyToClipboard(context, it) },
                onCut = { viewModel.cutToClipboard(context, it) },
                onPaste = { viewModel.pasteFromClipboard(context) },
                onShare = { viewModel.shareCalculation(context, it) },
                onAllClear = { viewModel.onAllClear() },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (uiState.isExpandedBarOpen) 0.38f else 0.46f)
            )

            HorizontalDivider(
                color = if (isGlass) androidx.compose.ui.graphics.Color(0x35FFFFFF) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                thickness = 1.dp
            )

            // Lower Section: Always lightweight clean keypad
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (uiState.isExpandedBarOpen) 0.62f else 0.54f)
                    .then(
                        if (isGlass) {
                            Modifier
                                .background(if (isDarkTheme) androidx.compose.ui.graphics.Color(0x18FFFFFF) else androidx.compose.ui.graphics.Color(0x40FFFFFF))
                                .padding(horizontal = 2.dp)
                        } else {
                            Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        }
                    )
                    .padding(bottom = 6.dp, top = 2.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                ClevKeypad(
                    numeralStyle = uiState.numeralStyle,
                    isExpanded = uiState.isExpandedBarOpen,
                    taxRate = uiState.taxRate,
                    discountRate = uiState.discountRate,
                    themeMode = if (isGlass) com.example.model.ThemeMode.GLASS else uiState.themeMode,
                    isIosStyle = uiState.isIosStyle,
                    onDigit = { viewModel.onDigit(it) },
                    onDoubleZero = { viewModel.onDoubleZero() },
                    onTripleZero = { viewModel.onTripleZero() },
                    onDot = { viewModel.onDot() },
                    onParentheses = { viewModel.onParentheses() },
                    onOperator = { viewModel.onOperator(it) },
                    onEquals = { viewModel.onEquals() },
                    onClear = { viewModel.onClear() },
                    onAllClear = { viewModel.onAllClear() },
                    onTaxPlus = { viewModel.onTaxPlus() },
                    onTaxMinus = { viewModel.onTaxMinus() },
                    onDiscount = { viewModel.onDiscount() },
                    onGrandTotal = { viewModel.onGrandTotalRecall() },
                    onMemoryAdd = { viewModel.onMemoryAdd() },
                    onMemorySub = { viewModel.onMemorySub() },
                    onMemoryRecall = { viewModel.onMemoryRecall() },
                    onMemoryClear = { viewModel.onMemoryClear() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    // Local History Drawer backed by Room Database
    if (showHistory) {
        HistoryDrawer(
            sheetState = sheetState,
            history = historyList,
            numeralStyle = uiState.numeralStyle,
            onDismiss = { showHistory = false },
            onSelectItem = { viewModel.selectHistoryItem(it) },
            onSelectExpression = { viewModel.onSelectHistoryExpression(it) },
            onSelectResult = { viewModel.onSelectHistoryResult(it) },
            onDeleteItem = { viewModel.deleteHistoryItem(it) },
            onClearAll = { viewModel.clearAllHistory() },
            onCopyResult = { viewModel.copyToClipboard(context, it) }
        )
    }

    // Settings & Personalization Dialog
    if (showSettings) {
        SettingsDialog(
            currentTaxRate = uiState.taxRate,
            currentDiscountRate = uiState.discountRate,
            currentCurrency = uiState.currencyName,
            currentNumeralStyle = uiState.numeralStyle,
            hapticEnabled = uiState.hapticEnabled,
            soundEnabled = uiState.soundEnabled,
            currentThemeMode = uiState.themeMode,
            currentColorTheme = uiState.colorTheme,
            currentIsIosStyle = uiState.isIosStyle,
            currentIsGlass = isGlass,
            currentDisplayFormatMode = uiState.displayFormatMode,
            currentFixDecimalPlaces = uiState.fixDecimalPlaces,
            currentSciDigits = uiState.sciDigits,
            onDismiss = { showSettings = false },
            onSave = { tax, disc, cur, style, haptic, sound, themeMode, colorTheme, isIosStyle, isGlassStyle, displayMode, fixPlaces, sciDigits ->
                viewModel.updateTaxRate(tax)
                viewModel.updateDiscountRate(disc)
                viewModel.updateCurrency(cur, "هللة")
                viewModel.setNumeralStyle(style)
                viewModel.toggleHaptics(haptic)
                viewModel.toggleSound(sound)
                viewModel.setThemeMode(themeMode)
                viewModel.setColorTheme(colorTheme)
                viewModel.toggleIosStyle(isIosStyle)
                viewModel.toggleGlass(isGlassStyle)
                viewModel.updateDisplayFormat(displayMode, fixPlaces, sciDigits)
            }
        )
    }
}
