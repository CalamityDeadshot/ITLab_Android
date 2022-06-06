package ru.rtuitlab.itlab.presentation.screens.micro_file_service.components

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import ru.rtuitlab.itlab.R
import ru.rtuitlab.itlab.data.remote.api.micro_file_service.models.FileInfo
import ru.rtuitlab.itlab.presentation.screens.devices.DevicesViewModel
import ru.rtuitlab.itlab.presentation.screens.devices.components.DeviceCard
import ru.rtuitlab.itlab.presentation.screens.devices.components.FloatActionButton
import ru.rtuitlab.itlab.presentation.screens.micro_file_service.MFSViewModel
import ru.rtuitlab.itlab.presentation.ui.components.*
import ru.rtuitlab.itlab.presentation.ui.components.bottom_sheet.BottomSheetViewModel
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.SharedElement
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.utils.SharedElementsTransitionSpec
import ru.rtuitlab.itlab.presentation.ui.components.top_app_bars.AppBarOption
import ru.rtuitlab.itlab.presentation.ui.extensions.fromIso8601
import ru.rtuitlab.itlab.presentation.ui.theme.AppColors
import ru.rtuitlab.itlab.presentation.utils.AppScreen

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun BaseElements(
	mfsViewModel: MFSViewModel
) {
	val file = mfsViewModel.file.collectAsState().value

	val filesResource by mfsViewModel.listFileInfoResponseFlow.collectAsState()
	var isRefreshing by remember { mutableStateOf(false) }




	Scaffold(
		modifier = Modifier
			.fillMaxSize(),

	) {
		//for attach files
		/*
		Column(

		) {

			val fileDescription = remember { mutableStateOf("") }

			Row() {

				//provideFile
				Button(
					modifier = Modifier
						.weight(3f),
					onClick = {
						mfsViewModel.provideFile()
					}
				) {
					Text(text = stringResource(R.string.Select_a_file))
				}

				if(file!=null) {
					Text(
						modifier = Modifier
							.weight(3f),
						text = file.name,
						overflow = TextOverflow.Ellipsis
					)
					//FileNull
					IconButton(
						modifier = Modifier
							.background(Color.Cyan)
							.weight(1f),
						onClick = {
							mfsViewModel.setFileNull()
						}
					) {
						Icon(Icons.Outlined.Delete, contentDescription = null)

					}
				}
			}

			//UploadFile
			Button(onClick = {
				mfsViewModel.uploadFile(fileDescription.value)
			}){
				Text(text = stringResource(R.string.Upload_a_file))

			}

			//fileDescription
			OutlinedTextField(
				value = fileDescription.value,
				onValueChange = {
					fileDescription.value = it

				},
				placeholder = {
					Text(text = stringResource(R.string.description))
				},
				colors = TextFieldDefaults.outlinedTextFieldColors(
					backgroundColor = MaterialTheme.colors.background,
					focusedBorderColor = MaterialTheme.colors.onSurface

				),
				modifier = Modifier
					.fillMaxWidth()
			)
		}


		 */
		Column(
			modifier = Modifier
				.fillMaxWidth()
		) {
			val state = remember{ mutableStateOf(false)}
			val stringSearch  = remember { mutableStateOf("")}
			val focusRequester = remember { FocusRequester() }

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(10.dp),
				horizontalArrangement = Arrangement.End,
				verticalAlignment = Alignment.CenterVertically
			){
				SideEffect {
					mfsViewModel.onSearch(stringSearch.value)
				}
				OutlinedTextField(
					modifier = Modifier
						.fillMaxWidth()
						.weight(0.9f)
						.focusRequester(focusRequester),
					value = stringSearch.value,
					onValueChange = {
						stringSearch.value = it
						mfsViewModel.onSearch(stringSearch.value)
					},
					placeholder = {

						Text(text = stringResource(R.string.search))


					},
					singleLine = true,
					colors = TextFieldDefaults.outlinedTextFieldColors(
						backgroundColor = MaterialTheme.colors.background,
						focusedBorderColor = MaterialTheme.colors.onSurface

					)

				)
				DisposableEffect(Unit) {
					//focusRequester.requestFocus()
					onDispose {
						mfsViewModel.onSearch("")
					}
				}
				AppDropdownMenu(
					modifier = Modifier
						.fillMaxWidth()
						.weight(0.1f),
					anchor = {
						IconButton(
							modifier = Modifier
								.height(36.dp)
								.width(36.dp),
							onClick = it
						) {
							Icon(
								imageVector = Icons.Default.FilterList,
								contentDescription = stringResource(R.string.filter),
								tint = MaterialTheme.colors.onSurface
							)
						}
					},
					content = {
							collapseAction ->
						LabelledRadioButton(
							state = !state.value,
							onCheckedChange = {
								mfsViewModel.setSortedBy("date")
								state.value=!state.value
								collapseAction()
							},
							label = stringResource(R.string.byDate)
						)
						LabelledRadioButton(
							state = state.value,
							onCheckedChange = {
								mfsViewModel.setSortedBy("user")
								state.value=!state.value
								collapseAction()
							},
							label = stringResource(R.string.byUser)
						)
					}
				)

			}
			SwipeRefresh(
				modifier = Modifier
					.fillMaxWidth(),
				state = rememberSwipeRefreshState(isRefreshing),
				onRefresh = mfsViewModel::onRefresh
			) {

				Column(
					modifier = Modifier
						.fillMaxWidth()
				) {
					filesResource.handle(
						onLoading = {
							isRefreshing = true
						},
						onError = { msg ->
							isRefreshing = false
							LoadingError(msg = msg)
						},
						onSuccess = {
							isRefreshing = false
							mfsViewModel.onResourceSuccess(it)

							FileList(mfsViewModel)


						}

					)
				}


			}


	}
	}
}
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
private fun FileList(
	mfsViewModel: MFSViewModel
) {
	val files = mfsViewModel.listFileInfoFlow.collectAsState().value.reversed()



	LazyColumn(
		verticalArrangement = Arrangement.spacedBy(10.dp),
		contentPadding = PaddingValues(horizontal = 15.dp, vertical = 15.dp),
		modifier = Modifier.fillMaxSize()
	) {


		items(files) { file ->

			FileCard(
				mfsViewModel = mfsViewModel,
				file = file,
				modifier = Modifier
					.fillMaxWidth()

			)


		}


	}

}

