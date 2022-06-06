package ru.rtuitlab.itlab.presentation.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun LabelledRadioButton(
	state: Boolean,
	onCheckedChange: ((Boolean) -> Unit),
	label: String,
	modifier: Modifier = Modifier
) {
	Row(
		verticalAlignment = Alignment.CenterVertically,
		modifier = modifier
			.clickable(
				indication = rememberRipple(color = MaterialTheme.colors.onSurface),
				interactionSource = remember { MutableInteractionSource() },
				onClick = { onCheckedChange(!state) }
			)
			.padding(horizontal = 16.dp, vertical = 8.dp)
	) {

		RadioButton(
			selected = state,
			onClick = {onCheckedChange(!state)},
			colors = RadioButtonDefaults.colors(
				selectedColor = Color.White
			)
		)

		Spacer(Modifier.width(6.dp))

		Text(
			text = label,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)


	}
}