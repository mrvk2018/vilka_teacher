# KoreanImmersion

Android-приложение для изучения разговорного корейского через фонетическое погружение (Kotlin + Jetpack Compose).

## Архитектура

```
app/
├── data/local/          Room entities, DAOs, DatabaseSeeder
├── data/repository/     ContentRepository, ProgressRepository, ExamRepository
├── domain/
│   ├── playback/        Очередь сегментов урока, replay
│   ├── srs/             Интервалы 1→3→7→14→21→30 дней
│   ├── exam/            Триггер экзамена (2 условия), scoring MC
│   └── stt/             PhraseSttHelper (бинарная обратная связь)
├── service/             AudioPlaybackService (Foreground + Media3)
└── ui/                  Compose screens, navigation, ViewModels
```

## Ключевая логика

### Триггер экзамена (`ExamTriggerLogic`)
1. Урок полностью прослушан ≥1 раз (`timesCompletedFullPlaythrough`)
2. Пользователь нажал **Stop** (не просто завершился цикл в replay)

Диалог показывается только при разблокировке экрана, если оба условия выполнены.

### Foreground audio (`AudioPlaybackService`)
- ExoPlayer + MediaSession
- Уведомление с Play/Pause/Stop
- Replay loop без ограничения повторов
- Broadcast `CYCLE_COMPLETED` / `USER_STOP` для ViewModel

### SRS (`SrsScheduler`)
Упрощённый SM-2 на уровне фразы.

### Экзамен
- Multiple choice → оценка 1–5
- STT → только «услышано / нет» (не влияет на оценку)

## Тестовые данные

Одна тема **«Кафе»** с 6 фразами и 1 уроком. Аудио URL — заглушки `asset:///audio/cafe/...`.

## Сборка

```bash
./gradlew :app:assembleDebug
```

Открыть в Android Studio: `File → Open → KoreanImmersion`.

## Задел на будущее (не реализовано)

- `HangulStagePlaceholderEntity` — Этап 2 (Хангыль)
- Phoneme scoring
- Монетизация
