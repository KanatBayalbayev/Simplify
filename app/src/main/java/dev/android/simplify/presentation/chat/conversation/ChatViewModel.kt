package dev.android.simplify.presentation.chat.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.android.simplify.domain.model.User
import dev.android.simplify.domain.usecase.auth.GetCurrentUserUseCase
import dev.android.simplify.domain.usecase.chat.GetChatMessagesUseCase
import dev.android.simplify.domain.usecase.chat.MarkMessagesAsReadUseCase
import dev.android.simplify.domain.usecase.chat.SendMessageUseCase
import dev.android.simplify.domain.usecase.user.GetUserByIdUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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

    // Текущий пользователь
    val currentUser = getCurrentUserUseCase()
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
        // Загружаем информацию о другом пользователе в чате
        loadOtherUser()

        // Отмечаем сообщения как прочитанные при открытии чата
        markMessagesAsRead()
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
        val currentUserId = currentUser.value?.id ?: return

        viewModelScope.launch {
            try {
                markMessagesAsReadUseCase(chatId, currentUserId)
            } catch (e: Exception) {
                // Можно обработать ошибку при необходимости
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