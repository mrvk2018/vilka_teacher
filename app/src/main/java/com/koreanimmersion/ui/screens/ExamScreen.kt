package com.koreanimmersion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.koreanimmersion.R
import com.koreanimmersion.domain.playback.PhraseAudioPlayer
import com.koreanimmersion.ui.viewmodel.ExamViewModel

@Composable
fun ExamScreen(
    lessonId: Long,
    viewModel: ExamViewModel,
    onFinish: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val audioPlayer = remember { PhraseAudioPlayer(context) }

    LaunchedEffect(lessonId) { viewModel.loadExam(lessonId) }

    DisposableEffect(Unit) {
        onDispose { audioPlayer.release() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        when (state.phase) {
            ExamViewModel.ExamPhase.LOADING -> Text(stringResource(R.string.exam_loading))
            ExamViewModel.ExamPhase.MULTIPLE_CHOICE -> {
                val q = state.mcQuestions.getOrNull(state.currentMcIndex)
                if (q != null) {
                    LaunchedEffect(state.currentMcIndex) {
                        q.audioUrl?.let { audioPlayer.play(it) }
                    }

                    Text(
                        text = stringResource(
                            R.string.exam_question_progress,
                            state.currentMcIndex + 1,
                            state.mcQuestions.size
                        ),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.exam_listen_instruction),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.exam_no_hangul_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    FilledTonalButton(
                        onClick = { q.audioUrl?.let { audioPlayer.play(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = q.audioUrl != null
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null)
                        Text(" ${stringResource(R.string.exam_replay_audio)}")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    q.options.forEachIndexed { index, option ->
                        OutlinedButton(
                            onClick = { viewModel.submitMcAnswer(state.currentMcIndex, index) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(option)
                        }
                    }
                }
            }
            ExamViewModel.ExamPhase.RESULT -> {
                Text(
                    text = stringResource(R.string.exam_result_title),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                val correct = state.correctCount ?: 0
                val total = state.totalCount ?: 0
                Text(
                    text = stringResource(R.string.exam_result_score, correct, total),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                if (state.finalScore != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.exam_result_grade, state.finalScore!!),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.exam_done))
                }
            }
        }
    }
}
