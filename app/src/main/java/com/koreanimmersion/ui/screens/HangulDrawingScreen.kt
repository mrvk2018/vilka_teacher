package com.koreanimmersion.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.koreanimmersion.KoreanImmersionApp
import com.koreanimmersion.R
import com.koreanimmersion.domain.hangul.HangulAlphabet
import com.koreanimmersion.tts.LocaleTtsEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HangulDrawingScreen(
    letter: String,
    onBack: () -> Unit
) {
    val strokes = remember { mutableStateListOf<List<Offset>>() }
    val currentStroke = remember { mutableStateListOf<Offset>() }
    val scope = rememberCoroutineScope()
    val tts = KoreanImmersionApp.instance.ttsEngine
    val meta = HangulAlphabet.findByChar(letter)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.hangul_drawing_title, letter))
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
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.hangul_drawing_instruction),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            meta?.let {
                Text(
                    text = letter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 28.sp
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .padding(top = 16.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        MaterialTheme.shapes.large
                    )
            ) {
                Text(
                    text = letter,
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 200.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    textAlign = TextAlign.Center
                )
                HangulDrawingCanvas(
                    strokes = strokes,
                    currentStroke = currentStroke,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Button(
                onClick = {
                    strokes.clear()
                    currentStroke.clear()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Text(stringResource(R.string.hangul_clear_canvas))
            }
            Button(
                onClick = {
                    scope.launch {
                        tts.speakAloud(letter, LocaleTtsEngine.LOCALE_KO)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(stringResource(R.string.hangul_play_letter))
            }
        }
    }
}

@Composable
private fun HangulDrawingCanvas(
    strokes: SnapshotStateList<List<Offset>>,
    currentStroke: SnapshotStateList<Offset>,
    modifier: Modifier = Modifier
) {
    val inkColor = MaterialTheme.colorScheme.primary
    val guideColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    currentStroke.clear()
                    currentStroke.add(offset)
                },
                onDrag = { change, _ ->
                    currentStroke.add(change.position)
                },
                onDragEnd = {
                    if (currentStroke.isNotEmpty()) {
                        strokes.add(currentStroke.toList())
                        currentStroke.clear()
                    }
                },
                onDragCancel = { currentStroke.clear() }
            )
        }
    ) {
        val dash = floatArrayOf(12f, 12f)
        drawLine(
            color = guideColor,
            start = Offset(size.width / 2f, 0f),
            end = Offset(size.width / 2f, size.height),
            strokeWidth = 2f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(dash)
        )
        drawLine(
            color = guideColor,
            start = Offset(0f, size.height / 2f),
            end = Offset(size.width, size.height / 2f),
            strokeWidth = 2f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(dash)
        )
        drawRect(
            color = guideColor,
            style = Stroke(
                width = 2f,
                pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(dash)
            )
        )

        val strokeStyle = Stroke(
            width = 8f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        fun drawStroke(points: List<Offset>) {
            if (points.size < 2) return
            val path = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path, color = inkColor, style = strokeStyle)
        }
        strokes.forEach { drawStroke(it) }
        drawStroke(currentStroke)
    }
}
