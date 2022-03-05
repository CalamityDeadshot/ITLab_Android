package ru.rtuitlab.itlab.presentation.screens.devices.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.rtuitlab.itlab.R
import ru.rtuitlab.itlab.data.remote.api.devices.models.EquipmentNewRequest
import ru.rtuitlab.itlab.presentation.screens.devices.DevicesViewModel
import ru.rtuitlab.itlab.presentation.ui.components.bottom_sheet.BottomSheetViewModel
import ru.rtuitlab.itlab.presentation.ui.components.dialog.DialogViewModel
import ru.rtuitlab.itlab.presentation.ui.theme.AppColors
import java.util.*

@ExperimentalMaterialApi
@Composable
fun DeviceNewAcceptDialogContent(
        dialogViewModel: DialogViewModel,
        bottomSheetViewModel: BottomSheetViewModel,
        devicesViewModel: DevicesViewModel,
        title:String,
        serialNumber:String,
        description:String,
        equipmentTypeId:String,
        onRefreshLines:() -> Unit
) {

        val scope = rememberCoroutineScope()


        Card(
                shape = RoundedCornerShape(10.dp)
        ) {
                Column(
                        modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                        top = 20.dp,
                                        start = 20.dp,
                                        bottom = 10.dp,
                                        end = 20.dp
                                )
                ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                        text = "Проверьте информацию",
                                        style = MaterialTheme.typography.h6
                                )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        painter = painterResource(R.drawable.ic_title),
                                        contentDescription = stringResource(R.string.title),
                                        modifier = Modifier
                                                .width(16.dp)
                                                .height(16.dp),

                                        )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                        text = "$title",
                                        fontWeight = FontWeight(500),
                                        fontSize = 16.sp,
                                        lineHeight = 22.sp
                                )

                        }
                        Spacer(modifier = Modifier.height(5.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                        painter = painterResource(R.drawable.ic_serial_number),
                                        contentDescription = stringResource(R.string.serial_number),
                                        modifier = Modifier
                                                .width(16.dp)
                                                .height(16.dp),

                                        )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                        text = "$serialNumber",
                                        fontWeight = FontWeight(500),
                                        fontSize = 16.sp,
                                        lineHeight = 22.sp
                                )

                        }
                        Spacer(modifier = Modifier.height(5.dp))

                        Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                        painter = painterResource(R.drawable.ic_edit),
                                        contentDescription = stringResource(R.string.description),
                                        modifier = Modifier
                                                .width(16.dp)
                                                .height(16.dp),

                                        )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                        text = "$description",
                                        fontWeight = FontWeight(500),
                                        fontSize = 16.sp,
                                        lineHeight = 22.sp
                                )

                        }

                        Button(
                                modifier = Modifier
                                        .align(Alignment.End)
                                        .clipToBounds(),
                                onClick = {

                                },
                                colors = ButtonDefaults.buttonColors(
                                        backgroundColor = Color.Transparent
                                ),
                                elevation = ButtonDefaults.elevation(
                                        defaultElevation = 0.dp,
                                        pressedElevation = 0.dp
                                )
                        ) {
                                Text(
                                        text = stringResource(R.string.confirm).uppercase(Locale.getDefault()),
                                        color = AppColors.accent.collectAsState().value,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight(500),
                                        lineHeight = 22.sp,
                                        modifier = Modifier
                                                .clickable {
                                                        if(serialNumber.isNotEmpty() && title.isNotEmpty() && description.isNotEmpty()) {
                                                                val equipmentNewRequest = EquipmentNewRequest(
                                                                        serialNumber,
                                                                        equipmentTypeId,
                                                                        description
                                                                )
                                                                devicesViewModel.onCreateEquipment(equipmentNewRequest){
                                                                                isSuccessful ->
                                                                                        if(isSuccessful) {
                                                                                                dialogViewModel.hide()
                                                                                                bottomSheetViewModel.hide(scope)
                                                                                                devicesViewModel.onRefresh()
                                                                                                onRefreshLines()
                                                                                        }
                                                                }
                                                        }
                                                }
                                )
                        }

                }
        }
}
