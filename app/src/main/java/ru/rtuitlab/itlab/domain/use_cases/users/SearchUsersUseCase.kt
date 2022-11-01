package ru.rtuitlab.itlab.domain.use_cases.users

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.rtuitlab.itlab.data.remote.api.users.models.UserResponse
import ru.rtuitlab.itlab.domain.repository.UsersRepositoryInterface
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val repo: UsersRepositoryInterface
) {
    operator fun invoke(query: String): Flow<List<UserResponse>> {
        return repo.searchUsers(query).map {
            it.map {
                it.toUserResponse()
            }
        }
    }
}