@Composable
fun FileCard(mfsViewModel: MFSViewModel, file: FileInfo, modifier: Modifier) {
	val context = LocalContext.current
	val exp = file.filename.substring(file.filename.lastIndexOf(".") + 1)
	val duration = 300

	Card(
		modifier = modifier
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(10.dp),
		) {
			SharedElement(
				key = "${file.id}/time",
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
						text = file.uploadDate.fromIso8601(LocalContext.current),
						style = MaterialTheme.typography.subtitle1
					)
				}
			}
			Row {
				Column(
					modifier = Modifier
						.weight(1f)
						.padding(10.dp),
					verticalArrangement = Arrangement.Center,
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					SharedElement(
						key = "${file.id}/applicant",
						screenKey = AppScreen.ReportDetails.route,
						transitionSpec = SharedElementsTransitionSpec(
							durationMillis = ru.rtuitlab.itlab.presentation.screens.reports.duration
						)
					) {
						IconizedRow(
							imageVector = Icons.Default.Person,
							opacity = .7f,
							spacing = 0.dp
						) {
							UserLink(user = file.applicant)
						}
					}
					TextButton(
						onClick = { mfsViewModel.downloadFile(context,file) }){
						Text(
							text = stringResource(R.string.download),
							color = MaterialTheme.colors.onPrimary
						)
					}
				}
				Column(
					modifier = Modifier
						.weight(1f)
						.padding(10.dp),
					verticalArrangement = Arrangement.Center,
					horizontalAlignment = Alignment.CenterHorizontally
				) {
					val( bitmap,setBitmap) = rememberSaveable{mutableStateOf<Bitmap?>(null)}
					Log.d("BASE","$exp")
					if((exp == "png" || exp == "jpeg" || exp == "jpg"|| exp == "gif") ) {
						if (bitmap == null)
							SideEffect {
								//mfsViewModel.getBitmapFromFile(context, file, setBitmap)
							}

						if(bitmap != null){
							Image(
								bitmap = bitmap.asImageBitmap(),
								contentDescription = stringResource(R.string.report),
								contentScale = ContentScale.Fit
							)
						}else{
							Icon(
								painter = painterResource(R.drawable.ic_attach),
								contentDescription = stringResource(R.string.report)
							)
							Text(
								text = file.filename,
								overflow = TextOverflow.Clip
							)
							}
						DisposableEffect(Unit) {
							//focusRequester.requestFocus()
							onDispose {
								mfsViewModel.getBitmapFromFile(context,null,setBitmap)
							}
						}
					}else {
						Icon(
							painter = painterResource(R.drawable.ic_attach),
							contentDescription = stringResource(R.string.report)
						)
						Text(
							text = file.filename,
							overflow = TextOverflow.Clip
						)
					}

				}
			}
		}
	}
}
