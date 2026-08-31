package com.koreanimmersion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.koreanimmersion.ui.KoreanImmersionAppRoot
import com.koreanimmersion.ui.theme.KoreanImmersionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KoreanImmersionTheme {
                KoreanImmersionAppRoot()
            }
        }
    }
}
