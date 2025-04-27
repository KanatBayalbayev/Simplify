package dev.android.simplify.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.android.simplify.domain.usecase.auth.SignOutUseCase
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun TemporaryChatHome(onLogout: () -> Unit) {
    val signOutUseCase = koinInject<SignOutUseCase>()
    val scope = rememberCoroutineScope()

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chat Home",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Вы успешно вошли в систему. Экран чата будет реализован в следующем этапе разработки.",
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    scope.launch {
                        signOutUseCase()
                        onLogout()
                    }
                }
            ) {
                Text("Выйти")
            }
        }
    }
}