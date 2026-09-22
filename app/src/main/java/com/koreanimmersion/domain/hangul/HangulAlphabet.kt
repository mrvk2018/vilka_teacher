package com.koreanimmersion.domain.hangul

data class HangulLetter(
    val char: String,
    val nameRu: String,
    val nameEn: String,
    val isVowel: Boolean
)

object HangulAlphabet {
    val consonants: List<HangulLetter> = listOf(
        HangulLetter("ㄱ", "г/k", "g/k", false),
        HangulLetter("ㄴ", "н/n", "n", false),
        HangulLetter("ㄷ", "д/t", "d/t", false),
        HangulLetter("ㄹ", "р/r", "r", false),
        HangulLetter("ㅁ", "м/m", "m", false),
        HangulLetter("ㅂ", "б/p", "b/p", false),
        HangulLetter("ㅅ", "с/s", "s", false),
        HangulLetter("ㅇ", "н/ ng", "ng", false),
        HangulLetter("ㅈ", "дж/ch", "j/ch", false),
        HangulLetter("ㅊ", "ч/ch'", "ch", false),
        HangulLetter("ㅋ", "к/k'", "k", false),
        HangulLetter("ㅌ", "т/t'", "t", false),
        HangulLetter("ㅍ", "п/p'", "p", false),
        HangulLetter("ㅎ", "х/h", "h", false)
    )

    val vowels: List<HangulLetter> = listOf(
        HangulLetter("ㅏ", "а/a", "a", true),
        HangulLetter("ㅑ", "я/ya", "ya", true),
        HangulLetter("ㅓ", "о/eo", "eo", true),
        HangulLetter("ㅕ", "ё/yeo", "yeo", true),
        HangulLetter("ㅗ", "о/o", "o", true),
        HangulLetter("ㅛ", "ё/yo", "yo", true),
        HangulLetter("ㅜ", "у/u", "u", true),
        HangulLetter("ㅠ", "ю/yu", "yu", true),
        HangulLetter("ㅡ", "ы/eu", "eu", true),
        HangulLetter("ㅣ", "и/i", "i", true)
    )

    fun findByChar(char: String): HangulLetter? =
        (consonants + vowels).firstOrNull { it.char == char }
}
