package com.koreanimmersion.data.local

import com.koreanimmersion.core.database.entity.PhraseEntity
import com.koreanimmersion.core.database.entity.TopicEntity

/**
 * Контент курса (подэтапы 1–3): фразы с TTS по `korean_text`, ID совпадают с legacy `topics`.
 */
object CourseContentSeeder {

    /** Полная синхронизация контента (REPLACE) — для MVP и обновлений на устройстве. */
    suspend fun seedIfEmpty(db: AppDatabase) {
        syncAll(db)
    }

    suspend fun syncAll(db: AppDatabase) {
        db.courseTopicDao().insertAll(courseTopics)
        db.coursePhraseDao().insertAll(coursePhrases)
    }

    private val courseTopics = listOf(
        TopicEntity(1, "shop", "Магазин", "Shop", 1),
        TopicEntity(2, "cafe", "Кафе", "Cafe", 2),
        TopicEntity(3, "metro", "Метро", "Subway", 3),
        TopicEntity(4, "bus", "Автобус", "Bus", 4),
        TopicEntity(5, "taxi", "Такси", "Taxi", 5),
        TopicEntity(6, "airport", "Аэропорт", "Airport", 6),
        TopicEntity(7, "train", "Ж/д вокзал", "Train station", 7),
        TopicEntity(8, "bus_station", "Автовокзал", "Bus terminal", 8),
        TopicEntity(9, "beach", "Пляж", "Beach", 9),
        TopicEntity(10, "park", "Парк", "Park", 10),
        TopicEntity(11, "aquapark", "Аквапарк", "Water park", 11),
        TopicEntity(12, "immigration", "Миграционная служба", "Immigration office", 12),
        TopicEntity(13, "greetings", "Знакомство / вежливость", "Greetings", 0),
        TopicEntity(14, "salon", "Парикмахерская", "Hair salon", 13)
    )

