package com.koreanimmersion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.koreanimmersion.data.local.entity.TopicEntity
import kotlinx.coroutines.flow.StateFlow

@Composable
fun HomeScreen(
    topics: StateFlow<List<TopicEntity>>,
    onTopicClick: (Long) -> Unit,
    onManualTopicClick: (Long) -> Unit
) {
    val topicList by topics.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "KoreanImmersion",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Этап 1: разговорный корейский через фонетическое погружение",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(topicList, key = { it.id }) { topic ->
                TopicCard(
                    topic = topic,
                    onClick = { onTopicClick(topic.id) },
                    onManualRepeat = { onManualTopicClick(topic.id) }
                )
            }
        }
    }
}

@Composable
private fun TopicCard(
    topic: TopicEntity,
    onClick: () -> Unit,
    onManualRepeat: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = topic.nameDisplay, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (topic.id == 2L) "Доступен 1 урок" else "Скоро",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            IconButton(onClick = onManualRepeat) {
                Icon(Icons.Default.Replay, contentDescription = "Ручное повторение")
            }
        }
    }
}
