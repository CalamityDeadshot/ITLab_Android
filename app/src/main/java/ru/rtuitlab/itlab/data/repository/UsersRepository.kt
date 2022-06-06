package ru.rtuitlab.itlab.data.repository

import ru.rtuitlab.itlab.common.ResponseHandler
import ru.rtuitlab.itlab.data.remote.api.users.UsersApi
import ru.rtuitlab.itlab.data.remote.api.users.models.UserEditRequest
import ru.rtuitlab.itlab.data.remote.api.users.models.UserPropertyEditRequest
import ru.rtuitlab.itlab.data.remote.api.users.models.UserResponse
import javax.inject.Inject

class UsersRepository @Inject constructor(
    private val usersApi: UsersApi,
    private val handler: ResponseHandler
) {
    suspend fun fetchUserInfo(url: String, accessToken: String) = handler {
        usersApi.getUserInfo(url, "Bearer $accessToken")
    }

    suspend fun fetchUserCredentials(userId: String) = handler {
        usersApi.getUser(userId)
    }

 suspend fun fetchUserDevices(userId: String) = handler {
        usersApi.getUserDevices(userId)
    }
    suspend fun fetchUserEvents(userId: String, beginTime: String, endTime: String) = handler {
        usersApi.getUserEvents(userId, beginTime, endTime)
    }

    suspend fun fetchUsers() = handler {
        usersApi.getUsers()
    }

    suspend fun editUserInfo(info: UserEditRequest) = handler {
        usersApi.editUserInfo(info)
    }

    suspend fun fetchPropertyTypes() = handler {
        usersApi.getPropertyTypes()
    }

    suspend fun editUserProperty(id: String, value: String) = handler {
        usersApi.editUserProperty(
            UserPropertyEditRequest(id, value)
        )
    }

    suspend fun getUserById(userId:String) = handler {
        usersApi.getUser(userId)
    }
}