package ru.rtuitlab.itlab.presentation.screens.purchases

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.rtuitlab.itlab.R
import ru.rtuitlab.itlab.data.remote.api.purchases.PurchaseStatusApi
import ru.rtuitlab.itlab.presentation.screens.reports.duration
import ru.rtuitlab.itlab.presentation.ui.components.IconizedRow
import ru.rtuitlab.itlab.presentation.ui.components.UserLink
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.SharedElement
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.utils.SharedElementsTransitionSpec
import ru.rtuitlab.itlab.presentation.ui.components.top_app_bars.AppBarViewModel
import ru.rtuitlab.itlab.presentation.ui.extensions.fromIso8601
import ru.rtuitlab.itlab.presentation.utils.AppScreen
import ru.rtuitlab.itlab.presentation.utils.singletonViewModel

@ExperimentalAnimationApi
@Composable
fun Purchase(
    id: Int,
    purchasesViewModel: PurchasesViewModel = singletonViewModel(),
    appBarViewModel: AppBarViewModel = singletonViewModel()
) {

    val state by purchasesViewModel.state.collectAsState()
    val purchase = state.selectedPurchaseState!!.purchase
    val animationState by remember {
        mutableStateOf(MutableTransitionState(false))
    }


    val scaffoldState = rememberScaffoldState(
        snackbarHostState = SnackbarHostState()
    )

    LaunchedEffect(Unit) {
        animationState.targetState = true
        appBarViewModel.onNavigate(
            screen = AppScreen.PurchaseDetails(purchase.name)
        )
        purchasesViewModel.events.collect { event ->
            when (event) {
                is PurchasesViewModel.PurchaseEvent.Snackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        scaffoldState = scaffoldState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 15.dp)
        ) {

            SharedElement(
                key = purchase.id,
                screenKey = AppScreen.PurchaseDetails.route,
                transitionSpec = SharedElementsTransitionSpec(
                    durationMillis = duration
                )
            ) {
                Surface(
                    modifier = Modifier
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


                        SharedElement(
                            key = "${purchase.id}/time",
                            screenKey = AppScreen.PurchaseDetails.route,
                            transitionSpec = SharedElementsTransitionSpec(
                                durationMillis = duration
                            )
                        ) {
                            IconizedRow(
                                imageVector = Icons.Default.Schedule,
                                opacity = .7f,
                                spacing = 10.dp
                            ) {
                                Text(text = stringResource(R.string.purchase_date))
                                Text(
                                    text = purchase.purchaseDate.fromIso8601(
                                        context = LocalContext.current,
                                        parseWithTime = false
                                    ),
                                    style = MaterialTheme.typography.subtitle1
                                )
                            }
                        }

                        // Purchaser
                        SharedElement(
                            key = "${purchase.id}/purchaser",
                            screenKey = AppScreen.PurchaseDetails.route,
                            transitionSpec = SharedElementsTransitionSpec(
                                durationMillis = duration
                            )
                        ) {
                            IconizedRow(
                                imageVector = Icons.Default.Person,
                                opacity = .7f,
                                spacing = 10.dp
                            ) {
                                Text(text = stringResource(R.string.purchase_buyer))
                                UserLink(user = purchase.purchaser)
                            }
                        }

                        // Approver
                        purchase.solution.solver?.let {
                            SharedElement(
                                key = "${purchase.id}/approver",
                                screenKey = AppScreen.PurchaseDetails.route,
                                transitionSpec = SharedElementsTransitionSpec(
                                    durationMillis = duration
                                )
                            ) {
                                IconizedRow(
                                    imageVector = Icons.Default.ManageAccounts,
                                    opacity = .7f,
                                    spacing = 10.dp
                                ) {
                                    Text(
                                        text = stringResource(
                                            if (purchase.solution.status == PurchaseStatusApi.ACCEPT)
                                                R.string.purchase_approved_by
                                            else R.string.purchase_rejected_by
                                        )
                                    )
                                    UserLink(user = it)
                                }
                            }
                        }

                        // Compensation
                        SharedElement(
                            key = "${purchase.id}/price",
                            screenKey = AppScreen.PurchaseDetails.route,
                            transitionSpec = SharedElementsTransitionSpec(
                                durationMillis = duration
                            )
                        ) {
                            IconizedRow(
                                imageVector = Icons.Default.Payment,
                                opacity = .7f,
                                spacing = 10.dp
                            ) {
                                Text(text = stringResource(R.string.purchase_price))
                                Text(
                                    text = stringResource(R.string.salary_float, purchase.price),
                                    style = MaterialTheme.typography.subtitle1
                                )
                            }
                        }

                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!purchase.description.isNullOrBlank())
                PurchaseDescription(
                    header = {
                        Text(
                            text = stringResource(R.string.description),
                            style = MaterialTheme.typography.h6
                        )
                    },
                    description = purchase.description,
                    visibleState = animationState
                )
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun PurchaseDescription(
    header: @Composable () -> Unit,
    description: String,
    visibleState: MutableTransitionState<Boolean>
) {
    AnimatedVisibility(
        visibleState = visibleState,
        enter = slideInVertically(
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            initialOffsetY = { it }
        ) + fadeIn(
            animationSpec = spring(stiffness = Spring.StiffnessLow)
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            border = ButtonDefaults.outlinedBorder
        ) {
            Column {
                Box(
                    Modifier
                        .background(color = MaterialTheme.colors.onSurface.copy(alpha = .1f))
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                        .fillMaxWidth()
                ) {
                    header()
                }
                Divider()
                Text(
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                    text = description
                )
            }
        }
    }
}