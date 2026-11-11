package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.widget.EditText
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("ViewConstructor")
class EditableCalculatorEditText(context: Context) : EditText(context) {

    // Must be nullable var assigned AFTER constructor to avoid crash during super() call
    var onSelectionChangeCallback: ((cursor: Int, selStart: Int, selEnd: Int) -> Unit)? = null
    var onUserTextChangeCallback: ((String, Int) -> Unit)? = null
    var onPasteCallback: (() -> Unit)? = null
    var onCutCallback: (() -> Unit)? = null
    var onCopyCallback: (() -> Unit)? = null

    var isUpdatingTextFromCompose = false
    private var lastClearPulse: Long = 0L

    init {
        // Essential: Keep the cursor movable on tap but NEVER show the system soft keyboard
        showSoftInputOnFocus = false
        isFocusable = true
        isFocusableInTouchMode = true
        isCursorVisible = true
        background = null
        gravity = Gravity.END or Gravity.BOTTOM
        setSingleLine(false)
        isVerticalScrollBarEnabled = true
        scrollBarStyle = android.view.View.SCROLLBARS_INSIDE_OVERLAY
        overScrollMode = android.view.View.OVER_SCROLL_IF_CONTENT_SCROLLS
        setPadding(0, 0, 0, 0)

        // Two-way sync: Watch for any native text changes (e.g., keyboard, autofill, accessibility)
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isUpdatingTextFromCompose) {
                    val raw = s?.toString() ?: ""
                    val cur = selectionStart.coerceIn(0, raw.length)
                    onUserTextChangeCallback?.invoke(raw, cur)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        // Intercept native Android context menu actions (Paste, Cut, Copy)
        // so they are processed safely through the ViewModel with 15-digit validation!
        when (id) {
            android.R.id.paste, android.R.id.pasteAsPlainText -> {
                onPasteCallback?.invoke()
                return true
            }
            android.R.id.cut -> {
                onCutCallback?.invoke()
                return true
            }
            android.R.id.copy -> {
                onCopyCallback?.invoke()
                return true
            }
        }
        return super.onTextContextMenuItem(id)
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        // Allow user to pull down / scroll smoothly through the numbers
        parent?.requestDisallowInterceptTouchEvent(true)
        if (event.action == android.view.MotionEvent.ACTION_UP || event.action == android.view.MotionEvent.ACTION_CANCEL) {
            parent?.requestDisallowInterceptTouchEvent(false)
        }
        return super.onTouchEvent(event)
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        try {
            if (!isUpdatingTextFromCompose) {
                val cursor = if (selStart == selEnd) selStart else selEnd
                onSelectionChangeCallback?.invoke(cursor, selStart, selEnd)
            }
        } catch (_: Throwable) {}
    }

    fun handleClearPulse(pulse: Long, newText: String) {
        if (pulse != lastClearPulse) {
            lastClearPulse = pulse
            forceClear(newText)
        }
    }

    fun forceClear(fallbackText: String = "0") {
        isUpdatingTextFromCompose = true
        setText(fallbackText)
        val len = fallbackText.length
        setSelection(len)
        post {
            try {
                scrollTo(0, 0)
            } catch (_: Throwable) {}
        }
        isUpdatingTextFromCompose = false
    }

    fun syncTextAndSelection(newText: String, requestedCursor: Int, reqStart: Int, reqEnd: Int) {
        try {
            val currentText = text?.toString() ?: ""
            if (currentText != newText) {
                isUpdatingTextFromCompose = true
                setText(newText)
                val safeCursor = requestedCursor.coerceIn(0, newText.length)
                val safeStart = reqStart.coerceIn(0, newText.length)
                val safeEnd = reqEnd.coerceIn(safeStart, newText.length)
                if (safeStart != safeEnd) {
                    setSelection(safeStart, safeEnd)
                } else {
                    setSelection(safeCursor)
                }
                // Automatically scroll down to the cursor position so the active number stays visible at the bottom
                post {
                    try {
                        val lay = layout
                        if (lay != null) {
                            val line = lay.getLineForOffset(safeCursor.coerceIn(0, lay.text.length))
                            val y = lay.getLineBottom(line)
                            val targetScrollY = (y - height).coerceAtLeast(0)
                            scrollTo(0, targetScrollY)
                        }
                    } catch (_: Throwable) {}
                }
                isUpdatingTextFromCompose = false
            } else {
                val curStart = selectionStart
                val curEnd = selectionEnd
                val safeStart = reqStart.coerceIn(0, newText.length)
                val safeEnd = reqEnd.coerceIn(safeStart, newText.length)
                if (curStart != safeStart || curEnd != safeEnd) {
                    isUpdatingTextFromCompose = true
                    if (safeStart != safeEnd) {
                        setSelection(safeStart, safeEnd)
                    } else {
                        setSelection(requestedCursor.coerceIn(0, newText.length))
                    }
                    isUpdatingTextFromCompose = false
                }
            }
        } catch (_: Throwable) {
            isUpdatingTextFromCompose = false
        }
    }
}

@Composable
fun CalculatorEditText(
    text: String,
    cursorPosition: Int,
    selectionStart: Int,
    selectionEnd: Int,
    clearPulse: Long = 0L,
    textColor: Color,
    cursorColor: Color,
    fontSizeSp: Float = 24f,
    onCursorOrSelectionChanged: (cursor: Int, selStart: Int, selEnd: Int) -> Unit,
    onUserTextChanged: (String, Int) -> Unit = { _, _ -> },
    onPaste: () -> Unit = {},
    onCut: () -> Unit = {},
    onCopy: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            EditableCalculatorEditText(ctx).apply {
                this.onSelectionChangeCallback = onCursorOrSelectionChanged
                this.onUserTextChangeCallback = onUserTextChanged
                this.onPasteCallback = onPaste
                this.onCutCallback = onCut
                this.onCopyCallback = onCopy
                textSize = fontSizeSp
                setTextColor(textColor.toArgb())
                highlightColor = cursorColor.copy(alpha = 0.35f).toArgb()

                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        textCursorDrawable?.colorFilter = PorterDuffColorFilter(
                            cursorColor.toArgb(),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                } catch (_: Throwable) {}

                syncTextAndSelection(
                    newText = text,
                    requestedCursor = cursorPosition,
                    reqStart = selectionStart,
                    reqEnd = selectionEnd
                )
            }
        },
        update = { editText ->
            editText.onSelectionChangeCallback = onCursorOrSelectionChanged
            editText.onUserTextChangeCallback = onUserTextChanged
            editText.onPasteCallback = onPaste
            editText.onCutCallback = onCut
            editText.onCopyCallback = onCopy
            editText.textSize = fontSizeSp
            editText.setTextColor(textColor.toArgb())
            editText.highlightColor = cursorColor.copy(alpha = 0.35f).toArgb()

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    editText.textCursorDrawable?.colorFilter = PorterDuffColorFilter(
                        cursorColor.toArgb(),
                        PorterDuff.Mode.SRC_IN
                    )
                }
            } catch (_: Throwable) {}

            editText.handleClearPulse(clearPulse, text)
            editText.syncTextAndSelection(
                newText = text,
                requestedCursor = cursorPosition,
                reqStart = selectionStart,
                reqEnd = selectionEnd
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .testTag("interactive_calculator_edit_text")
    )
}
