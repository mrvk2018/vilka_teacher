package com.koreanimmersion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.koreanimmersion.KoreanImmersionApp
import com.koreanimmersion.R
import com.koreanimmersion.domain.hangul.HangulAlphabet
import com.koreanimmersion.domain.hangul.HangulLetter
import com.koreanimmersion.tts.LocaleTtsEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HangulAlphabetScreen(
    onBack: () -> Unit,
    onOpenDrawing: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val tts = KoreanImmersionApp.instance.ttsEngine
    val locale = LocalConfiguration.current.locales[0]
    val useEnglish = locale.language.equals("en", ignoreCase = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.hangul_alphabet_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Text(
                    text = stringResource(R.string.hangul_alphabet_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(stringResource(R.string.hangul_section_consonants))
            }
            items(HangulAlphabet.consonants, key = { it.char }) { letter ->
                HangulLetterCard(
                    letter = letter,
                    useEnglish = useEnglish,
                    onPlay = {
                        scope.launch {
                            tts.speakAloud(letter.char, LocaleTtsEngine.LOCALE_KO)
                        }
                    },
                    onPractice = { onOpenDrawing(letter.char) }
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(stringResource(R.string.hangul_section_vowels))
            }
            items(HangulAlphabet.vowels, key = { it.char }) { letter ->
                HangulLetterCard(
                    letter = letter,
                    useEnglish = useEnglish,
                    onPlay = {
                        scope.launch {
                            tts.speakAloud(letter.char, LocaleTtsEngine.LOCALE_KO)
                        }
                    },
                    onPractice = { onOpenDrawing(letter.char) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun HangulLetterCard(
    letter: HangulLetter,
    useEnglish: Boolean,
    onPlay: () -> Unit,
    onPractice: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = letter.char,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (useEnglish) letter.nameEn else letter.nameRu,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )
            IconButton(onClick = onPractice, modifier = Modifier.padding(0.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.hangul_practice_letter),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
