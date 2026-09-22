package com.koreanimmersion.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.koreanimmersion.R
import com.koreanimmersion.domain.speech.SpeechRecognitionState
import com.koreanimmersion.ui.viewmodel.PhraseLlmDialogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhraseLlmDialogScreen(
    topicId: Long,
    viewModel: PhraseLlmDialogViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val topic by viewModel.topic.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val input by viewModel.inputText.collectAsState()
    val isSending by viewModel.isSending.collectAsState()
    val errorBanner by viewModel.errorBanner.collectAsState()
    val speechState by viewModel.speechState.collectAsState()

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

    LaunchedEffect(topicId, useEnglish) {
        viewModel.loadTopic(topicId, useEnglish)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.releaseSpeech() }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        topic?.let { if (useEnglish) it.titleEn else it.titleRu }
                            ?: stringResource(R.string.llm_dialog_title_default)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(8.dp)
            ) {
                if (errorBanner != null) {
                    Text(
                        text = mapLlmError(errorBanner!!, useEnglish),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = viewModel::updateInput,
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(stringResource(R.string.llm_dialog_input_hint))
                        },
                        enabled = !isSending,
                        maxLines = 4
                    )
                    IconButton(
                        onClick = {
                            if (!micGranted) {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.pointerInput(micGranted, isSending) {
                            if (!micGranted || isSending) return@pointerInput
                            detectTapGestures(
                                onPress = {
                                    viewModel.startVoiceInput()
                                    tryAwaitRelease()
                                    viewModel.stopVoiceInput()
                                }
                            )
                        }
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = stringResource(R.string.llm_dialog_mic),
                            tint = when (speechState) {
                                SpeechRecognitionState.Listening ->
                                    MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                    IconButton(
                        onClick = { viewModel.sendMessage() },
                        enabled = input.isNotBlank() && !isSending
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(4.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = stringResource(R.string.llm_dialog_send)
                            )
                        }
                    }
                }
                if (!micGranted) {
                    Text(
                        text = stringResource(R.string.speaking_mic_permission_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.llm_dialog_stage_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.llm_dialog_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        count = messages.size,
                        key = { index -> "msg-$index-${messages[index].javaClass.simpleName}" }
                    ) { index ->
                        val message = messages[index]
                        when (message) {
                            is PhraseLlmDialogViewModel.ChatMessage.User ->
                                ChatBubble(
                                    text = message.text,
                                    isUser = true
                                )
                            is PhraseLlmDialogViewModel.ChatMessage.Teacher ->
                                ChatBubble(
                                    text = message.text,
                                    isUser = false
                                )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(text: String, isUser: Boolean) {
    val bg = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val align = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = align
    ) {
        Text(
            text = text,
            modifier = Modifier
                .widthIn(max = 320.dp)
                .background(bg, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun mapLlmError(raw: String, useEnglish: Boolean): String {
    return when {
        raw == "empty_message" -> stringResource(R.string.llm_dialog_error_empty)
        raw.contains("Cannot connect", ignoreCase = true) ||
            raw.contains("connect", ignoreCase = true) ||
            raw.contains("network", ignoreCase = true) ||
            raw.contains("Failed to connect", ignoreCase = true) ->
            stringResource(R.string.llm_dialog_error_server)
        raw.contains("timed out", ignoreCase = true) ->
            stringResource(R.string.llm_dialog_error_timeout)
        else -> if (useEnglish) raw else raw
    }
}
