package com.fivucsas.shared.ui.components.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fivucsas.shared.ui.theme.AppColors
import com.fivucsas.shared.ui.theme.AppTypography

data class FilterChipItem(
    val label: String,
    val value: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterChipRow(
    items: List<FilterChipItem>,
    selectedValue: String,
    onSelected: (FilterChipItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items) { item ->
            FilterChip(
                selected = item.value == selectedValue,
                onClick = { onSelected(item) },
                label = {
                    Text(
                        text = item.label,
                        style = AppTypography.LabelMedium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AppColors.Primary.copy(alpha = 0.12f),
                    selectedLabelColor = AppColors.Primary,
                    containerColor = AppColors.Surface,
                    labelColor = AppColors.OnSurfaceVariant
                ),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}
