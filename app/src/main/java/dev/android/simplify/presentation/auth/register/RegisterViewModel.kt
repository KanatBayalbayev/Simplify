package dev.android.simplify.presentation.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.usecase.SignUpUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
        validatePasswordConfirmation()
    }

    fun onPasswordConfirmationChanged(passwordConfirmation: String) {
        _uiState.update { it.copy(passwordConfirmation = passwordConfirmation, passwordConfirmationError = null) }
        validatePasswordConfirmation()
    }

    private fun validatePasswordConfirmation() {
        val state = _uiState.value
        if (state.password.isNotEmpty() && state.passwordConfirmation.isNotEmpty() &&
            state.password != state.passwordConfirmation) {
            _uiState.update { it.copy(passwordConfirmationError = "Пароли не совпадают") }
        } else if (state.passwordConfirmationError != null) {
            _uiState.update { it.copy(passwordConfirmationError = null) }
        }
    }

    fun signUp() {
        if (!validateInputs()) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            when (val result = signUpUseCase(_uiState.value.email, _uiState.value.password)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        registrationSuccess = true
                    ) }
                }
                is AuthResult.Error -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = result.exception
                    ) }
                }
                is AuthResult.Loading -> {
                    // Уже обрабатывается
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        // Проверка email
        if (_uiState.value.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email не может быть пустым") }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
            _uiState.update { it.copy(emailError = "Введите корректный email") }
            isValid = false
        }

        // Проверка пароля
        if (_uiState.value.password.isBlank()) {
            _uiState.update { it.copy(passwordError = "Пароль не может быть пустым") }
            isValid = false
        } else if (_uiState.value.password.length < 6) {
            _uiState.update { it.copy(passwordError = "Пароль должен содержать минимум 6 символов") }
            isValid = false
        }

        // Проверка подтверждения пароля
        if (_uiState.value.passwordConfirmation.isBlank()) {
            _uiState.update { it.copy(passwordConfirmationError = "Подтвердите пароль") }
            isValid = false
        } else if (_uiState.value.password != _uiState.value.passwordConfirmation) {
            _uiState.update { it.copy(passwordConfirmationError = "Пароли не совпадают") }
            isValid = false
        }

        return isValid
    }

    fun resetErrors() {
        _uiState.update { it.copy(
            error = null,
            emailError = null,
            passwordError = null,
            passwordConfirmationError = null
        ) }
    }
}

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val passwordConfirmation: String = "",
    val isLoading: Boolean = false,
    val registrationSuccess: Boolean = false,
    val error: Throwable? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val passwordConfirmationError: String? = null
)