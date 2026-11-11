package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CalculatorMode
import com.example.model.NumeralStyle
import java.math.BigDecimal

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "clev_calculator_preferences",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val KEY_EXPRESSION = "key_last_expression"
        private const val KEY_RESULT = "key_last_result"
        private const val KEY_PREVIOUS_CALC = "key_last_previous_calc"
        private const val KEY_TAFQEET = "key_last_tafqeet"
        private const val KEY_MEMORY = "key_memory_val"
        private const val KEY_GRAND_TOTAL = "key_grand_total_val"
        private const val KEY_HAPTIC = "key_haptic_enabled"
        private const val KEY_SOUND = "key_sound_enabled"
        private const val KEY_NUMERAL_STYLE = "key_numeral_style"
        private const val KEY_CALC_MODE = "key_calc_mode"
        private const val KEY_TAX_RATE = "key_tax_rate"
        private const val KEY_DISCOUNT_RATE = "key_discount_rate"
        private const val KEY_CURRENCY = "key_currency_name"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_COLOR_THEME = "key_color_theme"
        private const val KEY_IOS_STYLE = "key_ios_style_enabled"
        private const val KEY_GLASS_STYLE = "key_glass_style_enabled"
        private const val KEY_DISPLAY_FORMAT_MODE = "key_display_format_mode"
        private const val KEY_FIX_DECIMAL_PLACES = "key_fix_decimal_places"
        private const val KEY_SCI_DIGITS = "key_sci_digits"
    }

    fun saveCalculatorState(
        expression: String,
        result: String,
        previousCalc: String,
        tafqeet: String,
        memory: BigDecimal,
        grandTotal: BigDecimal
    ) {
        prefs.edit()
            .putString(KEY_EXPRESSION, expression)
            .putString(KEY_RESULT, result)
            .putString(KEY_PREVIOUS_CALC, previousCalc)
            .putString(KEY_TAFQEET, tafqeet)
            .putString(KEY_MEMORY, memory.toPlainString())
            .putString(KEY_GRAND_TOTAL, grandTotal.toPlainString())
            .apply()
    }

    fun getLastExpression(): String = prefs.getString(KEY_EXPRESSION, "") ?: ""
    fun getLastResult(): String = prefs.getString(KEY_RESULT, "") ?: ""
    fun getLastPreviousCalc(): String = prefs.getString(KEY_PREVIOUS_CALC, "") ?: ""
    fun getLastTafqeet(): String = prefs.getString(KEY_TAFQEET, "") ?: ""

    fun getMemory(): BigDecimal {
        val s = prefs.getString(KEY_MEMORY, "0") ?: "0"
        return try { BigDecimal(s) } catch (_: Exception) { BigDecimal.ZERO }
    }

    fun getGrandTotal(): BigDecimal {
        val s = prefs.getString(KEY_GRAND_TOTAL, "0") ?: "0"
        return try { BigDecimal(s) } catch (_: Exception) { BigDecimal.ZERO }
    }

    fun isHapticEnabled(): Boolean = prefs.getBoolean(KEY_HAPTIC, true)
    fun setHapticEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()

    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND, true)
    fun setSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SOUND, enabled).apply()

    fun getNumeralStyle(): NumeralStyle {
        val name = prefs.getString(KEY_NUMERAL_STYLE, NumeralStyle.WESTERN.name) ?: NumeralStyle.WESTERN.name
        return try { NumeralStyle.valueOf(name) } catch (_: Exception) { NumeralStyle.WESTERN }
    }
    fun setNumeralStyle(style: NumeralStyle) = prefs.edit().putString(KEY_NUMERAL_STYLE, style.name).apply()

    fun getCalculatorMode(): CalculatorMode {
        val name = prefs.getString(KEY_CALC_MODE, CalculatorMode.CLEVCALC_STYLE.name) ?: CalculatorMode.CLEVCALC_STYLE.name
        return try { CalculatorMode.valueOf(name) } catch (_: Exception) { CalculatorMode.CLEVCALC_STYLE }
    }
    fun setCalculatorMode(mode: CalculatorMode) = prefs.edit().putString(KEY_CALC_MODE, mode.name).apply()

    fun getTaxRate(): BigDecimal {
        val s = prefs.getString(KEY_TAX_RATE, "15.0") ?: "15.0"
        return try { BigDecimal(s) } catch (_: Exception) { BigDecimal("15.0") }
    }
    fun setTaxRate(rate: BigDecimal) = prefs.edit().putString(KEY_TAX_RATE, rate.toPlainString()).apply()

    fun getDiscountRate(): BigDecimal {
        val s = prefs.getString(KEY_DISCOUNT_RATE, "10.0") ?: "10.0"
        return try { BigDecimal(s) } catch (_: Exception) { BigDecimal("10.0") }
    }
    fun setDiscountRate(rate: BigDecimal) = prefs.edit().putString(KEY_DISCOUNT_RATE, rate.toPlainString()).apply()

    fun getCurrency(): String = prefs.getString(KEY_CURRENCY, "ريال") ?: "ريال"
    fun setCurrency(currency: String) = prefs.edit().putString(KEY_CURRENCY, currency).apply()

    fun getThemeMode(): com.example.model.ThemeMode {
        val name = prefs.getString(KEY_THEME_MODE, com.example.model.ThemeMode.SYSTEM.name) ?: com.example.model.ThemeMode.SYSTEM.name
        return try { com.example.model.ThemeMode.valueOf(name) } catch (_: Exception) { com.example.model.ThemeMode.SYSTEM }
    }
    fun setThemeMode(mode: com.example.model.ThemeMode) = prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()

    fun getColorTheme(): com.example.model.CalculatorColorTheme {
        val name = prefs.getString(KEY_COLOR_THEME, com.example.model.CalculatorColorTheme.ROYAL_BLUE.name) ?: com.example.model.CalculatorColorTheme.ROYAL_BLUE.name
        return try { com.example.model.CalculatorColorTheme.valueOf(name) } catch (_: Exception) { com.example.model.CalculatorColorTheme.ROYAL_BLUE }
    }
    fun setColorTheme(theme: com.example.model.CalculatorColorTheme) = prefs.edit().putString(KEY_COLOR_THEME, theme.name).apply()

    fun isIosStyle(): Boolean = prefs.getBoolean(KEY_IOS_STYLE, true)
    fun setIosStyle(enabled: Boolean) = prefs.edit().putBoolean(KEY_IOS_STYLE, enabled).apply()

    fun isGlassStyle(): Boolean = prefs.getBoolean(KEY_GLASS_STYLE, false)
    fun setGlassStyle(enabled: Boolean) = prefs.edit().putBoolean(KEY_GLASS_STYLE, enabled).apply()

    fun getDisplayFormatMode(): com.example.model.DisplayFormatMode {
        val name = prefs.getString(KEY_DISPLAY_FORMAT_MODE, com.example.model.DisplayFormatMode.NORM.name)
            ?: com.example.model.DisplayFormatMode.NORM.name
        return try { com.example.model.DisplayFormatMode.valueOf(name) } catch (_: Exception) { com.example.model.DisplayFormatMode.NORM }
    }
    fun setDisplayFormatMode(mode: com.example.model.DisplayFormatMode) =
        prefs.edit().putString(KEY_DISPLAY_FORMAT_MODE, mode.name).apply()

    fun getFixDecimalPlaces(): Int = prefs.getInt(KEY_FIX_DECIMAL_PLACES, 2).coerceIn(0, 9)
    fun setFixDecimalPlaces(places: Int) = prefs.edit().putInt(KEY_FIX_DECIMAL_PLACES, places.coerceIn(0, 9)).apply()

    fun getSciDigits(): Int = prefs.getInt(KEY_SCI_DIGITS, 2).coerceIn(0, 9)
    fun setSciDigits(digits: Int) = prefs.edit().putInt(KEY_SCI_DIGITS, digits.coerceIn(0, 9)).apply()
}
