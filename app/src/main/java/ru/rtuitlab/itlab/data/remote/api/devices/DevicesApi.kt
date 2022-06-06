package ru.rtuitlab.itlab.data.remote.api.devices

import retrofit2.Response
import retrofit2.http.*
import ru.rtuitlab.itlab.data.remote.api.devices.models.*
import ru.rtuitlab.itlab.data.remote.api.users.models.UserResponse


interface DevicesApi {

        @GET("equipment?count=-1")
        suspend fun getDevices() : List<DeviceDetailDto>

        @DELETE("equipment/{id}")
        suspend fun deleteDevice(
                @Path("id") id: Int
        ): Response<Unit>

        @GET("equipment/{deviceId}")
        suspend fun getDevice(
                @Path("deviceId") deviceId: String
        ) : DeviceDetailDto

        @GET("user/{ownerid}")
        suspend fun getOwner(
                @Path("ownerid") ownerId: String
        ): UserResponse



        @POST("Equipment")
        suspend fun createEquipment(
                @Body equipmentNewRequest: EquipmentNewRequest
        ):DeviceDetailDto

        @PUT("Equipment")
        suspend fun updateEquipment(
                @Body equipmentEditRequest: EquipmentEditRequest
        ):DeviceDetailDto

        @HTTP(method = "DELETE",  path = "Equipment",hasBody = true)
        suspend fun deleteEquipment(
                @Body id:EquipmentIdRequest
        ):Response<Unit>

        //EquipmentType

        @POST("EquipmentType")
        suspend fun createEquipmentType(
                @Body equipmentTypeNewRequest: EquipmentTypeNewRequest
        ):Response<Unit>

        @GET("EquipmentType")
        suspend fun getListEquipmentType(
                @Query("match")match:String,
                @Query("all")all:Boolean
        ): List<EquipmentTypeResponse>

        //EquipmentOwner

        @POST("Equipment/user/{userId}")
        suspend fun  setOwner(
                @Path("userId") userId:String,
                @Body id:EquipmentIdRequest
        ): Response<Unit>

        @HTTP(method = "DELETE",  path = "Equipment/user/{userId}",hasBody = true)
        suspend fun  deleteOwner(
                @Path("userId") userId:String,
                @Body id:EquipmentIdRequest
        ): Response<Unit>

        //Filtering
        @GET("Equipment/user/free")
        suspend fun getFreeEquipments(): List<DeviceDetailDto>
}

