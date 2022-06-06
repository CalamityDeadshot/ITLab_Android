package ru.rtuitlab.itlab.presentation.screens.events

import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import com.google.accompanist.pager.ExperimentalPagerApi
import ru.rtuitlab.itlab.R
import ru.rtuitlab.itlab.domain.model.EventDetail
import ru.rtuitlab.itlab.presentation.screens.events.components.ShiftCard
import ru.rtuitlab.itlab.presentation.ui.components.IconizedRow
import ru.rtuitlab.itlab.presentation.ui.components.InteractiveField
import ru.rtuitlab.itlab.presentation.ui.components.LoadingError
import ru.rtuitlab.itlab.presentation.ui.components.LoadingIndicator
import ru.rtuitlab.itlab.presentation.ui.components.bottom_sheet.BottomSheetViewModel
import ru.rtuitlab.itlab.presentation.ui.components.top_app_bars.AppBarViewModel
import ru.rtuitlab.itlab.presentation.ui.components.top_app_bars.CollapsibleScrollArea
import ru.rtuitlab.itlab.presentation.ui.components.top_app_bars.SwipingStates
import ru.rtuitlab.itlab.presentation.ui.extensions.fromIso8601
import ru.rtuitlab.itlab.presentation.utils.AppBottomSheet
import ru.rtuitlab.itlab.presentation.utils.AppScreen
import ru.rtuitlab.itlab.presentation.utils.singletonViewModel

@ExperimentalPagerApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Event(
	eventViewModel: EventViewModel,
	bottomSheetViewModel: BottomSheetViewModel = singletonViewModel(),
	appBarViewModel: AppBarViewModel = singletonViewModel()
) {

	val eventResource by eventViewModel.eventResourceFlow.collectAsState()

	Scaffold(
		scaffoldState = rememberScaffoldState(snackbarHostState = eventViewModel.snackbarHostState)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
		) {
			eventResource.handle(
				onLoading = {
					LoadingIndicator()
				},
				onError = { msg ->
					LoadingError(msg = msg)
				},
				onSuccess = {
					LaunchedEffect(null) {
						if (appBarViewModel.currentScreen.value is AppScreen.EventDetails)
							appBarViewModel.onNavigate(
								AppScreen.EventDetails(it.first.title)
							)
					}
					EventInfoWithList(
						event = it.first.toEvent(it.second),
						eventViewModel = eventViewModel,
						bottomSheetViewModel = bottomSheetViewModel
					)
				}
			)
		}
	}
}

@OptIn(ExperimentalMotionApi::class, ExperimentalMaterialApi::class)
@Composable
private fun EventInfoWithList(
	event: EventDetail,
	eventViewModel: EventViewModel,
	bottomSheetViewModel: BottomSheetViewModel
) {
	val swipingState = rememberSwipeableState(SwipingStates.EXPANDED)

	MotionLayout(
		start = startConstraintSet(),
		end = endConstraintSet(),
		progress = if (swipingState.progress.to == SwipingStates.COLLAPSED) swipingState.progress.fraction else 1f - swipingState.progress.fraction
	) {
		var height by remember { mutableStateOf(200) }

		val density = LocalDensity.current
		EventInfo(
			event = event,
			modifier = Modifier
				.layoutId("info")
				.onGloballyPositioned {
					height = with(density) { it.size.height.toDp().value.toInt() }
				},
			bottomSheetViewModel = bottomSheetViewModel
		)
		CollapsibleScrollArea(
			swipingState = swipingState,
			heightDelta = height,
			modifier = Modifier
				.layoutId("list")
		) {
			EventShifts(
				event = event,
				eventViewModel = eventViewModel,
				bottomSheetViewModel = bottomSheetViewModel
			)
		}
	}
}

@Composable
private fun startConstraintSet() = ConstraintSet {
	val info = createRefFor("info")
	val list = createRefFor("list")
	constrain(info) {
		start.linkTo(parent.start)
		top.linkTo(parent.top)
		end.linkTo(parent.end)
		pivotY = 1f
	}

	constrain(list) {
		top.linkTo(info.bottom)
	}
}

