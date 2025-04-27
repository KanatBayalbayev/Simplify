package dev.android.simplify.presentation.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.android.simplify.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserSearchBottomSheet(
    onDismiss: () -> Unit,
    onUserSelected: (User) -> Unit,
    searchUsers: (String) -> Unit,
    userSearchResults: List<User>,
    isLoading: Boolean,
    sheetState: SheetState = rememberModalBottomSheetState(),
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Заголовок
            Text(
                text = "Поиск пользователей",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Поле поиска
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    if (it.length >= 3) {
                        searchUsers(it)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Введите email для поиска") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Поиск") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Очистить")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Результаты поиска
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (searchQuery.length >= 3 && userSearchResults.isEmpty()) {
                Text(
                    text = "Пользователи не найдены",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else if (searchQuery.isNotEmpty() && searchQuery.length < 3) {
                Text(
                    text = "Введите не менее 3 символов для поиска",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn {
                    items(userSearchResults) { user ->
                        UserSearchItem(
                            user = user,
                            onAddClick = { onUserSelected(user) }
                        )
                        Divider()
                    }
                }
            }

            // Добавляем отступ внизу для лучшего вида
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun UserSearchItem(
    user: User,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Аватар пользователя
        UserAvatar(
            photoUrl = user.photoUrl,
            displayName = user.displayName,
            size = 48.dp
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Информация о пользователе
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.displayName.ifEmpty { "Пользователь" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        // Кнопка добавления
        IconButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Добавить пользователя",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}