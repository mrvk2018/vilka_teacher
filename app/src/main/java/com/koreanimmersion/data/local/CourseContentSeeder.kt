package com.koreanimmersion.data.local

import com.koreanimmersion.core.database.entity.PhraseEntity
import com.koreanimmersion.core.database.entity.TopicEntity

/** Начальный контент курса (подэтап 1). ID тем совпадают с legacy для навигации. */
object CourseContentSeeder {

    suspend fun seedIfEmpty(db: AppDatabase) {
        if (db.courseTopicDao().getAll().isNotEmpty()) return
        db.courseTopicDao().insertAll(courseTopics)
        db.coursePhraseDao().insertAll(coursePhrases)
    }

    private val courseTopics = listOf(
        TopicEntity(
            id = 5,
            key = "taxi",
            titleRu = "Такси",
            titleEn = "Taxi",
            order = 1
        ),
        TopicEntity(
            id = 6,
            key = "airport",
            titleRu = "Аэропорт",
            titleEn = "Airport",
            order = 2
        )
    )

    private val coursePhrases = listOf(
        // Такси
        PhraseEntity(
            id = 5001,
            topicId = 5,
            koreanText = "공항 가 주세요.",
            russianTranslation = "Пожалуйста, отвезите в аэропорт.",
            englishTranslation = "Please take me to the airport.",
            audioUrlOrPath = "",
            order = 1
        ),
        PhraseEntity(
            id = 5002,
            topicId = 5,
            koreanText = "여기서 세워 주세요.",
            russianTranslation = "Остановите здесь, пожалуйста.",
            englishTranslation = "Please stop here.",
            audioUrlOrPath = "",
            order = 2
        ),
        PhraseEntity(
            id = 5003,
            topicId = 5,
            koreanText = "얼마예요?",
            russianTranslation = "Сколько стоит?",
            englishTranslation = "How much is it?",
            audioUrlOrPath = "",
            order = 3
        ),
        PhraseEntity(
            id = 5004,
            topicId = 5,
            koreanText = "카드로 결제할게요.",
            russianTranslation = "Я заплачу картой.",
            englishTranslation = "I'll pay by card.",
            audioUrlOrPath = "",
            order = 4
        ),
        PhraseEntity(
            id = 5005,
            topicId = 5,
            koreanText = "영수증 주세요.",
            russianTranslation = "Дайте чек, пожалуйста.",
            englishTranslation = "Receipt, please.",
            audioUrlOrPath = "",
            order = 5
        ),
        // Аэропорт
        PhraseEntity(
            id = 6001,
            topicId = 6,
            koreanText = "체크인 카운터가 어디예요?",
            russianTranslation = "Где стойка регистрации?",
            englishTranslation = "Where is the check-in counter?",
            audioUrlOrPath = "",
            order = 1
        ),
        PhraseEntity(
            id = 6002,
            topicId = 6,
            koreanText = "탑승권 보여 드릴게요.",
            russianTranslation = "Сейчас покажу посадочный талон.",
            englishTranslation = "I'll show you my boarding pass.",
            audioUrlOrPath = "",
            order = 2
        ),
        PhraseEntity(
            id = 6003,
            topicId = 6,
            koreanText = "짐을 부치고 싶어요.",
            russianTranslation = "Хочу сдать багаж.",
            englishTranslation = "I'd like to check my luggage.",
            audioUrlOrPath = "",
            order = 3
        ),
        PhraseEntity(
            id = 6004,
            topicId = 6,
            koreanText = "게이트가 어디예요?",
            russianTranslation = "Где выход на посадку (гейт)?",
            englishTranslation = "Where is the gate?",
            audioUrlOrPath = "",
            order = 4
        ),
        PhraseEntity(
            id = 6005,
            topicId = 6,
            koreanText = "비행기가 연착됐어요?",
            russianTranslation = "Рейс задерживается?",
            englishTranslation = "Is the flight delayed?",
            audioUrlOrPath = "",
            order = 5
        )
    )
}
