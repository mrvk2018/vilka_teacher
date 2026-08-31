package com.koreanimmersion.ui.navigation

/** Маршруты приложения (строковые идентификаторы для Navigation Compose). */
object NavRoutes {
    const val HOME = "home"
    const val PROGRESS = "progress"
    fun topic(topicId: Long) = "topic/$topicId"
    fun lesson(lessonId: Long) = "lesson/$lessonId"
    fun manual(topicId: Long) = "manual/$topicId"
    fun exam(lessonId: Long) = "exam/$lessonId"
}
