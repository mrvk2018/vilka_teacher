package com.koreanimmersion.data.local

import android.content.Context
import androidx.room.Room
import com.koreanimmersion.data.local.entity.LessonEntity
import com.koreanimmersion.data.local.entity.PhraseEntity
import com.koreanimmersion.data.local.entity.TopicEntity

/**
 * Контент для тем 1–13 (магазин … знакомство).
 * Аудио URL — заглушки под assets; озвучка — отдельным шагом (TTS).
 * ⚠️ Весь словарь требует проверки носителем перед продакшеном.
 */
object DatabaseSeeder {

    const val DEFAULT_USER_ID = "local_user"

    suspend fun seedIfEmpty(db: AppDatabase) {
        val topicDao = db.topicDao()
        if (topicDao.getAll().isNotEmpty()) return

        val topics = listOf(
            TopicEntity(1, "shop", "Магазин", 1, "store"),
            TopicEntity(2, "cafe", "Кафе", 2, "local_cafe"),
            TopicEntity(3, "metro", "Метро", 3, "subway"),
            TopicEntity(4, "bus", "Автобус", 4, "directions_bus"),
            TopicEntity(5, "taxi", "Такси", 5, "local_taxi"),
            TopicEntity(6, "airport", "Аэропорт", 6, "flight"),
            TopicEntity(7, "train", "Ж/д вокзал", 7, "train"),
            TopicEntity(8, "bus_station", "Автовокзал", 8, "directions_bus_filled"),
            TopicEntity(9, "beach", "Пляж", 9, "beach_access"),
            TopicEntity(10, "park", "Парк", 10, "park"),
            TopicEntity(11, "aquapark", "Аквапарк", 11, "pool"),
            TopicEntity(12, "immigration", "Миграционная служба", 12, "badge"),
            TopicEntity(13, "greetings", "Знакомство и вежливость", 13, "emoji_people")
        )

        val lessons = shopLessons + cafeLessons + metroLessons + busLessons + taxiLessons +
            airportLessons + trainLessons + busStationLessons + beachLessons + parkLessons +
            aquaparkLessons + immigrationLessons + greetingsLessons

        val phrases = shopPhrases + cafePhrases + metroPhrases + busPhrases + taxiPhrases +
            airportPhrases + trainPhrases + busStationPhrases + beachPhrases + parkPhrases +
            aquaparkPhrases + immigrationPhrases + greetingsPhrases

        topicDao.insertAll(topics)
        db.lessonDao().insertAll(lessons)
        db.phraseDao().insertAll(phrases)
    }

