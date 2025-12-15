package team.swyp.sdu.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import team.swyp.sdu.navigation.Screen
import team.swyp.sdu.ui.home.HomeScreen
import team.swyp.sdu.ui.record.RecordScreen
import team.swyp.sdu.ui.walking.WalkingScreen

/**
 * 메인 탭 화면: 각 피처 화면 호출만 담당
 */
@Composable
fun MainScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "홈",
                        )
                    },
                    label = { Text("홈") },
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "산책 기록",
                        )
                    },
                    label = { Text("산책 기록") },
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "마이 페이지",
                        )
                    },
                    label = { Text("마이 페이지") },
                )
            }
        },
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            when (selectedTabIndex) {
                0 -> HomeScreen(
                    onClickWalk = {
                        // WalkingScreen으로 네비게이션
                        navController.navigate(Screen.Walking.route)
                    },
                    onClickGoal = { /* TODO: 목표 설정 네비게이션 */ },
                    onClickMission = {
                        navController.navigate(Screen.Mission.route)
                    },
                )

                1 -> RecordScreen(
                    onStartOnboarding = {
                        navController.navigate(Screen.Onboarding.route)
                    },
                )

                2 -> {
                    team.swyp.sdu.ui.settings.SettingsScreen(
                        navController = navController,
                    )
                }
            }
        }
    }
}

