package team.swyp.sdu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import team.swyp.sdu.navigation.NavGraph
import team.swyp.sdu.presentation.viewmodel.UserViewModel
import team.swyp.sdu.ui.theme.WalkItTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WalkItTheme {
                val userViewModel: UserViewModel = hiltViewModel()
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        NavGraph(
                            navController = navController,
                            userViewModel = userViewModel
                        )
                    }
                }
            }
        }
    }
}