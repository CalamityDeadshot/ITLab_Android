package ru.rtuitlab.itlab.presentation.screens.reports

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.rtuitlab.itlab.BuildConfig
import ru.rtuitlab.itlab.data.remote.api.micro_file_service.models.FileInfoResponse
import ru.rtuitlab.itlab.data.remote.api.users.models.User
import ru.rtuitlab.itlab.domain.use_cases.users.GetCurrentUserUseCase
import ru.rtuitlab.itlab.domain.use_cases.users.GetUsersUseCase
import ru.rtuitlab.itlab.presentation.screens.reports.state.NewReportUiState
import ru.rtuitlab.itlab.presentation.ui.components.markdown.MdAction
import java.io.File
import javax.inject.Inject

@HiltViewModel
class NewReportViewModel @Inject constructor(
	getCurrentUser: GetCurrentUserUseCase,
	getUsers: GetUsersUseCase
): ViewModel() {

	init {
		viewModelScope.launch {
			getCurrentUser().onEach {
				it?.toUser()?.let {
					onUserSelected(it)
					cancel()
				}
			}.collect()
		}
	}

	private val _reportState = MutableStateFlow(NewReportUiState())
	val reportState = _reportState.asStateFlow()

	val users = reportState.flatMapLatest {
		getUsers.search(it.implementerSearchQuery).map {
			it.map { it.toUser() }
		}
	}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

	fun onUserSelected(user: User) = viewModelScope.launch {
		_reportState.emit(
			_reportState.value.copy(
				selectedImplementer = user,
				implementerSearchQuery = user.abbreviatedName
			)
		)
	}

	fun onQueryChanged(newQuery: String) = viewModelScope.launch {
		_reportState.update {
			it.copy(
				implementerSearchQuery = newQuery,
				selectedImplementer = null
			)
		}
	}

	fun onTitleChanged(title: String) = viewModelScope.launch {
		_reportState.emit(
			_reportState.value.copy(
				reportTitle = title,
				isReportTitleEdited = true
			)
		)
	}

	fun onSegmentChanged(showPreview: Boolean) = viewModelScope.launch {
		_reportState.emit(
			_reportState.value.copy(
				isPreviewShown = showPreview
			)
		)
	}

	fun onReportTextChanged(reportText: TextFieldValue) {
		_reportState.value = _reportState.value.copy(
			reportText = reportText,
			isReportTextEdited = true
		)
	}

	fun onAttachFile(file: File) {
		_reportState.value = _reportState.value.copy(
			isConfirmationDialogShown = true,
			providedFile = file
		)
	}

	fun onUploadFile() {
		_reportState.value = _reportState.value.copy(
			isFileUploading = true
		)
	}

	fun onConfirmationDialogDismissed() {
		_reportState.value = _reportState.value.copy(
			isConfirmationDialogShown = false,
			isFileUploading = false,
			providedFile = null
		)
	}

	fun onSendReport() {
		_reportState.value = _reportState.value.copy(
			isLoading = true
		)
	}

	fun onSendReportResult() {
		_reportState.value = _reportState.value.copy(
			isLoading = false
		)
	}

	fun resetState() {
		_reportState.update {
			NewReportUiState()
		}
	}

	fun onFileUploadingError(message: String) {
		_reportState.value = _reportState.value.copy(
			errorMessage = message
		)
	}

	fun onFileUploaded(fileInfoResponse: FileInfoResponse) = viewModelScope.launch(Dispatchers.Default) {
		val isFileAnImage = fileInfoResponse.filename.endsWith(
			"png",
			"jpg",
			"jpeg",
			"bmp"
		)
		// If selection length is 0, file name should be
		// the default name provided by file.filename;
		// Otherwise, user selection should be the provided file name.
		val newTextValue = MdAction.process(
			prefix = if (isFileAnImage) "![" else "[",
			postfix = "](${BuildConfig.API_URI}mfs/download/${fileInfoResponse.id})",
			delimiter = "",
			textValueToProcess = _reportState.value.reportText,
			emptySelectionDefaultText = fileInfoResponse.filename
		)

		_reportState.value = _reportState.value.copy(
			reportText = _reportState.value.reportText.copy(text = newTextValue.text)
		)
		delay(100)
		_reportState.value = _reportState.value.copy(
			reportText = _reportState.value.reportText.copy(selection = newTextValue.selection)
		)
	}

	private fun String.endsWith(vararg suffixes: String): Boolean =
		suffixes.map { suffix ->
			endsWith(suffix, true)
		}.contains(true)

}