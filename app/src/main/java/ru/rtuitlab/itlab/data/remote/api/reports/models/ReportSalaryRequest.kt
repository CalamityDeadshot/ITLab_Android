package ru.rtuitlab.itlab.data.remote.api.reports.models

import kotlinx.serialization.Serializable

@Serializable
data class ReportSalaryRequest(
	val count: Int,
	val description: String
) {

}