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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.koreanimmersion.ui.viewmodel.ManualTopicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualTopicScreen(
    topicId: Long,
    viewModel: ManualTopicViewModel,
    onBack: () -> Unit
) {
    val segments by viewModel.segments.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isReplayLoop by viewModel.isReplayLoop.collectAsState()
    val isQueueReady by viewModel.isQueueReady.collectAsState()

    LaunchedEffect(topicId) { viewModel.loadTopic(topicId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ручное повторение") },
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
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Ручной режим",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Сначала русский контекст, затем корейская фраза, пауза для повтора.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Фраз в очереди: ${segments.count { it.type.name.contains("AUDIO") && !it.type.name.contains("SRS") }}",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (!isQueueReady) {
                Text(
                    text = "Подготовка очереди и озвучки…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!isPlaying) {
                    FilledTonalButton(
                        onClick = { viewModel.startPlayback(replayLoop = false) },
                        modifier = Modifier.weight(1f),
                        enabled = isQueueReady && segments.isNotEmpty()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text(" Старт")
                    }
                } else {
                    FilledTonalButton(
                        onClick = { viewModel.startPlayback(replayLoop = true) },
                        modifier = Modifier.weight(1f),
                        enabled = !isReplayLoop
                    ) {
                        Icon(Icons.Default.Repeat, contentDescription = null)
                        Text(" Replay")
                    }
                }
                OutlinedButton(
                    onClick = { viewModel.stopPlayback() },
                    enabled = isPlaying
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Text(" Stop")
                }
            }

            if (isReplayLoop) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Зацикленное повторение включено.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
