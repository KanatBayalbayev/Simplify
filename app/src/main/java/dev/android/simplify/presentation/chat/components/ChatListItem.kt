package dev.android.simplify.presentation.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.android.simplify.domain.model.ChatWithUser
import dev.android.simplify.presentation.common.UserAvatar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatListItem(
    chatWithUser: ChatWithUser,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Аватар пользователя с индикатором онлайн-статуса
        Box(
            contentAlignment = Alignment.BottomEnd
        ) {
            UserAvatar(
                photoUrl = chatWithUser.user.photoUrl,
                displayName = chatWithUser.user.displayName,
                size = 56.dp
            )

            // Индикатор онлайн-статуса
            if (chatWithUser.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(Color.Green)
                        .padding(2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Информация о чате
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Имя пользователя
            Text(
                text = chatWithUser.user.displayName.ifEmpty { chatWithUser.user.email },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Последнее сообщение
            Text(
                text = if (chatWithUser.lastMessageSentByMe) "Вы: ${chatWithUser.lastMessageText}" else chatWithUser.lastMessageText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (chatWithUser.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            // Время последнего сообщения
            Text(
                text = formatTime(chatWithUser.lastMessageTime),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.padding(top = 4.dp))

            // Количество непрочитанных сообщений
            if (chatWithUser.unreadCount > 0) {
                Badge {
                    Text(
                        text = chatWithUser.unreadCount.toString(),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

private fun formatTime(date: Date): String {
    val calendar = java.util.Calendar.getInstance()
    val today = java.util.Calendar.getInstance()
    calendar.time = date

    return when {
        // Сегодня - показываем только время
        calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                calendar.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR) -> {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }
        // Вчера
        calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                calendar.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR) - 1 -> {
            "Вчера"
        }
        // В этом году - показываем день и месяц
        calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) -> {
            SimpleDateFormat("d MMM", Locale.getDefault()).format(date)
        }
        // В других случаях - полная дата
        else -> {
            SimpleDateFormat("dd.MM.yy", Locale.getDefault()).format(date)
        }
    }
}