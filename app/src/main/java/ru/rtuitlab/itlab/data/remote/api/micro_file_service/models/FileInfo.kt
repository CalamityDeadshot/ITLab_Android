package ru.rtuitlab.itlab.data.remote.api.micro_file_service.models

import android.graphics.Bitmap
import kotlinx.serialization.Serializable
import ru.rtuitlab.itlab.data.remote.api.users.models.User
import ru.rtuitlab.itlab.data.remote.api.users.models.UserResponse

@Serializable
data class FileInfoResponse (
	val chunkSize: Int,
	val filename: String,
	val id: String,
	val length: Int,
	val metadata: Metadata,
	val uploadDate: String,

)
{
	fun toFileInfo(sender: User?) = FileInfo (
		chunkSize = chunkSize,
		filename = filename,
		id = id,
		length = length,
		fileDescription = metadata.fileDescription,
		fileSender = metadata.fileSender,
		uploadDate = uploadDate,
		senderfirstName = sender?.firstName,
		senderlastName = sender?.lastName,
		sendermiddleName = sender?.middleName,
		applicant = sender?.toUserResponse()!!


	)
}

data class FileInfo(
	val chunkSize: Int,
	val filename: String,
	val id: String,
	val length: Int,
	val fileDescription: String,
	val fileSender: String,
	val uploadDate: String,
	val senderfirstName:String?,
	val senderlastName: String?,
	val sendermiddleName:String?,
	val applicant: UserResponse,
){

}