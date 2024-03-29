package ru.rtuitlab.itlab.data.repository

import kotlinx.coroutines.CoroutineScope
import ru.rtuitlab.itlab.common.Resource
import ru.rtuitlab.itlab.common.ResponseHandler
import ru.rtuitlab.itlab.data.local.AppDatabase
import ru.rtuitlab.itlab.data.remote.api.reports.ReportsApi
import ru.rtuitlab.itlab.data.remote.api.reports.models.ReportDto
import ru.rtuitlab.itlab.data.remote.api.reports.models.ReportRequest
import ru.rtuitlab.itlab.data.remote.api.reports.models.ReportSalary
import ru.rtuitlab.itlab.data.remote.api.reports.models.ReportSalaryRequest
import ru.rtuitlab.itlab.data.repository.util.tryUpdate
import ru.rtuitlab.itlab.domain.repository.ReportsRepository
import javax.inject.Inject

class ReportsRepositoryImpl @Inject constructor(
    private val reportsApi: ReportsApi,
    private val handler: ResponseHandler,
    private val scope: CoroutineScope,
    db: AppDatabase
): ReportsRepository {

    private val dao = db.reportsDao

    override var reportsUpdatedAtLeastOnce: Boolean = false
        private set

    override fun getReports() = dao.getReports()

    override fun searchReports(query: String) = dao.searchReports(query)

    override fun searchReportsAboutUser(
        searchQuery: String,
        userId: String
    ) = dao.searchReportsAboutUser(searchQuery, userId)

    override fun searchReportsFromUser(
        searchQuery: String,
        userId: String
    ) = dao.searchReportsFromUser(searchQuery, userId)

    /**
     * Updates all reports regardless of user`s claims,
     * then updates report salaries with [userId].
     * This is needed to preserve order of UPSERT operations
     * since we may not have control of it outside this class
     */
    override suspend fun updateReports(userId: String) = tryUpdate(
        inScope = scope,
        withHandler = handler,
        from = { reportsApi.getReports() },
        into = {
            reportsUpdatedAtLeastOnce = true
            dao.upsertReports(
                it.map {
                    it.toReportEntity()
                }
            )
            updateReportSalaries(userId, it.map { it.id })
        }
    )

    override suspend fun updateUserReports(
        userId: String,
        begin: String?,
        end: String?
    ) = tryUpdate(
        inScope = scope,
        withHandler = handler,
        from = { reportsApi.getReportsOfEmployee(
            dateBegin = begin,
            dateEnd = end,
            employeeId = userId
        ) },
        into = {
            dao.upsertReports(
                it.map {
                    it.toReportEntity()
                }
            )
            updateReportSalaries(userId)
        }
    )

    override suspend fun updateReportSalaries(userId: String) = tryUpdate(
        inScope = scope,
        withHandler = handler,
        from = { reportsApi.getListReportSalary(userId) },
        into = { dao.upsertReportsSalary(it) }
    )

    /**
     * There can be a situation where server`s data violates foreign keys constraint
     * on reportId field of [ReportSalary]. This method filters out salaries that do that
     * and then upserts. This is highly undesirable since time complexity is O(n^2)
     */
    override suspend fun updateReportSalaries(
        userId: String,
        reportIds: List<String>
    ) = tryUpdate(
        inScope = scope,
        withHandler = handler,
        from = { reportsApi.getListReportSalary(userId) },
        into = { dao.upsertReportsSalary(it.filter { it.reportId in reportIds }) }
    )

    /**
     * Updates all reports regardless of user`s claims,
     * then updates report salaries with [userId].
     * This is needed to preserve order of UPSERT operations
     * since we may not have control of it outside this class
     */
    override suspend fun updateReports(
        sortedBy: String,
        userId: String
    ) = tryUpdate(
        inScope = scope,
        withHandler = handler,
        from = { reportsApi.getReports(sortedBy) },
        into = {
            reportsUpdatedAtLeastOnce = true
            dao.upsertReports(
                it.map {
                    it.toReportEntity()
                }
            )
            updateReportSalaries(userId)
        }
    )

    override suspend fun createReport(
        implementerId: String?,
        name: String?,
        text: String
    ): Resource<ReportDto> = tryUpdate(
        inScope = scope,
        withHandler = handler,
        from = {
            reportsApi.createReport(
                implementerId = implementerId,
                report = ReportRequest(
                    name = name,
                    text = text
                )
            )
        },
        into = {
            dao.upsertReport(it.toReportEntity())
        }
    )

    override suspend fun updateReport(id: String) = tryUpdate(
        inScope = scope,
        withHandler = handler,
        from = { reportsApi.getReport(id) },
        into = { dao.upsertReport(it.toReportEntity()) }
    )

    override suspend fun updateSalaryForUser(userId: String) = tryUpdate(
        inScope = scope,
        withHandler = handler,
        from = { reportsApi.getListReportSalary(userId) },
        into = { dao.upsertReportsSalary(it) }
    )

    override suspend fun editReportSalary(
        reportId: String,
        salary: ReportSalaryRequest
    ): Resource<ReportSalary> = tryUpdate(
        inScope = scope,
        withHandler = handler,
        from = { reportsApi.updateReportSalary(reportId, salary) },
        into = {
            dao.upsertReportsSalary(it)
        }
    )

    override suspend fun clearReports() {
        dao.deleteReportSalaries()
        dao.deleteReports()
        reportsUpdatedAtLeastOnce = false
    }
}