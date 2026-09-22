"""
FastAPI backend for KoreanImmersion LLM teacher (Ollama / Qwen 2.5).

Run: uvicorn server:app --host 0.0.0.0 --port 8000
"""

from __future__ import annotations

import os
from typing import Literal

import httpx
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

OLLAMA_BASE_URL = os.getenv("OLLAMA_BASE_URL", "http://localhost:11434")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "qwen2.5")
OLLAMA_CHAT_URL = f"{OLLAMA_BASE_URL.rstrip('/')}/api/chat"
OLLAMA_TIMEOUT_SEC = float(os.getenv("OLLAMA_TIMEOUT_SEC", "120"))

app = FastAPI(title="KoreanImmersion LLM Teacher", version="1.0.0")


class ChatMessageRequest(BaseModel):
    userId: str = Field(..., min_length=1)
    userMessage: str = Field(..., min_length=1)
    currentTopicKey: str = Field(..., min_length=1)
    nativeLanguage: Literal["ru", "en"]
    learnedPhrases: list[str] = Field(default_factory=list)


class ChatMessageResponse(BaseModel):
    assistantMessage: str
    model: str = OLLAMA_MODEL


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


async def call_ollama_chat(system_prompt: str, user_message: str) -> str:
    payload = {
        "model": OLLAMA_MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_message},
        ],
        "stream": False,
    }
    try:
        async with httpx.AsyncClient(timeout=OLLAMA_TIMEOUT_SEC) as client:
            response = await client.post(OLLAMA_CHAT_URL, json=payload)
    except httpx.TimeoutException as exc:
        raise HTTPException(
            status_code=504,
            detail="Ollama request timed out. Check that the model is loaded.",
        ) from exc
    except httpx.ConnectError as exc:
        raise HTTPException(
            status_code=503,
            detail=f"Cannot connect to Ollama at {OLLAMA_BASE_URL}. Start Ollama locally.",
        ) from exc
    except httpx.HTTPError as exc:
        raise HTTPException(
            status_code=502,
            detail=f"Ollama HTTP error: {exc}",
        ) from exc

    if response.status_code != 200:
        raise HTTPException(
            status_code=502,
            detail=f"Ollama returned {response.status_code}: {response.text[:500]}",
        )

    data = response.json()
    message = data.get("message") or {}
    content = (message.get("content") or "").strip()
    if not content:
        raise HTTPException(status_code=502, detail="Ollama returned empty response")
    return content


@app.get("/health")
async def health() -> dict[str, str]:
    return {"status": "ok", "ollama": OLLAMA_BASE_URL, "model": OLLAMA_MODEL}


@app.post("/api/chat/v1/message", response_model=ChatMessageResponse)
async def chat_message(body: ChatMessageRequest) -> ChatMessageResponse:
    system_prompt = generate_teacher_prompt(
        body.nativeLanguage,
        body.currentTopicKey,
        body.learnedPhrases,
    )
    assistant_text = await call_ollama_chat(system_prompt, body.userMessage)
    return ChatMessageResponse(assistantMessage=assistant_text)
