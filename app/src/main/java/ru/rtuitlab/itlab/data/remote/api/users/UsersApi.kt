package ru.rtuitlab.itlab.data.remote.api.users

import retrofit2.http.*
import ru.rtuitlab.itlab.data.remote.api.devices.models.DeviceModel
import ru.rtuitlab.itlab.data.remote.api.users.models.*

interface UsersApi {

    @GET
    suspend fun getUserInfo(
            @Url url: String,
            @Header("Authorization") token: String
    ): UserInfoModel

    @GET("user/{id}")
    suspend fun getUser(
            @Path("id") userId: String
    ): UserResponse

    @GET("/api/equipment/user/{id}")
    suspend fun getUserDevices(
            @Path("id") userId: String
    ): List<DeviceModel>

    @GET("/api/event/user/{id}")
    suspend fun getUserEvents(
            @Path("id") userId: String,
            @Query("begin") beginTime: String,
            @Query("end") endTime: String
    ): List<UserEventModel>

    @GET("user?count=-1")
    suspend fun getUsers() : List<UserResponse>

    @PUT("/api/account")
    suspend fun editUserInfo(
        @Body info: UserEditRequest
    ) : UserResponse

    @GET("/api/account/property/type")
    suspend fun getPropertyTypes() : List<UserPropertyTypeModel>

    @PUT("/api/account/property")
    suspend fun editUserProperty(
        @Body property: UserPropertyEditRequest
    ) : UserPropertyModel
}