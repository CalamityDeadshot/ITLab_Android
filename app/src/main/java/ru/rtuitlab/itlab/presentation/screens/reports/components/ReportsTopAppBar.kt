package ru.rtuitlab.itlab.presentation.screens.reports.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import ru.rtuitlab.itlab.R
import ru.rtuitlab.itlab.presentation.screens.reports.ReportsViewModel
import ru.rtuitlab.itlab.presentation.ui.components.SearchBar
import ru.rtuitlab.itlab.presentation.ui.components.top_app_bars.AppBarOption
import ru.rtuitlab.itlab.presentation.ui.components.top_app_bars.ExtendedTopAppBarBody
import kotlin.math.ln


/**
 * This TopAppBar does not have any elevation and takes default surface color
 * because its tabs belong to separate composable due to its use-case with shared elements.
 */
@ExperimentalPagerApi
@Composable
fun ReportsTopAppBar(
	reportsViewModel: ReportsViewModel = viewModel()
) {
	var searchActivated by rememberSaveable { mutableStateOf(false) }

	if (searchActivated)
		BackHandler {
			searchActivated = false
		}

	Surface(
		color = calculateForegroundColor(backgroundColor = MaterialTheme.colors.surface, elevation = AppBarDefaults.TopAppBarElevation),
		contentColor = contentColorFor(MaterialTheme.colors.primarySurface),
		elevation = 0.dp,
		shape = RectangleShape
	) {
		Box(
			modifier = Modifier
				.height(56.dp)
				.padding(horizontal = 4.dp),
			contentAlignment = Alignment.Center
		) {

			ExtendedTopAppBarBody(
				options = listOf(
					/*AppBarOption.Clickable(
						icon = Icons.Default.Search,
						onClick = {
							searchActivated = true
						}
					)*/
				),
				hideBackButton = !searchActivated,
				hideOptions = searchActivated,
				onBackAction = {
					searchActivated = false
					reportsViewModel.onSearch("")
				}
			) {
				if (searchActivated) {
					SearchBar(
						onSearch = reportsViewModel::onSearch
					)
				} else {
					Text(
						text = stringResource(R.string.reports),
						fontSize = 20.sp,
						fontWeight = FontWeight(500),
						textAlign = TextAlign.Start,
						color = MaterialTheme.colors.onSurface
					)
				}

			}
		}
	}

}


/**
 * @return the alpha-modified foreground color to overlay on top of the surface color to produce
 * the resultant color. This color is the [contentColorFor] the [backgroundColor], with alpha
 * applied depending on the value of [elevation].
 */
@ReadOnlyComposable
@Composable
private fun calculateForegroundColor(backgroundColor: Color, elevation: Dp): Color {
	val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
	val baseForegroundColor = contentColorFor(backgroundColor)
	return baseForegroundColor.copy(alpha = alpha)
}