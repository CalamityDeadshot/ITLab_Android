package ru.rtuitlab.itlab.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.rtuitlab.itlab.common.Resource
import ru.rtuitlab.itlab.data.local.users.models.PropertyWithType
import ru.rtuitlab.itlab.data.local.users.models.UserEntity
import ru.rtuitlab.itlab.data.local.users.models.UserPropertyEntity
import ru.rtuitlab.itlab.data.local.users.models.UserWithProperties
import ru.rtuitlab.itlab.data.remote.api.users.models.*

interface UsersRepository {
    fun getAllUsers(): Flow<List<UserWithProperties>>

    fun searchUsers(query: String): Flow<List<UserWithProperties>>

    suspend fun getUserById(id: String): UserWithProperties?

    fun observeUserById(id: String): Flow<UserWithProperties?>

    suspend fun getCurrentUser(): UserWithProperties?

    fun observeCurrentUser(): Flow<UserWithProperties?>

    suspend fun getPropertyTypes(): List<UserPropertyTypeModel>

    fun observePropertyTypes(): Flow<List<UserPropertyTypeModel>>

    suspend fun getProperties(): List<UserPropertyEntity>

    suspend fun getPropertiesWithTypes(): List<PropertyWithType>

    suspend fun editUserInfo(info: UserEditRequest): Resource<UserResponse>

    suspend fun editUserProperty(
        propertyId: String,
        newValue: String
    ): Resource<UserPropertyModel>

    suspend fun insertUser(
        user: UserEntity,
        properties: List<UserPropertyEntity>
    )

    suspend fun fetchUserInfo(url: String, accessToken: String): Resource<UserInfoModel>

    suspend fun updateAllUsers(): Resource<List<UserResponse>>

    suspend fun updatePropertyTypes(): Resource<List<UserPropertyTypeModel>>

    suspend fun updateUser(id: String): Resource<UserResponse>

    suspend fun deleteUser(
        user: UserEntity
    )
}