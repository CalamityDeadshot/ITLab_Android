@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package ru.rtuitlab.itlab.presentation.screens.purchases

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Today
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener
import ru.rtuitlab.itlab.R
import ru.rtuitlab.itlab.presentation.navigation.LocalNavController
import ru.rtuitlab.itlab.presentation.screens.micro_file_service.FilesViewModel
import ru.rtuitlab.itlab.presentation.screens.purchases.state.NewPurchaseUiState
import ru.rtuitlab.itlab.presentation.screens.reports.UploadConfirmationDialog
import ru.rtuitlab.itlab.presentation.ui.components.LoadableButtonContent
import ru.rtuitlab.itlab.presentation.ui.components.PrimaryButton
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.FadeMode
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.SharedElement
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.utils.ProgressThresholds
import ru.rtuitlab.itlab.presentation.ui.components.shared_elements.utils.SharedElementsTransitionSpec
import ru.rtuitlab.itlab.presentation.ui.components.text_fields.OutlinedAppTextField
import ru.rtuitlab.itlab.common.extensions.fromIso8601
import ru.rtuitlab.itlab.common.extensions.toIso8601
import ru.rtuitlab.itlab.presentation.ui.components.FabAwareSnackbarHost
import ru.rtuitlab.itlab.presentation.ui.components.modifier.fabAwarePadding
import ru.rtuitlab.itlab.presentation.ui.extensions.collectUiEvents
import ru.rtuitlab.itlab.presentation.utils.AppScreen
import ru.rtuitlab.itlab.presentation.utils.LocalActivity
import ru.rtuitlab.itlab.presentation.utils.singletonViewModel
import java.io.File

