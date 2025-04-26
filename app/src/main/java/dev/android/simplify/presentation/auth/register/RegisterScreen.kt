package dev.android.simplify.presentation.auth.register

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.android.simplify.presentation.auth.common.AuthButton
import dev.android.simplify.presentation.auth.common.EmailField
import dev.android.simplify.presentation.auth.common.ErrorText
import dev.android.simplify.presentation.auth.common.PasswordField
import dev.android.simplify.presentation.auth.common.mapAuthErrorToMessage
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegistrationSuccess: () -> Unit,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    DisposableEffect(key1 = Unit) {
        onDispose {
            viewModel.resetState()
        }
    }

    LaunchedEffect(uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            onRegistrationSuccess()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Регистрация",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email field
                EmailField(
                    email = uiState.email,
                    onEmailChange = viewModel::onEmailChanged,
                    isError = uiState.emailError != null,
                    errorMessage = uiState.emailError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password field
                PasswordField(
                    password = uiState.password,
                    onPasswordChange = viewModel::onPasswordChanged,
                    isError = uiState.passwordError != null,
                    errorMessage = uiState.passwordError,
                    imeAction = ImeAction.Next
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm password field
                PasswordField(
                    password = uiState.passwordConfirmation,
                    onPasswordChange = viewModel::onPasswordConfirmationChanged,
                    label = "Подтверждение пароля",
                    isError = uiState.passwordConfirmationError != null,
                    errorMessage = uiState.passwordConfirmationError,
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        focusManager.clearFocus()
                        viewModel.signUp()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error message
                if (uiState.error != null) {
                    ErrorText(text = mapAuthErrorToMessage(uiState.error))
                }

                // Register button
                AuthButton(
                    text = "Зарегистрироваться",
                    onClick = viewModel::signUp,
                    isLoading = uiState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Login prompt
                TextButton(
                    onClick = onNavigateToLogin
                ) {
                    Text("Уже есть аккаунт? Войти")
                }
            }
        }
    }
}