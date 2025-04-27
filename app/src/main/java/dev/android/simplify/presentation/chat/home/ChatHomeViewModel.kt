package dev.android.simplify.presentation.chat.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.android.simplify.domain.model.AuthResult
import dev.android.simplify.domain.model.User
import dev.android.simplify.domain.usecase.auth.GetCurrentUserUseCase
import dev.android.simplify.domain.usecase.auth.SignOutUseCase
import dev.android.simplify.domain.usecase.chat.CreateChatUseCase
import dev.android.simplify.domain.usecase.chat.GetUserChatsUseCase
import dev.android.simplify.domain.usecase.chat.SetUserStatusUseCase
import dev.android.simplify.domain.usecase.user.SearchUsersByEmailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatHomeViewModel(
    private val getUserChatsUseCase: GetUserChatsUseCase,
    private val searchUsersByEmailUseCase: SearchUsersByEmailUseCase,
    private val createChatUseCase: CreateChatUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val setUserStatusUseCase: SetUserStatusUseCase,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatHomeUiState())
    val uiState: StateFlow<ChatHomeUiState> = _uiState.asStateFlow()

    // Получаем текущего пользователя
    val currentUser = getCurrentUserUseCase()
        .map { authResult ->
            when (authResult) {
                is AuthResult.Success -> authResult.data
                else -> null
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Получаем список чатов
    val chats = getUserChatsUseCase()
        .catch { error ->
            _uiState.update { it.copy(error = error.message ?: "Не удалось загрузить чаты") }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // При запуске устанавливаем статус "онлайн"
        viewModelScope.launch {
            currentUser.value?.id?.let { userId ->
                setUserStatusUseCase(userId, true)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchUsers(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.length < 3) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }

        _uiState.update { it.copy(isSearching = true, searchResults = emptyList()) }

        viewModelScope.launch {
            try {
                val results = searchUsersByEmailUseCase(trimmedQuery)
                _uiState.update { it.copy(isSearching = false, searchResults = results) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        error = e.message ?: "Ошибка при поиске пользователей"
                    )
                }
            }
        }
    }

    fun createChat(otherUser: User) {
        viewModelScope.launch {
            try {
                val currentUserId = currentUser.value?.id ?: return@launch
                _uiState.update { it.copy(isCreatingChat = true) }

                // Создаем новый чат или получаем существующий
                val chatId = createChatUseCase(currentUserId, otherUser.id)

                _uiState.update {
                    it.copy(
                        isCreatingChat = false,
                        isSearchSheetOpen = false, // Закрываем лист поиска
                        createdChatId = chatId
                    )
                }

                // Сбрасываем createdChatId через некоторое время, чтобы избежать повторной навигации
                launch {
                    kotlinx.coroutines.delay(100)
                    _uiState.update { it.copy(createdChatId = null) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCreatingChat = false,
                        error = e.message ?: "Ошибка при создании чата"
                    )
                }
            }
        }
    }

    fun openSearchSheet() {
        _uiState.update { it.copy(isSearchSheetOpen = true) }
    }

    fun closeSearchSheet() {
        _uiState.update {
            it.copy(
                isSearchSheetOpen = false,
                searchQuery = "",
                searchResults = emptyList()
            )
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                // Устанавливаем статус "оффлайн" при выходе
                currentUser.value?.id?.let { userId ->
                    setUserStatusUseCase(userId, false)
                }

                signOutUseCase()

                _uiState.update { it.copy(isSignedOut = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Ошибка при выходе из аккаунта") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Устанавливаем статус "оффлайн" при закрытии ViewModel
        viewModelScope.launch {
            currentUser.value?.id?.let { userId ->
                setUserStatusUseCase(userId, false)
            }
        }
    }
}

data class ChatHomeUiState(
    val searchQuery: String = "",
    val searchResults: List<User> = emptyList(),
    val isSearching: Boolean = false,
    val isCreatingChat: Boolean = false,
    val isSearchSheetOpen: Boolean = false,
    val createdChatId: String? = null,
    val isSignedOut: Boolean = false,
    val error: String? = null
)