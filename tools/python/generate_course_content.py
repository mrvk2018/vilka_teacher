#!/usr/bin/env python3
"""
Regenerates app/src/main/java/.../CourseContentSeeder.kt from legacy phrases + extensions.

Run from repo root:
  python tools/python/generate_course_content.py
"""

from __future__ import annotations

import re
from pathlib import Path

from course_content_phrases import EXTENSIONS_BY_TOPIC_ID, NEW_TOPIC_PHRASES

REPO_ROOT = Path(__file__).resolve().parents[2]
SEEDER_PATH = (
    REPO_ROOT
    / "app/src/main/java/com/koreanimmersion/data/local/CourseContentSeeder.kt"
)

LEGACY_TOPICS = [
    (1, "shop", "Магазин", "Shop", 1),
    (2, "cafe", "Кафе", "Cafe", 2),
    (3, "metro", "Метро", "Subway", 3),
    (4, "bus", "Автобус", "Bus", 4),
    (5, "taxi", "Такси", "Taxi", 5),
    (6, "airport", "Аэропорт", "Airport", 6),
    (7, "train", "Ж/д вокзал", "Train station", 7),
    (8, "bus_station", "Автовокзал", "Bus terminal", 8),
    (9, "beach", "Пляж", "Beach", 9),
    (10, "park", "Парк", "Park", 10),
    (11, "aquapark", "Аквапарк", "Water park", 11),
    (12, "immigration", "Миграционная служба", "Immigration office", 12),
    (13, "greetings", "Знакомство / вежливость", "Greetings", 0),
    (14, "salon", "Парикмахерская", "Hair salon", 13),
]

NEW_TOPICS = [
    (15, "police_street", "Полиция: На улице", "Police: On the street", 14),
    (16, "police_station", "Полиция: В участке", "Police: At the station", 15),
    (17, "housing_agency", "Жилье: В агентстве", "Housing: Real estate agency", 16),
    (18, "housing_owner", "Жилье: С хозяином", "Housing: With landlord", 17),
    (19, "city_hall", "Мэрия / Администрация", "City hall / District office", 18),
    (20, "tax_office", "Налоговая инспекция", "Tax office", 19),
    (21, "telecom_shop", "Салон связи / SIM-карта", "Telecom shop / SIM card", 20),
    (22, "bank", "Банк / Открытие счета", "Bank / Open account", 21),
]

TOPIC_COMMENT = {
    1: "Магазин",
    2: "Кафе",
    3: "Метро",
    4: "Автобус",
    5: "Такси",
    6: "Аэропорт",
    7: "Ж/д",
    8: "Автовокзал",
    9: "Пляж",
    10: "Парк",
    11: "Аквапарк",
    12: "Миграционка",
    13: "Знакомство",
    14: "Парикмахерская",
    15: "Полиция: улица",
    16: "Полиция: участок",
    17: "Жильё: агентство",
    18: "Жильё: хозяин",
    19: "Мэрия",
    20: "Налоговая",
    21: "Салон связи",
    22: "Банк",
}

PHRASE_LINE_RE = re.compile(
    r'p\(\s*(\d+)L?\s*,\s*(\d+)L?\s*,\s*"((?:[^"\\]|\\.)*)"\s*,\s*'
    r'"((?:[^"\\]|\\.)*)"\s*,\s*"((?:[^"\\]|\\.)*)"\s*,\s*(\d+)\s*\)'
)


def kotlin_escape(text: str) -> str:
    return text.replace("\\", "\\\\").replace('"', '\\"')


def parse_legacy_phrases(seeder_text: str) -> dict[int, list[tuple[str, str, str, int]]]:
    by_topic: dict[int, list[tuple[str, str, str, int, int]]] = {}
    for match in PHRASE_LINE_RE.finditer(seeder_text):
        topic_id = int(match.group(2))
        ko = match.group(3)
        ru = match.group(4)
        en = match.group(5)
        order = int(match.group(6))
        phrase_id = int(match.group(1))
        by_topic.setdefault(topic_id, []).append((ko, ru, en, order, phrase_id))

    result: dict[int, list[tuple[str, str, str, int]]] = {}
    for topic_id, items in by_topic.items():
        if topic_id > 14:
            continue
        items.sort(key=lambda x: x[3])
        legacy = [(ko, ru, en, order) for ko, ru, en, order, _pid in items if order <= 5]
        if len(legacy) != 5:
            raise ValueError(
                f"Topic {topic_id}: expected 5 legacy phrases (order 1-5), got {len(legacy)}"
            )
        result[topic_id] = legacy
    if len(result) != 14:
        raise ValueError(f"Expected 14 legacy topics in seeder, found {len(result)}")
    return result


