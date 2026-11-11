package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalculatorMode
import com.example.model.InvoiceEntry
import com.example.model.NumeralStyle
import com.example.model.ThemeMode
import com.example.ui.theme.ZeroSpecialGold
import com.example.util.NumberFormatter
import java.math.BigDecimal

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClevDisplay(
    expression: String,
    liveResult: String,
    previousCalculation: String,
    previousCalculations: List<String> = emptyList(),
    tafqeetText: String,
    mode: CalculatorMode,
    numeralStyle: NumeralStyle,
    memoryValue: BigDecimal,
    grandTotal: BigDecimal,
    invoiceEntries: List<InvoiceEntry>,
    evaluatedGroupCount: Int,
    isExpanded: Boolean,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    isIosStyle: Boolean = true,
    isGlass: Boolean = false,
    cursorPosition: Int = 0,
    selectionStart: Int = 0,
    selectionEnd: Int = 0,
    hasSelection: Boolean = false,
    selectedText: String = "",
    clearPulse: Long = 0L,
    onCursorOrSelectionChanged: (cursor: Int, start: Int, end: Int) -> Unit = { _, _, _ -> },
    onUserTextChanged: (String, Int) -> Unit = { _, _ -> },
    onToggleIosStyle: () -> Unit = {},
    onToggleGlass: () -> Unit = {},
    onToggleDegreeRad: () -> Unit = {},
    onPercent: () -> Unit = {},
    onToggleExpand: () -> Unit,
    onBackspace: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleMode: (CalculatorMode) -> Unit,
    onCopyResult: (String) -> Unit,
    onCut: (String) -> Unit = {},
    onPaste: () -> Unit = {},
    onShare: (String) -> Unit = {},
    onAllClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isGlassActive = isGlass || (themeMode == ThemeMode.GLASS)
    var showMoreToolsMenu by remember { mutableStateOf(false) }
    var showSelectionMenu by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf(false) }
    val displayExpression = if (expression.isEmpty()) "0" else expression
    val formattedExpr = NumberFormatter.formatDisplay(displayExpression, numeralStyle)

    // Format live result with numeral style
    val formattedLive = if (liveResult.isNotEmpty() && liveResult != expression) {
        if (numeralStyle == NumeralStyle.EASTERN) NumberFormatter.toEasternNumerals(liveResult) else liveResult
    } else ""

    val primaryColor = MaterialTheme.colorScheme.primary
    val historyScrollState = rememberScrollState()

    // Uniform font size for ALL numbers as strictly requested
    val uniformFontSizeSp = 24f

    val effectiveGroupCount = if (invoiceEntries.isNotEmpty()) {
        invoiceEntries.size
    } else if (evaluatedGroupCount > 0) {
        evaluatedGroupCount
    } else {
        0
    }

    fun d(text: String): String = if (numeralStyle == NumeralStyle.EASTERN) {
        NumberFormatter.toEasternNumerals(text)
    } else text

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(if (isGlassActive) Color.Transparent else MaterialTheme.colorScheme.background)
    ) {
        // 1. Dynamic Themed Top Bar (Tied dynamically to calculator color theme)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
            color = if (isGlassActive) primaryColor.copy(alpha = 0.72f) else primaryColor,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Action Icons: History Clock + Status Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenHistory,
                        modifier = Modifier.testTag("history_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = "سجل العمليات",
                            tint = Color.White
                        )
                    }

                    if (memoryValue.compareTo(BigDecimal.ZERO) != 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ZeroSpecialGold.copy(alpha = 0.25f),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "M",
                                color = ZeroSpecialGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (grandTotal.compareTo(BigDecimal.ZERO) != 0) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = primaryColor.copy(alpha = 0.25f),
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = "GT",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Right Title & Hamburger Menu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "آلة حاسبة",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { onOpenSettings() },
                        modifier = Modifier.testTag("menu_hamburger_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = "القائمة",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // 2. Display Container: Numbers on top pushing upwards, scrollable/pull-down, Result below!
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // (A) Top Row: Group counter badge
            if (effectiveGroupCount > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isGlassActive) Color(0x35FFFFFF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, if (isGlassActive) Color(0x80FFFFFF) else primaryColor.copy(alpha = 0.35f)),
                        modifier = Modifier.testTag("group_counter_badge")
                    ) {
                        Text(
                            text = d(effectiveGroupCount.toString()),
                            color = if (isGlassActive) Color.White else primaryColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // (B) Floating Contextual Action Menu (Share, Paste, Copy, Cut, %)
            val isMenuVisible = showSelectionMenu || hasSelection
            AnimatedVisibility(
                visible = isMenuVisible,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 4.dp,
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .align(Alignment.End)
                        .testTag("floating_selection_menu")
                ) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Row(
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مشاركة",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clickable {
                                        val textToShare = if (hasSelection && selectedText.isNotEmpty()) {
                                            selectedText
                                        } else if (liveResult.isNotEmpty()) {
                                            liveResult
                                        } else {
                                            displayExpression
                                        }
                                        onShare(textToShare)
                                        showSelectionMenu = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("menu_action_share")
                            )

                            VerticalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                modifier = Modifier.height(14.dp).width(1.dp)
                            )

                            Text(
                                text = "لصق",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clickable {
                                        onPaste()
                                        showSelectionMenu = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("menu_action_paste")
                            )

                            VerticalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                modifier = Modifier.height(14.dp).width(1.dp)
                            )

                            Text(
                                text = "نسخ",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clickable {
                                        showSelectionMenu = false
                                        if (hasSelection && selectedText.isNotEmpty()) {
                                            onCopyResult(selectedText)
                                        } else {
                                            showCopyDialog = true
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("menu_action_copy")
                            )

                            VerticalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                modifier = Modifier.height(14.dp).width(1.dp)
                            )

                            Text(
                                text = "قص",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .clickable {
                                        val textToCut = if (hasSelection && selectedText.isNotEmpty()) {
                                            selectedText
                                        } else if (liveResult.isNotEmpty()) {
                                            liveResult
                                        } else {
                                            displayExpression
                                        }
                                        onCut(textToCut)
                                        showSelectionMenu = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("menu_action_cut")
                            )

                            VerticalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                                modifier = Modifier.height(14.dp).width(1.dp)
                            )

                            Text(
                                text = "%",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier
                                    .clickable {
                                        onPercent()
                                        showSelectionMenu = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("menu_action_percent")
                            )
                        }
                    }
                }
            }

            // (C) Numbers Area: Multiline, pushes upwards, pull-down to scroll, editable anywhere
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 2.dp)
            ) {
                CalculatorEditText(
                    text = displayExpression,
                    cursorPosition = cursorPosition,
                    selectionStart = selectionStart,
                    selectionEnd = selectionEnd,
                    clearPulse = clearPulse,
                    textColor = if (isGlassActive) Color.White else MaterialTheme.colorScheme.onBackground,
                    cursorColor = primaryColor,
                    fontSizeSp = 26f,
                    onCursorOrSelectionChanged = { cursor, start, end ->
                        onCursorOrSelectionChanged(cursor, start, end)
                        showSelectionMenu = start < end
                    },
                    onUserTextChanged = onUserTextChanged,
                    onPaste = onPaste,
                    onCut = { onCut(if (hasSelection && selectedText.isNotEmpty()) selectedText else displayExpression) },
                    onCopy = { onCopyResult(if (hasSelection && selectedText.isNotEmpty()) selectedText else displayExpression) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // (D) Result Line: Right UNDER the numbers!
            if (formattedLive.isNotEmpty()) {
                Text(
                    text = formattedLive,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isGlassActive) Color.White else MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp)
                        .clickable { onCopyResult(formattedLive) }
                        .testTag("live_result_text")
                )
            }

            // (F) Arabic Tafqeet (Words readout)
            AnimatedVisibility(
                visible = tafqeetText.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clickable { onCopyResult(tafqeetText) }
                        .testTag("tafqeet_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "تفقيط:",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZeroSpecialGold,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = tafqeetText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 3. Ergonomic Divider Toolbar: [...], [^], [⌫] - strictly LTR matching ClevCalc
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        IconButton(
                            onClick = { showMoreToolsMenu = true },
                            modifier = Modifier
                                .size(44.dp)
                                .testTag("more_tools_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreHoriz,
                                contentDescription = "خيارات إضافية",
                                tint = primaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreToolsMenu,
                            onDismissRequest = { showMoreToolsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("%", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = primaryColor)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("نسبة مئوية (%)", fontSize = 14.sp)
                                    }
                                },
                                onClick = {
                                    onPercent()
                                    showMoreToolsMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (isIosStyle) "إيقاف نمط iOS" else "تفعيل نمط iOS", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }
                                },
                                onClick = {
                                    onToggleIosStyle()
                                    showMoreToolsMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(if (isGlassActive) "إيقاف الزجاج السائل" else "تفعيل الزجاج السائل", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                    }
                                },
                                onClick = {
                                    onToggleGlass()
                                    showMoreToolsMenu = false
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = primaryColor
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("الإعدادات والتخصيص", fontSize = 14.sp)
                                    }
                                },
                                onClick = {
                                    showMoreToolsMenu = false
                                    onOpenSettings()
                                }
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("expander_button")
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                            contentDescription = "توسيع الوظائف",
                            tint = primaryColor,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    IconButton(
                        onClick = onBackspace,
                        modifier = Modifier
                            .size(44.dp)
                            .testTag("backspace_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Backspace,
                            contentDescription = "مسح حرف",
                            tint = primaryColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

    if (showCopyDialog) {
        val targetNumber = if (liveResult.isNotEmpty()) liveResult else displayExpression
        CopyFormatDialog(
            rawNumber = targetNumber,
            onDismiss = { showCopyDialog = false },
            onCopied = { copiedText ->
                onCopyResult(copiedText)
            }
        )
    }
}
