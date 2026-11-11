package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyType
import com.example.model.NumeralStyle
import com.example.model.ThemeMode
import com.example.util.NumberFormatter
import java.math.BigDecimal

val LocalThemeIsGlass = compositionLocalOf { false }
val LocalThemeIsIosStyle = compositionLocalOf { true }

@Composable
fun ClevKeypad(
    numeralStyle: NumeralStyle,
    isExpanded: Boolean,
    taxRate: BigDecimal,
    discountRate: BigDecimal,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    isIosStyle: Boolean = true,
    onDigit: (String) -> Unit,
    onDoubleZero: () -> Unit,
    onTripleZero: () -> Unit,
    onDot: () -> Unit,
    onParentheses: () -> Unit,
    onOperator: (String) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onAllClear: () -> Unit,
    onTaxPlus: () -> Unit,
    onTaxMinus: () -> Unit,
    onDiscount: () -> Unit,
    onGrandTotal: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySub: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    fun d(num: String): String = if (numeralStyle == NumeralStyle.EASTERN) {
        NumberFormatter.toEasternNumerals(num)
    } else num

    // STRICTLY LTR (Left-To-Right) layout direction so numbers and operators
    // always follow the standard calculator layout:
    // Left: C, 7, 4, 1, 0  |  Right: ÷, ×, −, +, =
    CompositionLocalProvider(
        LocalLayoutDirection provides LayoutDirection.Ltr,
        LocalThemeIsGlass provides (themeMode == ThemeMode.GLASS),
        LocalThemeIsIosStyle provides isIosStyle
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Expandable Secondary Commercial & Memory Functions (toggled by ^)
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Secondary Row 1: Tax+, Tax-, Disc%, GT
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Clev3DButton(
                            text = "ضريبة+",
                            keyType = KeyType.FUNCTION,
                            onClick = onTaxPlus,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        Clev3DButton(
                            text = "ضريبة-",
                            keyType = KeyType.FUNCTION,
                            onClick = onTaxMinus,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        Clev3DButton(
                            text = "خصم%",
                            keyType = KeyType.FUNCTION,
                            onClick = onDiscount,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        Clev3DButton(
                            text = "GT",
                            keyType = KeyType.FUNCTION,
                            onClick = onGrandTotal,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }

                    // Secondary Row 2: Memory bank (MC, MR, M-, M+)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Clev3DButton(
                            text = "MC",
                            keyType = KeyType.MEMORY,
                            onClick = onMemoryClear,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        Clev3DButton(
                            text = "MR",
                            keyType = KeyType.MEMORY,
                            onClick = onMemoryRecall,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        Clev3DButton(
                            text = "M-",
                            keyType = KeyType.MEMORY,
                            onClick = onMemorySub,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        Clev3DButton(
                            text = "M+",
                            keyType = KeyType.MEMORY,
                            onClick = onMemoryAdd,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                }
            }

            // ==============================================================
            // Main Calculator Grid (5 Rows x 4 Equal Columns)
            // Exactly matching user specification:
            // Row 1: C    ( )   ×     ÷
            // Row 2: 7    8     9     .
            // Row 3: 4    5     6     −
            // Row 4: 1    2     3     +
            // Row 5: 0    00    000   =
            // ==============================================================

            // ==============================================================
            // Main Calculator Grid (5 Rows x 4 Equal Columns)
            // Exactly matching user specification:
            // Row 1: C    ( )%  .     ÷
            // Row 2: 7    8     9     ×
            // Row 3: 4    5     6     −
            // Row 4: 1    2     3     +
            // Row 5: 0    00    000   =
            // ==============================================================

            // Row 1: C, ( ) with %, ., ÷
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Clev3DButton(
                    text = "C",
                    subtitle = "AC",
                    keyType = KeyType.CLEAR,
                    onClick = onClear,
                    onLongClick = onAllClear,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = "( )",
                    subtitle = "%",
                    keyType = KeyType.PARENTHESES,
                    onClick = onParentheses,
                    onLongClick = { onOperator("%") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = ".",
                    keyType = KeyType.DIGIT,
                    onClick = onDot,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = "÷",
                    keyType = KeyType.OPERATOR,
                    onClick = { onOperator("÷") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            // Row 2: 7, 8, 9, ×
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Clev3DButton(
                    text = d("7"),
                    keyType = KeyType.DIGIT,
                    onClick = { onDigit("7") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = d("8"),
                    keyType = KeyType.DIGIT,
                    onClick = { onDigit("8") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = d("9"),
                    keyType = KeyType.DIGIT,
                    onClick = { onDigit("9") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = "×",
                    keyType = KeyType.OPERATOR,
                    onClick = { onOperator("×") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            // Row 3: 4, 5, 6, −
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Clev3DButton(
                    text = d("4"),
                    keyType = KeyType.DIGIT,
                    onClick = { onDigit("4") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = d("5"),
                    keyType = KeyType.DIGIT,
                    onClick = { onDigit("5") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = d("6"),
                    keyType = KeyType.DIGIT,
                    onClick = { onDigit("6") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = "−",
                    keyType = KeyType.OPERATOR,
                    onClick = { onOperator("-") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            // Row 4: 1, 2, 3, +
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Clev3DButton(
                    text = d("1"),
                    keyType = KeyType.DIGIT,
                    onClick = { onDigit("1") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = d("2"),
                    keyType = KeyType.DIGIT,
                    onClick = { onDigit("2") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = d("3"),
                    keyType = KeyType.DIGIT,
                    onClick = { onDigit("3") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = "+",
                    keyType = KeyType.OPERATOR,
                    onClick = { onOperator("+") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }

            // Row 5: 0, 00, 000, =
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Clev3DButton(
                    text = d("0"),
                    keyType = KeyType.DIGIT,
                    onClick = { onDigit("0") },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = d("00"),
                    keyType = KeyType.DOUBLE_ZERO,
                    onClick = onDoubleZero,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = d("000"),
                    keyType = KeyType.TRIPLE_ZERO,
                    onClick = onTripleZero,
                    onLongClick = onDot,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                Clev3DButton(
                    text = "=",
                    keyType = KeyType.EQUALS,
                    onClick = onEquals,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
        }
    }
}

/**
 * Semi-3D Tactile Calculator Button.
 * Features:
 * - Raised mechanical key face with bottom 3D bevel / rim
 * - Animated physical depression ("clicks down" 3dp into base when pressed)
 * - Clear boundaries and spacing so fingers never miss ("الاصبع مايخطيهن")
 * - High-contrast typography and clear secondary hints
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Clev3DButton(
    text: String,
    keyType: KeyType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    subtitle: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val isGlass = LocalThemeIsGlass.current
    val isIosStyle = LocalThemeIsIosStyle.current
    val isDark = MaterialTheme.colorScheme.background.value == com.example.ui.theme.ClevBgDark.value
    val primaryColor = MaterialTheme.colorScheme.primary

    // 3D Colors: Face (top surface), Bevel (bottom 3D rim), Border, and Text
    val (faceColor, bevelColor, borderColor, textColor) = if (isGlass) {
        // Liquid Glass (تصميم الزجاج السائل) - Works across both Light and Dark modes!
        when (keyType) {
            KeyType.EQUALS -> {
                val face = primaryColor.copy(alpha = if (isDark) 0.88f else 0.92f)
                val bevel = primaryColor.copy(alpha = 0.40f)
                val border = Color(0xCCFFFFFF)
                val textCol = Color.White
                Quad(face, bevel, border, textCol)
            }
            KeyType.CLEAR, KeyType.ALL_CLEAR -> {
                val face = if (isDark) Color(0x45E11D48) else Color(0x28E11D48)
                val bevel = Color(0x20000000)
                val border = if (isDark) Color(0x80FB7185) else Color(0x60FB7185)
                val textCol = if (isDark) Color(0xFFFECDD3) else Color(0xFFE11D48)
                Quad(face, bevel, border, textCol)
            }
            KeyType.DOUBLE_ZERO, KeyType.TRIPLE_ZERO -> {
                val face = if (isDark) Color(0x40D97706) else Color(0x30F59E0B)
                val bevel = Color(0x20000000)
                val border = if (isDark) Color(0x80F59E0B) else Color(0x70F59E0B)
                val textCol = if (isDark) Color(0xFFFDE68A) else Color(0xFFD97706)
                Quad(face, bevel, border, textCol)
            }
            KeyType.OPERATOR, KeyType.PARENTHESES -> {
                val face = primaryColor.copy(alpha = if (isDark) 0.42f else 0.28f)
                val bevel = Color(0x20000000)
                val border = primaryColor.copy(alpha = if (isDark) 0.75f else 0.65f)
                val textCol = if (isDark) Color.White else primaryColor
                Quad(face, bevel, border, textCol)
            }
            KeyType.FUNCTION, KeyType.MEMORY -> {
                val face = if (isDark) Color(0x24FFFFFF) else Color(0x50FFFFFF)
                val bevel = Color(0x18000000)
                val border = if (isDark) Color(0x45FFFFFF) else Color(0x80FFFFFF)
                val textCol = if (isDark) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.onBackground
                Quad(face, bevel, border, textCol)
            }
            KeyType.DIGIT -> {
                val face = if (isDark) Color(0x28FFFFFF) else Color(0x75FFFFFF)
                val bevel = Color(0x18000000)
                val border = if (isDark) Color(0x60FFFFFF) else Color(0xB5FFFFFF)
                val textCol = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground
                Quad(face, bevel, border, textCol)
            }
            else -> {
                val face = if (isDark) Color(0x28FFFFFF) else Color(0x75FFFFFF)
                val bevel = Color(0x18000000)
                val border = if (isDark) Color(0x60FFFFFF) else Color(0xB5FFFFFF)
                val textCol = if (isDark) Color.White else MaterialTheme.colorScheme.onBackground
                Quad(face, bevel, border, textCol)
            }
        }
    } else if (isIosStyle) {
        // Apple iOS Style Palette (Active across any mode when iPhone Mode is ON)
        when (keyType) {
            KeyType.EQUALS -> {
                val face = primaryColor
                val bevel = if (isDark) Color(0xFF15224D) else Color(0xFF1A3354)
                val border = primaryColor.copy(alpha = 0.9f)
                val textCol = Color.White
                Quad(face, bevel, border, textCol)
            }
            KeyType.CLEAR, KeyType.ALL_CLEAR -> {
                val face = if (isDark) Color(0xFFA5A5A5) else Color(0xFFD4D4D2)
                val bevel = if (isDark) Color(0xFF707070) else Color(0xFFB0B0AE)
                val border = if (isDark) Color(0xFFB5B5B5) else Color(0xFFE0E0DC)
                val textCol = Color.Black
                Quad(face, bevel, border, textCol)
            }
            KeyType.DOUBLE_ZERO, KeyType.TRIPLE_ZERO -> {
                val face = if (isDark) Color(0xFF382F1E) else Color(0xFFF7EEDD)
                val bevel = if (isDark) Color(0xFF221C11) else Color(0xFFDFD1B8)
                val border = if (isDark) Color(0xFF655432) else Color(0xFFECE0C5)
                val textCol = Color(0xFFFBBF24)
                Quad(face, bevel, border, textCol)
            }
            KeyType.OPERATOR, KeyType.PARENTHESES -> {
                val face = primaryColor
                val bevel = primaryColor.copy(alpha = 0.55f)
                val border = primaryColor.copy(alpha = 0.85f)
                val textCol = Color.White
                Quad(face, bevel, border, textCol)
            }
            KeyType.FUNCTION, KeyType.MEMORY -> {
                val face = if (isDark) Color(0xFF444444) else Color(0xFFE6E6E3)
                val bevel = if (isDark) Color(0xFF282828) else Color(0xFFCACAC6)
                val border = if (isDark) Color(0xFF585858) else Color(0xFFD8D8D4)
                val textCol = if (isDark) Color.White else Color(0xFF1C1C1E)
                Quad(face, bevel, border, textCol)
            }
            KeyType.DIGIT -> {
                val face = if (isDark) Color(0xFF333333) else Color(0xFFF7F7F5)
                val bevel = if (isDark) Color(0xFF1E1E1E) else Color(0xFFDADAD6)
                val border = if (isDark) Color(0xFF484848) else Color(0xFFE5E5E0)
                val textCol = if (isDark) Color.White else Color(0xFF1C1C1E)
                Quad(face, bevel, border, textCol)
            }
            else -> {
                val face = if (isDark) Color(0xFF333333) else Color(0xFFF7F7F5)
                val bevel = if (isDark) Color(0xFF1E1E1E) else Color(0xFFDADAD6)
                val border = if (isDark) Color(0xFF484848) else Color(0xFFE5E5E0)
                val textCol = if (isDark) Color.White else Color(0xFF1C1C1E)
                Quad(face, bevel, border, textCol)
            }
        }
    } else {
        // Classic Tactile Calculator Mode (When iPhone Mode is OFF)
        when (keyType) {
            KeyType.EQUALS -> {
                val face = primaryColor
                val bevel = if (isDark) Color(0xFF15224D) else Color(0xFF1A3354)
                val border = primaryColor.copy(alpha = 0.9f)
                val textCol = Color.White
                Quad(face, bevel, border, textCol)
            }
            KeyType.CLEAR, KeyType.ALL_CLEAR -> {
                val face = if (isDark) Color(0xFF2A151C) else Color(0xFFFBF0EE)
                val bevel = if (isDark) Color(0xFF17090D) else Color(0xFFE8C7C3)
                val border = if (isDark) Color(0xFF5A1E2D) else Color(0xFFF2D5D1)
                val textCol = if (isDark) Color(0xFFFB7185) else Color(0xFFC53030)
                Quad(face, bevel, border, textCol)
            }
            KeyType.DOUBLE_ZERO, KeyType.TRIPLE_ZERO -> {
                val face = if (isDark) Color(0xFF29241B) else Color(0xFFF9F3E5)
                val bevel = if (isDark) Color(0xFF16130D) else Color(0xFFDCCFB4)
                val border = if (isDark) Color(0xFF574728) else Color(0xFFEAE0C7)
                val textCol = if (isDark) Color(0xFFFBBF24) else Color(0xFF9E5C28)
                Quad(face, bevel, border, textCol)
            }
            KeyType.OPERATOR, KeyType.PARENTHESES -> {
                val face = if (isDark) Color(0xFF1B2435) else Color(0xFFF2ECE0)
                val bevel = if (isDark) Color(0xFF0F1520) else Color(0xFFD0C6B4)
                val border = if (isDark) Color(0xFF293750) else Color(0xFFDFD5C2)
                val textCol = primaryColor
                Quad(face, bevel, border, textCol)
            }
            KeyType.FUNCTION, KeyType.MEMORY -> {
                val face = if (isDark) Color(0xFF171F2E) else Color(0xFFEFE8DC)
                val bevel = if (isDark) Color(0xFF0C111A) else Color(0xFFD1C6B3)
                val border = if (isDark) Color(0xFF243247) else Color(0xFFDED4C0)
                val textCol = primaryColor
                Quad(face, bevel, border, textCol)
            }
            KeyType.DIGIT -> {
                val face = if (isDark) Color(0xFF222A3A) else Color(0xFFFFFDF8)
                val bevel = if (isDark) Color(0xFF121721) else Color(0xFFDDD5C7)
                val border = if (isDark) Color(0xFF323D52) else Color(0xFFE4DCCF)
                val textCol = MaterialTheme.colorScheme.onBackground
                Quad(face, bevel, border, textCol)
            }
            else -> {
                val face = if (isDark) Color(0xFF222A3A) else Color(0xFFFFFDF8)
                val bevel = if (isDark) Color(0xFF121721) else Color(0xFFDDD5C7)
                val border = if (isDark) Color(0xFF323D52) else Color(0xFFE4DCCF)
                val textCol = MaterialTheme.colorScheme.onBackground
                Quad(face, bevel, border, textCol)
            }
        }
    }

    // Mechanical Push-Down Offset & Apple Organic Spring Scale:
    val pushOffset by animateDpAsState(
        targetValue = if (isPressed) (if (isIosStyle) 2.dp else 3.5.dp) else 0.dp,
        label = "clev_button_push"
    )

    val springScale by animateFloatAsState(
        targetValue = if (isPressed && isIosStyle) 0.93f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ios_spring_scale"
    )

    // Rounded squircle (30dp for iPhone mode, 12dp for classic tactile mode)
    val shape = if (isIosStyle) RoundedCornerShape(30.dp) else RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = springScale
                scaleY = springScale
            }
            .testTag("clev_key_${text.replace("÷", "div").replace("×", "mul").replace("%", "pct").replace("−", "sub").replace("-", "sub").replace(".", "dot").replace("(", "lp").replace(")", "rp").replace(" ", "")}")
    ) {
        // 1. The 3D Bevel Base (underneath the key)
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 3.5.dp),
            shape = shape,
            color = bevelColor
        ) {}

        // 2. The Elevated Key Surface that depresses when pressed
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 3.5.dp)
                .offset(y = pushOffset)
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            shape = shape,
            color = if (isPressed) faceColor.copy(alpha = 0.88f) else faceColor,
            border = BorderStroke(1.2.dp, borderColor)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (isGlass) {
                    // Specular reflection sheen line across the top edge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.82f)
                            .height(1.dp)
                            .align(Alignment.TopCenter)
                            .padding(top = 1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = 0.70f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val fontSize = when {
                        keyType == KeyType.EQUALS -> 32.sp
                        text == "." -> 34.sp
                        text == "( )" -> 22.sp
                        keyType == KeyType.OPERATOR && subtitle == "%" -> 25.sp
                        keyType == KeyType.OPERATOR -> 29.sp
                        keyType == KeyType.DOUBLE_ZERO -> 23.sp
                        keyType == KeyType.CLEAR -> 25.sp
                        text.length > 3 -> 14.sp
                        text.length > 2 -> 16.sp
                        else -> 27.sp
                    }

                    Text(
                        text = text,
                        fontSize = fontSize,
                        fontWeight = if (keyType == KeyType.EQUALS || keyType == KeyType.OPERATOR || keyType == KeyType.DOUBLE_ZERO || keyType == KeyType.CLEAR || text == ".") {
                            FontWeight.Bold
                        } else {
                            FontWeight.Medium
                        },
                        color = textColor,
                        textAlign = TextAlign.Center
                    )

                    // Secondary Subtitle Hint (e.g. "%" under ×, "AC" under C, "000" under 00)
                    if (subtitle != null) {
                        val isPercent = subtitle == "%"
                        Text(
                            text = subtitle,
                            fontSize = if (isPercent) 15.sp else 10.5.sp,
                            fontWeight = if (isPercent) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isPercent) textColor else textColor.copy(alpha = 0.75f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.offset(y = if (isPercent) (-2).dp else (-1).dp)
                        )
                    }
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
