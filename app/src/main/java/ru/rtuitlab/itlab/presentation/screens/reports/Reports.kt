
package ru.rtuitlab.itlab.presentation.screens.reports

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import ru.rtuitlab.itlab.R
import ru.rtuitlab.itlab.data.remote.api.reports.models.Report
import ru.rtuitlab.itlab.presentation.navigation.LocalNavController
import ru.rtuitlab.itlab.presentation.screens.micro_file_service.MFSViewModel
import ru.rtuitlab.itlab.presentation.screens.micro_file_service.components.BaseElements
import ru.rtuitlab.itlab.presentation.screens.reports.components.NewReportFab
import ru.rtuitlab.itlab.presentation.ui.components.IconizedRow
import ru.rtuitlab.itlab.presentation.ui.components.LoadingError
import ru.rtuitlab.itlab.presentation.ui.components.UserLink
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.SharedElement
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.utils.SharedElementsTransitionSpec
import ru.rtuitlab.itlab.presentation.ui.components.top_app_bars.AppBarTabRow
import ru.rtuitlab.itlab.presentation.ui.extensions.fromIso8601
import ru.rtuitlab.itlab.presentation.ui.theme.AppColors
import ru.rtuitlab.itlab.presentation.utils.AppScreen
import ru.rtuitlab.itlab.presentation.utils.ReportsTab
import ru.rtuitlab.itlab.presentation.utils.singletonViewModel

val duration = 300
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalPagerApi
@Composable
fun Reports(
	mfsViewModel: MFSViewModel = singletonViewModel(),
	reportsViewModel: ReportsViewModel = singletonViewModel()
) {
	val reportsResource by reportsViewModel.reportsResponseFlow.collectAsState()

	var isRefreshing by remember { mutableStateOf(false) }
	val userId by reportsViewModel.userIdFlow.collectAsState()

	val tabs = listOf(
		ReportsTab.AboutUser,
		ReportsTab.FromUser,
		ReportsTab.Files
	)
	Column {
		Surface(
			color = MaterialTheme.colors.primarySurface,
			contentColor = contentColorFor(MaterialTheme.colors.primarySurface),
			elevation = AppBarDefaults.TopAppBarElevation
		) {
			AppBarTabRow(
				pagerState = reportsViewModel.pagerState,
				tabs = tabs,
				isScrollable = true
			)
		}

		SwipeRefresh(
			modifier = Modifier.fillMaxSize(),
			state = rememberSwipeRefreshState(isRefreshing),
			onRefresh = reportsViewModel::fetchReports
		) {
			val (transitionProgress, transitionProgressSetter) = remember { mutableStateOf(0f) }
			Scaffold(
				floatingActionButton = {
					if (!isRefreshing) NewReportFab(transitionProgressSetter)
				},
				scaffoldState = rememberScaffoldState(snackbarHostState = reportsViewModel.snackbarHostState)
			) {
				HorizontalPager(
					modifier = Modifier.fillMaxSize(),
					verticalAlignment = Alignment.Top,
					count = tabs.size,
					state = reportsViewModel.pagerState
				) { index ->
					reportsResource.handle(
						onLoading = {
							isRefreshing = true
						},
						onError = {
							isRefreshing = false
							LoadingError(msg = it)
						},
						onSuccess = { reports ->
							isRefreshing = false
							Box {
								when(tabs[index]) {
									ReportsTab.AboutUser -> ReportsList(
										reports
											.filter { it.implementer.id == userId }
											.sortedByDescending { it.id }
									)
									ReportsTab.FromUser -> ReportsList(
										reports
											.filter { it.applicant.id == userId }
											.sortedByDescending { it.id }
									)
									ReportsTab.Files -> {
										BaseElements(mfsViewModel)
									}
								}

								// Providing scrimming during fab transition
								Canvas(
									modifier = Modifier.fillMaxSize(),
									onDraw = {
										drawRect(color = Color.Black.copy(alpha = 0.32f * (transitionProgress)))
									}
								)
							}
						}
					)
				}
			}
		}
	}
}


@Composable
fun ReportsList(
	reports: List<Report>
) {
	LazyColumn(
		modifier = Modifier
			.fillMaxSize(),
		verticalArrangement = Arrangement.spacedBy(10.dp),
		contentPadding = PaddingValues(
			horizontal = 15.dp,
			vertical = 15.dp
		)
	) {
		items(
			items = reports,
			key = { it.id }
		) { report ->
			SharedElement(
				key = report.id,
				screenKey = AppScreen.Reports.route,
				transitionSpec = SharedElementsTransitionSpec(
					durationMillis = duration
				)
			) {
				ReportCard(report)
			}
		}
	}
}

@Composable
fun ReportCard(
	report: Report
) {

	val accentColor = AppColors.accent.collectAsState().value

	val navController = LocalNavController.current

	Card(
		modifier = Modifier
			.fillMaxWidth()
			.clickable {
				navController.navigate("${AppScreen.ReportDetails.navLink}/${report.id}")
			},
		elevation = 2.dp,
		shape = RoundedCornerShape(5.dp)
	) {

		Box {
			Canvas(
				modifier = Modifier.matchParentSize()
			) {
				drawRect(
					color = if (report.approver != null) AppColors.green else accentColor
				)
			}
			Surface(
				modifier = Modifier
					.padding(
						start = 4.dp
					)
					.fillMaxWidth()
			) {
				Column(
					modifier = Modifier
						.padding(
							top = 10.dp,
							bottom = 8.dp,
							start = 15.dp,
							end = 15.dp
						)
				) {
					Text(
						text = report.title,
						style = MaterialTheme.typography.h6
					)

					Spacer(modifier = Modifier.height(10.dp))

					Column(
						verticalArrangement = Arrangement.spacedBy(8.dp)
					){
						SharedElement(
							key = "${report.id}/time",
							screenKey = AppScreen.Reports.route,
							transitionSpec = SharedElementsTransitionSpec(
								durationMillis = duration
							)
						) {
							IconizedRow(
								imageVector = Icons.Default.Schedule,
								imageWidth = 18.dp,
								imageHeight = 18.dp,
								spacing = 8.dp
							) {
								Text(
									text = "${report.applicationDate}Z".fromIso8601(LocalContext.current),
									style = MaterialTheme.typography.subtitle1
								)
							}
						}

						// Applicant
						SharedElement(
							key = "${report.id}/applicant",
							screenKey = AppScreen.Reports.route,
							transitionSpec = SharedElementsTransitionSpec(
								durationMillis = duration
							)
						) {
							IconizedRow(
								imageVector = Icons.Default.ManageAccounts,
								spacing = 0.dp
							) {
								UserLink(user = report.applicant)
							}
						}

						// Implementer
						SharedElement(
							key = "${report.id}/implementer",
							screenKey = AppScreen.Reports.route,
							transitionSpec = SharedElementsTransitionSpec(
								durationMillis = duration
							)
						) {
							IconizedRow(
								imageVector = Icons.Default.Person,
								spacing = 0.dp
							) {
								UserLink(user = report.implementer)
							}
						}

						// Salary
						SharedElement(
							key = "${report.id}/salary",
							screenKey = AppScreen.Reports.route,
							transitionSpec = SharedElementsTransitionSpec(
								durationMillis = duration
							)
						) {
							IconizedRow(
								imageVector = Icons.Default.Payment
							) {
								Text(
									text = if (report.salary != null) stringResource(
										R.string.salary,
										report.salary
									) else stringResource(
										R.string.salary_not_specified
									),
									style = MaterialTheme.typography.subtitle1
								)
							}
						}

					}
				}
			}
		}
	}
}