# Python-скрипты (смежные инструменты)

Каталог для офлайн-утилит и **FastAPI-сервера учителя (подэтап 3, OpenRouter / Qwen 2.5 free)**.

## LLM teacher server

```bash
cd tools/python
python -m venv .venv
.venv\Scripts\activate   # Windows
pip install -r requirements.txt
uvicorn server:app --host 0.0.0.0 --port 8000
```

- `POST /api/chat/v1/message` — тело как в `server.py` (`userId`, `userMessage`, `currentTopicKey`, `nativeLanguage`, `learnedPhrases`).
- Провайдер: [OpenRouter](https://openrouter.ai) (`qwen/qwen-2.5-7b-instruct:free` по умолчанию).
- Переменные окружения: **`OPENROUTER_API_KEY`** (обязательно), `OPENROUTER_MODEL`, `OPENROUTER_TIMEOUT_SEC`.
- Windows (PowerShell): `$env:OPENROUTER_API_KEY="sk-or-v1-..."`

Android по умолчанию: `http://10.0.2.2:8000` (эмулятор). На телефоне задайте LAN IP ПК в `LlmBackendConfig.baseUrl`.

Формат `learnedPhraseIdsJson` в Room совпадает с JSON-массивом целых ID, см. `LearnedPhrasesJson` в `:core:database`.
