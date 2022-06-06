package ru.rtuitlab.itlab.data.repository

import android.util.Log
import ru.rtuitlab.itlab.common.ResponseHandler
import ru.rtuitlab.itlab.data.remote.api.devices.DevicesApi
import ru.rtuitlab.itlab.data.remote.api.devices.models.EquipmentEditRequest
import ru.rtuitlab.itlab.data.remote.api.devices.models.EquipmentIdRequest
import ru.rtuitlab.itlab.data.remote.api.devices.models.EquipmentNewRequest
import ru.rtuitlab.itlab.data.remote.api.devices.models.EquipmentTypeNewRequest
import javax.inject.Inject

class DevicesRepository @Inject constructor(
        private val devicesApi: DevicesApi,
        private val handler: ResponseHandler
) {


        suspend fun fetchDevices() = handler {
                devicesApi.getDevices()
        }
        suspend fun fetchOwner(ownerId: String) = handler {
                devicesApi.getOwner(ownerId)
        }

        suspend fun fetchEquipmentNew(equipmentNewRequest: EquipmentNewRequest) = handler {
               val deviceDetailDto =  devicesApi.createEquipment(equipmentNewRequest)
                Log.d("REPO","$deviceDetailDto")
                deviceDetailDto
        }
        suspend fun fetchEquipmentEdit(equipmentEditRequest: EquipmentEditRequest) = handler {
                devicesApi.updateEquipment(equipmentEditRequest)
        }
        suspend fun fetchEquipmentDelete(id:String) = handler {
                devicesApi.deleteEquipment(EquipmentIdRequest( id))
        }

        //EquipmentType
        suspend fun fetchEquipmentTypeNew(equipmentTypeNewRequest: EquipmentTypeNewRequest) = handler {
                devicesApi.createEquipmentType(equipmentTypeNewRequest)
        }
        suspend fun fetchListEquipmentType(match:String,all:Boolean) = handler {
                devicesApi.getListEquipmentType(match,all)
        }
        //EquipmentOwner
        suspend fun fetchEquipmentOwnerNew(ownerid:String,equipmentId:String) = handler {
                devicesApi.setOwner(ownerid, EquipmentIdRequest( equipmentId))
        }
        suspend fun fetchEquipmentOwnerPickUp(ownerid:String,equipmentId:String) = handler {
                devicesApi.deleteOwner(ownerid,  EquipmentIdRequest( equipmentId))
        }


        //Filtering
        suspend fun fetchFreeEquipmentList() = handler{
                devicesApi.getFreeEquipments()
        }

}