    private val coursePhrases = listOf(
        // 1 — Магазин
        p(1001, 1, "얼마예요?", "Сколько это стоит?", "How much is it?", 1),
        p(1002, 1, "이 봉투 주세요.", "Дайте этот пакет, пожалуйста.", "Please give me this bag.", 2),
        p(1003, 1, "물은 어디에 있어요?", "Где лежит вода?", "Where is the water?", 3),
        p(1004, 1, "계산해 주세요.", "Пробейте, пожалуйста.", "Please ring it up.", 4),
        p(1005, 1, "카드로 결제할게요.", "Я заплачу картой.", "I'll pay by card.", 5),
        // 2 — Кафе
        p(2001, 2, "메뉴판 주세요.", "Меню, пожалуйста.", "Menu, please.", 1),
        p(2002, 2, "아메리카노 한 잔 주세요.", "Один американо, пожалуйста.", "One americano, please.", 2),
        p(2003, 2, "포장해 주세요.", "Упакуйте с собой, пожалуйста.", "To go, please.", 3),
        p(2004, 2, "덜 달게 해 주세요.", "Сделайте менее сладким, пожалуйста.", "Less sweet, please.", 4),
        p(2005, 2, "영수증 주세요.", "Чек, пожалуйста.", "Receipt, please.", 5),
        // 3 — Метро
        p(3001, 3, "역이 어디예요?", "Где станция?", "Where is the station?", 1),
        p(3002, 3, "표 한 장 주세요.", "Один билет, пожалуйста.", "One ticket, please.", 2),
        p(3003, 3, "몇 번 출구예요?", "Какой выход?", "Which exit?", 3),
        p(3004, 3, "환승은 어디서 해요?", "Где пересаживаться?", "Where do I transfer?", 4),
        p(3005, 3, "이쪽으로 가면 돼요?", "Идти в эту сторону?", "Should I go this way?", 5),
        // 4 — Автобус
        p(4001, 4, "버스 정류장이 어디예요?", "Где автобусная остановка?", "Where is the bus stop?", 1),
        p(4002, 4, "이 버스 타면 돼요?", "Можно сесть на этот автобус?", "Can I take this bus?", 2),
        p(4003, 4, "다음 정류장에서 내려 주세요.", "Остановите на следующей, пожалуйста.", "Please stop at the next stop.", 3),
        p(4004, 4, "교통카드 찍을게요.", "Приложу транспортную карту.", "I'll tap my transit card.", 4),
        p(4005, 4, "얼마예요?", "Сколько стоит?", "How much is the fare?", 5),
        // 5 — Такси
        p(5001, 5, "공항 가 주세요.", "Пожалуйста, отвезите в аэропорт.", "Please take me to the airport.", 1),
        p(5002, 5, "여기서 세워 주세요.", "Остановите здесь, пожалуйста.", "Please stop here.", 2),
        p(5003, 5, "얼마예요?", "Сколько стоит?", "How much is it?", 3),
        p(5004, 5, "카드로 결제할게요.", "Я заплачу картой.", "I'll pay by card.", 4),
        p(5005, 5, "영수증 주세요.", "Дайте чек, пожалуйста.", "Receipt, please.", 5),
        // 6 — Аэропорт
        p(6001, 6, "체크인 카운터가 어디예요?", "Где стойка регистрации?", "Where is the check-in counter?", 1),
        p(6002, 6, "탑승권 보여 드릴게요.", "Сейчас покажу посадочный талон.", "I'll show you my boarding pass.", 2),
        p(6003, 6, "짐을 부치고 싶어요.", "Хочу сдать багаж.", "I'd like to check my luggage.", 3),
        p(6004, 6, "게이트가 어디예요?", "Где выход на посадку?", "Where is the gate?", 4),
        p(6005, 6, "비행기가 연착됐어요?", "Рейс задерживается?", "Is the flight delayed?", 5),
        // 7 — Ж/д
        p(7001, 7, "표 두 장 주세요.", "Два билета, пожалуйста.", "Two tickets, please.", 1),
        p(7002, 7, "몇 번 플랫폼이에요?", "Какой перрон?", "Which platform?", 2),
        p(7003, 7, "기차가 언제 출발해요?", "Когда отправляется поезд?", "When does the train leave?", 3),
        p(7004, 7, "예약했어요.", "Я бронировал.", "I have a reservation.", 4),
        p(7005, 7, "창가 자리 주세요.", "Место у окна, пожалуйста.", "A window seat, please.", 5),
        // 8 — Автовокзал
        p(8001, 8, "서울행 버스 어디서 타요?", "Откуда автобус до Сеула?", "Where is the bus to Seoul?", 1),
        p(8002, 8, "표 예매했어요.", "Билет уже куплен.", "I booked a ticket.", 2),
        p(8003, 8, "몇 번 승강장이에요?", "Какая платформа?", "Which platform?", 3),
        p(8004, 8, "짐을 맡길 수 있어요?", "Можно сдать багаж?", "Can I check luggage?", 4),
        p(8005, 8, "출발 시간이 언제예요?", "Во сколько отправление?", "What time is departure?", 5),
        // 9 — Пляж
        p(9001, 9, "해변이 어디예요?", "Где пляж?", "Where is the beach?", 1),
        p(9002, 9, "수영복 파는 곳이 어디예요?", "Где купить купальник?", "Where can I buy a swimsuit?", 2),
        p(9003, 9, "구명조끼 빌릴 수 있어요?", "Можно взять спасжилет?", "Can I rent a life vest?", 3),
        p(9004, 9, "그늘이 있는 곳이 어디예요?", "Где есть тень?", "Where is there shade?", 4),
        p(9005, 9, "샤워실이 어디예요?", "Где душ?", "Where are the showers?", 5),
        // 10 — Парк
        p(10001, 10, "공원 입구가 어디예요?", "Где вход в парк?", "Where is the park entrance?", 1),
        p(10002, 10, "화장실이 어디예요?", "Где туалет?", "Where is the restroom?", 2),
        p(10003, 10, "벤치가 있는 곳이 어디예요?", "Где можно сесть на скамейку?", "Where are the benches?", 3),
        p(10004, 10, "사진 찍어도 돼요?", "Можно фотографировать?", "May I take photos?", 4),
        p(10005, 10, "여기 앉아도 돼요?", "Можно здесь сидеть?", "Can I sit here?", 5),
        // 11 — Аквапарк
        p(11001, 11, "락커는 어디예요?", "Где камера хранения?", "Where are the lockers?", 1),
        p(11002, 11, "튜브 빌릴 수 있어요?", "Можно взять круг?", "Can I rent a tube?", 2),
        p(11003, 11, "키 제한이 있어요?", "Есть ограничение по росту?", "Is there a height limit?", 3),
        p(11004, 11, "줄이 얼마나 길어요?", "Насколько длинная очередь?", "How long is the line?", 4),
        p(11005, 11, "수건 대여돼요?", "Полотенце можно взять?", "Are towels available?", 5),
        // 12 — Миграционка
        p(12001, 12, "여권 여기 있어요.", "Вот мой паспорт.", "Here is my passport.", 1),
        p(12002, 12, "비자 연장하러 왔어요.", "Я пришёл продлить визу.", "I came to extend my visa.", 2),
        p(12003, 12, "신청서 어디서 받아요?", "Где взять бланк?", "Where can I get the form?", 3),
        p(12004, 12, "서류 제출할게요.", "Сейчас подам документы.", "I'd like to submit documents.", 4),
        p(12005, 12, "통역 서비스 있어요?", "Есть переводчик?", "Is interpretation available?", 5),
        // 13 — Знакомство
        p(13001, 13, "안녕하세요.", "Здравствуйте.", "Hello.", 1),
        p(13002, 13, "만나서 반가워요.", "Рад познакомиться.", "Nice to meet you.", 2),
        p(13003, 13, "저는 러시아 사람이에요.", "Я из России.", "I'm from Russia.", 3),
        p(13004, 13, "한국어를 배우고 있어요.", "Я учу корейский.", "I'm learning Korean.", 4),
        p(13005, 13, "감사합니다.", "Спасибо.", "Thank you.", 5),
        // 14 — Парикмахерская
        p(14001, 14, "짧게 잘라 주세요.", "Подстригите покороче, пожалуйста.", "Please cut it shorter.", 1),
        p(14002, 14, "머리 감아 주세요.", "Помойте голову, пожалуйста.", "Please wash my hair.", 2),
        p(14003, 14, "앞머리만 잘라 주세요.", "Мне только чёлку, пожалуйста.", "Just the bangs, please.", 3),
        p(14004, 14, "너무 짧지 않게 해 주세요.", "Не слишком коротко, пожалуйста.", "Not too short, please.", 4),
        p(14005, 14, "드라이만 해 주세요.", "Только сушка, пожалуйста.", "Blow-dry only, please.", 5)
    )

    private fun p(
        id: Long,
        topicId: Long,
        korean: String,
        ru: String,
        en: String,
        order: Int
    ) = PhraseEntity(
        id = id,
        topicId = topicId,
        koreanText = korean,
        russianTranslation = ru,
        englishTranslation = en,
        audioUrlOrPath = "",
        order = order
    )
}
