package com.koreanimmersion.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.koreanimmersion.R
import com.koreanimmersion.core.database.entity.PhraseEntity
import com.koreanimmersion.domain.speech.SpeechRecognitionState
import com.koreanimmersion.ui.viewmodel.PhraseSpeakingViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhraseSpeakingScreen(
    topicId: Long,
    viewModel: PhraseSpeakingViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val topic by viewModel.topic.collectAsState()
    val phrase by viewModel.currentPhrase.collectAsState()
    val currentIndex by viewModel.currentIndex.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    val speechState by viewModel.speechState.collectAsState()
    val learnedCount by viewModel.learnedCount.collectAsState()
    val phraseCount by viewModel.phraseCount.collectAsState()

    val locale = LocalConfiguration.current.locales[0]
    val useEnglish = locale.language.equals("en", ignoreCase = true)

    var micGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> micGranted = granted }

    LaunchedEffect(topicId) { viewModel.loadTopic(topicId) }

    DisposableEffect(Unit) {
        onDispose { viewModel.resetSpeechIdle() }
    }

    val phraseCardColor = when (feedback) {
        PhraseSpeakingViewModel.SpeakingFeedback.Success ->
            MaterialTheme.colorScheme.primaryContainer
        is PhraseSpeakingViewModel.SpeakingFeedback.TryAgain ->
            MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        topic?.let { if (useEnglish) it.titleEn else it.titleRu }
                            ?: stringResource(R.string.speaking_screen_title_default)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.speaking_stage_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = stringResource(R.string.speaking_progress_learned, learnedCount),
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            phrase?.let { current ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = phraseCardColor)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(
                                R.string.speaking_phrase_progress,
                                currentIndex + 1,
                                phraseCount.coerceAtLeast(1)
                            ),
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = current.koreanText,
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = localizedTranslation(current, useEnglish),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = { viewModel.playCurrentPhrase() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null)
                    Text(" ${stringResource(R.string.speaking_play_phrase)}")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isListening = speechState is SpeechRecognitionState.Listening
            MicButton(
                isListening = isListening,
                enabled = micGranted && phrase != null,
                onPressStart = {
                    if (!micGranted) {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else {
                        viewModel.startListeningKorean()
                    }
                },
                onPressEnd = { viewModel.stopListening() }
            )

            if (!micGranted) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.speaking_mic_permission_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            FeedbackText(feedback = feedback)

            if (feedback is PhraseSpeakingViewModel.SpeakingFeedback.Success) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.goToNextPhrase() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.speaking_next_phrase))
                }
            }
            if (feedback is PhraseSpeakingViewModel.SpeakingFeedback.TryAgain) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.repeatCurrentPhrase() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.speaking_try_again))
                }
            }
        }
    }
}

@Composable
private fun MicButton(
    isListening: Boolean,
    enabled: Boolean,
    onPressStart: () -> Unit,
    onPressEnd: () -> Unit
) {
    val bg = if (isListening) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(16.dp))
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        onPressStart()
                        tryAwaitRelease()
                        onPressEnd()
                    }
                )
            }
            .padding(vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Mic,
            contentDescription = null,
            tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            modifier = Modifier.height(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isListening) {
                stringResource(R.string.speaking_listening)
            } else {
                stringResource(R.string.speaking_hold_or_tap)
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FeedbackText(feedback: PhraseSpeakingViewModel.SpeakingFeedback) {
    when (feedback) {
        PhraseSpeakingViewModel.SpeakingFeedback.None -> Unit
        PhraseSpeakingViewModel.SpeakingFeedback.Success -> Text(
            text = stringResource(R.string.speaking_feedback_success),
            color = Color(0xFF2E7D32),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        is PhraseSpeakingViewModel.SpeakingFeedback.TryAgain -> Text(
            text = stringResource(
                R.string.speaking_feedback_try_again,
                feedback.similarityPercent
            ),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        is PhraseSpeakingViewModel.SpeakingFeedback.Error -> Text(
            text = feedback.message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

private fun localizedTranslation(phrase: PhraseEntity, useEnglish: Boolean): String =
    if (useEnglish) phrase.englishTranslation else phrase.russianTranslation
