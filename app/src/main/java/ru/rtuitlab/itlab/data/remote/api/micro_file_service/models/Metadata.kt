package ru.rtuitlab.itlab.data.remote.api.micro_file_service.models

import kotlinx.serialization.Serializable

@Serializable
data class Metadata(
	val fileDescription: String,
	val fileSender: String
) {
}