package com.koreanimmersion.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.koreanimmersion.domain.stt.PhraseSttHelper
import com.koreanimmersion.ui.viewmodel.ExamViewModel

@Composable
fun ExamScreen(
    lessonId: Long,
    viewModel: ExamViewModel,
    onFinish: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val sttHelper = remember { PhraseSttHelper(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startSttForCurrentPhrase(sttHelper, state, viewModel)
    }

    LaunchedEffect(lessonId) { viewModel.loadExam(lessonId) }

    DisposableEffect(Unit) {
        onDispose { sttHelper.destroy() }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        when (state.phase) {
            ExamViewModel.ExamPhase.LOADING -> Text("Загрузка экзамена…")
            ExamViewModel.ExamPhase.MULTIPLE_CHOICE -> {
                val q = state.mcQuestions.getOrNull(state.currentMcIndex)
                if (q != null) {
                    Text("Вопрос ${state.currentMcIndex + 1}/${state.mcQuestions.size}", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(q.questionText, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    q.options.forEachIndexed { index, option ->
                        Button(
                            onClick = { viewModel.submitMcAnswer(state.currentMcIndex, index) },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(option)
                        }
                    }
                }
            }
            ExamViewModel.ExamPhase.STT -> {
                val phrase = state.sttPhrases.getOrNull(state.currentSttIndex)
                Text("Произнеси фразу вслух", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("(не влияет на оценку — только обратная связь)", style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                if (phrase != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(phrase.koreanText, style = MaterialTheme.typography.headlineSmall)
                            Text(phrase.koreanRomanization, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    var lastResult by remember { mutableStateOf<String?>(null) }
                    Button(
                        onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                                == PackageManager.PERMISSION_GRANTED
                            ) {
                                sttHelper.startListening(phrase.koreanText) { result ->
                                    when (result) {
                                        is PhraseSttHelper.SttResult.Heard -> {
                                            lastResult = if (result.success) "Услышано!" else "Не расслышано, попробуй ещё раз"
                                            if (result.success) {
                                                viewModel.recordSttResult(phrase.id, true)
                                            }
                                        }
                                    }
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🎤 Начать запись")
                    }
                    lastResult?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.recordSttResult(phrase.id, false) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Пропустить STT-проверку")
                    }
                }
            }
            ExamViewModel.ExamPhase.RESULT -> {
                Text("Результат экзамена", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Оценка: ${state.finalScore ?: "—"}/5",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("(Оценка основана только на вопросах с вариантами ответа)")
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) {
                    Text("Готово")
                }
            }
        }
    }
}

private fun startSttForCurrentPhrase(
    sttHelper: PhraseSttHelper,
    state: ExamViewModel.ExamUiState,
    viewModel: ExamViewModel
) {
    val phrase = state.sttPhrases.getOrNull(state.currentSttIndex) ?: return
    sttHelper.startListening(phrase.koreanText) { result ->
        if (result is PhraseSttHelper.SttResult.Heard && result.success) {
            viewModel.recordSttResult(phrase.id, true)
        }
    }
}
