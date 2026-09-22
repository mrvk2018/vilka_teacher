package com.koreanimmersion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.koreanimmersion.R
import com.koreanimmersion.ui.viewmodel.TopicDetailViewModel
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    topicId: Long,
    viewModel: TopicDetailViewModel,
    onLessonClick: (Long) -> Unit,
    onIntroductionClick: (Long) -> Unit,
    onSpeakingClick: (Long) -> Unit,
    onLlmDialogClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    var topicName by remember { mutableStateOf("") }
    val lessons: StateFlow<List<com.koreanimmersion.data.local.entity.LessonEntity>> =
        remember(topicId) { viewModel.observeLessons(topicId) }
    val lessonList by lessons.collectAsState()

    LaunchedEffect(topicId) {
        topicName = viewModel.getTopic(topicId)?.nameDisplay ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topicName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        if (lessonList.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
            ) {
                Text("Уроки для этой темы пока не добавлены.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilledTonalButton(
                        onClick = { onIntroductionClick(topicId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.intro_open_stage))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = { onSpeakingClick(topicId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.speaking_open_stage))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    FilledTonalButton(
                        onClick = { onLlmDialogClick(topicId) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.llm_dialog_open_stage))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(lessonList, key = { it.id }) { lesson ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onLessonClick(lesson.id) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                            Text(
                                "~${lesson.durationTargetMin} мин",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
