package dev.android.simplify.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun UserAvatar(
    photoUrl: String,
    displayName: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    if (photoUrl.isNotEmpty()) {
        // Если есть URL фото, загружаем изображение
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(photoUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Аватар $displayName",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
        )
    } else {
        // Если нет фото, создаем плейсхолдер с инициалами
        val initials = displayName
            .split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.toString() }
            .joinToString("")
            .uppercase()
            .take(2)

        // Генерируем цвет на основе имени
        val backgroundColor = generateColorFromName(displayName)

        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor)
        ) {
            Text(
                text = initials,
                fontSize = (size.value * 0.4).sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Генерирует цвет на основе строки имени
 */
private fun generateColorFromName(name: String): Color {
    // Использование хэша имени для создания стабильного цвета
    val hash = name.hashCode()
    val hue = ((hash % 360) + 360) % 360

    // Преобразуем HSV в RGB и создаем цвет
    return Color.hsl(
        hue = hue.toFloat(),
        saturation = 0.7f,
        lightness = 0.4f
    )
}