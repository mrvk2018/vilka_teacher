package com.koreanimmersion.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.koreanimmersion.ui.navigation.AppRoute
import com.koreanimmersion.ui.screens.ExamScreen
import com.koreanimmersion.ui.screens.HomeScreen
import com.koreanimmersion.ui.screens.LessonPlayerScreen
import com.koreanimmersion.ui.screens.ManualTopicScreen
import com.koreanimmersion.ui.screens.ProgressScreen
import com.koreanimmersion.ui.screens.TopicDetailScreen
import com.koreanimmersion.ui.viewmodel.ExamViewModel
import com.koreanimmersion.ui.viewmodel.HomeViewModel
import com.koreanimmersion.ui.viewmodel.LessonPlayerViewModel
import com.koreanimmersion.ui.viewmodel.ManualTopicViewModel
import com.koreanimmersion.ui.viewmodel.ProgressViewModel
import com.koreanimmersion.ui.viewmodel.TopicDetailViewModel

@Composable
fun KoreanImmersionAppRoot() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val showBottomBar = currentRoute?.contains("Home") == true ||
        currentRoute?.contains("Progress") == true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute?.contains("Home") == true,
                        onClick = { navController.navigate("home") { popUpTo("home") { inclusive = false } } },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Уроки") }
                    )
                    NavigationBarItem(
                        selected = currentRoute?.contains("Progress") == true,
                        onClick = { navController.navigate("progress") },
                        icon = { Icon(Icons.Default.TrendingUp, contentDescription = null) },
                        label = { Text("Прогресс") }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") {
                val vm: HomeViewModel = viewModel()
                HomeScreen(
                    topics = vm.topics,
                    onTopicClick = { navController.navigate("topic/$it") },
                    onManualTopicClick = { navController.navigate("manual/$it") }
                )
            }
            composable(
                "topic/{topicId}",
                arguments = listOf(navArgument("topicId") { type = NavType.LongType })
            ) { entry ->
                val topicId = entry.arguments?.getLong("topicId") ?: return@composable
                val vm: TopicDetailViewModel = viewModel()
                TopicDetailScreen(
                    topicId = topicId,
                    viewModel = vm,
                    onLessonClick = { navController.navigate("lesson/$it") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                "lesson/{lessonId}",
                arguments = listOf(navArgument("lessonId") { type = NavType.LongType })
            ) { entry ->
                val lessonId = entry.arguments?.getLong("lessonId") ?: return@composable
                val vm: LessonPlayerViewModel = viewModel()
                LessonPlayerScreen(
                    lessonId = lessonId,
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onNavigateToExam = { navController.navigate("exam/$it") }
                )
            }
            composable(
                "manual/{topicId}",
                arguments = listOf(navArgument("topicId") { type = NavType.LongType })
            ) { entry ->
                val topicId = entry.arguments?.getLong("topicId") ?: return@composable
                val vm: ManualTopicViewModel = viewModel()
                ManualTopicScreen(
                    topicId = topicId,
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                "exam/{lessonId}",
                arguments = listOf(navArgument("lessonId") { type = NavType.LongType })
            ) { entry ->
                val lessonId = entry.arguments?.getLong("lessonId") ?: return@composable
                val vm: ExamViewModel = viewModel()
                ExamScreen(
                    lessonId = lessonId,
                    viewModel = vm,
                    onFinish = { navController.popBackStack() }
                )
            }
            composable("progress") {
                val vm: ProgressViewModel = viewModel()
                ProgressScreen(viewModel = vm)
            }
        }
    }
}
