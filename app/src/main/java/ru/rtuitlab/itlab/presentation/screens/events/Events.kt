package ru.rtuitlab.itlab.presentation.screens.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import ru.rtuitlab.itlab.R
import ru.rtuitlab.itlab.presentation.navigation.LocalNavController
import ru.rtuitlab.itlab.presentation.screens.events.components.EventCard
import ru.rtuitlab.itlab.presentation.screens.events.components.UserEventCard
import ru.rtuitlab.itlab.presentation.ui.components.LoadingError
import ru.rtuitlab.itlab.presentation.ui.components.top_app_bars.CollapsibleScrollArea
import ru.rtuitlab.itlab.presentation.ui.components.top_app_bars.heightDelta
import ru.rtuitlab.itlab.presentation.utils.AppScreen
import ru.rtuitlab.itlab.presentation.utils.EventTab
import ru.rtuitlab.itlab.presentation.utils.singletonViewModel

@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun Events(
	eventsViewModel: EventsViewModel = singletonViewModel()
) {
	val eventsResource by eventsViewModel.eventsListResponsesFlow.collectAsState()
	val userEventsResource by eventsViewModel.userEventsListResponsesFlow.collectAsState()
	val pastEventsResource by eventsViewModel.pastEventsListResponseFlow.collectAsState()
	val events by eventsViewModel.eventsFlow.collectAsState()
	val pastEvents by eventsViewModel.pastEventsFlow.collectAsState()
	val showPastEvents by eventsViewModel.showPastEvents.collectAsState()

	var isRefreshing by remember { mutableStateOf(false) }
	var secondPageVisited by rememberSaveable { mutableStateOf(false) }

	val pagerState = eventsViewModel.pagerState
	val swipingState = eventsViewModel.swipingState
	val tabs = listOf(EventTab.All, EventTab.My)

	LaunchedEffect(pagerState) {
		snapshotFlow { pagerState.currentPage }.collect { page ->
			if (secondPageVisited) cancel()
			if (page == 1 && !secondPageVisited) {
				secondPageVisited = true
				eventsViewModel.fetchUserEvents()
			}
		}

	}

	HorizontalPager(
		modifier = Modifier
			.fillMaxSize(),
		verticalAlignment = Alignment.Top,
		count = tabs.size,
		state = pagerState,
		itemSpacing = 1.dp
	) { index ->
		SwipeRefresh(
			modifier = Modifier.fillMaxSize(),
			state = rememberSwipeRefreshState(isRefreshing),
			onRefresh = {
				eventsViewModel.fetchPendingEvents()
				eventsViewModel.fetchInvitations()
			}
		) {
			CollapsibleScrollArea(
				swipingState = swipingState,
				heightDelta = heightDelta
			) {
				when (tabs[index]) {
					EventTab.All -> {
						eventsResource.handle(
							onLoading = {
								isRefreshing = true
							},
							onError = { msg ->
								isRefreshing = false
								LoadingError(msg = msg)
							},
							onSuccess = {
								isRefreshing = false
								eventsViewModel.onResourceSuccess(it)

								EventsList(
									eventsViewModel = eventsViewModel,
									listState = eventsViewModel.allEventsListState
								)
							}
						)
						pastEventsResource.handle(
							onLoading = { isRefreshing = true },
							onSuccess = {
								isRefreshing = false
								eventsViewModel.onPastResourceSuccess(it)
							}
						)
					}
					EventTab.My -> {
						userEventsResource.handle(
							onLoading = {
								isRefreshing = true
							},
							onError = { msg ->
								isRefreshing = false
								LoadingError(msg = msg)
							},
							onSuccess = {
								isRefreshing = false
								eventsViewModel.onUserResourceSuccess(it)
								if (it.isEmpty())
									LoadingError(msg = stringResource(R.string.no_user_events))
								else
									UserEventsList(
										eventsViewModel = eventsViewModel,
										listState = eventsViewModel.userEventsListState
									)
							}
						)
					}
				}
			}

		}
	}


}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun EventsList(
	eventsViewModel: EventsViewModel,
	listState: LazyListState
) {
	val events by eventsViewModel.eventsFlow.collectAsState()
	val pastEvents by eventsViewModel.pastEventsFlow.collectAsState()
	val showPastEvents by eventsViewModel.showPastEvents.collectAsState()

	val navController = LocalNavController.current
	if (events.isEmpty() && (pastEvents.isEmpty() || !showPastEvents))
		LoadingError(msg = stringResource(R.string.no_pending_events))
	else
		LazyColumn(
			modifier = Modifier.fillMaxSize(),
			state = listState,
			verticalArrangement = Arrangement.spacedBy(10.dp),
			contentPadding = PaddingValues(horizontal = 15.dp, vertical = 15.dp)
		) {
			items(
				items = events.sortedByDescending { it.beginTime },
				key = { it.id }
			) {
				EventCard(
					modifier = Modifier.clickable {
						navController.navigate("${AppScreen.EventDetails.navLink}/${it.id}")
					},
					event = it
				)
			}
			if (showPastEvents) {
				if (events.isNotEmpty())
					item {
						Spacer(modifier = Modifier.height(16.dp))
					}
				items(
					items = pastEvents.sortedByDescending { it.beginTime },
					key = { it.id }
				) {
					EventCard(
						modifier = Modifier.clickable {
							navController.navigate("${AppScreen.EventDetails.navLink}/${it.id}")
						},
						event = it
					)
				}
			}
		}
}

@ExperimentalPagerApi
@ExperimentalMaterialApi
@Composable
fun UserEventsList(
	eventsViewModel: EventsViewModel,
	listState: LazyListState
) {
	val events by eventsViewModel.userEventsFlow.collectAsState()
	val navController = LocalNavController.current
	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		state = listState,
		verticalArrangement = Arrangement.spacedBy(10.dp),
		contentPadding = PaddingValues(horizontal = 15.dp, vertical = 15.dp)
	) {
		items(
			items = events.sortedByDescending { it.beginTime },
			key = { it.id }
		) {
			UserEventCard(
				modifier = Modifier.clickable {
					navController.navigate("${AppScreen.EventDetails.navLink}/${it.id}")
				},
				event = it
			)
		}
	}
}