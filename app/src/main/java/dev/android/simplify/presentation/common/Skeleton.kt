package dev.android.simplify.presentation.common

import android.annotation.SuppressLint
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Модификатор, добавляющий эффект мерцания для скелетона
 */
@SuppressLint("SuspiciousModifierThen")
fun Modifier.shimmerEffect() = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer effect"
    )

    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(0f, 0f),
        end = Offset(300f, 300f)
    )

    this.then(background(brush))
}

/**
 * Скелетон сообщения в чате
 */
@Composable
fun MessageSkeleton(
    isFromCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = if (isFromCurrentUser) Alignment.End else Alignment.Start,
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .width(if (isFromCurrentUser) 200.dp else 180.dp)
                .height(48.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                        bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
                    )
                )
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Время сообщения
        Box(
            modifier = Modifier
                .align(if (isFromCurrentUser) Alignment.End else Alignment.Start)
                .width(40.dp)
                .height(12.dp)
                .padding(horizontal = 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerEffect()
        )
    }
}

/**
 * Скелетон загрузки списка сообщений
 */
@Composable
fun ChatSkeletonLoader(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        // Генерируем несколько скелетонов сообщений с чередующимся направлением
        items(5) { index ->
            MessageSkeleton(
                isFromCurrentUser = index % 2 == 0,
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )
        }

        // Добавим "заглушку" аватара собеседника
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Аватар
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Имя и "печатает..."
                Column {
                    // Имя
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // "Печатает..."
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerEffect()
                    )
                }
            }
        }
    }
}

/**
 * Скелетон элемента списка чатов
 */
@Composable
fun ChatListItemSkeleton(
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Аватар пользователя
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Информация о чате (имя пользователя и последнее сообщение)
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Имя пользователя
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Последнее сообщение
            Box(
                modifier = Modifier
                    .width(200.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            // Время
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Количество непрочитанных (круглый бейдж)
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
            )
        }
    }
}

/**
 * Скелетон списка чатов
 */
@Composable
fun ChatListSkeletonLoader(
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        // Генерируем несколько скелетонов элементов списка чатов
        items(6) {
            ChatListItemSkeleton()
            HorizontalDivider()
        }
    }
}