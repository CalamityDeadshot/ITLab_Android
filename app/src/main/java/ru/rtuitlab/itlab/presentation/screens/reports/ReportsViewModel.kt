package ru.rtuitlab.itlab.presentation.screens.reports

import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.rtuitlab.itlab.common.Resource
import ru.rtuitlab.itlab.common.persistence.AuthStateStorage
import ru.rtuitlab.itlab.common.persistence.IAuthStateStorage
import ru.rtuitlab.itlab.data.remote.api.reports.models.Report
import ru.rtuitlab.itlab.data.remote.api.reports.models.ReportDto
import ru.rtuitlab.itlab.data.remote.api.reports.models.ReportSalary
import ru.rtuitlab.itlab.data.remote.api.users.models.UserResponse
import ru.rtuitlab.itlab.data.repository.ReportsRepository
import ru.rtuitlab.itlab.data.repository.UsersRepository
import javax.inject.Inject

@ExperimentalPagerApi
@HiltViewModel
class ReportsViewModel @Inject constructor(
	private val repository: ReportsRepository,
	private val usersRepository: UsersRepository,
	private val authStateStorage: IAuthStateStorage
) : ViewModel() {

	val userIdFlow = authStateStorage.userIdFlow.stateIn(
		viewModelScope,
		SharingStarted.Lazily,
		runBlocking { authStateStorage.userIdFlow.first() }
	)

	val pagerState = PagerState()
	val snackbarHostState = SnackbarHostState()
	val newReportSnackbarHostState = SnackbarHostState()

	private val _reportsResponseFlow: MutableStateFlow<Resource<List<Report>>> =
		MutableStateFlow(Resource.Loading)
	val reportsResponseFlow = _reportsResponseFlow.asStateFlow()

	val userResponsesFlow = usersRepository.usersResponsesFlow
	val usersFlow = usersRepository.cachedUsersFlow

	private val _searchQuery = MutableStateFlow("")
	val searchQuery = _searchQuery.asStateFlow()

	init {
		// There can be a situation where userResponsesFlow is a Resource.Loading,
		// which is ignored by a ResourceGroup in fetchReports(), causing an exception.
		// fetchReports() should be invoked only with present user data.
		viewModelScope.launch {
			userResponsesFlow.collect {
				it.handle(
					onSuccess = {
						fetchReports()
					}
				)
			}
		}
	}

	@Suppress("UNCHECKED_CAST")
	fun fetchReports() = viewModelScope.launch(Dispatchers.IO) {
		var resource: Resource<List<Report>> = Resource.Loading
		_reportsResponseFlow.emit(resource)
		// Executing in parallel, because any one data set does not depend on another
		val reports = async { repository.fetchReportsAboutUser() }
		val salaries = async { repository.fetchPricedReports(userIdFlow.value) }

		(reports.await() + salaries.await() + userResponsesFlow.value).handle(
			onError = {
				resource = Resource.Error(it)
			},
			onSuccess = { (r, s, u) ->
				resource = try {
					Resource.Success(
						(r as List<ReportDto>).map { reportDto ->
							val salary = (s as List<ReportSalary>).find { it.reportId == reportDto.id }
							reportDto.toReport(
								salary = salary,
								applicant = (u as List<UserResponse>).find { it.id == reportDto.assignees.reporterId }!!,
								approver = u.find { it.id == salary?.approverId },
								implementer = u.find { it.id == reportDto.assignees.implementerId }!!
							)
						}
					)
				} catch (e: Throwable) {
					Resource.Error(e.localizedMessage ?: "Parsing error")
				}
			}
		)

		_reportsResponseFlow.emit(resource)
	}

	fun onSearch(query: String) {
		_searchQuery.value = query
	}

	fun onSubmitReport(
		title: String,
		text: String,
		implementerId: String? = null,
		successMessage: String,
		onFinished: (isSuccessful: Boolean, newReport: ReportDto?) -> Unit
	) = viewModelScope.launch(Dispatchers.IO) {
		repository.createReport(
			implementerId,
			title,
			text
		).handle(
			onSuccess = { newReport ->
				_reportsResponseFlow.value = Resource.Success(
					(_reportsResponseFlow.value as Resource.Success).data +
							newReport.toReport(
								salary = null,
								approver = null,
								applicant = usersFlow.value.find { it.id == newReport.assignees.reporterId }!!.toUserResponse(),
								implementer = usersFlow.value.find {it.id == newReport.assignees.implementerId }!!.toUserResponse()
							)
				)
				withContext(Dispatchers.Main) {
					pagerState.scrollToPage(1)
					onFinished(true, newReport)
				}
				snackbarHostState.showSnackbar(successMessage)
			},
			onError = {
				withContext(Dispatchers.Main) {
					onFinished(false, null)
				}
				newReportSnackbarHostState.showSnackbar(it)
			}
		)
	}

}

fun List<Report>.performQuery(query: String): List<Report> {
	val q = query.trim()
	return filter {
		"${it.applicant.lastName} ${it.applicant.firstName} ${it.applicant.middleName}".contains(q, true) ||
				"${it.implementer.lastName} ${it.implementer.firstName} ${it.implementer.middleName}".contains(q, true) ||
				it.title.contains(q, true)
	}
}
