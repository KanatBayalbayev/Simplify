package dev.android.simplify.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.usecase.GetCurrentUserUseCase
import dev.android.simplify.domain.usecase.IsUserAuthenticatedUseCase
import dev.android.simplify.domain.usecase.SignInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val signInUseCase: SignInUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val isUserAuthenticatedUseCase: IsUserAuthenticatedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        if (isUserAuthenticatedUseCase()) {
            _uiState.update { it.copy(isAuthenticated = true) }
        }
    }

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    fun signIn() {
        if (!validateInputs()) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            when (val result = signInUseCase(_uiState.value.email, _uiState.value.password)) {
                is AuthResult.Success -> {
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

    fun resetErrors() {
        _uiState.update { it.copy(error = null, emailError = null, passwordError = null) }
    }
}

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val error: Throwable? = null,
    val emailError: String? = null,
    val passwordError: String? = null
)