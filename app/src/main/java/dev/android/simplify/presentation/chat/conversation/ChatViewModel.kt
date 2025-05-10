package dev.android.simplify.presentation.chat.conversation

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
import kotlinx.coroutines.flow.onEach
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

    val currentUser = getCurrentUserUseCase()
        .map { authResult ->
            when (authResult) {
                is AuthResult.Success -> {
                    val user = authResult.data
                    user
                }
                is AuthResult.Loading -> null
                is AuthResult.Error -> null
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val messages = getChatMessagesUseCase(chatId)
        .catch { error ->
            _uiState.update { it.copy(error = error.message ?: "Не удалось загрузить сообщения") }
        }
        .onEach { messagesList ->
            _uiState.update { it.copy(isLoading = false) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    init {
        _uiState.update { it.copy(isLoading = true) }
        loadOtherUser()

        viewModelScope.launch {
            delay(300)
            markMessagesAsRead()
        }
    }


    private fun loadOtherUser() {
        viewModelScope.launch {
            val user = currentUser.value ?: return@launch

            val chatMessages = messages.value
            if (chatMessages.isEmpty()) return@launch

            val otherUserId = chatMessages
                .map { it.senderId }
                .distinct()
                .find { it != user.id } ?: return@launch

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

                sendMessageUseCase(chatId, currentUserId, text)

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
    val isLoading: Boolean = true,
    val otherUser: User? = null,
    val error: String? = null
)