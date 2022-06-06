package ru.rtuitlab.itlab.presentation.screens.feedback

import androidx.compose.animation.*
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import ru.rtuitlab.itlab.data.remote.api.feedback.models.FeedbackModel
import ru.rtuitlab.itlab.presentation.screens.feedback.components.FeedbackCard
import ru.rtuitlab.itlab.presentation.ui.components.LoadingError
import ru.rtuitlab.itlab.presentation.ui.extensions.TransitionState
import ru.rtuitlab.itlab.presentation.ui.extensions.transitionState
import ru.rtuitlab.itlab.presentation.utils.singletonViewModel

@ExperimentalTransitionApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@Composable
fun Feedback(feedbackViewModel: FeedbackViewModel = singletonViewModel()) {

	val incomingFeedbackResource by feedbackViewModel.incomingFeedbackResponsesFlow.collectAsState()
	val readFeedbackResource by feedbackViewModel.readFeedbackResponsesFlow.collectAsState()

	var isRefreshing by remember { mutableStateOf(false) }

	val pagerState = feedbackViewModel.pagerState
	val tabs = listOf(
		ru.rtuitlab.itlab.presentation.utils.FeedbackTab.Incoming,
		ru.rtuitlab.itlab.presentation.utils.FeedbackTab.Read
	)

	Scaffold(
		scaffoldState = rememberScaffoldState(snackbarHostState = feedbackViewModel.snackbarHostState)
	) {
		HorizontalPager(
			modifier = Modifier.fillMaxSize(),
			verticalAlignment = Alignment.Top,
			count = tabs.size,
			state = pagerState,
			itemSpacing = 1.dp // Causes pager to lazy-load pages. Why? Who knows.
		) { index ->
			SwipeRefresh(
				modifier = Modifier
					.fillMaxSize(),
				state = rememberSwipeRefreshState(isRefreshing),
				onRefresh = feedbackViewModel::onRefresh
			) {
				when (tabs[index]) {
					ru.rtuitlab.itlab.presentation.utils.FeedbackTab.Incoming -> {
						incomingFeedbackResource.handle(
							onLoading = {
								isRefreshing = true
							},
							onError = { msg ->
								isRefreshing = false
								LoadingError(msg = msg)
							},
							onSuccess = {
								isRefreshing = false
								feedbackViewModel.onResourceSuccess(it, false)
								IncomingFeedbackList(feedbackViewModel)
							}
						)
					}
					ru.rtuitlab.itlab.presentation.utils.FeedbackTab.Read -> {
						readFeedbackResource.handle(
							onLoading = {
								isRefreshing = true
							},
							onError = { msg ->
								isRefreshing = false
								LoadingError(msg = msg)
							},
							onSuccess = {
								isRefreshing = false
								feedbackViewModel.onResourceSuccess(it, true)
								ReadFeedbackList(feedbackViewModel)
							}
						)
					}
				}
			}
		}
	}
}

@ExperimentalTransitionApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@Composable
fun IncomingFeedbackList(feedbackViewModel: FeedbackViewModel) {

	val feedback by feedbackViewModel.incomingFeedbackFlow.collectAsState()

	AnimatedFeedbackList(
		feedback = feedback,
		feedbackViewModel = feedbackViewModel,
		enterTransition = slideInVertically(
			initialOffsetY = { it }
		) + fadeIn(),
		exitTransition = slideOutHorizontally(
			targetOffsetX = { it + 100 },
			animationSpec = tween(
				durationMillis = 400
			)
		)
	)

}

@ExperimentalTransitionApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@Composable
fun ReadFeedbackList(feedbackViewModel: FeedbackViewModel) {

	val feedback by feedbackViewModel.readFeedbackFlow.collectAsState()

	AnimatedFeedbackList(
		feedback = feedback,
		feedbackViewModel = feedbackViewModel,
		enterTransition = slideInVertically(
			initialOffsetY = { it }
		) + fadeIn(),
		exitTransition = slideOutHorizontally(
			targetOffsetX = { -it - 100 },
			animationSpec = tween(
				durationMillis = 400
			)
		)
	)

}

@ExperimentalTransitionApi
@ExperimentalAnimationApi
@ExperimentalPagerApi
@Composable
fun AnimatedFeedbackList(
	feedback: List<FeedbackModel>,
	feedbackViewModel: FeedbackViewModel,
	enterTransition: EnterTransition,
	exitTransition: ExitTransition
) {
	val lazyListState = rememberLazyListState()

	LazyColumn(
		modifier = Modifier.fillMaxSize(),
		state = lazyListState,
		verticalArrangement = Arrangement.spacedBy(10.dp),
		contentPadding = PaddingValues(horizontal = 15.dp, vertical = 15.dp)
	) {
		items(
			items = feedback,
			key = { it.id }
		) { feedback ->
			val animationState by remember {
				mutableStateOf(MutableTransitionState(false))
			}
			LaunchedEffect(Unit) {
				animationState.targetState = true
			}

			var lastTransitionState by remember {
				mutableStateOf(TransitionState.Invisible)
			}

			when (
				animationState.transitionState(
					lastTransitionState,
					onUpdate = {
						lastTransitionState = it
					}
				)
			) {
				TransitionState.ArrivedToInvisible -> feedbackViewModel.delete(feedback)
				else -> {}
			}
			AnimatedVisibility(
				visibleState = animationState,
				enter = enterTransition,
				exit = exitTransition
			) {
				FeedbackCard(
					feedback = feedback,
					feedbackViewModel = feedbackViewModel,
					animationState = animationState
				)
			}
		}
	}
}