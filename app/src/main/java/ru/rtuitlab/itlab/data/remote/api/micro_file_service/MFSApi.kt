package ru.rtuitlab.itlab.data.remote.api.micro_file_service

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import ru.rtuitlab.itlab.data.remote.api.micro_file_service.models.FileInfoResponse

interface MFSApi {
	@Streaming
	@GET("/api/mfs/download/{id}")
	suspend fun downloadFile(
		@Path("id") fileId:String //required
	): Response<Unit>

	@GET("/api/mfs/files")
	suspend fun getFilesInfo(
		@Query("user") userId:String? = null,
		@Query("sorted_by") sortedBy:String? = null
	):List<FileInfoResponse>


	@POST("/api/mfs/files/upload")
	suspend fun uploadFile(
		@Body body:RequestBody
	): FileInfoResponse

	@GET("/api/mfs/files/{id}")
	suspend fun getFileInfo(
		@Path("id") fileId:String //required
	):FileInfoResponse

	@DELETE("/api/mfs/files/{id}")
	suspend fun deleteFile(
		@Path("id") fileId:String //required
	):Response<Unit>


}