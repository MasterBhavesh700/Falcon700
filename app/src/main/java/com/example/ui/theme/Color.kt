package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// --- Premium Dark Cosmic Slate & Electric Gold Palette ---
val CosmicSpaceBg = Color(0xFF0C0E14)      // Super dark ambient background
val CosmicSlateCard = Color(0xFF161A26)    // Dark elevated surfaces
val ElectricGold = Color(0xFFFFB300)       // Vivid gold primary accent
val MedicalCyan = Color(0xFF00E5FF)        // High-yield medic theme secondary
val SteelGrayText = Color(0xFFE2E8F0)      // High clarity body color
val MutedSlate = Color(0xFF64748B)         // Secondary labels

val GoldDim = Color(0xFFFFD54F)
val SteelDim = Color(0xFF90A4AE)

val FlinchRed = Color(0xFFFF5252)          // Flinch state
val HoldAmber = Color(0xFFFFAB40)          // Retaining state
val MasteredGreen = Color(0xFF00E676)      // Active recall master state

enum class AppThemeValue {
    GOLD,       // Solaris Gold (Core Motivation)
    CYAN,       // Clinical Cyan (MBBS Surgeon Focus)
    EMERALD,    // Active Recall Forest Green (Spaced Rep Success)
    MONOCHROME, // Obsidian Sleek (Quiet Professionalism)
    CRIMSON     // High-Prestige Emergency Crimson (Flinch Buster)
}

enum class AiEngine {
    GEMINI,      // Strict accountability medical officer
    CLAUDE,      // Socratic clinical case deep-diver
    CHATGPT,     // Bullet cheat-code mnemonic cracker
    NOTEBOOK_LM  // Medical clerk indexer & diary logs synthesizer
}

enum class AiVoiceStyle {
    STRICT_COMMANDER, // Tough accountability and textbook checks
    HELPFUL_MENTOR,   // Detailed conceptual background explanation
    MNEMONIC_WIZARD,  // Acronyms and association tricks
    SOCRATIC_DRILLER  // Rapid Q&A questions and diagnostic challenges
}

fun AppThemeValue.getPrimary(): Color = when (this) {
    AppThemeValue.GOLD -> ElectricGold
    AppThemeValue.CYAN -> MedicalCyan
    AppThemeValue.EMERALD -> MasteredGreen
    AppThemeValue.MONOCHROME -> SteelGrayText
    AppThemeValue.CRIMSON -> FlinchRed
}

fun AppThemeValue.getSecondary(): Color = when (this) {
    AppThemeValue.GOLD -> MedicalCyan
    AppThemeValue.CYAN -> ElectricGold
    AppThemeValue.EMERALD -> MedicalCyan
    AppThemeValue.MONOCHROME -> MutedSlate
    AppThemeValue.CRIMSON -> HoldAmber
}