def phrase_id(topic_id: int, order: int) -> int:
    return topic_id * 1000 + order


def format_p_line(topic_id: int, order: int, ko: str, ru: str, en: str) -> str:
    pid = phrase_id(topic_id, order)
    return (
        f'        p({pid}, {topic_id}, "{kotlin_escape(ko)}", '
        f'"{kotlin_escape(ru)}", "{kotlin_escape(en)}", {order}),'
    )


def build_phrase_blocks(
    legacy: dict[int, list[tuple[str, str, str, int]]],
) -> list[str]:
    lines: list[str] = []
    for topic_id in range(1, 23):
        comment = TOPIC_COMMENT.get(topic_id, f"Topic {topic_id}")
        lines.append(f"        // {topic_id} — {comment}")
        order = 1
        if topic_id <= 14:
            for ko, ru, en, legacy_order in legacy[topic_id]:
                lines.append(format_p_line(topic_id, legacy_order, ko, ru, en))
            order = 6
            for ko, ru, en in EXTENSIONS_BY_TOPIC_ID[topic_id]:
                lines.append(format_p_line(topic_id, order, ko, ru, en))
                order += 1
            if order != 16:
                raise ValueError(f"Topic {topic_id}: expected 15 phrases, got {order - 1}")
        else:
            phrases = NEW_TOPIC_PHRASES[topic_id]
            if len(phrases) != 15:
                raise ValueError(f"New topic {topic_id}: need 15 phrases, got {len(phrases)}")
            for ko, ru, en in phrases:
                lines.append(format_p_line(topic_id, order, ko, ru, en))
                order += 1
        lines.append("")
    if lines and lines[-1] == "":
        lines.pop()
    return lines


def format_topics() -> list[str]:
    lines = ["    private val courseTopics = listOf("]
    for tid, key, ru, en, order in LEGACY_TOPICS + NEW_TOPICS:
        lines.append(
            f'        TopicEntity({tid}, "{key}", "{kotlin_escape(ru)}", '
            f'"{kotlin_escape(en)}", {order}),'
        )
    lines.append("    )")
    return lines


def generate_seeder(legacy: dict[int, list[tuple[str, str, str, int]]]) -> str:
    phrase_lines = build_phrase_blocks(legacy)
    topic_lines = format_topics()
    return f"""package com.koreanimmersion.data.local

import com.koreanimmersion.core.database.entity.PhraseEntity
import com.koreanimmersion.core.database.entity.TopicEntity

/**
 * Контент курса (подэтапы 1–3): фразы с TTS по `korean_text`.
 * Сгенерировано tools/python/generate_course_content.py — не редактировать вручную.
 */
object CourseContentSeeder {{

    /** Полная синхронизация контента (REPLACE) — для MVP и обновлений на устройстве. */
    suspend fun seedIfEmpty(db: AppDatabase) {{
        syncAll(db)
    }}

    suspend fun syncAll(db: AppDatabase) {{
        db.courseTopicDao().insertAll(courseTopics)
        db.coursePhraseDao().insertAll(coursePhrases)
    }}

{chr(10).join(topic_lines)}

    private val coursePhrases = listOf(
{chr(10).join(phrase_lines)}
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
}}
"""


def main() -> None:
    if not SEEDER_PATH.is_file():
        raise SystemExit(f"Seeder not found: {SEEDER_PATH}")
    legacy = parse_legacy_phrases(SEEDER_PATH.read_text(encoding="utf-8"))
    kotlin = generate_seeder(legacy)
    SEEDER_PATH.write_text(kotlin, encoding="utf-8", newline="\n")
    topic_count = len(LEGACY_TOPICS) + len(NEW_TOPICS)
    phrase_count = 14 * 15 + 8 * 15
    print(f"Updated {SEEDER_PATH.relative_to(REPO_ROOT)}")
    print(f"Topics: {topic_count}, phrases: {phrase_count}")


if __name__ == "__main__":
    main()
