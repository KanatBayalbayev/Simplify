package dev.android.simplify.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import dev.android.simplify.app.navigation.AppNavigation
import dev.android.simplify.app.navigation.Routes
import dev.android.simplify.domain.usecase.IsUserAuthenticatedUseCase
import dev.android.simplify.ui.theme.SimplifyTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val isUserAuthenticatedUseCase: IsUserAuthenticatedUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            SimplifyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val startDestination = if (isUserAuthenticatedUseCase()) {
                        Routes.CHAT_HOME
                    } else {
                        Routes.LOGIN
                    }

                    AppNavigation(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}