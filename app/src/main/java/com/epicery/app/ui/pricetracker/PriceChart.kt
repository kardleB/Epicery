package com.epicery.app.ui.pricetracker

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.epicery.app.domain.model.PriceHistory
import com.epicery.app.ui.theme.EpiceryTheme

/**
 * Gráfico de línea simple del histórico de precios de un artículo (ver
 * `docs/design/wireframes.md`, sección "3. Price Tracker"). [priceHistory] debe venir ordenado
 * cronológicamente; el color de la línea refleja la [trend] calculada por el ViewModel.
 */
@Composable
fun PriceChart(
    priceHistory: List<PriceHistory>,
    trend: PriceTrend,
    modifier: Modifier = Modifier
) {
    val lineColor = when (trend) {
        PriceTrend.UP -> MaterialTheme.colorScheme.error
        PriceTrend.DOWN -> MaterialTheme.colorScheme.primary
        PriceTrend.STABLE -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        if (priceHistory.size < 2) return@Canvas

        val prices = priceHistory.map { it.price }
        val minPrice = prices.min()
        val maxPrice = prices.max()
        val priceRange = (maxPrice - minPrice).takeIf { it > 0.0 } ?: 1.0
        val stepX = size.width / (priceHistory.size - 1)
        val pointRadius = 4.dp.toPx()

        val points = priceHistory.mapIndexed { index, entry ->
            val x = index * stepX
            val y = size.height - ((entry.price - minPrice) / priceRange * size.height).toFloat()
            Offset(x, y)
        }

        val linePath = Path().apply {
            points.forEachIndexed { index, point ->
                if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
            }
        }
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
        points.forEach { point ->
            drawCircle(color = lineColor, radius = pointRadius, center = point)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PriceChartPreview() {
    EpiceryTheme {
        PriceChart(
            priceHistory = listOf(
                PriceHistory(foodItemId = 1, storeName = "Metro", price = 3.5, recordedAt = 1L),
                PriceHistory(foodItemId = 1, storeName = "Metro", price = 3.8, recordedAt = 2L),
                PriceHistory(foodItemId = 1, storeName = "Metro", price = 4.2, recordedAt = 3L)
            ),
            trend = PriceTrend.UP
        )
    }
}
