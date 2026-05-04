package com.underthykilt.poketypes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.underthykilt.poketypes.data.PokemonType

@Composable
fun TypeBadge(type: PokemonType, large: Boolean = false) {
    Box(
        modifier = Modifier
            .background(type.color, MaterialTheme.shapes.medium)
            .padding(
                horizontal = if (large) 20.dp else 8.dp,
                vertical = if (large) 10.dp else 4.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            type.displayName,
            color = Color.White,
            fontSize = if (large) 20.sp else 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
