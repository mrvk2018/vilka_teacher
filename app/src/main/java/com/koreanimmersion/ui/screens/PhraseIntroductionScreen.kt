package com.koreanimmersion.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.koreanimmersion.R
import com.koreanimmersion.core.database.entity.PhraseEntity
import com.koreanimmersion.core.database.entity.TopicEntity
import com.koreanimmersion.ui.viewmodel.PhraseIntroductionViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhraseIntroductionScreen(
    topicId: Long,
    viewModel: PhraseIntroductionViewModel,
    onBack: () -> Unit
) {
    val topic by viewModel.topic.collectAsState()
    val phrases by viewModel.observePhrases(topicId).collectAsState()
    val isStarting by viewModel.isStarting.collectAsState()
    val locale = LocalConfiguration.current.locales[0]
    val useEnglish = isEnglishUi(locale)

    LaunchedEffect(topicId) { viewModel.loadTopic(topicId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        topic?.let { localizedTopicTitle(it, useEnglish) }
                            ?: stringResource(R.string.intro_screen_title_default)
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
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.intro_stage_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            FilledTonalButton(
                onClick = { viewModel.startAudioLesson(topicId, phrases) },
                enabled = phrases.isNotEmpty() && !isStarting,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Text(" ${stringResource(R.string.intro_start_audio_lesson)}")
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(phrases, key = { it.id }) { phrase ->
                    PhraseIntroCard(phrase = phrase, useEnglish = useEnglish)
                }
            }
        }
    }
}

@Composable
private fun PhraseIntroCard(phrase: PhraseEntity, useEnglish: Boolean) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = phrase.koreanText, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (useEnglish) phrase.englishTranslation else phrase.russianTranslation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

private fun localizedTopicTitle(topic: TopicEntity, useEnglish: Boolean): String =
    if (useEnglish) topic.titleEn else topic.titleRu

private fun isEnglishUi(locale: Locale): Boolean =
    locale.language.equals("en", ignoreCase = true)
