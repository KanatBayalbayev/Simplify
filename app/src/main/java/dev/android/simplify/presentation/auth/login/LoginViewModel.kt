package dev.android.simplify.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.usecase.auth.ClearCredentialsUseCase
import dev.android.simplify.domain.usecase.auth.GetSavedCredentialsUseCase
import dev.android.simplify.domain.usecase.auth.HasSavedCredentialsUseCase
import dev.android.simplify.domain.usecase.auth.IsUserAuthenticatedUseCase
import dev.android.simplify.domain.usecase.auth.SaveCredentialsUseCase
import dev.android.simplify.domain.usecase.auth.SignInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val signInUseCase: SignInUseCase,
    private val isUserAuthenticatedUseCase: IsUserAuthenticatedUseCase,
    private val saveCredentialsUseCase: SaveCredentialsUseCase,
    private val getSavedCredentialsUseCase: GetSavedCredentialsUseCase,
    private val hasSavedCredentialsUseCase: HasSavedCredentialsUseCase,
    private val clearCredentialsUseCase: ClearCredentialsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
        loadSavedCredentials()
    }

    private fun checkAuthState() {
        if (isUserAuthenticatedUseCase()) {
            _uiState.update { it.copy(isAuthenticated = true) }
        }
    }

    private fun loadSavedCredentials() {
        if (hasSavedCredentialsUseCase()) {
            val (savedEmail, savedPassword) = getSavedCredentialsUseCase()
            _uiState.update {
                it.copy(
                    email = savedEmail.orEmpty(),
                    password = savedPassword.orEmpty(),
                    rememberMe = true
                )
            }
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun onRememberMeChanged(rememberMe: Boolean) {
        _uiState.update { it.copy(rememberMe = rememberMe) }

        if (!rememberMe) {
            clearCredentialsUseCase()
        }
    }

    fun signIn() {
        if (!validateInputs()) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            when (val result = signInUseCase(_uiState.value.email, _uiState.value.password)) {
                is AuthResult.Success -> {
                    // Если "запомнить меня" включено, сохраняем учетные данные
                    if (_uiState.value.rememberMe) {
                        saveCredentialsUseCase(_uiState.value.email, _uiState.value.password)
                    } else {
                        // Иначе удаляем сохраненные учетные данные
                        clearCredentialsUseCase()
                    }

                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.exception) }
                }
                is AuthResult.Loading -> {
                    // Уже обрабатывается
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (_uiState.value.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email не может быть пустым") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
            _uiState.update { it.copy(emailError = "Введите корректный email") }
            isValid = false
        }

        if (_uiState.value.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Пароль не может быть пустым") }
            isValid = false
        } else if (_uiState.value.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Пароль должен содержать минимум 6 символов") }
            isValid = false
        }

        return isValid
    }

    fun resetState() {
        val currentState = _uiState.value
        if (currentState.rememberMe) {
            _uiState.update {
                LoginUiState(
                    email = currentState.email,
                    password = currentState.password,
                    rememberMe = true
                )
            }
        } else {
            _uiState.update { LoginUiState() }
        }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: Throwable? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)