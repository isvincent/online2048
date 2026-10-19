package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.model.ThemeOption

data class BoardThemeConfig(
    val backgroundStart: Color,
    val backgroundEnd: Color,
    val boardBackground: Color,
    val emptyCellColor: Color,
    val cardSurface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val isDark: Boolean,
    val tileColors: (Int) -> Color,
    val tileTextColor: (Int) -> Color
)

object ThemeManager {

    fun getConfig(option: ThemeOption): BoardThemeConfig {
        return when (option) {
            ThemeOption.CLASSIC -> classicConfig
            ThemeOption.CYBER_NEON -> cyberNeonConfig
            ThemeOption.COSMIC_DARK -> cosmicDarkConfig
            ThemeOption.MACARON -> macaronConfig
            ThemeOption.LUXURY_GOLD -> luxuryGoldConfig
        }
    }

    private val classicConfig = BoardThemeConfig(
        backgroundStart = Color(0xFFFAF8EF),
        backgroundEnd = Color(0xFFF3ECE0),
        boardBackground = Color(0xFFBBADA0),
        emptyCellColor = Color(0xFFCDC1B4),
        cardSurface = Color(0xFFFFFFFF),
        textPrimary = Color(0xFF776E65),
        textSecondary = Color(0xFF8F7A66),
        accent = Color(0xFFF65E3B),
        isDark = false,
        tileColors = { value ->
            when (value) {
                2 -> Color(0xFFEEE4DA)
                4 -> Color(0xFFEDE0C8)
                8 -> Color(0xFFF2B179)
                16 -> Color(0xFFF59563)
                32 -> Color(0xFFF67C5F)
                64 -> Color(0xFFF65E3B)
                128 -> Color(0xFFEDCF72)
                256 -> Color(0xFFEDCC61)
                512 -> Color(0xFFEDC850)
                1024 -> Color(0xFFEDC53F)
                2048 -> Color(0xFFEDC22E)
                4096 -> Color(0xFF3C3A32)
                else -> Color(0xFF1E1E1E)
            }
        },
        tileTextColor = { value ->
            if (value <= 4) Color(0xFF776E65) else Color(0xFFFFFFFF)
        }
    )

    private val cyberNeonConfig = BoardThemeConfig(
        backgroundStart = Color(0xFF090A1A),
        backgroundEnd = Color(0xFF13112E),
        boardBackground = Color(0xFF1A1C3B),
        emptyCellColor = Color(0xFF262A54),
        cardSurface = Color(0xFF1A1C3B),
        textPrimary = Color(0xFF00F0FF),
        textSecondary = Color(0xFFA5B4FC),
        accent = Color(0xFFFF0077),
        isDark = true,
        tileColors = { value ->
            when (value) {
                2 -> Color(0xFF0D9488)
                4 -> Color(0xFF059669)
                8 -> Color(0xFFD97706)
                16 -> Color(0xFFDC2626)
                32 -> Color(0xFFE11D48)
                64 -> Color(0xFFC026D3)
                128 -> Color(0xFF7C3AED)
                256 -> Color(0xFF4F46E5)
                512 -> Color(0xFF2563EB)
                1024 -> Color(0xFF0284C7)
                2048 -> Color(0xFFFF007F)
                4096 -> Color(0xFF00F0FF)
                else -> Color(0xFFFFD600)
            }
        },
        tileTextColor = { Color(0xFFFFFFFF) }
    )

    private val cosmicDarkConfig = BoardThemeConfig(
        backgroundStart = Color(0xFF0F172A),
        backgroundEnd = Color(0xFF1E1B4B),
        boardBackground = Color(0xFF1E293B),
        emptyCellColor = Color(0xFF334155),
        cardSurface = Color(0xFF1E293B),
        textPrimary = Color(0xFFF8FAFC),
        textSecondary = Color(0xFF94A3B8),
        accent = Color(0xFF8B5CF6),
        isDark = true,
        tileColors = { value ->
            when (value) {
                2 -> Color(0xFF38BDF8)
                4 -> Color(0xFF818CF8)
                8 -> Color(0xFFA78BFA)
                16 -> Color(0xFFC084FC)
                32 -> Color(0xFFF472B6)
                64 -> Color(0xFFFB7185)
                128 -> Color(0xFF34D399)
                256 -> Color(0xFFFBBF24)
                512 -> Color(0xFFF87171)
                1024 -> Color(0xFF60A5FA)
                2048 -> Color(0xFFFACC15)
                4096 -> Color(0xFFEC4899)
                else -> Color(0xFFA855F7)
            }
        },
        tileTextColor = { value ->
            if (value in 2..4) Color(0xFF0F172A) else Color(0xFFFFFFFF)
        }
    )

    private val macaronConfig = BoardThemeConfig(
        backgroundStart = Color(0xFFFFF9F5),
        backgroundEnd = Color(0xFFFDECEF),
        boardBackground = Color(0xFFF0DFD5),
        emptyCellColor = Color(0xFFE2CFC3),
        cardSurface = Color(0xFFFFFFFF),
        textPrimary = Color(0xFF6B584E),
        textSecondary = Color(0xFF8D7B72),
        accent = Color(0xFFFF8FAB),
        isDark = false,
        tileColors = { value ->
            when (value) {
                2 -> Color(0xFFFFE5EC)
                4 -> Color(0xFFFFC2D1)
                8 -> Color(0xFFFFB3C6)
                16 -> Color(0xFFFF8FAB)
                32 -> Color(0xFFFB6F92)
                64 -> Color(0xFFE8AEB7)
                128 -> Color(0xFFB8E0D2)
                256 -> Color(0xFF95D5B2)
                512 -> Color(0xFFD8BBFF)
                1024 -> Color(0xFFC8B6FF)
                2048 -> Color(0xFFFFB703)
                else -> Color(0xFFFAEDCD)
            }
        },
        tileTextColor = { value ->
            if (value <= 8) Color(0xFF5E4940) else Color(0xFFFFFFFF)
        }
    )

    private val luxuryGoldConfig = BoardThemeConfig(
        backgroundStart = Color(0xFF111113),
        backgroundEnd = Color(0xFF1A1A20),
        boardBackground = Color(0xFF22222A),
        emptyCellColor = Color(0xFF2D2D38),
        cardSurface = Color(0xFF1E1E26),
        textPrimary = Color(0xFFDFB86C),
        textSecondary = Color(0xFFA09988),
        accent = Color(0xFFFFD700),
        isDark = true,
        tileColors = { value ->
            when (value) {
                2 -> Color(0xFF3A3833)
                4 -> Color(0xFF4A443B)
                8 -> Color(0xFF615443)
                16 -> Color(0xFF7A654A)
                32 -> Color(0xFF947752)
                64 -> Color(0xFFAF8B5A)
                128 -> Color(0xFFC79F63)
                256 -> Color(0xFFDFB46C)
                512 -> Color(0xFFECC478)
                1024 -> Color(0xFFF5D688)
                2048 -> Color(0xFFFFE299)
                else -> Color(0xFFFFD700)
            }
        },
        tileTextColor = { value ->
            if (value >= 128) Color(0xFF1B1811) else Color(0xFFF2E6D0)
        }
    )
}
