package dev.android.simplify.presentation.auth.forgot_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.usecase.auth.ForgotPasswordUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
    private val forgotPasswordUseCase: ForgotPasswordUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    fun resetPassword() {
        if (!validateEmail()) return

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            when (val result = forgotPasswordUseCase(_uiState.value.email)) {
                is AuthResult.Success -> {
                    _uiState.update { it.copy(
                        isLoading = false,
                        resetEmailSent = true
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

    private fun validateEmail(): Boolean {
        if (_uiState.value.email.isBlank()) {
            _uiState.update { it.copy(emailError = "Email не может быть пустым") }
            return false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
            _uiState.update { it.copy(emailError = "Введите корректный email") }
            return false
        }
        return true
    }

    fun resetState() {
        _uiState.update { ForgotPasswordUiState() }
    }
}

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val resetEmailSent: Boolean = false,
    val error: Throwable? = null,
    val emailError: String? = null
)