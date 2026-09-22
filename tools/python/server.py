"""
FastAPI backend for KoreanImmersion LLM teacher (OpenRouter / Qwen 2.5).

Run: uvicorn server:app --host 0.0.0.0 --port 8000
"""

from __future__ import annotations

import os
from typing import Any, Literal

import httpx
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

OPENROUTER_BASE_URL = "https://openrouter.ai"
OPENROUTER_CHAT_URL = f"{OPENROUTER_BASE_URL}/api/v1/chat/completions"
OPENROUTER_MODEL = os.getenv(
    "OPENROUTER_MODEL", "qwen/qwen-2.5-7b-instruct:free"
)
OPENROUTER_TIMEOUT_SEC = float(os.getenv("OPENROUTER_TIMEOUT_SEC", "120"))
OPENROUTER_API_KEY = os.getenv("OPENROUTER_API_KEY", "")


def openrouter_headers() -> dict[str, str]:
    if not OPENROUTER_API_KEY.strip():
        raise HTTPException(
            status_code=503,
            detail="OPENROUTER_API_KEY is not set. Export it before starting the server.",
        )
    return {
        "Authorization": f"Bearer {OPENROUTER_API_KEY.strip()}",
        "HTTP-Referer": "https://localhost",
        "X-Title": "Korean Learning App",
        "Content-Type": "application/json",
    }

app = FastAPI(title="KoreanImmersion LLM Teacher", version="1.1.0")


class ChatMessageRequest(BaseModel):
    userId: str = Field(..., min_length=1)
    userMessage: str = Field(..., min_length=1)
    currentTopicKey: str = Field(..., min_length=1)
    nativeLanguage: Literal["ru", "en"]
    learnedPhrases: list[str] = Field(default_factory=list)


class ChatMessageResponse(BaseModel):
    assistantMessage: str
    model: str = OPENROUTER_MODEL


def generate_teacher_prompt(
    native_language: Literal["ru", "en"],
    current_topic_key: str,
    learned_phrases: list[str],
) -> str:
    lang_label = "русский" if native_language == "ru" else "English"
    phrases_block = (
        "\n".join(f"- {p}" for p in learned_phrases if p.strip())
        if learned_phrases
        else "- (пока нет заученных фраз)"
    )
    return (
        f"Ты — терпеливый учитель корейского языка для студента, "
        f"чей родной язык {lang_label}. "
        f"Вы общаетесь строго в рамках темы {current_topic_key}.\n"
        "Правила:\n"
        "1. Отвечай коротко (2–4 предложения), дружелюбно.\n"
        "2. Используй корейские фразы из списка студента, когда уместно; "
        "давай хангыль и краткий перевод на родной язык студента.\n"
        "3. Не уходи в другие темы; мягко возвращай к сценарию темы.\n"
        "4. Если студент ошибается — поправь и предложи повторить.\n"
        f"Заученные фразы студента:\n{phrases_block}\n"
    )


def extract_assistant_content(data: dict[str, Any]) -> str:
    choices = data.get("choices")
    if not choices or not isinstance(choices, list):
        raise HTTPException(status_code=502, detail="OpenRouter: missing choices in response")
    first = choices[0] if choices else {}
    message = first.get("message") or {}
    content = (message.get("content") or "").strip()
    if not content:
        raise HTTPException(status_code=502, detail="OpenRouter returned empty message content")
    return content


async def call_openrouter_chat(system_prompt: str, user_message: str) -> str:
    payload = {
        "model": OPENROUTER_MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_message},
        ],
    }
    try:
        async with httpx.AsyncClient(timeout=OPENROUTER_TIMEOUT_SEC) as client:
            response = await client.post(
                OPENROUTER_CHAT_URL,
                headers=openrouter_headers(),
                json=payload,
            )
    except httpx.TimeoutException as exc:
        raise HTTPException(
            status_code=504,
            detail="OpenRouter request timed out.",
        ) from exc
    except httpx.ConnectError as exc:
        raise HTTPException(
            status_code=503,
            detail=f"Cannot connect to OpenRouter at {OPENROUTER_BASE_URL}.",
        ) from exc
    except httpx.HTTPError as exc:
        raise HTTPException(
            status_code=502,
            detail=f"OpenRouter HTTP error: {exc}",
        ) from exc

    if response.status_code != 200:
        raise HTTPException(
            status_code=502,
            detail=f"OpenRouter returned {response.status_code}: {response.text[:500]}",
        )

    return extract_assistant_content(response.json())


@app.get("/health")
async def health() -> dict[str, str]:
    return {
        "status": "ok",
        "provider": "openrouter",
        "base_url": OPENROUTER_BASE_URL,
        "model": OPENROUTER_MODEL,
    }


@app.post("/api/chat/v1/message", response_model=ChatMessageResponse)
async def chat_message(body: ChatMessageRequest) -> ChatMessageResponse:
    system_prompt = generate_teacher_prompt(
        body.nativeLanguage,
        body.currentTopicKey,
        body.learnedPhrases,
    )
    assistant_text = await call_openrouter_chat(system_prompt, body.userMessage)
    return ChatMessageResponse(assistantMessage=assistant_text, model=OPENROUTER_MODEL)
