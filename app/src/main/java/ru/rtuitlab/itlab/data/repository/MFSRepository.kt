package ru.rtuitlab.itlab.data.repository

import android.content.Context
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import ru.rtuitlab.itlab.common.ResponseHandler
import ru.rtuitlab.itlab.data.remote.api.micro_file_service.MFSApi
import java.io.File
import javax.inject.Inject
import okhttp3.MultipartBody

import okhttp3.RequestBody
import retrofit2.http.Url
import ru.rtuitlab.itlab.BuildConfig
import ru.rtuitlab.itlab.presentation.utils.DownloadFileFromWeb
import java.net.URL


class MFSRepository @Inject constructor(
	private val MFSApi: MFSApi,
	private val handler: ResponseHandler
) {



	fun fetchFile(fileId:String?):String {
		//MFSApi.downloadFile(fileId)
		return (BuildConfig.API_URI+"mfs/download/"+fileId)
	}

	suspend fun fetchFilesInfo(userId:String?=null,sortedBy:String?=null) = handler {
		MFSApi.getFilesInfo(userId,sortedBy)
	}
	suspend fun fetchFileInfo(fileId:String) = handler {
		MFSApi.getFileInfo(fileId)
	}
	suspend fun uploadFile(file:File, fileDescription:String) = handler {
		val requestBody: RequestBody = MultipartBody.Builder()
			.setType(MultipartBody.FORM)
			.addFormDataPart("uploadingForm", file.name,(file).asRequestBody("text/plain".toMediaTypeOrNull()))
			.addFormDataPart("fileDescription", fileDescription)
			.build()

		MFSApi.uploadFile(requestBody)
	}
	suspend fun deleteFile(fileId: String) = handler {
		MFSApi.deleteFile(fileId)
	}
}