@SuppressLint("Range")
@Composable
private fun endConstraintSet() = ConstraintSet {
	val info = createRefFor("info")
	val list = createRefFor("list")
	constrain(info) {
		start.linkTo(parent.start)
		top.linkTo(parent.top)
		end.linkTo(parent.end)
		pivotY = -.4f
		scaleX = .7f
		scaleY = .7f
		alpha = 0f
	}

	constrain(list) {
		top.linkTo(parent.top)
		bottom.linkTo(parent.bottom)
	}
}

@ExperimentalMaterialApi
@Composable
private fun EventInfo(
	event: EventDetail,
	modifier: Modifier = Modifier,
	bottomSheetViewModel: BottomSheetViewModel
) {
	val context = LocalContext.current
	val coroutineScope = rememberCoroutineScope()
	Surface(
		modifier = modifier
			.fillMaxWidth(),
		color = MaterialTheme.colors.surface,
		elevation = 1.dp
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(20.dp),
			verticalArrangement = Arrangement.spacedBy(10.dp)
		) {
			IconizedRow(
				imageVector = Icons.Default.Schedule,
				opacity = .7f,
				spacing = 10.dp
			) {
				Text(
					text = "${
						event.beginTime.fromIso8601(
							context,
							","
						)
					} — ${event.endTime.fromIso8601(context, ",")}"
				)
			}

			IconizedRow(
				imageVector = Icons.Default.Payment,
				opacity = .7f,
				spacing = 10.dp
			) {
				Text(
					text = if (event.salary != null) stringResource(
						R.string.salary,
						event.salary
					) else stringResource(R.string.salary_not_specified)
				)
			}

			IconizedRow(
				imageVector = Icons.Default.LocationOn,
				opacity = .7f,
				spacing = 10.dp
			) {
				Text(
					text = event.address
				)
			}

			IconizedRow(
				imageVector = Icons.Default.People,
				opacity = .7f,
				spacing = 10.dp
			) {
				Text(
					text = "${event.currentParticipationCount}/${event.targetParticipationCount}"
				)
			}

			IconizedRow(
				imageVector = Icons.Default.Info,
				opacity = .7f,
				spacing = 10.dp
			) {
				Row(
					modifier = Modifier
						.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					Text(
						text = event.type
					)

					InteractiveField(
						value = stringResource(R.string.more),
						hasArrow = true
					) {
						bottomSheetViewModel.show(
							sheet = AppBottomSheet.EventDescription(event.description),
							scope = coroutineScope
						)
					}
				}
			}


		}
	}
}

@ExperimentalMaterialApi
@Composable
private fun EventShifts(
	event: EventDetail,
	eventViewModel: EventViewModel,
	bottomSheetViewModel: BottomSheetViewModel
) {
	val coroutineScope = rememberCoroutineScope()
	Column(
		modifier = Modifier
			.fillMaxSize()
	) {
		LazyColumn(
			modifier = Modifier
				.fillMaxWidth(),
			verticalArrangement = Arrangement.spacedBy(10.dp),
			contentPadding = PaddingValues(bottom = 15.dp, start = 20.dp, end = 20.dp)
		) {
			item {
				Spacer(modifier = Modifier.height(15.dp))
				Text(
					text = stringResource(R.string.shifts),
					style = MaterialTheme.typography.h3
				)
			}
			items(
				items = event.shifts.sortedBy { it.beginTime },
				key = { it.id }
			) { shift ->
				ShiftCard(
					modifier = Modifier
						.fillMaxWidth()
						.clickable {
							bottomSheetViewModel.show(
								AppBottomSheet.EventShift(
									shift,
									salaries = shift.places.mapIndexed { index, _ ->
										event.placeSalaries.getOrElse(index) {
											event.shiftSalaries.getOrElse(event.shifts.indexOf(shift)) {
												event.salary ?: -1
											}
										}
									},
									eventViewModel = eventViewModel
								),
								coroutineScope
							)
						},
					shift = shift,
					salary = event.shiftSalaries.getOrElse(event.shifts.indexOf(shift)) { event.salary }
				)
			}
		}
	}
}
