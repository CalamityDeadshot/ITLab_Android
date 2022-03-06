package ru.rtuitlab.itlab.presentation.screens.devices.components

import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.core.util.Pair
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.android.material.datepicker.MaterialDatePicker
import ru.rtuitlab.itlab.R
import ru.rtuitlab.itlab.presentation.screens.devices.DevicesViewModel
import ru.rtuitlab.itlab.presentation.ui.components.LabelledCheckBox
import ru.rtuitlab.itlab.presentation.ui.components.SearchBar
import ru.rtuitlab.itlab.presentation.ui.components.top_app_bars.AppBarOption
import ru.rtuitlab.itlab.presentation.ui.components.top_app_bars.ExtendedTopAppBar

@ExperimentalPagerApi
@Composable
fun DevicesTopAppBar(
        devicesViewModel: DevicesViewModel = viewModel()
) {
        var searchActivated by rememberSaveable { mutableStateOf(false) }

        val showFreeDevicesChecked by devicesViewModel.freeFilteringIs.collectAsState()


        if (searchActivated)
                BackHandler {
                        searchActivated = false
                }

        ExtendedTopAppBar(
                options = listOf(
                        AppBarOption.Dropdown(
                                icon = Icons.Default.FilterList,
                                dropdownMenuContent = { collapseAction ->
                                        LabelledCheckBox(
                                                checked = showFreeDevicesChecked,
                                                onCheckedChange = {
                                                        devicesViewModel.onChangeFiltering()
                                                        collapseAction()
                                                },
                                                label = stringResource(R.string.show_only_free)
                                        )
                                }
                        ),
                        AppBarOption.Clickable(
                                icon = Icons.Default.Search,
                                onClick = {
                                        searchActivated = true
                                }
                        )
                ),
                hideBackButton = !searchActivated,
                hideOptions = searchActivated,
                onBackAction = {
                        searchActivated = false
                        devicesViewModel.onSearch("")
                }
        ) {
                if (searchActivated) {
                        SearchBar(
                                onSearch = devicesViewModel::onSearch
                        )
                } else {
                        Text(
                                text = stringResource(R.string.devices),
                                fontSize = 20.sp,
                                fontWeight = FontWeight(500),
                                textAlign = TextAlign.Start,
                                color = MaterialTheme.colors.onSurface
                        )
                }

        }
}

