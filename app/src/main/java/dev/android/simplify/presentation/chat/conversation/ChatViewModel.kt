package dev.android.simplify.presentation.chat.conversation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.model.Message
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

    // Current user flow
    val currentUser = getCurrentUserUseCase()
        .map { authResult ->
            when (authResult) {
                is AuthResult.Success -> {
                    authResult.data
                }
                is AuthResult.Loading -> null
                is AuthResult.Error -> {
                    _uiState.update { it.copy(error = "Ошибка авторизации: ${authResult.exception.message}") }
                    null
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Messages flow
    val messages = getChatMessagesUseCase(chatId)
        .onEach { messagesList ->
            _uiState.update { it.copy(isLoading = false) }

            val currentUserValue = currentUser.value
            if (currentUserValue != null) {
                determineChatParticipants(currentUserValue, messagesList)
            }
        }
        .catch { error ->
            _uiState.update { it.copy(
                isLoading = false,
                error = error.message ?: "Не удалось загрузить сообщения"
            ) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    determineChatParticipants(user, messages.value)
                }
            }
        }
    }

    /**
     * Determine chat participants and handle self-chat scenario
     */
    private fun determineChatParticipants(currentUser: User, messagesList: List<Message>) {
        if (messagesList.isEmpty()) {
            _uiState.update { it.copy(
                chatTitle = "Новый чат",
                isSelfChat = false
            )}
            return
        }

        val uniqueSenderIds = messagesList.map { it.senderId }.distinct()

        if (uniqueSenderIds.size == 1 && uniqueSenderIds.first() == currentUser.id) {
            _uiState.update { it.copy(
                isSelfChat = true,
                chatTitle = "Заметки",
                otherUser = currentUser
            )}
            return
        }

        val otherUserId = uniqueSenderIds.find { it != currentUser.id }

        if (otherUserId == null) {
            Log.d("ChatViewModel", "No other participant found")
            _uiState.update { it.copy(
                chatTitle = "Чат",
                isSelfChat = false
            )}
            return
        }

        viewModelScope.launch {
            try {
                val otherUser = getUserByIdUseCase(otherUserId)
                _uiState.update { it.copy(
                    otherUser = otherUser,
                    chatTitle = otherUser?.displayName ?: otherUser?.email ?: "Чат",
                    isSelfChat = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Не удалось загрузить данные собеседника: ${e.message}",
                    chatTitle = "Чат",
                    isSelfChat = false
                )}
            }
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
    val chatTitle: String = "Чат",
    val isSelfChat: Boolean = false,
    val error: String? = null
)