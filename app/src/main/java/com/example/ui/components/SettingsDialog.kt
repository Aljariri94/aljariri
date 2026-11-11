package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CalculatorColorTheme
import com.example.model.DisplayFormatMode
import com.example.model.NumeralStyle
import com.example.model.ThemeMode
import com.example.util.NumberFormatter
import java.math.BigDecimal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsDialog(
    currentTaxRate: BigDecimal,
    currentDiscountRate: BigDecimal,
    currentCurrency: String,
    currentNumeralStyle: NumeralStyle,
    hapticEnabled: Boolean,
    soundEnabled: Boolean,
    currentThemeMode: ThemeMode,
    currentColorTheme: CalculatorColorTheme,
    currentIsIosStyle: Boolean = true,
    currentIsGlass: Boolean = false,
    currentDisplayFormatMode: DisplayFormatMode = DisplayFormatMode.NORM,
    currentFixDecimalPlaces: Int = 2,
    currentSciDigits: Int = 2,
    onDismiss: () -> Unit,
    onSave: (
        taxRate: BigDecimal,
        discountRate: BigDecimal,
        currency: String,
        numeralStyle: NumeralStyle,
        haptics: Boolean,
        sound: Boolean,
        themeMode: ThemeMode,
        colorTheme: CalculatorColorTheme,
        isIosStyle: Boolean,
        isGlass: Boolean,
        displayFormatMode: DisplayFormatMode,
        fixDecimalPlaces: Int,
        sciDigits: Int
    ) -> Unit
) {
    var taxInput by remember { mutableStateOf(currentTaxRate.toPlainString()) }
    var discountInput by remember { mutableStateOf(currentDiscountRate.toPlainString()) }
    var currencyInput by remember { mutableStateOf(currentCurrency) }
    var selectedNumeralStyle by remember { mutableStateOf(currentNumeralStyle) }
    var isHapticsOn by remember { mutableStateOf(hapticEnabled) }
    var isSoundOn by remember { mutableStateOf(soundEnabled) }
    var selectedThemeMode by remember { mutableStateOf(if (currentThemeMode == ThemeMode.GLASS) ThemeMode.SYSTEM else currentThemeMode) }
    var selectedColorTheme by remember { mutableStateOf(currentColorTheme) }
    var isIosStyleOn by remember { mutableStateOf(currentIsIosStyle) }
    var isGlassOn by remember { mutableStateOf(currentIsGlass || currentThemeMode == ThemeMode.GLASS) }
    var selectedDisplayFormat by remember { mutableStateOf(currentDisplayFormatMode) }
    var selectedFixPlaces by remember { mutableIntStateOf(currentFixDecimalPlaces) }
    var selectedSciDigits by remember { mutableIntStateOf(currentSciDigits) }

    val taxPresets = listOf("5", "10", "14", "15", "16", "20")
    val currencies = listOf("ريال", "دينار", "درهم", "جنيه", "دولار", "ليرة")
    val previewSample = BigDecimal("6478.64000060")

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "الإعدادات",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Decimal Settings Like Physical Calculator (NORM, FIX 0-9, SCI 0-9)
                Column {
                    Text(
                        text = "صيغة الأرقام العشرية",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DisplayFormatMode.values().forEach { mode ->
                            FilterChip(
                                selected = selectedDisplayFormat == mode,
                                onClick = { selectedDisplayFormat = mode },
                                label = { Text(mode.name, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Secondary options for FIX and SCI modes
                    if (selectedDisplayFormat == DisplayFormatMode.FIX) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "عدد الخانات العشرية الثابتة (FIX):",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (0..9).forEach { num ->
                                FilterChip(
                                    selected = selectedFixPlaces == num,
                                    onClick = { selectedFixPlaces = num },
                                    label = { Text(num.toString(), fontSize = 12.sp) }
                                )
                            }
                        }
                    } else if (selectedDisplayFormat == DisplayFormatMode.SCI) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "عدد أرقام التدوين العلمي (SCI):",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (0..9).forEach { num ->
                                FilterChip(
                                    selected = selectedSciDigits == num,
                                    onClick = { selectedSciDigits = num },
                                    label = { Text(num.toString(), fontSize = 12.sp) }
                                )
                            }
                        }
                    }

                    // Live Preview box
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "معاينة العرض:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val previewText = NumberFormatter.formatWithPhysicalMode(
                                number = previewSample,
                                mode = selectedDisplayFormat,
                                fixPlaces = selectedFixPlaces,
                                sciDigits = selectedSciDigits,
                                style = selectedNumeralStyle
                            )
                            Text(
                                text = previewText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Section 2: Shape (الشكل)
                Column {
                    Text(
                        text = "الشكل",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        CalculatorColorTheme.values().forEach { colorTheme ->
                            val isSelected = selectedColorTheme == colorTheme
                            val themeColor = Color(colorTheme.primaryHex)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) themeColor.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 0.8.dp,
                                    if (isSelected) themeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedColorTheme = colorTheme }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(themeColor)
                                                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = colorTheme.titleAr,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) themeColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    if (isSelected) {
                                        Text(
                                            text = "مُفعّل",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColor
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نمط iOS",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = isIosStyleOn,
                            onCheckedChange = { isIosStyleOn = it },
                            modifier = Modifier.testTag("switch_ios_style")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تصميم الزجاج السائل",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = isGlassOn,
                            onCheckedChange = { isGlassOn = it },
                            modifier = Modifier.testTag("switch_glass_style")
                        )
                    }
                }

                HorizontalDivider()

                // Section 3: Theme mode
                Column {
                    Text(
                        text = "وضع المظهر",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ThemeMode.values().filter { it != ThemeMode.GLASS }.forEach { mode ->
                            val isSelected = selectedThemeMode == mode
                            val (icon, label) = when (mode) {
                                ThemeMode.SYSTEM -> Pair(Icons.Default.BrightnessAuto, "حسب النظام")
                                ThemeMode.LIGHT -> Pair(Icons.Default.LightMode, "نهاري")
                                ThemeMode.DARK -> Pair(Icons.Default.DarkMode, "ليلي")
                                else -> Pair(Icons.Default.BrightnessAuto, "حسب النظام")
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedThemeMode = mode },
                                leadingIcon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(label, fontSize = 11.5.sp) }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Section 4: Haptic & Sound Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الاهتزاز اللمسي",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = isHapticsOn,
                        onCheckedChange = { isHapticsOn = it },
                        modifier = Modifier.testTag("switch_haptics")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "التأثيرات الصوتية للأزرار",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = isSoundOn,
                        onCheckedChange = { isSoundOn = it },
                        modifier = Modifier.testTag("switch_sound")
                    )
                }

                HorizontalDivider()

                // Section 5: Numeral Style
                Column {
                    Text(
                        text = "نمط عرض الأرقام",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedNumeralStyle == NumeralStyle.WESTERN,
                            onClick = { selectedNumeralStyle = NumeralStyle.WESTERN },
                            label = { Text("0123456789") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = selectedNumeralStyle == NumeralStyle.EASTERN,
                            onClick = { selectedNumeralStyle = NumeralStyle.EASTERN },
                            label = { Text("٠١٢٣٤٥٦٧٨٩") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider()

                // Section 6: Tax Rate
                Column {
                    Text(
                        text = "نسبة ضريبة القيمة المضافة (TAX %)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = taxInput,
                        onValueChange = { taxInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_tax_input"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        trailingIcon = { Text("%", modifier = Modifier.padding(end = 8.dp)) }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        taxPresets.forEach { preset ->
                            FilterChip(
                                selected = taxInput == preset,
                                onClick = { taxInput = preset },
                                label = { Text("$preset%") }
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Section 7: Currency Label
                Column {
                    Text(
                        text = "اسم العملة لتفقيط الأرقام",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = currencyInput,
                        onValueChange = { currencyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_currency_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        currencies.forEach { cur ->
                            FilterChip(
                                selected = currencyInput == cur,
                                onClick = { currencyInput = cur },
                                label = { Text(cur) }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                // أسفل خيارات الإعدادات: اسم المستخدم (ابو حمد الجريري)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ابو حمد الجريري",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTax = try { BigDecimal(taxInput) } catch (_: Exception) { currentTaxRate }
                    val finalDisc = try { BigDecimal(discountInput) } catch (_: Exception) { currentDiscountRate }
                    val finalCur = if (currencyInput.isNotBlank()) currencyInput else "ريال"
                    onSave(
                        finalTax,
                        finalDisc,
                        finalCur,
                        selectedNumeralStyle,
                        isHapticsOn,
                        isSoundOn,
                        selectedThemeMode,
                        selectedColorTheme,
                        isIosStyleOn,
                        isGlassOn,
                        selectedDisplayFormat,
                        selectedFixPlaces,
                        selectedSciDigits
                    )
                    onDismiss()
                },
                modifier = Modifier.testTag("settings_save_btn")
            ) {
                Text("حفظ الإعدادات")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
