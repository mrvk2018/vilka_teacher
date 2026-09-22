# Python-скрипты (смежные инструменты)

Каталог для офлайн-утилит и **FastAPI-сервера учителя (подэтап 3, Ollama / Qwen 2.5)**.

## LLM teacher server

```bash
cd tools/python
python -m venv .venv
.venv\Scripts\activate   # Windows
pip install -r requirements.txt
ollama pull qwen2.5
uvicorn server:app --host 0.0.0.0 --port 8000
```

- `POST /api/chat/v1/message` — тело как в `server.py` (`userId`, `userMessage`, `currentTopicKey`, `nativeLanguage`, `learnedPhrases`).
- Переменные окружения: `OLLAMA_BASE_URL`, `OLLAMA_MODEL` (по умолчанию `qwen2.5`), `OLLAMA_TIMEOUT_SEC`.

Android по умолчанию: `http://10.0.2.2:8000` (эмулятор). На телефоне задайте LAN IP ПК в `LlmBackendConfig.baseUrl`.

Формат `learnedPhraseIdsJson` в Room совпадает с JSON-массивом целых ID, см. `LearnedPhrasesJson` в `:core:database`.
