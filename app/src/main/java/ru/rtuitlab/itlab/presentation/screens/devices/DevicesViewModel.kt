package ru.rtuitlab.itlab.presentation.screens.devices

import android.util.Log
import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import ru.rtuitlab.itlab.common.Resource
import ru.rtuitlab.itlab.common.emitInIO
import ru.rtuitlab.itlab.common.persistence.AuthStateStorage
import ru.rtuitlab.itlab.data.remote.api.devices.models.*
import ru.rtuitlab.itlab.data.remote.api.users.models.User
import ru.rtuitlab.itlab.data.remote.api.users.models.UserClaimCategories
import ru.rtuitlab.itlab.data.remote.api.users.models.UserResponse
import ru.rtuitlab.itlab.data.repository.DevicesRepository
import ru.rtuitlab.itlab.data.repository.UsersRepository
import java.sql.Time
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
        private val devicesRepo: DevicesRepository,
        private val usersRepository: UsersRepository,
        private val authStateStorage:  AuthStateStorage
) : ViewModel() {

        private val userClaimsFlow = authStateStorage.userClaimsFlow
        private var isAccesible:Boolean = false
        private var _accesibleFlow = MutableStateFlow(isAccesible)
        val accesibleFlow = _accesibleFlow.asStateFlow()
        init {
                viewModelScope.launch {
                        userClaimsFlow.collect {

                                isAccesible = it.contains(UserClaimCategories.DEVICES.EDIT)
                                _accesibleFlow.value = isAccesible
                        }
                }
        }

        private val _freeFilteringIs = MutableStateFlow(false)
        val freeFilteringIs = _freeFilteringIs.asStateFlow()

        private var _deviceIdFlow = MutableStateFlow("")
        val deviceIdFlow = _deviceIdFlow.asStateFlow()

        private val _devicesResponsesFlow = MutableStateFlow<Resource<MutableList<Pair<DeviceDetailDto,UserResponse?>>>>(Resource.Loading)
        val deviceResponsesFlow = _devicesResponsesFlow.asStateFlow().also {fetchDevices() }


        var cachedDevices = mutableListOf<DeviceDetails>()

        private val _devicesFlow = MutableStateFlow(cachedDevices)
        val devicesFlow = _devicesFlow.asStateFlow()

        private var deviceFromSheet: DeviceDetails? = null

        private val _deviceFromSheetFlow = MutableStateFlow(deviceFromSheet)
        val deviceFromSheetFlow = _deviceFromSheetFlow.asStateFlow()
        fun setDeviceFromSheet(deviceDetails: DeviceDetails?){
                deviceFromSheet = deviceDetails
                _deviceFromSheetFlow.value = deviceFromSheet
        }



        private val _equipmentTypeResponsesFlow = MutableStateFlow<Resource<List<EquipmentTypeResponse>>>(Resource.Loading)
        val equipmentTypeResponsesFlow = _equipmentTypeResponsesFlow.asStateFlow().also { fetchListEquipmentType() }

        var cachedEquipmentType = emptyList<EquipmentTypeResponse>()

        private val _equipmentTypeFlow = MutableStateFlow(cachedEquipmentType)
        val equipmentTypeFlow = _equipmentTypeFlow.asStateFlow()

        val snackbarHostState = SnackbarHostState()

        private fun fetchListEquipmentType() = _equipmentTypeResponsesFlow.emitInIO(viewModelScope){
                devicesRepo.fetchListEquipmentType("",true)
        }
        fun onCreateEquipmentType(equipmentTypeNewRequest: EquipmentTypeNewRequest,
                                  onFinish: (Boolean) -> Unit) = viewModelScope.launch {
                devicesRepo.fetchEquipmentTypeNew(equipmentTypeNewRequest).handle(
                        onError = { msg ->
                                onFinish(false)
                                snackbarHostState.showSnackbar(
                                        message = msg
                                )
                        },
                        onSuccess = {
                                onFinish(true)
                                snackbarHostState.showSnackbar(
                                        message = "Successful"
                                )
                        }
                )
        }
        fun onCreateEquipment(equipmentNewRequest: EquipmentNewRequest,createdDevice: (DeviceDetailDto?) ->Unit) = viewModelScope.launch {
                devicesRepo.fetchEquipmentNew(equipmentNewRequest).handle(
                        onError = { msg ->
                                createdDevice(null)
                                Log.d("DeviceCreate",msg)

                                snackbarHostState.showSnackbar(
                                        message = msg
                                )
                        },
                        onSuccess = {deviceDto ->
                                createdDevice(deviceDto)
                                Log.d("DeviceCreate","$deviceDto")
                                snackbarHostState.showSnackbar(
                                        message = "Successful"
                                )
                        }
                )
        }
        fun onUpdateEquipment(equipmentEditRequest: EquipmentEditRequest,editedDevice: (DeviceDetailDto?) ->Unit) = viewModelScope.launch {
                devicesRepo.fetchEquipmentEdit(equipmentEditRequest).handle(
                        onError = { msg ->
                                editedDevice(null)
                                snackbarHostState.showSnackbar(
                                        message = msg
                                )
                        },
                        onSuccess = { deviceDto ->
                                editedDevice(deviceDto)
                                snackbarHostState.showSnackbar(
                                        message = "Successful"
                                )
                        }
                )
        }
        fun onDeleteEquipment(id: String,onFinish: (Boolean) ->Unit) = viewModelScope.launch {
                devicesRepo.fetchEquipmentDelete(id).handle(
                        onError = { msg ->
                                onFinish(false)
                                snackbarHostState.showSnackbar(
                                        message = msg
                                )
                        },
                        onSuccess = {
                                onFinish(true)
                                snackbarHostState.showSnackbar(
                                        message = "Successful"
                                )
                        }
                )
        }

        fun onSearch(query: String) {
                _devicesFlow.value = cachedDevices.filter { device ->

                        "${device.equipmentType?.title} ${device.serialNumber} ${device.number} ${device.ownerlastName} ${device.ownerfirstName}".contains(query.trim(), ignoreCase = true)
                } as MutableList<DeviceDetails>
        }
        private val _equipmentTypeFilterFlow = MutableStateFlow(cachedEquipmentType)
        val equipmentTypeFilterFlow = _equipmentTypeFilterFlow.asStateFlow()

        fun equipmentfiltering(match:String){
                _equipmentTypeFilterFlow.value = cachedEquipmentType.filter { equipment ->
                        equipment.title.contains(match.trim(), ignoreCase = true)
                }
        }

        fun onResourceSuccess(devices: List<Pair<DeviceDetailDto,UserResponse?>>) {

                cachedDevices = devices.map {
                        if(it.second!=null){
                                it.first.toDevice(it.second!!.toUser())
                        }else{
                                it.first.toDevice(null)
                        }

                } as MutableList<DeviceDetails>
                _devicesFlow.value = cachedDevices
        }

        fun onEquipmentTypeResourceSuccess(equipmentTypeResponse: List<EquipmentTypeResponse>) {

                cachedEquipmentType =equipmentTypeResponse
                _equipmentTypeFlow.value = cachedEquipmentType
        }

        fun onRefreshEquipmentTypes() = fetchListEquipmentType()
        fun onRefresh() {
                if (_freeFilteringIs.value) {
                        fetchFreeDevices()
                } else {
                        fetchDevices()

                }
        }

        private fun fetchDevices() = _devicesResponsesFlow.emitInIO(viewModelScope) {
                var resources: Resource<MutableList<Pair<DeviceDetailDto, UserResponse?>>> = Resource.Loading

                var usersList: MutableList<UserResponse>? = null
                usersRepository.fetchUsers().handle(
                        onSuccess = { users ->
                                usersList = users as MutableList<UserResponse>
                        }
                )
                devicesRepo.fetchDevices().handle (
                        onSuccess = { details ->
                                val listPair = mutableListOf<Pair<DeviceDetailDto, UserResponse?>>()

                                details.map { detail ->
                                        var resource: Pair<DeviceDetailDto, UserResponse?>
                                        resource = detail to null
                                        if(detail.ownerId!=null) {
                                                val sender = usersList?.find { it.id == detail.ownerId }
                                                resource = detail to sender
                                        }
                                        listPair.add(resource)

                                }
                                resources = Resource.Success(listPair)

                        },
                        onError = {resources = Resource.Error(it)}
                )
                resources

        }

        //Owner

        private val _userResponsesFlow = MutableStateFlow<Resource<List<UserResponse>>>(Resource.Loading)
        val userResponsesFlow = _userResponsesFlow.asStateFlow().also { fetchUsers() }

        var cachedUsers = emptyList<UserResponse>()

        private val _usersFlow = MutableStateFlow(cachedUsers)
        val usersFlow = _usersFlow.asStateFlow()

        private fun fetchUsers() =
                _userResponsesFlow.emitInIO(viewModelScope) {
                        usersRepository.fetchUsers()
                }

        fun onUserResourceSuccess(users: List<UserResponse>) {
                cachedUsers = users
                _usersFlow.value = cachedUsers
        }


        private val _userFilterFlow = MutableStateFlow(cachedUsers )
        val userFilterFlow = _userFilterFlow.asStateFlow()

        fun userfiltering(match:String){
                _userFilterFlow.value = cachedUsers .filter {  user->
                        "${user.lastName} ${user.firstName} ${user.middleName}".contains(match.trim(), ignoreCase = true)
                }
        }

        fun onChangeEquipmentOwner(ownerId: String,equipmentId: String,
                                  onFinish: (Boolean) -> Unit) = viewModelScope.launch {
                devicesRepo.fetchEquipmentOwnerNew(ownerId,equipmentId).handle(
                        onError = { msg ->
                                onFinish(false)
                                snackbarHostState.showSnackbar(
                                        message = msg
                                )
                        },
                        onSuccess = {
                                onFinish(true)
                                snackbarHostState.showSnackbar(
                                        message = "Successful"
                                )
                        }
                )
        }
        fun onPickUpEquipment(ownerId: String,equipmentId: String,
                                   onFinish: (Boolean) -> Unit) = viewModelScope.launch {
                devicesRepo.fetchEquipmentOwnerPickUp(ownerId,equipmentId).handle(
                        onError = { msg ->
                                onFinish(false)
                                snackbarHostState.showSnackbar(
                                        message = msg
                                )
                        },
                        onSuccess = {
                                onFinish(true)
                                snackbarHostState.showSnackbar(
                                        message = "Successful"
                                )
                        }
                )
        }

        //Filtering

        fun onFreeRefresh() = fetchFreeDevices()


        fun onChangeFiltering(){
                _freeFilteringIs.value = !_freeFilteringIs.value
                onRefresh()
        }


        private fun fetchFreeDevices() = _devicesResponsesFlow.emitInIO(viewModelScope){
                var resources: Resource<MutableList<Pair<DeviceDetailDto, UserResponse?>>> = Resource.Loading

                devicesRepo.fetchFreeEquipmentList().handle (
                        onSuccess = { details ->
                                val listPair = mutableListOf<Pair<DeviceDetailDto, UserResponse?>>()

                                details.map {
                                        var resource: Pair<DeviceDetailDto, UserResponse?>
                                        resource = it to null
                                        Log.d("DeviceViewModel",it.toString())
                                        if(it.ownerId!=null) {
                                                devicesRepo.fetchOwner(it.ownerId).handle(
                                                        onSuccess = { userResponce ->
                                                                resource = it to userResponce
                                                        }
                                                )
                                        }
                                        listPair.add(resource)

                                }
                                resources = Resource.Success(listPair)

                        },
                        onError = {resources = Resource.Error(it)}
                )
                resources

        }

        fun onUpdateCachedDevice(tempDevice: DeviceDetailDto) = viewModelScope.launch  {
                var resource: Pair<DeviceDetailDto, UserResponse?>
                resource = tempDevice to null
                if(tempDevice.ownerId!=null) {
                        devicesRepo.fetchOwner(tempDevice.ownerId).handle(
                                onSuccess = { userResponce ->
                                        resource = tempDevice to userResponce

                                }
                        )
                }
                val cachedDevice:DeviceDetails =
                        if(resource.second!=null){
                                resource.first.toDevice(resource.second!!.toUser())
                        }else{
                                resource.first.toDevice(null)
                        }


                val oldDevice =  cachedDevices.find{ it -> it.id==cachedDevice.id}
                val index = cachedDevices.indexOf(oldDevice)
                cachedDevices[index] = cachedDevice
                val mutableListofPair = cachedDevices.map{
                        var pair :Pair<DeviceDetailDto,UserResponse?>
                        var deviceDetailDto: DeviceDetailDto? = null
                        var userResponse: UserResponse? = null
                        it.toDeviceDtoAndUser(
                                deviceDetailDto = {
                                                  deviceDetailDto = it
                                },
                                owner = {
                                        if (it != null) {
                                                userResponse = it.toUserResponse()
                                        }
                                }
                        )
                        pair = deviceDetailDto!! to userResponse
                        pair
                } as MutableList
                _devicesFlow.value = cachedDevices
                _devicesResponsesFlow.value = Resource.Success(mutableListofPair)
                _deviceFromSheetFlow.value = cachedDevice
        }

        fun onDeleteCachedDevice(id: String) = viewModelScope.launch  {

                val oldDevice =  cachedDevices.find{ it -> it.id==id}
                cachedDevices.remove(oldDevice)
                val mutableListofPair = cachedDevices.map{
                        var pair :Pair<DeviceDetailDto,UserResponse?>
                        var deviceDetailDto: DeviceDetailDto? = null
                        var userResponse: UserResponse? = null
                        it.toDeviceDtoAndUser(
                                deviceDetailDto = {
                                        deviceDetailDto = it
                                },
                                owner = {
                                        if (it != null) {
                                                userResponse = it.toUserResponse()
                                        }
                                }
                        )
                        pair = deviceDetailDto!! to userResponse
                        pair
                } as MutableList
                _devicesFlow.value = cachedDevices
                _devicesResponsesFlow.value = Resource.Success(mutableListofPair)
        }

        fun onCreateCachedDevice(tempDevice: DeviceDetailDto) = viewModelScope.launch  {
                var resource: Pair<DeviceDetailDto, UserResponse?>
                resource = tempDevice to null
                if(tempDevice.ownerId!=null) {
                        devicesRepo.fetchOwner(tempDevice.ownerId).handle(
                                onSuccess = { userResponce ->
                                        resource = tempDevice to userResponce

                                }
                        )
                }
                val cachedDevice:DeviceDetails =
                        if(resource.second!=null){
                                resource.first.toDevice(resource.second!!.toUser())
                        }else{
                                resource.first.toDevice(null)
                        }

                cachedDevices.add(cachedDevice)
                val mutableListofPair = cachedDevices.map{
                        var pair :Pair<DeviceDetailDto,UserResponse?>
                        var deviceDetailDto: DeviceDetailDto? = null
                        var userResponse: UserResponse? = null
                        it.toDeviceDtoAndUser(
                                deviceDetailDto = {
                                        deviceDetailDto = it
                                },
                                owner = {
                                        if (it != null) {
                                                userResponse = it.toUserResponse()
                                        }
                                }
                        )
                        pair = deviceDetailDto!! to userResponse
                        pair
                } as MutableList
                _devicesFlow.value = cachedDevices
                _devicesResponsesFlow.value = Resource.Success(mutableListofPair)
        }

}
