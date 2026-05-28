package com.lumis.character

import androidx.compose.ui.graphics.Color

enum class LumisState {
    EXCELLENT,  // score > 85
    GOOD,       // score 70–85
    REGULAR,    // score 50–70
    BAD,        // score 30–50
    TERRIBLE,   // score < 30
}

object LumisController {

    fun getStateFromScore(score: Int): LumisState = when {
        score > 85 -> LumisState.EXCELLENT
        score > 70 -> LumisState.GOOD
        score > 50 -> LumisState.REGULAR
        score > 30 -> LumisState.BAD
        else -> LumisState.TERRIBLE
    }

    fun getAnimationName(state: LumisState): String = when (state) {
        LumisState.EXCELLENT -> "idle_excellent"
        LumisState.GOOD -> "idle_good"
        LumisState.REGULAR -> "idle_regular"
        LumisState.BAD -> "idle_bad"
        LumisState.TERRIBLE -> "idle_terrible"
    }

    fun getGlowColor(state: LumisState): Color = when (state) {
        LumisState.EXCELLENT -> Color(0xFFFFD700)  // dourado
        LumisState.GOOD -> Color(0xFF87CEEB)        // azul céu
        LumisState.REGULAR -> Color(0xFFB0C4DE)     // azul opaco
        LumisState.BAD -> Color(0xFF708090)         // cinza
        LumisState.TERRIBLE -> Color(0xFF8B0000)    // vermelho escuro
    }

    fun getStatusText(state: LumisState): String = when (state) {
        LumisState.EXCELLENT -> "Semana incrível de sono! ✨"
        LumisState.GOOD -> "Dormindo bem essa semana 😊"
        LumisState.REGULAR -> "Sono regular, pode melhorar 😐"
        LumisState.BAD -> "Semana difícil de sono 😔"
        LumisState.TERRIBLE -> "Precisamos melhorar seu sono 💤"
    }
}
