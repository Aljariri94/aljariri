package com.example.model

enum class CalculatorMode(val titleAr: String, val titleEn: String) {
    CLEVCALC_STYLE("حاسبة كليف المبسطة", "ClevCalc Style"),
    COMMERCIAL("حاسبة تجارية شاملة", "Commercial")
}

enum class NumeralStyle(val title: String) {
    WESTERN("0123456789 (أرقام قياسية)"),
    EASTERN("٠١٢٣٤٥٦٧٨٩ (أرقام مشرقية)")
}

enum class DisplayFormatMode(val titleAr: String, val titleEn: String) {
    NORM("عادي (NORM)", "Normal (NORM)"),
    FIX("منازل عشرية ثابتة (FIX)", "Fixed Decimals (FIX)"),
    SCI("تدوين علمي (SCI)", "Scientific (SCI)")
}

enum class ThemeMode(val titleAr: String) {
    SYSTEM("حسب النظام"),
    LIGHT("نهاري"),
    DARK("ليلي"),
    GLASS("الوضع الزجاجي (تصميم الزجاج السائل)")
}

enum class CalculatorColorTheme(
    val titleAr: String,
    val primaryHex: Long,
    val previewColorHex: Long
) {
    APPLE_ORANGE("برتقالي آيفون الكلاسيكي (iOS)", 0xFFFF9F0A, 0xFFFF9F0A),
    ROYAL_BLUE("أزرق حبري دفتري", 0xFF2D5584, 0xFF2D5584),
    EMERALD_GREEN("أخضر ميرمية نباتي مريح", 0xFF3D6B52, 0xFF3D6B52),
    NAVY_SLATE("بترولي كلاسيكي مهدئ", 0xFF2B6B78, 0xFF2B6B78),
    VIOLET_INDIGO("عنابي قرطاسية عتيق", 0xFF7D3C48, 0xFF7D3C48),
    AMBER_WARM("عسلي جلدي دافئ", 0xFF9E5C28, 0xFF9E5C28),
    GRAPHITE_DARK("رصاصي ريشة ناعم", 0xFF4A525E, 0xFF4A525E)
}

enum class KeyType {
    DIGIT,
    DOUBLE_ZERO,
    TRIPLE_ZERO,
    OPERATOR,
    EQUALS,
    CLEAR,
    ALL_CLEAR,
    BACKSPACE,
    PARENTHESES,
    FUNCTION,
    MEMORY
}

data class InvoiceEntry(
    val index: Int,
    val amount: String,
    val operatorBefore: String? = null,
    val isDuplicate: Boolean = false,
    val duplicateWithIndices: List<Int> = emptyList()
)

