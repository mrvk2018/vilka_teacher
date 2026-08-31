package com.koreanimmersion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.koreanimmersion.ui.viewmodel.ProgressViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProgressScreen(viewModel: ProgressViewModel) {
    val scores by viewModel.lessonScores.collectAsState()

    val examined = scores.filter { it.attemptCount > 0 }
    val avgScore = if (examined.isNotEmpty()) {
        examined.mapNotNull { it.latestScore }.average()
    } else 0.0

    val totalLessons = scores.size
    val examinedCount = examined.size

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Мой прогресс", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Этап 1 — разговорный корейский", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Средний балл: ${if (examined.isEmpty()) "—" else "%.1f".format(avgScore)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Пройдено экзаменов: $examinedCount из $totalLessons уроков",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Уроки", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(scores, key = { it.lessonId }) { item ->
                val recommendRepeat = item.latestScore != null && item.latestScore < 4
                LessonProgressCard(item = item, recommendRepeat = recommendRepeat)
            }
        }
    }
}

@Composable
private fun LessonProgressCard(
    item: com.koreanimmersion.data.local.dao.LessonWithLatestScore,
    recommendRepeat: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (recommendRepeat) {
            CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall)
            Text(
                when {
                    item.latestScore != null -> "Оценка: ${item.latestScore}/5 (${item.attemptCount} попыток)"
                    item.timesCompleted > 0 -> "Прослушан ${item.timesCompleted}×, экзамен не сдан"
                    else -> "Не начат"
                },
                style = MaterialTheme.typography.bodySmall
            )
            if (recommendRepeat) {
                Text(
                    "Рекомендуется повторить",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFE65100)
                )
            }
        }
    }
}

@Composable
fun ExamHistoryList(
    lessonId: Long,
    viewModel: ProgressViewModel
) {
    val history = remember(lessonId) { viewModel.observeExamHistory(lessonId) }
    val attempts by history.collectAsState()
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))

    LazyColumn {
        items(attempts, key = { it.id }) { attempt ->
            Text(
                "Попытка ${attempt.attemptNumber}: ${attempt.score}/5 — ${dateFormat.format(Date(attempt.date))}",
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
