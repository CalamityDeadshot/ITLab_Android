package ru.rtuitlab.itlab.presentation.screens.devices.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ru.rtuitlab.itlab.R
import ru.rtuitlab.itlab.data.remote.api.devices.models.DeviceDetails
import ru.rtuitlab.itlab.data.remote.api.users.models.UserResponse
import ru.rtuitlab.itlab.presentation.screens.devices.DevicesViewModel
import ru.rtuitlab.itlab.presentation.ui.components.PrimaryTextButton
import ru.rtuitlab.itlab.presentation.ui.components.text_fields.OutlinedAppTextField

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalAnimationApi
@Composable
fun DeviceChangeOwnerDialog(
    onDismissRequest: () -> Unit,
    device: DeviceDetails,
    devicesViewModel: DevicesViewModel,
    afterChange: () -> Unit,
    owner: UserResponse?
) {
    val query by devicesViewModel.dialogSearchQuery.collectAsState()

    val users = devicesViewModel.queriedUsers.collectAsState().value

    var selectedUser: UserResponse? by remember { mutableStateOf(null) }


    Dialog(
        onDismissRequest = onDismissRequest,
    ) {

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
                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .padding(10.dp)
                ) {


                    OutlinedAppTextField(
                        value = query,
                        onValueChange = devicesViewModel::onDialogQueryChanged,
                        placeholder = {
                            Text(
                                text = stringResource(R.string.to_assign_owner),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(.8f),
                            )
                        },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            disabledTextColor = LocalContentColor.current,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(.6f)

                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    UserList(
                        users = users,
                        onUserSelected = {
                            selectedUser = it
                            devicesViewModel.onDialogQueryChanged(it.fullName)
                        }
                    )

                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        AnimatedVisibility(owner != null) {

                            Button(
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent
                                ),
                                onClick = {
                                    devicesViewModel.onPickUpEquipment(
                                        owner?.id.toString(),
                                        device.id
                                    ) { isSuccessful ->
                                        if (isSuccessful) {
                                            devicesViewModel.onRefresh()
                                        }

                                    }
                                    afterChange()
                                }) {
                                Text(
                                    text = stringResource(id = R.string.pick_up),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 14.sp
                                )
                            }

                        }
                        Spacer(modifier = Modifier.width(15.dp))
                        PrimaryTextButton(
                            onClick = {
                                selectedUser?.let {
                                    devicesViewModel.onChangeEquipmentOwner(
                                        it.id,
                                        device.id
                                    ) { isSuccessful ->
                                        if (isSuccessful) {
                                            devicesViewModel.onRefresh()
                                        }
                                    }
                                    afterChange()
                                }
                            },
                            text = stringResource(id = R.string.to_choose),
                        )

                    }
                    Spacer(modifier = Modifier.height(5.dp))

                }

            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun UserList(
    users: List<UserResponse>,
    onUserSelected: (UserResponse) -> Unit
) {

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 15.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        items(users) { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onUserSelected(user)
                    }
            ) {
                Row(
                    modifier = Modifier
                        .height(30.dp)
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(.8f),
                        modifier = Modifier.padding(5.dp, 0.dp)
                    )
                }
                Divider(
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}