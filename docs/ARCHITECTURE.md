# Архитектура KoreanImmersion

## Модули Gradle

| Модуль | Назначение |
|--------|------------|
| `:app` | Android-приложение: Compose UI, `AudioPlaybackService`, TTS, `AppDatabase` (контент + прогресс уроков) |
| `:core:database` | KMP-ready слой Room 2.7: сущности/DAO, общие для будущего iOS |

## План расширения

- **iOS**: `iosApp` + таргеты в `:core:database`, UI/SwiftUI и AVAudioEngine отдельно от Android.
- **Python**: каталог `tools/python/` — формат `learnedPhraseIdsJson` совместим с `LearnedPhrasesJson`.
- **Domain**: следующий шаг — вынести SRS, очередь воспроизведения и экзамен в `:core:domain` (pure Kotlin).

## Зависимости Room

Версии централизованы в `gradle/libs.versions.toml` (`room = 2.7.0`, `sqlite-bundled` для будущего iOS).
