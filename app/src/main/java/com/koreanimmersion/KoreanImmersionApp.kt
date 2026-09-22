package com.koreanimmersion

import android.app.Application
import com.koreanimmersion.data.local.DatabaseSeeder
import com.koreanimmersion.data.local.CourseContentSeeder
import com.koreanimmersion.data.repository.ContentRepository
import com.koreanimmersion.data.repository.CourseContentRepository
import com.koreanimmersion.data.repository.ExamRepository
import com.koreanimmersion.data.remote.KtorLlmChatApiService
import com.koreanimmersion.data.repository.LlmChatRepositoryImpl
import com.koreanimmersion.data.repository.ProgressRepository
import com.koreanimmersion.tts.LocaleTtsEngine
import com.koreanimmersion.tts.PhraseTtsCacheManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class KoreanImmersionApp : Application() {

    lateinit var database: com.koreanimmersion.data.local.AppDatabase
        private set

    lateinit var contentRepository: ContentRepository
        private set

    lateinit var courseContentRepository: CourseContentRepository
        private set

    lateinit var progressRepository: ProgressRepository
        private set

    lateinit var examRepository: ExamRepository
        private set

    lateinit var ttsEngine: LocaleTtsEngine
        private set

    lateinit var phraseTtsCacheManager: PhraseTtsCacheManager
        private set

    lateinit var llmChatRepository: LlmChatRepositoryImpl
        private set

    private val appScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = DatabaseSeeder.createDatabase(this)
        ttsEngine = LocaleTtsEngine(this)
        phraseTtsCacheManager = PhraseTtsCacheManager(this, database.phraseDao(), ttsEngine)
        contentRepository = ContentRepository(database, phraseTtsCacheManager)
        courseContentRepository = CourseContentRepository(database)
        progressRepository = ProgressRepository(database)
        examRepository = ExamRepository(database, phraseTtsCacheManager)
        llmChatRepository = LlmChatRepositoryImpl(KtorLlmChatApiService())
        appScope.launch {
            DatabaseSeeder.seedIfEmpty(database)
            CourseContentSeeder.seedIfEmpty(database)
            phraseTtsCacheManager.preloadAllPhrases()
        }
    }

    override fun onTerminate() {
        ttsEngine.shutdown()
        super.onTerminate()
    }

    companion object {
        lateinit var instance: KoreanImmersionApp
            private set
    }
}
