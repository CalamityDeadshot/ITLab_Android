package ru.rtuitlab.itlab.data.remote.api.devices.models

import kotlinx.serialization.Serializable

@Serializable
data class DeviceModel (
    val id: String,
    val serialNumber: String? = null,
    val description : String? = null,
    val number: Int,
    val equipmentType: EquipmentTypeResponse,
    val equipmentTypeId: String,
    val ownerId: String? = null,
    val parentId: String? = null,
    val children: List<DeviceDetailDto>? = null
)
