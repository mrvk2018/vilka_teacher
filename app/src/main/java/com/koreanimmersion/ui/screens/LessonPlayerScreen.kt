package com.koreanimmersion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.koreanimmersion.R
import com.koreanimmersion.ui.viewmodel.LessonPlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonPlayerScreen(
    lessonId: Long,
    viewModel: LessonPlayerViewModel,
    onBack: () -> Unit,
    onNavigateToExam: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lessonId) { viewModel.loadLesson(lessonId) }

    LaunchedEffect(state.navigateToExamLessonId) {
        state.navigateToExamLessonId?.let {
            onNavigateToExam(it)
            viewModel.clearNavigateToExam()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> viewModel.onScreenLocked()
                Lifecycle.Event.ON_START -> viewModel.onScreenUnlocked()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (state.showExamPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissExamPrompt() },
            title = { Text(stringResource(R.string.exam_prompt_title)) },
            text = { Text(stringResource(R.string.exam_prompt_message)) },
            confirmButton = {
                Button(onClick = { viewModel.acceptExamPrompt() }) {
                    Text(stringResource(R.string.exam_prompt_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissExamPrompt() }) {
                    Text(stringResource(R.string.exam_prompt_later))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.lessonTitle ?: "Урок") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Структура урока",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Интро → ${state.segments.count { it.type.name.contains("PHRASE") }} сегментов → SRS-повтор",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Полных прослушиваний: ${state.fullPlaythroughsRecorded}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(32.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!state.isPlaying) {
                    FilledTonalButton(
                        onClick = { viewModel.startPlayback(replayLoop = false) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text(" Старт")
                    }
                } else {
                    FilledTonalButton(
                        onClick = { viewModel.startPlayback(replayLoop = true) },
                        modifier = Modifier.weight(1f),
                        enabled = !state.isReplayLoop
                    ) {
                        Icon(Icons.Default.Repeat, contentDescription = null)
                        Text(" Replay")
                    }
                }
                OutlinedButton(
                    onClick = { viewModel.stopPlayback() },
                    enabled = state.isPlaying || state.isReplayLoop
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Text(" Stop")
                }
            }

            if (state.isReplayLoop) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Режим Replay: урок зациклен. Экзамен доступен только после Stop.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
