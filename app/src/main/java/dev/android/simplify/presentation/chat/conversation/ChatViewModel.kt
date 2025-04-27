package dev.android.simplify.presentation.chat.conversation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.model.User
import dev.android.simplify.domain.usecase.auth.GetCurrentUserUseCase
import dev.android.simplify.domain.usecase.chat.GetChatMessagesUseCase
import dev.android.simplify.domain.usecase.chat.MarkMessagesAsReadUseCase
import dev.android.simplify.domain.usecase.chat.SendMessageUseCase
import dev.android.simplify.domain.usecase.user.GetUserByIdUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatId: String,
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getUserByIdUseCase: GetUserByIdUseCase,
    private val markMessagesAsReadUseCase: MarkMessagesAsReadUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // В ChatViewModel.kt добавим логирование в определение currentUser:
    val currentUser = getCurrentUserUseCase()
        .map { authResult ->
            Log.d("markMessagesAsRead", "Получен результат из getCurrentUserUseCase: $authResult")
            when (authResult) {
                is AuthResult.Success -> {
                    val user = authResult.data
                    Log.d("markMessagesAsRead", "Преобразовано в пользователя: ${user?.id ?: "null"}")
                    user
                }
                is AuthResult.Loading -> {
                    Log.d("markMessagesAsRead", "Получено состояние Loading")
                    null
                }
                is AuthResult.Error -> {
                    Log.e("markMessagesAsRead", "Получена ошибка: ${authResult.exception.message}")
                    null
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )


    // Сообщения чата
    val messages = getChatMessagesUseCase(chatId)
        .catch { error ->
            _uiState.update { it.copy(error = error.message ?: "Не удалось загрузить сообщения") }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {

        Log.d("markMessagesAsRead", "Инициализация ChatViewModel для чата $chatId")
        Log.d("markMessagesAsRead", "Текущее значение currentUser при init: ${currentUser.value?.id ?: "null"}")
        // Загружаем информацию о другом пользователе в чате
        loadOtherUser()

        // Отмечаем сообщения как прочитанные при открытии чата
        viewModelScope.launch {
            // Небольшая задержка, чтобы дать Flow время на получение значения
            delay(300)
            Log.d("markMessagesAsRead", "После задержки, currentUser: ${currentUser.value?.id ?: "null"}")
            markMessagesAsRead()
        }
    }

    private fun loadOtherUser() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch

            // Получаем все сообщения, чтобы найти других участников чата
            val chatMessages = messages.value
            if (chatMessages.isEmpty()) return@launch

            // Находим ID другого пользователя (не текущего)
            val otherUserId = chatMessages
                .map { it.senderId }
                .distinct()
                .find { it != user.id } ?: return@launch

            // Загружаем информацию о другом пользователе
            val otherUser = getUserByIdUseCase(otherUserId)
            _uiState.update { it.copy(otherUser = otherUser) }
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        val currentUserId = currentUser.value?.id ?: return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isSending = true) }

                // Отправляем сообщение
                sendMessageUseCase(chatId, currentUserId, text)

                // Очищаем поле ввода
                _uiState.update { it.copy(messageText = "", isSending = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSending = false,
                        error = e.message ?: "Не удалось отправить сообщение"
                    )
                }
            }
        }
    }

    fun onMessageTextChanged(text: String) {
        _uiState.update { it.copy(messageText = text) }
    }

    fun markMessagesAsRead() {
        val currentUserId = currentUser.value?.id ?: run {
            Log.w("markMessagesAsRead", "Невозможно отметить сообщения как прочитанные: currentUserId is null")
            return
        }

        Log.d("markMessagesAsRead", "Отмечаем сообщения как прочитанные в чате $chatId для пользователя $currentUserId")

        viewModelScope.launch {
            try {
                markMessagesAsReadUseCase(chatId, currentUserId)
                Log.d("markMessagesAsRead", "Сообщения успешно отмечены как прочитанные")
            } catch (e: Exception) {
                Log.e("markMessagesAsRead", "Ошибка при отметке сообщений как прочитанных", e)
                // Можно также обновить UI с информацией об ошибке при необходимости
                _uiState.update { it.copy(error = "Ошибка при отметке сообщений: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ChatUiState(
    val messageText: String = "",
    val isSending: Boolean = false,
    val otherUser: User? = null,
    val error: String? = null
)