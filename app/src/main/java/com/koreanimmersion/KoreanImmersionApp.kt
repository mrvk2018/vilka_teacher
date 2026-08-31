package com.koreanimmersion

import android.app.Application
import com.koreanimmersion.data.local.DatabaseSeeder
import com.koreanimmersion.data.repository.ContentRepository
import com.koreanimmersion.data.repository.ExamRepository
import com.koreanimmersion.data.repository.ProgressRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class KoreanImmersionApp : Application() {

    lateinit var database: com.koreanimmersion.data.local.AppDatabase
        private set

    lateinit var contentRepository: ContentRepository
        private set

    lateinit var progressRepository: ProgressRepository
        private set

    lateinit var examRepository: ExamRepository
        private set

    private val appScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = DatabaseSeeder.createDatabase(this)
        contentRepository = ContentRepository(database)
        progressRepository = ProgressRepository(database)
        examRepository = ExamRepository(database)
        appScope.launch {
            DatabaseSeeder.seedIfEmpty(database)
        }
    }

    companion object {
        lateinit var instance: KoreanImmersionApp
            private set
    }
}