    fun createDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "korean_immersion.db"
        ).build()
    }

    // ── Тема 1: Магазин ───────────────────────────────────────────────────────

    private val shopLessons = listOf(
        LessonEntity(
            id = 101,
            topicId = 1,
            orderInTopic = 1,
            title = "Магазин: на кассе",
            introAudioUrl = "asset:///audio/shop/intro_lesson1.mp3",
            durationTargetMin = 15
        ),
        LessonEntity(
            id = 102,
            topicId = 1,
            orderInTopic = 2,
            title = "Магазин: поиск товаров",
            introAudioUrl = "asset:///audio/shop/intro_lesson2.mp3",
            durationTargetMin = 15
        )
    )

    private val shopPhrases = listOf(
        PhraseEntity(
            id = 10101,
            lessonId = 101,
            koreanText = "얼마예요?",
            koreanRomanization = "eolmayeyo?",
            russianContext = "Спрашиваешь итоговую сумму на кассе, когда кассир пробил все товары.",
            audioUrlKorean = "asset:///audio/shop/10101.mp3"
        ),
        PhraseEntity(
            id = 10102,
            lessonId = 101,
            koreanText = "계산해 주세요.",
            koreanRomanization = "gyesanhae juseyo",
            russianContext = "Кладёшь товары на ленту и просишь пробить — самая частая фраза у кассы.",
            russianLiteralMeaning = "«Посчитайте, пожалуйста»",
            audioUrlKorean = "asset:///audio/shop/10102.mp3"
        ),
        PhraseEntity(
            id = 10103,
            lessonId = 101,
            koreanText = "카드로 결제할게요.",
            koreanRomanization = "kadeuro gyeoljehalgeyo",
            russianContext = "Говоришь, что платишь картой — обычно после вопроса кассира.",
            audioUrlKorean = "asset:///audio/shop/10103.mp3"
        ),
        PhraseEntity(
            id = 10104,
            lessonId = 101,
            koreanText = "현금으로 할게요.",
            koreanRomanization = "hyeongeumeuro halgeyo",
            russianContext = "Говоришь, что платишь наличными.",
            audioUrlKorean = "asset:///audio/shop/10104.mp3"
        ),
        PhraseEntity(
            id = 10105,
            lessonId = 101,
            koreanText = "봉투 하나 주세요.",
            koreanRomanization = "bongtu hana juseyo",
            russianContext = "Просишь пакет — в Корее пакеты часто платные или спрашивают отдельно.",
            audioUrlKorean = "asset:///audio/shop/10105.mp3"
        ),
        PhraseEntity(
            id = 10106,
            lessonId = 101,
            koreanText = "영수증 주세요.",
            koreanRomanization = "yeongsujeung juseyo",
            russianContext = "Просишь чек после оплаты.",
            audioUrlKorean = "asset:///audio/shop/10106.mp3"
        ),
        PhraseEntity(
            id = 10107,
            lessonId = 101,
            koreanText = "적립해 주세요.",
            koreanRomanization = "jeongniphae juseyo",
            russianContext = "Просишь начислить бонусы на карту лояльности — кассир обычно спрашивает карту сам.",
            audioUrlKorean = "asset:///audio/shop/10107.mp3"
        ),
        PhraseEntity(
            id = 10201,
            lessonId = 102,
            koreanText = "이거 어디에 있어요?",
            koreanRomanization = "igeo eodie isseoyo?",
            russianContext = "Показываешь на товар и спрашиваешь, в каком отделе или на какой полке он лежит.",
            audioUrlKorean = "asset:///audio/shop/10201.mp3"
        ),
        PhraseEntity(
            id = 10202,
            lessonId = 102,
            koreanText = "이거 있어요?",
            koreanRomanization = "igeo isseoyo?",
            russianContext = "Спрашиваешь, есть ли такой товар в наличии.",
            audioUrlKorean = "asset:///audio/shop/10202.mp3"
        ),
        PhraseEntity(
            id = 10203,
            lessonId = 102,
            koreanText = "더 큰 거 있어요?",
            koreanRomanization = "deo keun geo isseoyo?",
            russianContext = "Просишь размер или упаковку побольше.",
            audioUrlKorean = "asset:///audio/shop/10203.mp3"
        ),
        PhraseEntity(
            id = 10204,
            lessonId = 102,
            koreanText = "할인해요?",
            koreanRomanization = "harinhaeyo?",
            russianContext = "Уточняешь, действует ли скидка — когда видишь акционный ценник.",
            audioUrlKorean = "asset:///audio/shop/10204.mp3"
        ),
        PhraseEntity(
            id = 10205,
            lessonId = 102,
            koreanText = "한 개만 주세요.",
            koreanRomanization = "han gaeman juseyo",
            russianContext = "Берёшь ровно одну штуку — когда кладут несколько, а тебе нужна одна.",
            audioUrlKorean = "asset:///audio/shop/10205.mp3"
        ),
        PhraseEntity(
            id = 10206,
            lessonId = 102,
            koreanText = "봉투 필요 없어요.",
            koreanRomanization = "bongtu pillyo eopseoyo",
            russianContext = "Отказываешься от пакета — несёшь в своей сумке или корзине.",
            audioUrlKorean = "asset:///audio/shop/10206.mp3"
        )
    )

    // ── Тема 2: Кафе ─────────────────────────────────────────────────────────

    private val cafeLessons = listOf(
        LessonEntity(
            id = 201,
            topicId = 2,
            orderInTopic = 1,
            title = "Кафе: заказ и оплата",
            introAudioUrl = "asset:///audio/cafe/intro_lesson1.mp3",
            durationTargetMin = 15
        ),
        LessonEntity(
            id = 202,
            topicId = 2,
            orderInTopic = 2,
            title = "Кафе: уточнения и ситуации",
            introAudioUrl = "asset:///audio/cafe/intro_lesson2.mp3",
            durationTargetMin = 15
        )
    )

    private val cafePhrases = listOf(
        // Урок 201 — исходные 6 фраз (без изменений) + 2 дополнения
        PhraseEntity(
            id = 20101,
            lessonId = 201,
            koreanText = "메뉴판 주세요.",
            koreanRomanization = "menyupan juseyo",
            russianContext = "Просишь меню, когда садишься за столик.",
            russianLiteralMeaning = "«Меню, пожалуйста»",
            audioUrlKorean = "asset:///audio/cafe/phrase_01.mp3"
        ),
        PhraseEntity(
            id = 20102,
            lessonId = 201,
            koreanText = "아메리카노 한 잔 주세요.",
            koreanRomanization = "amerikano han jan juseyo",
            russianContext = "Заказываешь американо — самый частый заказ.",
            audioUrlKorean = "asset:///audio/cafe/phrase_02.mp3"
        ),
        PhraseEntity(
            id = 20103,
            lessonId = 201,
            koreanText = "따뜻하게 해 주세요.",
            koreanRomanization = "ttatteutage hae juseyo",
            russianContext = "Просишь подать напиток горячим.",
            audioUrlKorean = "asset:///audio/cafe/phrase_03.mp3"
        ),
        PhraseEntity(
            id = 20104,
            lessonId = 201,
            koreanText = "얼마예요?",
            koreanRomanization = "eolmayeyo?",
            russianContext = "Спрашиваешь цену перед оплатой.",
            audioUrlKorean = "asset:///audio/cafe/phrase_04.mp3"
        ),
        PhraseEntity(
            id = 20105,
            lessonId = 201,
            koreanText = "카드로 결제할게요.",
            koreanRomanization = "kadeuro gyeoljehalgeyo",
            russianContext = "Говоришь, что платишь картой.",
            audioUrlKorean = "asset:///audio/cafe/phrase_05.mp3"
        ),
        PhraseEntity(
            id = 20106,
            lessonId = 201,
            koreanText = "포장해 주세요.",
            koreanRomanization = "pojanghae juseyo",
            russianContext = "Просишь упаковать напиток с собой.",
            audioUrlKorean = "asset:///audio/cafe/phrase_06.mp3"
        ),
        PhraseEntity(
            id = 20107,
            lessonId = 201,
            koreanText = "여기서 먹을게요.",
            koreanRomanization = "yeogiseo meogeulgeyo",
            russianContext = "Отвечаешь, что будешь пить/есть на месте, а не на вынос.",
            audioUrlKorean = "asset:///audio/cafe/20107.mp3"
        ),
        PhraseEntity(
            id = 20108,
            lessonId = 201,
            koreanText = "아이스로 주세요.",
            koreanRomanization = "aiseuro juseyo",
            russianContext = "Просишь холодный напиток со льдом.",
            audioUrlKorean = "asset:///audio/cafe/20108.mp3"
        ),
        // Урок 202
        PhraseEntity(
            id = 20201,
            lessonId = 202,
            koreanText = "자리 있어요?",
            koreanRomanization = "jari isseoyo?",
            russianContext = "Спрашиваешь у персонала, есть ли свободные места.",
            audioUrlKorean = "asset:///audio/cafe/20201.mp3"
        ),
        PhraseEntity(
            id = 20202,
            lessonId = 202,
            koreanText = "샷 하나 추가해 주세요.",
            koreanRomanization = "syat hana chugahae juseyo",
            russianContext = "Просишь добавить одну порцию эспрессо — крепче напиток.",
            audioUrlKorean = "asset:///audio/cafe/20202.mp3"
        ),
        PhraseEntity(
            id = 20203,
            lessonId = 202,
            koreanText = "얼음 적게 넣어 주세요.",
            koreanRomanization = "eoreum jeokge neoeo juseyo",
            russianContext = "Просишь положить меньше льда в холодный напиток.",
            audioUrlKorean = "asset:///audio/cafe/20203.mp3"
        ),
        PhraseEntity(
            id = 20204,
            lessonId = 202,
            koreanText = "디카페인으로 해 주세요.",
            koreanRomanization = "dikapein euro hae juseyo",
            russianContext = "Просишь декаф — когда не хочешь кофеина.",
            audioUrlKorean = "asset:///audio/cafe/20204.mp3"
        ),
        PhraseEntity(
            id = 20205,
            lessonId = 202,
            koreanText = "영수증 주세요.",
            koreanRomanization = "yeongsujeung juseyo",
            russianContext = "Просишь чек после оплаты.",
            audioUrlKorean = "asset:///audio/cafe/20205.mp3"
        ),
        PhraseEntity(
            id = 20206,
            lessonId = 202,
            koreanText = "고마워요.",
            koreanRomanization = "gomawoyo",
            russianContext = "Благодаришь бариста или официанта после заказа.",
            audioUrlKorean = "asset:///audio/cafe/20206.mp3"
        )
    )

    // ── Тема 3: Метро ─────────────────────────────────────────────────────────

    private val metroLessons = listOf(
        LessonEntity(
            id = 301,
            topicId = 3,
            orderInTopic = 1,
            title = "Метро: билеты и карта",
            introAudioUrl = "asset:///audio/metro/intro_lesson1.mp3",
            durationTargetMin = 15
        ),
        LessonEntity(
            id = 302,
            topicId = 3,
            orderInTopic = 2,
            title = "Метро: навигация и пересадки",
            introAudioUrl = "asset:///audio/metro/intro_lesson2.mp3",
            durationTargetMin = 15
        )
    )

    private val metroPhrases = listOf(
        PhraseEntity(
            id = 30101,
            lessonId = 301,
            koreanText = "교통카드 충전해 주세요.",
            koreanRomanization = "gyotongkadeu chungjeonhae juseyo",
            russianContext = "Просишь пополнить T-money или другую транспортную карту у кассира.",
            audioUrlKorean = "asset:///audio/metro/30101.mp3"
        ),
        PhraseEntity(
            id = 30102,
            lessonId = 301,
            koreanText = "한 장 주세요.",
            koreanRomanization = "han jang juseyo",
            russianContext = "Покупаешь один одноразовый билет на метро.",
            audioUrlKorean = "asset:///audio/metro/30102.mp3"
        ),
        PhraseEntity(
            id = 30103,
            lessonId = 301,
            koreanText = "강남역까지 얼마예요?",
            koreanRomanization = "gangnamyeokkkaji eolmayeyo?",
            russianContext = "Уточняешь стоимость проезда до нужной станции (подставь своё название).",
            audioUrlKorean = "asset:///audio/metro/30103.mp3"
        ),
        PhraseEntity(
            id = 30104,
            lessonId = 301,
            koreanText = "어디서 갈아타요?",
            koreanRomanization = "eodiseo garatayo?",
            russianContext = "Спрашиваешь, на какой станции или линии нужно делать пересадку.",
            audioUrlKorean = "asset:///audio/metro/30104.mp3"
        ),
        PhraseEntity(
            id = 30105,
            lessonId = 301,
            koreanText = "몇 번 출구예요?",
            koreanRomanization = "myeot beon churiguyeyo?",
            russianContext = "Уточняешь номер выхода — когда выходишь к нужному месту.",
            audioUrlKorean = "asset:///audio/metro/30105.mp3"
        ),
        PhraseEntity(
            id = 30106,
            lessonId = 301,
            koreanText = "2호선 타는 곳이 어디예요?",
            koreanRomanization = "i hoseon saneun gosi eodiyeyo?",
            russianContext = "Ищешь платформу нужной линии — подставь номер своей линии.",
            audioUrlKorean = "asset:///audio/metro/30106.mp3"
        ),
        PhraseEntity(
            id = 30107,
            lessonId = 301,
            koreanText = "카드 찍을게요.",
            koreanRomanization = "kadeu jjigeulgeyo",
            russianContext = "Говоришь, что приложишь транспортную карту к турникету.",
            audioUrlKorean = "asset:///audio/metro/30107.mp3"
        ),
        PhraseEntity(
            id = 30201,
            lessonId = 302,
            koreanText = "이쪽으로 가면 돼요?",
            koreanRomanization = "ijjogeuro gamyeon dwaeyo?",
            russianContext = "Переспрашиваешь, правильно ли идёшь к выходу или пересадке.",
            audioUrlKorean = "asset:///audio/metro/30201.mp3"
        ),
        PhraseEntity(
            id = 30202,
            lessonId = 302,
            koreanText = "엘리베이터 어디예요?",
            koreanRomanization = "ellibeiteo eodiyeyo?",
            russianContext = "Ищешь лифт — с чемоданом, коляской или просто удобнее.",
            audioUrlKorean = "asset:///audio/metro/30202.mp3"
        ),
        PhraseEntity(
            id = 30203,
            lessonId = 302,
            koreanText = "환승은 어디서 해요?",
            koreanRomanization = "hwanseungeun eodiseo haeyo?",
            russianContext = "Спрашиваешь, где именно на станции делать пересадку на другую линию.",
            audioUrlKorean = "asset:///audio/metro/30203.mp3"
        ),
        PhraseEntity(
            id = 30204,
            lessonId = 302,
            koreanText = "막차가 몇 시예요?",
            koreanRomanization = "makchaga myeot siyeyo?",
            russianContext = "Уточняешь время последнего поезда, чтобы не опоздать.",
            audioUrlKorean = "asset:///audio/metro/30204.mp3"
        ),
        PhraseEntity(
            id = 30205,
            lessonId = 302,
            koreanText = "급행이에요?",
            koreanRomanization = "geupaengieyo?",
            russianContext = "Проверяешь, экспресс ли это — некоторые поезда не останавливаются на всех станциях.",
            audioUrlKorean = "asset:///audio/metro/30205.mp3"
        ),
        PhraseEntity(
            id = 30206,
            lessonId = 302,
            koreanText = "다음 역이 어디예요?",
            koreanRomanization = "daeum yeogi eodiyeyo?",
            russianContext = "Уточняешь название следующей станции, когда сомневаешься.",
            audioUrlKorean = "asset:///audio/metro/30206.mp3"
        )
    )

    // ── Тема 4: Автобус ───────────────────────────────────────────────────────

    private val busLessons = listOf(
        LessonEntity(
            id = 401,
            topicId = 4,
            orderInTopic = 1,
            title = "Автобус: посадка и оплата",
            introAudioUrl = "asset:///audio/bus/intro_lesson1.mp3",
            durationTargetMin = 15
        ),
        LessonEntity(
            id = 402,
            topicId = 4,
            orderInTopic = 2,
            title = "Автобус: маршрут и высадка",
            introAudioUrl = "asset:///audio/bus/intro_lesson2.mp3",
            durationTargetMin = 15
        )
    )

    private val busPhrases = listOf(
        PhraseEntity(
            id = 40101,
            lessonId = 401,
            koreanText = "이 버스 명동 가요?",
            koreanRomanization = "i beoseu myeongdong gayo?",
            russianContext = "Проверяешь у водителя, идёт ли этот автобус к нужному месту (подставь своё).",
            audioUrlKorean = "asset:///audio/bus/40101.mp3"
        ),
        PhraseEntity(
            id = 40102,
            lessonId = 401,
            koreanText = "정류장이 어디예요?",
            koreanRomanization = "jeongnyujangi eodiyeyo?",
            russianContext = "Спрашиваешь, где ближайшая остановка автобуса.",
            audioUrlKorean = "asset:///audio/bus/40102.mp3"
        ),
        PhraseEntity(
            id = 40103,
            lessonId = 401,
            koreanText = "교통카드 찍을게요.",
            koreanRomanization = "gyotongkadeu jjigeulgeyo",
            russianContext = "Прикладываешь карту к считывателю при посадке.",
            audioUrlKorean = "asset:///audio/bus/40103.mp3"
        ),
        PhraseEntity(
            id = 40104,
            lessonId = 401,
            koreanText = "내릴 때 불러 주세요.",
            koreanRomanization = "naeril ttae bullaeo juseyo",
            russianContext = "Просишь водителя сказать, когда твоя остановка — очень важная фраза в Корее.",
            russianLiteralMeaning = "«Позовите, когда нужно выходить»",
            audioUrlKorean = "asset:///audio/bus/40104.mp3"
        ),
        PhraseEntity(
            id = 40105,
            lessonId = 401,
            koreanText = "다음 정류장이 어디예요?",
            koreanRomanization = "daeum jeongnyujangi eodiyeyo?",
            russianContext = "Смотришь на табло и переспрашиваешь название следующей остановки.",
            audioUrlKorean = "asset:///audio/bus/40105.mp3"
        ),
        PhraseEntity(
            id = 40106,
            lessonId = 401,
            koreanText = "현금으로 낼게요.",
            koreanRomanization = "hyeongeumeuro naelgeyo",
            russianContext = "Платишь наличными, если нет транспортной карты.",
            audioUrlKorean = "asset:///audio/bus/40106.mp3"
        ),
        PhraseEntity(
            id = 40107,
            lessonId = 401,
            koreanText = "얼마예요?",
            koreanRomanization = "eolmayeyo?",
            russianContext = "Спрашиваешь стоимость проезда при оплате наличными.",
            audioUrlKorean = "asset:///audio/bus/40107.mp3"
        ),
        PhraseEntity(
            id = 40201,
            lessonId = 402,
            koreanText = "여기서 세워 주세요.",
            koreanRomanization = "yeogiseo sewo juseyo",
            russianContext = "Просишь остановиться прямо сейчас — нажал кнопку и подтверждаешь вслух.",
            audioUrlKorean = "asset:///audio/bus/40201.mp3"
        ),
        PhraseEntity(
            id = 40202,
            lessonId = 402,
            koreanText = "몇 정거장 남았어요?",
            koreanRomanization = "myeot jeonggeojang namasseoyo?",
            russianContext = "Уточняешь, сколько остановок до твоего выхода.",
            audioUrlKorean = "asset:///audio/bus/40202.mp3"
        ),
        PhraseEntity(
            id = 40203,
            lessonId = 402,
            koreanText = "환승해야 해요?",
            koreanRomanization = "hwanseunghaeya haeyo?",
            russianContext = "Спрашиваешь, нужно ли пересаживаться на другой автобус.",
            audioUrlKorean = "asset:///audio/bus/40203.mp3"
        ),
        PhraseEntity(
            id = 40204,
            lessonId = 402,
            koreanText = "앞문으로 탈게요.",
            koreanRomanization = "ammuneuro talgeyo",
            russianContext = "Садишься через переднюю дверь — так принято в городских автобусах.",
            audioUrlKorean = "asset:///audio/bus/40204.mp3"
        ),
        PhraseEntity(
            id = 40205,
            lessonId = 402,
            koreanText = "뒷문으로 내릴게요.",
            koreanRomanization = "dwinmuneuro naerilgeyo",
            russianContext = "Выходишь через заднюю дверь — стандарт в Корее.",
            audioUrlKorean = "asset:///audio/bus/40205.mp3"
        ),
        PhraseEntity(
            id = 40206,
            lessonId = 402,
            koreanText = "좌석 있어요?",
            koreanRomanization = "jwaseok isseoyo?",
            russianContext = "Спрашиваешь, есть ли свободное место, когда автобус полный.",
            audioUrlKorean = "asset:///audio/bus/40206.mp3"
        )
    )

    // ── Тема 5: Такси ─────────────────────────────────────────────────────────

    private val taxiLessons = listOf(
        LessonEntity(
            id = 501,
            topicId = 5,
            orderInTopic = 1,
            title = "Такси: поездка и оплата",
            introAudioUrl = "asset:///audio/taxi/intro_lesson1.mp3",
            durationTargetMin = 15
        ),
        LessonEntity(
            id = 502,
            topicId = 5,
            orderInTopic = 2,
            title = "Такси: уточнения по дороге",
            introAudioUrl = "asset:///audio/taxi/intro_lesson2.mp3",
            durationTargetMin = 15
        )
    )

    private val taxiPhrases = listOf(
        PhraseEntity(
            id = 50101,
            lessonId = 501,
            koreanText = "강남역으로 가 주세요.",
            koreanRomanization = "gangnamyeok euro ga juseyo",
            russianContext = "Называешь пункт назначения — подставь нужную станцию или адрес.",
            audioUrlKorean = "asset:///audio/taxi/50101.mp3"
        ),
        PhraseEntity(
            id = 50102,
            lessonId = 501,
            koreanText = "여기로 가 주세요.",
            koreanRomanization = "yeogiro ga juseyo",
            russianContext = "Показываешь точку на карте в телефоне — универсальная фраза.",
            audioUrlKorean = "asset:///audio/taxi/50102.mp3"
        ),
        PhraseEntity(
            id = 50103,
            lessonId = 501,
            koreanText = "멈춰 주세요.",
            koreanRomanization = "meomchwo juseyo",
            russianContext = "Просишь остановиться — когда подъезжаете к месту.",
            audioUrlKorean = "asset:///audio/taxi/50103.mp3"
        ),
        PhraseEntity(
            id = 50104,
            lessonId = 501,
            koreanText = "카드로 할게요.",
            koreanRomanization = "kadeuro halgeyo",
            russianContext = "Говоришь, что платишь картой — почти все такси принимают.",
            audioUrlKorean = "asset:///audio/taxi/50104.mp3"
        ),
        PhraseEntity(
            id = 50105,
            lessonId = 501,
            koreanText = "현금으로 할게요.",
            koreanRomanization = "hyeongeumeuro halgeyo",
            russianContext = "Платишь наличными — когда карта не проходит.",
            audioUrlKorean = "asset:///audio/taxi/50105.mp3"
        ),
        PhraseEntity(
            id = 50106,
            lessonId = 501,
            koreanText = "얼마예요?",
            koreanRomanization = "eolmayeyo?",
            russianContext = "Уточняешь сумму поездки перед оплатой.",
            audioUrlKorean = "asset:///audio/taxi/50106.mp3"
        ),
        PhraseEntity(
            id = 50107,
            lessonId = 501,
            koreanText = "영수증 주세요.",
            koreanRomanization = "yeongsujeung juseyo",
            russianContext = "Просишь чек — для отчётности или просто по привычке.",
            audioUrlKorean = "asset:///audio/taxi/50107.mp3"
        ),
        PhraseEntity(
            id = 50201,
            lessonId = 502,
            koreanText = "오른쪽에 세워 주세요.",
            koreanRomanization = "oreunjjoge sewo juseyo",
            russianContext = "Просишь остановиться у правого края дороги.",
            audioUrlKorean = "asset:///audio/taxi/50201.mp3"
        ),
        PhraseEntity(
            id = 50202,
            lessonId = 502,
            koreanText = "왼쪽에 세워 주세요.",
            koreanRomanization = "oenjjoge sewo juseyo",
            russianContext = "Просишь остановиться слева — когда так можно по правилам.",
            audioUrlKorean = "asset:///audio/taxi/50202.mp3"
        ),
        PhraseEntity(
            id = 50203,
            lessonId = 502,
            koreanText = "여기 내려요.",
            koreanRomanization = "yeogi naeryoyo",
            russianContext = "Говоришь, что выходишь здесь — коротко и понятно.",
            audioUrlKorean = "asset:///audio/taxi/50203.mp3"
        ),
        PhraseEntity(
            id = 50204,
            lessonId = 502,
            koreanText = "네비로 안내해 주세요.",
            koreanRomanization = "nebireo annaehae juseyo",
            russianContext = "Просишь водителя следовать навигатору — когда адрес сложный.",
            audioUrlKorean = "asset:///audio/taxi/50204.mp3"
        ),
        PhraseEntity(
            id = 50205,
            lessonId = 502,
            koreanText = "짐 넣어 주세요.",
            koreanRomanization = "jim neoeo juseyo",
            russianContext = "Просишь положить чемодан в багажник.",
            audioUrlKorean = "asset:///audio/taxi/50205.mp3"
        ),
        PhraseEntity(
            id = 50206,
            lessonId = 502,
            koreanText = "에어컨 켜 주세요.",
            koreanRomanization = "eeokeon kyeo juseyo",
            russianContext = "Просишь включить кондиционер — летом очень актуально.",
            audioUrlKorean = "asset:///audio/taxi/50206.mp3"
        )
    )

    // ── Тема 6: Аэропорт ──────────────────────────────────────────────────────

    private val airportLessons = listOf(
        LessonEntity(
            id = 601,
            topicId = 6,
            orderInTopic = 1,
            title = "Аэропорт: регистрация и посадка",
            introAudioUrl = "asset:///audio/airport/intro_lesson1.mp3",
            durationTargetMin = 15
        ),
        LessonEntity(
            id = 602,
            topicId = 6,
            orderInTopic = 2,
            title = "Аэропорт: после прилёта",
            introAudioUrl = "asset:///audio/airport/intro_lesson2.mp3",
            durationTargetMin = 15
        )
    )

    private val airportPhrases = listOf(
        PhraseEntity(
            id = 60101,
            lessonId = 601,
            koreanText = "체크인 어디예요?",
            koreanRomanization = "chekeuin eodiyeyo?",
            russianContext = "Ищешь стойку регистрации в терминале.",
            audioUrlKorean = "asset:///audio/airport/60101.mp3"
        ),
        PhraseEntity(
            id = 60102,
            lessonId = 601,
            koreanText = "탑승권 발급해 주세요.",
            koreanRomanization = "tapseunggwon balgeuphae juseyo",
            russianContext = "Просишь выдать посадочный талон на стойке.",
            audioUrlKorean = "asset:///audio/airport/60102.mp3"
        ),
        PhraseEntity(
            id = 60103,
            lessonId = 601,
            koreanText = "창가 좌석으로 해 주세요.",
            koreanRomanization = "changga jwaseogeuro hae juseyo",
            russianContext = "Просишь место у окна при регистрации.",
            audioUrlKorean = "asset:///audio/airport/60103.mp3"
        ),
        PhraseEntity(
            id = 60104,
            lessonId = 601,
            koreanText = "수하물 맡길게요.",
            koreanRomanization = "suhamul matgilgeyo",
            russianContext = "Сдаёшь багаж на стойке регистрации.",
            audioUrlKorean = "asset:///audio/airport/60104.mp3"
        ),
        PhraseEntity(
            id = 60105,
            lessonId = 601,
            koreanText = "게이트가 어디예요?",
            koreanRomanization = "geiteuga eodiyeyo?",
            russianContext = "Ищешь свой выход на посадку по номеру на билете.",
            audioUrlKorean = "asset:///audio/airport/60105.mp3"
        ),
        PhraseEntity(
            id = 60106,
            lessonId = 601,
            koreanText = "몇 게이트예요?",
            koreanRomanization = "myeot geiteuyeyo?",
            russianContext = "Уточняешь номер гейта — на табло или у сотрудника.",
            audioUrlKorean = "asset:///audio/airport/60106.mp3"
        ),
        PhraseEntity(
            id = 60107,
            lessonId = 601,
            koreanText = "비행기 몇 시에 출발해요?",
            koreanRomanization = "bihaenggi myeot sie chulbalhaeyo?",
            russianContext = "Уточняешь время вылета — когда сомневаешься в расписании.",
            audioUrlKorean = "asset:///audio/airport/60107.mp3"
        ),
        PhraseEntity(
            id = 60201,
            lessonId = 602,
            koreanText = "짐 찾는 곳이 어디예요?",
            koreanRomanization = "jim chatneun gosi eodiyeyo?",
            russianContext = "Ищешь ленту выдачи багажа после прилёта.",
            audioUrlKorean = "asset:///audio/airport/60201.mp3"
        ),
        PhraseEntity(
            id = 60202,
            lessonId = 602,
            koreanText = "입국 심사 어디예요?",
            koreanRomanization = "ipguk simsa eodiyeyo?",
            russianContext = "Ищешь паспортный контроль после выхода из самолёта.",
            audioUrlKorean = "asset:///audio/airport/60202.mp3"
        ),
        PhraseEntity(
            id = 60203,
            lessonId = 602,
            koreanText = "여권 보여 드릴게요.",
            koreanRomanization = "yeogwon boyeo deurilgeyo",
            russianContext = "Подаёшь паспорт сотруднику на паспортном контроле.",
            audioUrlKorean = "asset:///audio/airport/60203.mp3"
        ),
        PhraseEntity(
            id = 60204,
            lessonId = 602,
            koreanText = "환승은 어디서 해요?",
            koreanRomanization = "hwanseungeun eodiseo haeyo?",
            russianContext = "Спрашиваешь, куда идти на пересадочный рейс в транзитной зоне.",
            audioUrlKorean = "asset:///audio/airport/60204.mp3"
        ),
        PhraseEntity(
            id = 60205,
            lessonId = 602,
            koreanText = "면세점 어디예요?",
            koreanRomanization = "myeonsejeom eodiyeyo?",
            russianContext = "Ищешь duty free — часто спрашивают перед вылетом.",
            audioUrlKorean = "asset:///audio/airport/60205.mp3"
        ),
        PhraseEntity(
            id = 60206,
            lessonId = 602,
            koreanText = "화장실 어디예요?",
            koreanRomanization = "hwajangsil eodiyeyo?",
            russianContext = "Ищешь туалет в терминале — универсальная фраза.",
            audioUrlKorean = "asset:///audio/airport/60206.mp3"
        )
    )

    // ── Тема 7: Ж/д вокзал ──────────────────────────────────────────────────

    private val trainLessons = listOf(
        LessonEntity(
            id = 701,
            topicId = 7,
            orderInTopic = 1,
            title = "Ж/д вокзал: билеты и посадка",
            introAudioUrl = "asset:///audio/train/intro_lesson1.mp3",
            durationTargetMin = 15
        ),
        LessonEntity(
            id = 702,
            topicId = 7,
            orderInTopic = 2,
            title = "Ж/д вокзал: в пути и на перроне",
            introAudioUrl = "asset:///audio/train/intro_lesson2.mp3",
            durationTargetMin = 15
        )
    )

    private val trainPhrases = listOf(
        PhraseEntity(
            id = 70101,
            lessonId = 701,
            koreanText = "부산역행 표 한 장 주세요.",
            koreanRomanization = "busanyeokhaeng pyo han jang juseyo",
            russianContext = "Покупаешь билет до нужного города — подставь своё направление.",
            audioUrlKorean = "asset:///audio/train/70101.mp3"
        ),
        PhraseEntity(
            id = 70102,
            lessonId = 701,
            koreanText = "KTX 표 주세요.",
            koreanRomanization = "KTX pyo juseyo",
            russianContext = "Просишь билет на скоростной поезд KTX — самый частый междугородний.",
            audioUrlKorean = "asset:///audio/train/70102.mp3"
        ),
        PhraseEntity(
            id = 70103,
            lessonId = 701,
            koreanText = "창가 자리로 해 주세요.",
            koreanRomanization = "changga jariro hae juseyo",
            russianContext = "Просишь место у окна при покупке или на стойке.",
            audioUrlKorean = "asset:///audio/train/70103.mp3"
        ),
        PhraseEntity(
            id = 70104,
            lessonId = 701,
            koreanText = "몇 번 플랫폼이에요?",
            koreanRomanization = "myeot beon peullaetfomieyo?",
            russianContext = "Уточняешь номер платформы — смотри на табло вокзала.",
            audioUrlKorean = "asset:///audio/train/70104.mp3"
        ),
        PhraseEntity(
            id = 70105,
            lessonId = 701,
            koreanText = "몇 번 칸이에요?",
            koreanRomanization = "myeot beon kganieyo?",
            russianContext = "Уточняешь номер вагона — указан на билете.",
            audioUrlKorean = "asset:///audio/train/70105.mp3"
        ),
        PhraseEntity(
            id = 70106,
            lessonId = 701,
            koreanText = "예매했어요.",
            koreanRomanization = "yemaehaesseoyo",
            russianContext = "Говоришь, что билет уже куплен онлайн — показываешь приложение.",
            audioUrlKorean = "asset:///audio/train/70106.mp3"
        ),
        PhraseEntity(
            id = 70107,
            lessonId = 701,
            koreanText = "얼마예요?",
            koreanRomanization = "eolmayeyo?",
            russianContext = "Спрашиваешь цену билета на кассе.",
            audioUrlKorean = "asset:///audio/train/70107.mp3"
        ),
        PhraseEntity(
            id = 70201,
            lessonId = 702,
            koreanText = "표 확인해 주세요.",
            koreanRomanization = "pyo hwaginhae juseyo",
            russianContext = "Показываешь билет проводнику или на турникете.",
            audioUrlKorean = "asset:///audio/train/70201.mp3"
        ),
        PhraseEntity(
            id = 70202,
            lessonId = 702,
            koreanText = "강남역에서 내려요.",
            koreanRomanization = "gangnamyeogeseo naeryoyo",
            russianContext = "Говоришь, на какой станции выходишь — подставь свою.",
            audioUrlKorean = "asset:///audio/train/70202.mp3"
        ),
        PhraseEntity(
            id = 70203,
            lessonId = 702,
            koreanText = "부산까지 얼마나 걸려요?",
            koreanRomanization = "busan kkaji eolmana geollyeoyo?",
            russianContext = "Спрашиваешь время в пути до пункта назначения.",
            audioUrlKorean = "asset:///audio/train/70203.mp3"
        ),
        PhraseEntity(
            id = 70204,
            lessonId = 702,
            koreanText = "짐 보관함 어디예요?",
            koreanRomanization = "jim bogwanham eodiyeyo?",
            russianContext = "Ищешь камеру хранения на вокзале — когда нужно оставить чемодан.",
            audioUrlKorean = "asset:///audio/train/70204.mp3"
        ),
        PhraseEntity(
            id = 70205,
            lessonId = 702,
            koreanText = "환승역이 어디예요?",
            koreanRomanization = "hwanseungyeogi eodiyeyo?",
            russianContext = "Уточняешь, на какой станции пересаживаться на другой поезд.",
            audioUrlKorean = "asset:///audio/train/70205.mp3"
        ),
        PhraseEntity(
            id = 70206,
            lessonId = 702,
            koreanText = "곧 도착해요?",
            koreanRomanization = "got dochakhaeyo?",
            russianContext = "Спрашиваешь, скоро ли прибытие — когда не видишь объявлений.",
            audioUrlKorean = "asset:///audio/train/70206.mp3"
        )
    )

    // ── Тема 8: Автовокзал ───────────────────────────────────────────────────

    private val busStationLessons = listOf(
        LessonEntity(
            id = 801,
            topicId = 8,
            orderInTopic = 1,
            title = "Автовокзал: покупка билета",
            introAudioUrl = "asset:///audio/bus_station/intro_lesson1.mp3",
            durationTargetMin = 15
        ),
        LessonEntity(
            id = 802,
            topicId = 8,
            orderInTopic = 2,
            title = "Автовокзал: посадка и в пути",
            introAudioUrl = "asset:///audio/bus_station/intro_lesson2.mp3",
            durationTargetMin = 15
        )
    )

    private val busStationPhrases = listOf(
        PhraseEntity(
            id = 80101,
            lessonId = 801,
            koreanText = "부산 가는 버스 어디서 타요?",
            koreanRomanization = "busan ganeun beoseu eodiseo tayo?",
            russianContext = "Спрашиваешь, с какой платформы автобус на нужный город.",
            audioUrlKorean = "asset:///audio/bus_station/80101.mp3"
        ),
        PhraseEntity(
            id = 80102,
            lessonId = 801,
            koreanText = "표 한 장 주세요.",
            koreanRomanization = "pyo han jang juseyo",
            russianContext = "Покупаешь один билет на междугородний автобус.",
            audioUrlKorean = "asset:///audio/bus_station/80102.mp3"
        ),
        PhraseEntity(
            id = 80103,
            lessonId = 801,
            koreanText = "몇 번 승차장이에요?",
            koreanRomanization = "myeot beon seungchajangieyo?",
            russianContext = "Уточняешь номер посадочной платформы на билете или табло.",
            audioUrlKorean = "asset:///audio/bus_station/80103.mp3"
        ),
        PhraseEntity(
            id = 80104,
            lessonId = 801,
            koreanText = "몇 시에 출발해요?",
            koreanRomanization = "myeot sie chulbalhaeyo?",
            russianContext = "Уточняешь время отправления автобуса.",
            audioUrlKorean = "asset:///audio/bus_station/80104.mp3"
        ),
        PhraseEntity(
            id = 80105,
            lessonId = 801,
            koreanText = "창가 자리로 해 주세요.",
            koreanRomanization = "changga jariro hae juseyo",
            russianContext = "Просишь место у окна — на длинной поездке приятнее.",
            audioUrlKorean = "asset:///audio/bus_station/80105.mp3"
        ),
        PhraseEntity(
            id = 80106,
            lessonId = 801,
            koreanText = "짐 맡길 수 있어요?",
            koreanRomanization = "jim matgil su isseoyo?",
            russianContext = "Спрашиваешь, можно ли сдать багаж в багажное отделение автобуса.",
            audioUrlKorean = "asset:///audio/bus_station/80106.mp3"
        ),
        PhraseEntity(
            id = 80107,
            lessonId = 801,
            koreanText = "얼마예요?",
            koreanRomanization = "eolmayeyo?",
            russianContext = "Спрашиваешь стоимость билета на кассе.",
            audioUrlKorean = "asset:///audio/bus_station/80107.mp3"
        ),
        PhraseEntity(
            id = 80201,
            lessonId = 802,
            koreanText = "표 확인해 주세요.",
            koreanRomanization = "pyo hwaginhae juseyo",
            russianContext = "Показываешь билет водителю или контролёру при посадке.",
            audioUrlKorean = "asset:///audio/bus_station/80201.mp3"
        ),
        PhraseEntity(
            id = 80202,
            lessonId = 802,
            koreanText = "중간에 쉬어요?",
            koreanRomanization = "junggane swieoyo?",
            russianContext = "Уточняешь, будет ли остановка для отдыха и туалета по дороге.",
            audioUrlKorean = "asset:///audio/bus_station/80202.mp3"
        ),
        PhraseEntity(
            id = 80203,
            lessonId = 802,
            koreanText = "화장실 어디예요?",
            koreanRomanization = "hwajangsil eodiyeyo?",
            russianContext = "Ищешь туалет на автовокзале или на остановке отдыха.",
            audioUrlKorean = "asset:///audio/bus_station/80203.mp3"
        ),
        PhraseEntity(
            id = 80204,
            lessonId = 802,
            koreanText = "여기서 내려요.",
            koreanRomanization = "yeogiseo naeryoyo",
            russianContext = "Говоришь водителю, что выходишь на этой остановке.",
            audioUrlKorean = "asset:///audio/bus_station/80204.mp3"
        ),
        PhraseEntity(
            id = 80205,
            lessonId = 802,
            koreanText = "다음 버스 몇 시예요?",
            koreanRomanization = "daeum beoseu myeot siyeyo?",
            russianContext = "Уточняешь расписание следующего рейса — если опоздал.",
            audioUrlKorean = "asset:///audio/bus_station/80205.mp3"
        ),
        PhraseEntity(
            id = 80206,
            lessonId = 802,
            koreanText = "직행이에요?",
            koreanRomanization = "jikhaengieyo?",
            russianContext = "Проверяешь, идёт ли автобус без лишних остановок.",
            audioUrlKorean = "asset:///audio/bus_station/80206.mp3"
        )
    )

    // ── Тема 9: Пляж / пикник / шашлыки ───────────────────────────────────────

    private val beachLessons = listOf(
        LessonEntity(
            id = 901,
            topicId = 9,
            orderInTopic = 1,
            title = "Пляж: базовые фразы",
            introAudioUrl = "asset:///audio/beach/intro_lesson1.mp3",
            durationTargetMin = 15
        ),
        LessonEntity(
            id = 902,
            topicId = 9,
            orderInTopic = 2,
            title = "Пляж: пикник и барбекю",
            introAudioUrl = "asset:///audio/beach/intro_lesson2.mp3",
            durationTargetMin = 15
        )
    )

    private val beachPhrases = listOf(
        PhraseEntity(
            id = 90101,
            lessonId = 901,
            koreanText = "해변이 어디예요?",
            koreanRomanization = "haebyeoni eodiyeyo?",
            russianContext = "Спрашиваешь, как пройти к пляжу — когда вышел из парковки или автобуса.",
            audioUrlKorean = "asset:///audio/beach/90101.mp3"
        ),
        PhraseEntity(
            id = 90102,
            lessonId = 901,
            koreanText = "여기 앉아도 돼요?",
            koreanRomanization = "yeogi anjado dwaeyo?",
            russianContext = "Уточняешь, можно ли занять место на пляже или в зоне отдыха.",
            audioUrlKorean = "asset:///audio/beach/90102.mp3"
        ),
        PhraseEntity(
            id = 90103,
            lessonId = 901,
            koreanText = "수건 빌려 주세요.",
            koreanRomanization = "sugeon bilryeo juseyo",
            russianContext = "Просишь полотенце — на платных пляжах или в гостинице рядом.",
            audioUrlKorean = "asset:///audio/beach/90103.mp3"
        ),
        PhraseEntity(
            id = 90104,
            lessonId = 901,
            koreanText = "선크림 있어요?",
            koreanRomanization = "seokeurim isseoyo?",
            russianContext = "Ищешь солнцезащитный крем в магазине у пляжа.",
            audioUrlKorean = "asset:///audio/beach/90104.mp3"
        ),
        PhraseEntity(
            id = 90105,
            lessonId = 901,
            koreanText = "물 한 병 주세요.",
            koreanRomanization = "mul han byeong juseyo",
            russianContext = "Покупаешь воду — жарко и нужно быстро утолить жажду.",
            audioUrlKorean = "asset:///audio/beach/90105.mp3"
        ),
        PhraseEntity(
            id = 90106,
            lessonId = 901,
            koreanText = "샤워장 어디예요?",
            koreanRomanization = "syawojang eodiyeyo?",
            russianContext = "Ищешь душ после купания — на городском или resort-пляже.",
            audioUrlKorean = "asset:///audio/beach/90106.mp3"
        ),
        PhraseEntity(
            id = 90107,
            lessonId = 901,
            koreanText = "쓰레기통 어디예요?",
            koreanRomanization = "sseuregitong eodiyeyo?",
            russianContext = "Ищешь урну — на пляжах в Корее с мусором строго.",
            audioUrlKorean = "asset:///audio/beach/90107.mp3"
        ),
        PhraseEntity(
            id = 90201,
            lessonId = 902,
            koreanText = "고기 구워도 돼요?",
            koreanRomanization = "gogi guwodo dwaeyo?",
            russianContext = "Уточняешь, разрешено ли жарить мясо на мангале — не на всех пляжах можно.",
            audioUrlKorean = "asset:///audio/beach/90201.mp3"
        ),
        PhraseEntity(
            id = 90202,
            lessonId = 902,
            koreanText = "불 좀 빌려 주세요.",
            koreanRomanization = "bul jom bilryeo juseyo",
            russianContext = "Просишь огонь или угли у соседей по пикнику — типичная ситуация на BBQ-площадке.",
            audioUrlKorean = "asset:///audio/beach/90202.mp3"
        ),
        PhraseEntity(
            id = 90203,
            lessonId = 902,
            koreanText = "돗자리 빌려 주세요.",
            koreanRomanization = "dotjari bilryeo juseyo",
            russianContext = "Арендуешь коврик для пикника — часто выдают на входе в зону отдыха.",
            audioUrlKorean = "asset:///audio/beach/90203.mp3"
        ),
        PhraseEntity(
            id = 90204,
            lessonId = 902,
            koreanText = "숯 더 있어요?",
            koreanRomanization = "sut deo isseoyo?",
            russianContext = "Спрашиваешь, есть ли ещё угли — когда жаришь мясо на мангале.",
            audioUrlKorean = "asset:///audio/beach/90204.mp3"
        ),
        PhraseEntity(
            id = 90205,
            lessonId = 902,
            koreanText = "모기 기름 있어요?",
            koreanRomanization = "mogi gireum isseoyo?",
            russianContext = "Ищешь репеллент от комаров — актуально вечером на природе.",
            audioUrlKorean = "asset:///audio/beach/90205.mp3"
        ),
        PhraseEntity(
            id = 90206,
            lessonId = 902,
            koreanText = "음식 싸 와도 돼요?",
            koreanRomanization = "eumsik ssa wado dwaeyo?",
            russianContext = "Уточняешь, можно ли приносить еду с собой — на некоторых площадках запрещено.",
            audioUrlKorean = "asset:///audio/beach/90206.mp3"
        )
    )

    // ── Тема 10: Парк ─────────────────────────────────────────────────────────

    private val parkLessons = listOf(
        LessonEntity(
            id = 1001,
            topicId = 10,
            orderInTopic = 1,
            title = "Парк: вход и ориентирование",
            introAudioUrl = "asset:///audio/park/intro_lesson1.mp3",
            durationTargetMin = 15
        ),
        LessonEntity(
            id = 1002,
            topicId = 10,
            orderInTopic = 2,
            title = "Парк: прогулка и правила",
            introAudioUrl = "asset:///audio/park/intro_lesson2.mp3",
            durationTargetMin = 15
        )
    )

    private val parkPhrases = listOf(
        PhraseEntity(
            id = 100101,
            lessonId = 1001,
            koreanText = "입장료 얼마예요?",
            koreanRomanization = "ipjangnyo eolmayeyo?",
            russianContext = "Спрашиваешь цену входа — в платных парках и садах.",
            audioUrlKorean = "asset:///audio/park/100101.mp3"
        ),
        PhraseEntity(
            id = 100102,
            lessonId = 1001,
            koreanText = "지도 받을 수 있어요?",
            koreanRomanization = "jido badeul su isseoyo?",
            russianContext = "Просишь карту парка у стойки информации.",
            audioUrlKorean = "asset:///audio/park/100102.mp3"
        ),
        PhraseEntity(
            id = 100103,
            lessonId = 1001,
            koreanText = "화장실 어디예요?",
            koreanRomanization = "hwajangsil eodiyeyo?",
            russianContext = "Ищешь туалет после входа — универсальная фраза.",
            audioUrlKorean = "asset:///audio/park/100103.mp3"
        ),
        PhraseEntity(
            id = 100104,
            lessonId = 1001,
            koreanText = "벤치 어디예요?",
            koreanRomanization = "benchi eodiyeyo?",
            russianContext = "Ищешь скамейку, чтобы отдохнуть на прогулке.",
            audioUrlKorean = "asset:///audio/park/100104.mp3"
        ),
        PhraseEntity(
            id = 100105,
            lessonId = 1001,
            koreanText = "자전거 빌릴 수 있어요?",
            koreanRomanization = "jajeongeo bilril su isseoyo?",
            russianContext = "Спрашиваешь про прокат велосипедов — в больших парках бывает.",
            audioUrlKorean = "asset:///audio/park/100105.mp3"
        ),
        PhraseEntity(
            id = 100106,
            lessonId = 1001,
            koreanText = "강아지 데려와도 돼요?",
            koreanRomanization = "gangaji deryeowado dwaeyo?",
            russianContext = "Уточняешь, можно ли с собакой — правила разные в разных парках.",
            audioUrlKorean = "asset:///audio/park/100106.mp3"
        ),
        PhraseEntity(
            id = 100107,
            lessonId = 1001,
            koreanText = "사진 찍어도 돼요?",
            koreanRomanization = "sajin jjigeodo dwaeyo?",
            russianContext = "Спрашиваешь, можно ли фотографировать — в некоторых зонах запрещено.",
            audioUrlKorean = "asset:///audio/park/100107.mp3"
        ),
        PhraseEntity(
            id = 100201,
            lessonId = 1002,
            koreanText = "산책로가 어디예요?",
            koreanRomanization = "sanchaekroga eodiyeyo?",
            russianContext = "Ищешь пешеходную тропу — на табло или у работника парка.",
            audioUrlKorean = "asset:///audio/park/100201.mp3"
        ),
        PhraseEntity(
            id = 100202,
            lessonId = 1002,
            koreanText = "전망대 어디예요?",
            koreanRomanization = "jeonmangdae eodiyeyo?",
            russianContext = "Ищешь смотровую площадку — популярная цель в горах и больших парках.",
            audioUrlKorean = "asset:///audio/park/100202.mp3"
        ),
        PhraseEntity(
            id = 100203,
            lessonId = 1002,
            koreanText = "여기서 쉬어도 돼요?",
            koreanRomanization = "yeogiseo swieodo dwaeyo?",
            russianContext = "Уточняешь, можно ли присесть на газоне или в зоне отдыха.",
            audioUrlKorean = "asset:///audio/park/100203.mp3"
        ),
        PhraseEntity(
            id = 100204,
            lessonId = 1002,
            koreanText = "길을 잃었어요.",
            koreanRomanization = "gireul ireosseoyo",
            russianContext = "Говоришь, что заблудился — просишь помощи у прохожего или персонала.",
            audioUrlKorean = "asset:///audio/park/100204.mp3"
        ),
        PhraseEntity(
            id = 100205,
            lessonId = 1002,
            koreanText = "출구 어디예요?",
            koreanRomanization = "chulgu eodiyeyo?",
            russianContext = "Ищешь выход — когда уже пора уходить.",
            audioUrlKorean = "asset:///audio/park/100205.mp3"
        ),
        PhraseEntity(
            id = 100206,
            lessonId = 1002,
            koreanText = "음료 자판기 어디예요?",
            koreanRomanization = "eumnyo jatanggi eodiyeyo?",
            russianContext = "Ищешь автомат с напитками — жаркий день на прогулке.",
            audioUrlKorean = "asset:///audio/park/100206.mp3"
        )
    )

    // ── Тема 11: Аквапарк ─────────────────────────────────────────────────────

    private val aquaparkLessons = listOf(
        LessonEntity(
            id = 1101,
            topicId = 11,
            orderInTopic = 1,
            title = "Аквапарк: вход и оборудование",
            introAudioUrl = "asset:///audio/aquapark/intro_lesson1.mp3",
            durationTargetMin = 15
        ),
        LessonEntity(
            id = 1102,
            topicId = 11,
            orderInTopic = 2,
            title = "Аквапарк: горки и бассейны",
            introAudioUrl = "asset:///audio/aquapark/intro_lesson2.mp3",
            durationTargetMin = 15
        )
    )

    private val aquaparkPhrases = listOf(
        PhraseEntity(
            id = 110101,
            lessonId = 1101,
            koreanText = "입장권 두 장 주세요.",
            koreanRomanization = "ipjanggwon du jang juseyo",
            russianContext = "Покупаешь два билета на вход — подставь нужное число.",
            audioUrlKorean = "asset:///audio/aquapark/110101.mp3"
        ),
        PhraseEntity(
            id = 110102,
            lessonId = 1101,
            koreanText = "사물함 있어요?",
            koreanRomanization = "samullham isseoyo?",
            russianContext = "Спрашиваешь про локер для вещей и одежды.",
            audioUrlKorean = "asset:///audio/aquapark/110102.mp3"
        ),
        PhraseEntity(
            id = 110103,
            lessonId = 1101,
            koreanText = "수영복 팔아요?",
            koreanRomanization = "suyeongbok parayo?",
            russianContext = "Ищешь, где купить плавки или купальник — если забыли с собой.",
            audioUrlKorean = "asset:///audio/aquapark/110103.mp3"
        ),
        PhraseEntity(
            id = 110104,
            lessonId = 1101,
            koreanText = "튜브 빌려 주세요.",
            koreanRomanization = "tyubeu bilryeo juseyo",
            russianContext = "Арендуешь надувной круг — для бассейна или ленивой реки.",
            audioUrlKorean = "asset:///audio/aquapark/110104.mp3"
        ),
        PhraseEntity(
            id = 110105,
            lessonId = 1101,
            koreanText = "수건 빌려 주세요.",
            koreanRomanization = "sugeon bilryeo juseyo",
            russianContext = "Просишь полотенце — часто платно или под залог.",
            audioUrlKorean = "asset:///audio/aquapark/110105.mp3"
        ),
        PhraseEntity(
            id = 110106,
            lessonId = 1101,
            koreanText = "탈의실 어디예요?",
            koreanRomanization = "taluishil eodiyeyo?",
            russianContext = "Ищешь раздевалку перед бассейном.",
            audioUrlKorean = "asset:///audio/aquapark/110106.mp3"
        ),
        PhraseEntity(
            id = 110107,
            lessonId = 1101,
            koreanText = "구명조끼 있어요?",
            koreanRomanization = "gumyeongjokki isseoyo?",
            russianContext = "Спрашиваешь про спасательный жилет — для детей или слабых пловцов.",
            audioUrlKorean = "asset:///audio/aquapark/110107.mp3"
        ),
        PhraseEntity(
            id = 110201,
            lessonId = 1102,
            koreanText = "워터슬라이드 어디예요?",
            koreanRomanization = "woteoseullideu eodiyeyo?",
            russianContext = "Ищешь горки — главная цель в аквапарке.",
            audioUrlKorean = "asset:///audio/aquapark/110201.mp3"
        ),
        PhraseEntity(
            id = 110202,
            lessonId = 1102,
            koreanText = "키 제한 있어요?",
            koreanRomanization = "ki jehan isseoyo?",
            russianContext = "Уточняешь ростовое ограничение на аттракцион — важно для детей.",
            audioUrlKorean = "asset:///audio/aquapark/110202.mp3"
        ),
        PhraseEntity(
            id = 110203,
            lessonId = 1102,
            koreanText = "온수풀이 어디예요?",
            koreanRomanization = "onsupuri eodiyeyo?",
            russianContext = "Ищешь тёплый бассейн — актуально в прохладную погоду.",
            audioUrlKorean = "asset:///audio/aquapark/110203.mp3"
        ),
        PhraseEntity(
            id = 110204,
            lessonId = 1102,
            koreanText = "줄이 얼마나 기다려요?",
            koreanRomanization = "juri eolmana gidaryeoyo?",
            russianContext = "Спрашиваешь, сколько ждать в очереди на горку.",
            audioUrlKorean = "asset:///audio/aquapark/110204.mp3"
        ),
        PhraseEntity(
            id = 110205,
            lessonId = 1102,
            koreanText = "샤워기 어디예요?",
            koreanRomanization = "syawogi eodiyeyo?",
            russianContext = "Ищешь душ перед или после бассейна.",
            audioUrlKorean = "asset:///audio/aquapark/110205.mp3"
        ),
        PhraseEntity(
            id = 110206,
            lessonId = 1102,
            koreanText = "여기서 기다리면 돼요?",
            koreanRomanization = "yeogiseo gidarimyeon dwaeyo?",
            russianContext = "Уточняешь, правильно ли стоишь в очереди на аттракцион.",
            audioUrlKorean = "asset:///audio/aquapark/110206.mp3"
        )
    )

    // ── Тема 12: Миграционная служба ─────────────────────────────────────────

    private val immigrationLessons = listOf(
        LessonEntity(
            id = 1201,
            topicId = 12,
            orderInTopic = 1,
            title = "Миграционная служба: визит и документы",
            introAudioUrl = "asset:///audio/immigration/intro_lesson1.mp3",
            durationTargetMin = 15
        ),
        LessonEntity(
            id = 1202,
            topicId = 12,
            orderInTopic = 2,
            title = "Миграционная служба: продление и изменения",
            introAudioUrl = "asset:///audio/immigration/intro_lesson2.mp3",
            durationTargetMin = 15
        )
    )

    private val immigrationPhrases = listOf(
        PhraseEntity(
            id = 120101,
            lessonId = 1201,
            koreanText = "출입국관리사무소 어디예요?",
            koreanRomanization = "churipgukgwallisamuso eodiyeyo?",
            russianContext = "Спрашиваешь, где здание миграционной службы — когда приехал впервые.",
            audioUrlKorean = "asset:///audio/immigration/120101.mp3"
        ),
        PhraseEntity(
            id = 120102,
            lessonId = 1201,
            koreanText = "외국인등록증 발급받으러 왔어요.",
            koreanRomanization = "oegugindeungnokjeung balgeupbadeureo wasseoyo",
            russianContext = "Говоришь на стойке, что пришёл оформить карту иностранца (ARC).",
            audioUrlKorean = "asset:///audio/immigration/120102.mp3"
        ),
        PhraseEntity(
            id = 120103,
            lessonId = 1201,
            koreanText = "예약했어요.",
            koreanRomanization = "yeyakhaesseoyo",
            russianContext = "Сообщаешь, что записаны онлайн — многие отделения работают по записи.",
            audioUrlKorean = "asset:///audio/immigration/120103.mp3"
        ),
        PhraseEntity(
            id = 120104,
            lessonId = 1201,
            koreanText = "서류 제출할게요.",
            koreanRomanization = "seoryu jechulhalgeyo",
            russianContext = "Передаёшь пакет документов сотруднику на приёме.",
            audioUrlKorean = "asset:///audio/immigration/120104.mp3"
        ),
        PhraseEntity(
            id = 120105,
            lessonId = 1201,
            koreanText = "사진 필요해요?",
            koreanRomanization = "sajin pillyohaeyo?",
            russianContext = "Уточняешь, нужна ли фотография для заявления — частый вопрос.",
            audioUrlKorean = "asset:///audio/immigration/120105.mp3"
        ),
        PhraseEntity(
            id = 120106,
            lessonId = 1201,
            koreanText = "얼마나 걸려요?",
            koreanRomanization = "eolmana geollyeoyo?",
            russianContext = "Спрашиваешь, сколько займёт оформление или сколько ждать очередь.",
            audioUrlKorean = "asset:///audio/immigration/120106.mp3"
        ),
        PhraseEntity(
            id = 120107,
            lessonId = 1201,
            koreanText = "통역 서비스 있어요?",
            koreanRomanization = "tongyeok seobiseu isseoyo?",
            russianContext = "Спрашиваешь, есть ли переводчик — когда корейский пока слабый.",
            audioUrlKorean = "asset:///audio/immigration/120107.mp3"
        ),
        PhraseEntity(
            id = 120201,
            lessonId = 1202,
            koreanText = "체류 기간 연장하러 왔어요.",
            koreanRomanization = "cheryu gigan yeonjanghareo wasseoyo",
            russianContext = "Говоришь, что пришёл продлить срок пребывания визы или ARC.",
            audioUrlKorean = "asset:///audio/immigration/120201.mp3"
        ),
        PhraseEntity(
            id = 120202,
            lessonId = 1202,
            koreanText = "주소가 바뀌었어요.",
            koreanRomanization = "jusoga bakkwieosseoyo",
            russianContext = "Сообщаешь о смене адреса — нужно обновить регистрацию.",
            audioUrlKorean = "asset:///audio/immigration/120202.mp3"
        ),
        PhraseEntity(
            id = 120203,
            lessonId = 1202,
            koreanText = "여권을 잃어버렸어요.",
            koreanRomanization = "yeogwoneul ireobeoryeosseoyo",
            russianContext = "Сообщаешь о потере паспорта — срочная ситуация.",
            audioUrlKorean = "asset:///audio/immigration/120203.mp3"
        ),
        PhraseEntity(
            id = 120204,
            lessonId = 1202,
            koreanText = "신청서 어디서 받아요?",
            koreanRomanization = "sincheongseo eodiseo badayo?",
            russianContext = "Спрашиваешь, где взять бланк заявления.",
            audioUrlKorean = "asset:///audio/immigration/120204.mp3"
        ),
        PhraseEntity(
            id = 120205,
            lessonId = 1202,
            koreanText = "결과 언제 나와요?",
            koreanRomanization = "gyeolgwa eonje nawayo?",
            russianContext = "Уточняешь, когда будет готов результат рассмотрения.",
            audioUrlKorean = "asset:///audio/immigration/120205.mp3"
        ),
        PhraseEntity(
            id = 120206,
            lessonId = 1202,
            koreanText = "재입국 허가 필요해요?",
            koreanRomanization = "jaeipguk heoga pillyohaeyo?",
            russianContext = "Спрашиваешь, нужно ли разрешение на повторный въезд — при длинных поездках.",
            audioUrlKorean = "asset:///audio/immigration/120206.mp3"
        )
    )

    // ── Тема 13: Знакомство и вежливость ─────────────────────────────────────

    private val greetingsLessons = listOf(
        LessonEntity(
            id = 1301,
            topicId = 13,
            orderInTopic = 1,
            title = "Приветствие и прощание",
            introAudioUrl = "asset:///audio/greetings/intro_lesson1.mp3",
            durationTargetMin = 15
        ),
        LessonEntity(
            id = 1302,
            topicId = 13,
            orderInTopic = 2,
            title = "Вежливость и знакомство",
            introAudioUrl = "asset:///audio/greetings/intro_lesson2.mp3",
            durationTargetMin = 15
        )
    )

    private val greetingsPhrases = listOf(
        PhraseEntity(
            id = 130101,
            lessonId = 1301,
            koreanText = "안녕하세요.",
            koreanRomanization = "annyeonghaseyo",
            russianContext = "Здороваешься с незнакомым человеком — универсальное приветствие.",
            russianLiteralMeaning = "«Будьте в покое» / приветствие на день",
            audioUrlKorean = "asset:///audio/greetings/130101.mp3"
        ),
        PhraseEntity(
            id = 130102,
            lessonId = 1301,
            koreanText = "안녕히 가세요.",
            koreanRomanization = "annyeonghi gaseyo",
            russianContext = "Прощаешься, когда собеседник уходит — ты остаёшься.",
            russianLiteralMeaning = "«Идите с миром» — говорят уходящему",
            audioUrlKorean = "asset:///audio/greetings/130102.mp3"
        ),
        PhraseEntity(
            id = 130103,
            lessonId = 1301,
            koreanText = "안녕히 계세요.",
            koreanRomanization = "annyeonghi gyeseyo",
            russianContext = "Прощаешься, когда уходишь сам — собеседник остаётся.",
            russianLiteralMeaning = "«Оставайтесь с миром» — говорят тому, кто остаётся",
            audioUrlKorean = "asset:///audio/greetings/130103.mp3"
        ),
        PhraseEntity(
            id = 130104,
            lessonId = 1301,
            koreanText = "감사해요.",
            koreanRomanization = "gamsahaeyo",
            russianContext = "Благодаришь за помощь или услугу — повседневный уровень вежливости.",
            audioUrlKorean = "asset:///audio/greetings/130104.mp3"
        ),
        PhraseEntity(
            id = 130105,
            lessonId = 1301,
            koreanText = "죄송해요.",
            koreanRomanization = "joesonghaeyo",
            russianContext = "Извиняешься — за мелкую ошибку или когда мешаешь.",
            audioUrlKorean = "asset:///audio/greetings/130105.mp3"
        ),
        PhraseEntity(
            id = 130106,
            lessonId = 1301,
            koreanText = "괜찮아요.",
            koreanRomanization = "gwaenchanayo",
            russianContext = "Говоришь «ничего страшного» — в ответ на извинение или предложение помощи.",
            audioUrlKorean = "asset:///audio/greetings/130106.mp3"
        ),
        PhraseEntity(
            id = 130107,
            lessonId = 1301,
            koreanText = "잠시만요.",
            koreanRomanization = "jamsimanyo",
            russianContext = "Просишь подождать секунду — когда отвлекаешься или ищешь что-то.",
            audioUrlKorean = "asset:///audio/greetings/130107.mp3"
        ),
        PhraseEntity(
            id = 130201,
            lessonId = 1302,
            koreanText = "만나서 반가워요.",
            koreanRomanization = "mannaseo bangawoyo",
            russianContext = "Говоришь при первой встрече — знакомство с новым человеком.",
            audioUrlKorean = "asset:///audio/greetings/130201.mp3"
        ),
        PhraseEntity(
            id = 130202,
            lessonId = 1302,
            koreanText = "이름이 뭐예요?",
            koreanRomanization = "ireumi mwoyeyo?",
            russianContext = "Спрашиваешь имя собеседника при знакомстве.",
            audioUrlKorean = "asset:///audio/greetings/130202.mp3"
        ),
        PhraseEntity(
            id = 130203,
            lessonId = 1302,
            koreanText = "저는 마이클이에요.",
            koreanRomanization = "jeoneun maikeurieyo",
            russianContext = "Представляешься — подставь своё имя вместо «Майкл».",
            audioUrlKorean = "asset:///audio/greetings/130203.mp3"
        ),
        PhraseEntity(
            id = 130204,
            lessonId = 1302,
            koreanText = "한국어를 배우고 있어요.",
            koreanRomanization = "hangugeoreul baeugo isseoyo",
            russianContext = "Объясняешь, что учишь корейский — снимает напряжение при общении.",
            audioUrlKorean = "asset:///audio/greetings/130204.mp3"
        ),
        PhraseEntity(
            id = 130205,
            lessonId = 1302,
            koreanText = "천천히 말해 주세요.",
            koreanRomanization = "cheoncheonhi malhae juseyo",
            russianContext = "Просишь говорить медленнее — когда не успеваешь понять.",
            audioUrlKorean = "asset:///audio/greetings/130205.mp3"
        ),
        PhraseEntity(
            id = 130206,
            lessonId = 1302,
            koreanText = "다시 한번 말해 주세요.",
            koreanRomanization = "dasi hanbeon malhae juseyo",
            russianContext = "Просишь повторить — когда не расслышал или не понял.",
            audioUrlKorean = "asset:///audio/greetings/130206.mp3"
        )
    )
}