@Composable
fun NewPurchase(
    purchasesViewModel: PurchasesViewModel = singletonViewModel()
) {

    val purchasesState by purchasesViewModel.state.collectAsState()
    val state = purchasesState.newPurchaseState

    val (transitionProgress, tpSetter) = rememberSaveable {
        mutableStateOf(1f)
    }

    var sharedElementKey: Any by remember {
        mutableStateOf("Purchases/New")
    }

    val navController = LocalNavController.current

    val snackbarHostState = remember { SnackbarHostState() }

    purchasesViewModel.uiEvents.collectUiEvents(snackbarHostState)

    SharedElement(
        key = sharedElementKey,
        screenKey = AppScreen.NewPurchase.route,
        isFullscreen = false,
        transitionSpec = SharedElementsTransitionSpec(
            fadeMode = FadeMode.Through,
            fadeProgressThresholds = ProgressThresholds(.2f, 1f),
            scaleProgressThresholds = ProgressThresholds(0f, .8f)
        ),
        onFractionChanged = tpSetter
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(transitionProgress.dp * 128)),
            snackbarHost = { FabAwareSnackbarHost(snackbarHostState) }
        ) {
            Box(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(it)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fabAwarePadding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedAppTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = state.name,
                        onValueChange = purchasesViewModel::onNameChange,
                        label = {
                            Row {
                                Text(
                                    text = stringResource(R.string.purchase_name),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "*",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        trailingIcon = {
                            Text(
                                text = "${state.name.length}/63",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        },
                        singleLine = true
                    )

                    OutlinedAppTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = (state.price?.toString() ?: "").let {
                            if (it.isBlank()) it
                            else stringResource(R.string.salary_int, state.price!!)
                        },
                        onValueChange = purchasesViewModel::onPriceChange,
                        label = {
                            Row {
                                Text(
                                    text = stringResource(R.string.purchase_price)
                                )
                                Text(
                                    text = "*",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )

                    OutlinedAppTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize(),
                        value = state.description,
                        onValueChange = purchasesViewModel::onDescriptionChange,
                        label = {
                            Text(
                                text = stringResource(R.string.description)
                            )
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        trailingIcon = {
                            Text(
                                text = "${state.description.length}/255",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    )

                    DateSelector(state)

                    FileSelector(
                        state = state,
                        type = PurchasesViewModel.FileType.Check
                    )

                    FileSelector(
                        state = state,
                        type = PurchasesViewModel.FileType.Photo
                    )

                    val context = LocalContext.current

                    PrimaryButton(
                        modifier = Modifier
                            .align(Alignment.End),
                        onClick = {
                            if (!state.isPurchaseUploading) {
                                purchasesViewModel.onSendPurchase(
                                    successMessage = context.getString(R.string.purchase_created)
                                ) { newPurchase ->
                                    newPurchase?.let {
                                        sharedElementKey = it.id
                                        navController.popBackStack()
                                    }
                                }
                            }
                        },
                        text = stringResource(R.string.send),
                        enabled = state.isSendButtonEnabled
                    ) { text ->
                        LoadableButtonContent(
                            isLoading = state.isPurchaseUploading,
                            strokeWidth = 2.dp
                        ) {
                            text()
                        }
                    }
                }
            }
        }
    }

}

@Composable
private fun DateSelector(
    state: NewPurchaseUiState,
    purchasesViewModel: PurchasesViewModel = singletonViewModel()
) {

    val listener = MaterialPickerOnPositiveButtonClickListener<Long> {
        purchasesViewModel.onDateSelected(it)
    }

    val context = LocalContext.current

    OutlinedAppTextField(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable {
                MaterialDatePicker
                    .Builder
                    .datePicker()
                    .setSelection(
                        state.purchaseDate.toEpochMilliseconds()
                    )
                    .setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
                    .build()
                    .apply {
                        show((context as AppCompatActivity).supportFragmentManager, null)
                        addOnPositiveButtonClickListener(listener)
                    }
            },
        value = state.purchaseDate.toIso8601().fromIso8601(LocalContext.current, parseWithTime = false),
        onValueChange = {},
        enabled = false,
        label = {
            Row {
                Text(
                    text = stringResource(R.string.purchase_date)
                )
                Text(
                    text = "*",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            disabledTextColor = LocalContentColor.current,
            disabledBorderColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(.8f)
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Today,
                contentDescription = stringResource(R.string.purchase_date)
            )
        }
    )
}

@Composable
private fun FileSelector(
    state: NewPurchaseUiState,
    type: PurchasesViewModel.FileType,
    purchasesViewModel: PurchasesViewModel = singletonViewModel(),
    filesViewModel: FilesViewModel = singletonViewModel()
) {

    val activity = LocalActivity.current

    OutlinedAppTextField(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraSmall)
            .clickable {
                filesViewModel.provideFile(type.mimeTypes, activity) {
                    purchasesViewModel.onAttachFile(
                        type = type,
                        file = it
                    )
                }
            },
        value = when (type) {
            PurchasesViewModel.FileType.Check -> state.checkFileId?.let { state.checkFile?.name }
            PurchasesViewModel.FileType.Photo -> state.purchasePhotoId?.let { state.purchasePhotoFile?.name }
        } ?: "",
        onValueChange = {},
        enabled = false,
        label = {
            Row {
                Text(
                    text = stringResource(
                        when (type) {
                            PurchasesViewModel.FileType.Check -> R.string.purchase_check
                            PurchasesViewModel.FileType.Photo -> R.string.purchase_photo
                        }
                    )
                )
                if (type == PurchasesViewModel.FileType.Check)
                    Text(
                        text = "*",
                        color = MaterialTheme.colorScheme.error
                    )
            }
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(
            disabledTextColor = LocalContentColor.current,
            disabledBorderColor = MaterialTheme.colorScheme.onSurface,
            disabledLabelColor = MaterialTheme.colorScheme.onSurface,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(.8f)
        ),
        leadingIcon = {
            Icon(
                imageVector = when (type) {
                    PurchasesViewModel.FileType.Check -> Icons.Default.PictureAsPdf
                    PurchasesViewModel.FileType.Photo -> Icons.Default.Image
                },
                contentDescription = when (type) {
                    PurchasesViewModel.FileType.Check -> stringResource(R.string.purchase_check)
                    PurchasesViewModel.FileType.Photo -> stringResource(R.string.purchase_photo)
                }
            )
        },
        trailingIcon = if (type == PurchasesViewModel.FileType.Check && state.checkFileId != null) {
            {
                IconButton(
                    onClick = { purchasesViewModel.onRemoveFile(type) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cancel)
                    )
                }
            }
        } else if (type == PurchasesViewModel.FileType.Photo && state.purchasePhotoId != null) {
            {
                IconButton(
                    onClick = { purchasesViewModel.onRemoveFile(type) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cancel)
                    )
                }
            }
        } else null
    )

    when (type) {
        PurchasesViewModel.FileType.Check -> {
            if (state.isCheckFileDialogShown)
                PurchaseFileUploadingDialog(
                    type = type,
                    isUploading = state.isCheckFileUploading,
                    providedFile = state.checkFile!! // Cannot be null at this point
                )
        }
        PurchasesViewModel.FileType.Photo -> {
            if (state.isPurchasePhotoDialogShown)
                PurchaseFileUploadingDialog(
                    type = type,
                    isUploading = state.isPurchasePhotoUploading,
                    providedFile = state.purchasePhotoFile!! // Cannot be null at this point
                )
        }
    }
}

@Composable
private fun PurchaseFileUploadingDialog(
    type: PurchasesViewModel.FileType,
    isUploading: Boolean,
    providedFile: File,
    purchasesViewModel: PurchasesViewModel = singletonViewModel(),
    filesViewModel: FilesViewModel = singletonViewModel()
) {
    UploadConfirmationDialog(
        isUploading = isUploading,
        providedFile = providedFile
    ) { isConfirmed ->
        if (isConfirmed) {
            purchasesViewModel.onUploadFile(type)
            filesViewModel.uploadFile(
                onError = {
                    purchasesViewModel.onFileUploadingError(it)
                    purchasesViewModel.onConfirmationDialogDismissed(type)
                },
                onSuccess = {
                    purchasesViewModel.onFileUploaded(it, type)
                    purchasesViewModel.onConfirmationDialogDismissed(type)
                }
            )
        } else purchasesViewModel.onConfirmationDialogDismissed(type)
    }
}