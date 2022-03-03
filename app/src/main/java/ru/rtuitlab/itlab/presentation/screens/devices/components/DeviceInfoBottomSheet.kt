package ru.rtuitlab.itlab.presentation.screens.devices.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ru.rtuitlab.itlab.R
import ru.rtuitlab.itlab.data.remote.api.devices.models.DeviceDetails
import ru.rtuitlab.itlab.data.remote.api.devices.models.EquipmentEditRequest
import ru.rtuitlab.itlab.data.remote.api.devices.models.EquipmentTypeResponse
import ru.rtuitlab.itlab.presentation.screens.devices.DevicesViewModel
import ru.rtuitlab.itlab.presentation.ui.components.bottom_sheet.BottomSheetViewModel
import ru.rtuitlab.itlab.presentation.ui.components.dialog.DialogViewModel
import ru.rtuitlab.itlab.presentation.utils.AppDialog

@ExperimentalAnimationApi
@ExperimentalTransitionApi
@ExperimentalMaterialApi
@Composable
fun DeviceInfoBottomSheet(
	devicesViewModel: DevicesViewModel,
	dialogViewModel: DialogViewModel,
	bottomSheetViewModel: BottomSheetViewModel,
	deviceDetails: DeviceDetails?
) {

	val scope = rememberCoroutineScope()

	devicesViewModel.setdeviceFromSheet(deviceDetails)


	val tempDeviceDetails = devicesViewModel.deviceFromSheetFlow.collectAsState().value
	val equipmentIdString = tempDeviceDetails?.equipmentTypeId
	val equipmentId = remember { mutableStateOf(equipmentIdString) }
	val titleString = tempDeviceDetails?.equipmentType?.title
	val titleDevice = remember { mutableStateOf(titleString) }
	val serialNumberString = tempDeviceDetails?.serialNumber
	val serialNumberDevice = remember { mutableStateOf(serialNumberString) }
	val descriptionString = tempDeviceDetails?.description
	val descriptionDevice = remember { mutableStateOf(descriptionString) }

	if (!bottomSheetViewModel.visibilityAsState.collectAsState().value) {
		titleDevice.value = tempDeviceDetails?.equipmentType?.title
		serialNumberDevice.value = tempDeviceDetails?.serialNumber
		descriptionDevice.value = tempDeviceDetails?.description
	}

	var dialogEquipmentTypeIsShown by remember { mutableStateOf(false) }
	var dialogSerialNumberIsShown by remember { mutableStateOf(false) }
	var dialogDescriptionIsShown by remember { mutableStateOf(false) }




	val setEquipmentTypeLine: (EquipmentTypeResponse) -> Unit = {
		titleDevice.value = it.title
		equipmentId.value = it.id
		dialogEquipmentTypeIsShown = false
	}
	val setSerialNumberLine: (String) -> Unit = {
		serialNumberDevice.value = it
		dialogSerialNumberIsShown = false
	}
	val setDescriptionLine: (String) -> Unit = {
		descriptionDevice.value = it
	}

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(25.dp)

	) {
		if(dialogEquipmentTypeIsShown)
			Dialog(
				onDismissRequest = {dialogEquipmentTypeIsShown=false} ,
				content = {
					DeviceInfoEditEquipmentTypeDialogContent(
					tempDeviceDetails?.equipmentType?.title.toString(),
					dialogViewModel,
					devicesViewModel,
					setEquipmentTypeLine
				)
				}
			)
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.clickable {
					dialogEquipmentTypeIsShown = true
//					dialogViewModel.show(
//						AppDialog.DeviceInfoEditEquipmentType(
//							tempDeviceDetails?.equipmentType?.title.toString(),
//							dialogViewModel,
//							devicesViewModel,
//							setEquipmentTypeLine
//						)
//					)
				}
				.fillMaxWidth()
		) {
			Icon(
				modifier = Modifier
					.width(20.dp)
					.height(20.dp),
				painter = painterResource(R.drawable.ic_title),
				contentDescription = stringResource(R.string.equipmentType),
				tint = colorResource(R.color.accent)


			)
			Spacer(Modifier.width(8.dp))
			Text(
				text = titleDevice?.value.toString(),
				textDecoration = TextDecoration.Underline


			)


		}
		Spacer(Modifier.height(8.dp))

		if(dialogSerialNumberIsShown)
			Dialog(
				onDismissRequest = {dialogSerialNumberIsShown=false} ,
				content = {
					DeviceInfoEditSecondaryDialogContent(
						tempDeviceDetails?.serialNumber.toString(),
						stringResource(R.string.serial_number),
						dialogViewModel,
						devicesViewModel,
						setSerialNumberLine
				)
				}
			)
		Row(verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.clickable {

					dialogSerialNumberIsShown = true
//					dialogViewModel.show(
//						AppDialog.DeviceInfoEditSerialNumber(
//							tempDeviceDetails?.serialNumber.toString(),
//							dialogViewModel,
//							devicesViewModel,
//							setSerialNumberLine
//						)
//					)
				}) {
			Icon(
				modifier = Modifier
					.width(20.dp)
					.height(20.dp),
				painter = painterResource(R.drawable.ic_serial_number),
				contentDescription = stringResource(R.string.serial_number),
				tint = colorResource(R.color.accent)

			)
			Spacer(Modifier.width(8.dp))
			Text(
				text = serialNumberDevice.value.toString(),
				textDecoration = TextDecoration.Underline

			)
		}
		Spacer(Modifier.height(8.dp))

		if(dialogDescriptionIsShown)
			Dialog(
				onDismissRequest = {dialogDescriptionIsShown=false} ,
				content = {
					DeviceInfoEditSecondaryDialogContent(
						tempDeviceDetails?.description.toString(),
						stringResource(R.string.description),
						dialogViewModel,
						devicesViewModel,
						setDescriptionLine
					)
				}
			)
		Row(verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.clickable {

					dialogDescriptionIsShown = true
//					dialogViewModel.show(
//						AppDialog.DeviceInfoEditDescription(
//							tempDeviceDetails?.description.toString(),
//							dialogViewModel,
//							devicesViewModel,
//							setDescriptionLine
//						)
//					)
				}) {
			Icon(
				painter = painterResource(R.drawable.ic_info),
				contentDescription = stringResource(R.string.description),
				modifier = Modifier
					.width(20.dp)
					.height(20.dp),
				tint = colorResource(R.color.accent)


			)
			Spacer(Modifier.width(8.dp))
			Text(
				text = descriptionDevice.value.toString(),
				textDecoration = TextDecoration.Underline


			)
		}
		Spacer(Modifier.height(8.dp))

		Row(
			modifier = Modifier
				.fillMaxWidth(),
			horizontalArrangement = Arrangement.End,

			) {

			Text(
				text = "Изменить",
				fontWeight = FontWeight(500),
				fontSize = 17.sp,
				modifier = Modifier
					.padding(10.dp)
					.height(30.dp)
					.padding(0.dp)
					.clickable {


						val equipmentEditRequest = EquipmentEditRequest(
							serialNumberDevice.value,
							equipmentId.value,
							descriptionDevice.value,
							null,
							true,
							tempDeviceDetails?.id
						)


						devicesViewModel.onUpdateEquipment(
							equipmentEditRequest
						) { isSuccessful ->
							if (isSuccessful) {
								bottomSheetViewModel.hide(scope)
								devicesViewModel.onRefresh()
							}


						}


					}

			)
			Icon(
				Icons.Outlined.Delete,
				contentDescription = stringResource(R.string.delete),
				modifier = Modifier
					.padding(10.dp)
					.width(40.dp)
					.height(30.dp)
					.padding(0.dp)
					.clickable {

						if (tempDeviceDetails != null) {
							devicesViewModel.onDeleteEquipment(
								tempDeviceDetails.id
							) { isSuccessful ->
								if (isSuccessful) {
									bottomSheetViewModel.hide(
										scope
									)
									devicesViewModel.onRefresh()
								}


							}
						}

					}

			)

		}
	}


}

