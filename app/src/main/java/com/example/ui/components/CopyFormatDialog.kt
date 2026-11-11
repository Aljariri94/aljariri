package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.model.NumeralStyle
import com.example.util.NumberFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopyFormatDialog(
    rawNumber: String,
    onDismiss: () -> Unit,
    onCopied: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val haptic = LocalHapticFeedback.current

    // Clean number input to extract numeric expression or digits
    val cleanNumber = rawNumber.ifEmpty { "0" }

    // Prepare formats:
    // 1. Western formatted with commas
    val westernWithCommas = NumberFormatter.formatDisplay(cleanNumber, NumeralStyle.WESTERN)
    // 2. Western plain digits (no commas)
    val westernPlain = westernWithCommas.replace(",", "").replace(" ", "")
    // 3. Eastern Arabic formatted with commas
    val easternWithCommas = NumberFormatter.formatDisplay(cleanNumber, NumeralStyle.EASTERN)
    // 4. Eastern Arabic plain digits
    val easternPlain = NumberFormatter.toEasternNumerals(westernPlain)

    // State for interactive partial selection/editing
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = westernWithCommas,
                selection = TextRange(0, westernWithCommas.length)
            )
        )
    }

    fun executeCopy(text: String, label: String = "") {
        if (text.isNotEmpty()) {
            clipboardManager.setText(AnnotatedString(text))
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            Toast.makeText(context, "تم النسخ: $text", Toast.LENGTH_SHORT).show()
            onCopied(text)
            onDismiss()
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.92f)
            .testTag("copy_format_dialog")
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header matching ClevCalc style: Dark or theme-colored top header with title "نسخ"
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "نسخ",
                            color = Color.White,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Partial/Custom Selection Freedom ("حرية تحديد الرقم لانسخه كله او جزء منه")
                    Text(
                        text = "تحديد حر (انسخ الرقم كلياً أو جزءاً منه):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = textFieldValue,
                        onValueChange = { textFieldValue = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("copy_custom_selection_field"),
                        singleLine = false,
                        maxLines = 2,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Start
                        ),
                        trailingIcon = {
                            val selectedChunk = if (textFieldValue.selection.collapsed) {
                                textFieldValue.text
                            } else {
                                textFieldValue.text.substring(
                                    textFieldValue.selection.min,
                                    textFieldValue.selection.max
                                )
                            }
                            IconButtonCustom(
                                onClick = { executeCopy(selectedChunk, "المحدد") },
                                testTag = "btn_copy_selected_chunk"
                            )
                        }
                    )

                    if (!textFieldValue.selection.collapsed) {
                        val sub = textFieldValue.text.substring(
                            textFieldValue.selection.min,
                            textFieldValue.selection.max
                        )
                        Button(
                            onClick = { executeCopy(sub, "المحدد") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .testTag("btn_copy_selection_action"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "نسخ الجزء المحدد: $sub",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // 2. Preset Number Formats (matching ClevCalc dialog with Western + Eastern Arabic)
                    Text(
                        text = "اختر صيغة النسخ المباشرة:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // (A) Western with commas
                        FormatOptionRow(
                            displayValue = westernWithCommas,
                            description = "أرقام إنجليزية (مع فواصل)",
                            onClick = { executeCopy(westernWithCommas) },
                            testTag = "format_western_commas"
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            thickness = 0.8.dp
                        )

                        // (B) Western without commas
                        FormatOptionRow(
                            displayValue = westernPlain,
                            description = "أرقام إنجليزية (بدون فواصل)",
                            onClick = { executeCopy(westernPlain) },
                            testTag = "format_western_plain"
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            thickness = 0.8.dp
                        )

                        // (C) Eastern Arabic with commas
                        FormatOptionRow(
                            displayValue = easternWithCommas,
                            description = "أرقام عربية (مع فواصل)",
                            onClick = { executeCopy(easternWithCommas) },
                            testTag = "format_eastern_commas"
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            thickness = 0.8.dp
                        )

                        // (D) Eastern Arabic without commas
                        FormatOptionRow(
                            displayValue = easternPlain,
                            description = "أرقام عربية (بدون فواصل)",
                            onClick = { executeCopy(easternPlain) },
                            testTag = "format_eastern_plain"
                        )
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                )

                // CANCEL button matching ClevCalc screenshot (Large, centered or full width)
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("copy_dialog_cancel_btn")
                ) {
                    Text(
                        text = "CANCEL",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun IconButtonCustom(
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "نسخ التحديد",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun FormatOptionRow(
    displayValue: String,
    description: String,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = displayValue,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
