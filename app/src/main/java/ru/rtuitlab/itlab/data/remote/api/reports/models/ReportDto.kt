package ru.rtuitlab.itlab.data.remote.api.reports.models


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.rtuitlab.itlab.data.local.reports.models.ReportEntity
import ru.rtuitlab.itlab.data.remote.api.users.models.UserResponse

@Serializable
data class ReportDto(
    val id: String,
    val assignees: Assignees,
    val date: String,
    val text: String,
    @SerialName("name")
    val title: String?,
    @SerialName("archived")
    val isArchived: Boolean? = null
) {
    fun toReport(
        salary: ReportSalary?,
        applicant: UserResponse,
        approver: UserResponse?,
        implementer: UserResponse
    ) = Report(
        id = id,
        title = if (title.isNullOrBlank()) text.substringBefore("@\n\t\n@") else title,
        applicant = applicant,
        applicationDate = date,
        applicationCommentMd = text.substringAfter("@\n\t\n@"),
        approver = approver,
        approvingDate = salary?.approvingDate,
        salary = salary?.count,
        approvingCommentMd = salary?.description,
        implementer = implementer
    )

    fun toReportEntity() = ReportEntity(
        id = id,
        date = date,
        text = text,
        title = title,
        isArchived = isArchived,
        reporterId = assignees.reporterId,
        implementerId = assignees.implementerId
    )
}

data class Report(
    val id: String,
    val title: String,

    val applicant: UserResponse,
    val applicationDate: String,
    val applicationCommentMd: String,

    val approver: UserResponse? = null,
    val approvingDate: String? = null,
    val salary: Int? = null,
    val approvingCommentMd: String? = null,

    val implementer: UserResponse
)