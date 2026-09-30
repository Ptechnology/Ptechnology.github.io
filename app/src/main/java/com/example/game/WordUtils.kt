package com.example.game

import java.text.Normalizer

object WordUtils {

    fun normalizeText(text: String): String {
        val nfd = Normalizer.normalize(text.trim(), Normalizer.Form.NFD)
        val pattern = "\\p{InCombiningDiacriticalMarks}+".toRegex()
        return pattern.replace(nfd, "").uppercase()
    }

    /**
     * Calcula quais índices da palavra original devem ser revelados inicialmente
     * conforme a regra:
     * - Até 5 letras: mostrar 1 letra
     * - Até 10 letras: mostrar 2 letras
     * - Mais de 10 letras: mostrar 3 letras
     */
    fun computeInitialRevealedIndices(word: String): Set<Int> {
        val letterIndices = word.indices.filter { word[it].isLetter() }
        val count = letterIndices.size

        if (count == 0) return emptySet()

        val revealed = mutableSetOf<Int>()

        when {
            count <= 5 -> {
                // Revela 1 letra (primeira letra)
                revealed.add(letterIndices.first())
            }
            count <= 10 -> {
                // Revela 2 letras (primeira e uma no meio)
                revealed.add(letterIndices.first())
                val midIndex = letterIndices[count / 2]
                revealed.add(midIndex)
            }
            else -> {
                // Revela 3 letras (primeira, no terço central e penúltima)
                revealed.add(letterIndices.first())
                val midIndex = letterIndices[count / 2]
                revealed.add(midIndex)
                val lateIndex = letterIndices[(count * 3) / 4]
                revealed.add(lateIndex)
            }
        }

        return revealed
    }

    /**
     * Gera a representação mascarada para exibição ao usuário
     */
    fun getMaskedDisplay(
        word: String,
        revealedIndices: Set<Int>,
        userGuessedChars: Set<Char>
    ): String {
        val sb = StringBuilder()
        for (i in word.indices) {
            val ch = word[i]
            if (!ch.isLetter()) {
                sb.append(ch)
            } else {
                val normalizedCh = normalizeText(ch.toString()).firstOrNull() ?: ch.uppercaseChar()
                if (i in revealedIndices || normalizedCh in userGuessedChars) {
                    sb.append(ch)
                } else {
                    sb.append('_')
                }
            }
            if (i < word.length - 1) {
                sb.append(' ')
            }
        }
        return sb.toString()
    }

    /**
     * Verifica se todas as letras foram adivinhadas
     */
    fun isWordFullyGuessed(
        word: String,
        revealedIndices: Set<Int>,
        userGuessedChars: Set<Char>
    ): Boolean {
        for (i in word.indices) {
            val ch = word[i]
            if (ch.isLetter()) {
                val normalizedCh = normalizeText(ch.toString()).firstOrNull() ?: ch.uppercaseChar()
                if (i !in revealedIndices && normalizedCh !in userGuessedChars) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Gera código alfanumérico aleatório de 5 caracteres
     */
    fun generateRoomCode(): String {
        val allowedChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..5)
            .map { allowedChars.random() }
            .joinToString("")
    }
}
