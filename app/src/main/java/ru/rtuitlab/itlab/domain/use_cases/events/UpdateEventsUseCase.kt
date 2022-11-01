package ru.rtuitlab.itlab.domain.use_cases.events

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.rtuitlab.itlab.domain.repository.EventsRepositoryInterface
import ru.rtuitlab.itlab.common.nowAsIso8601
import javax.inject.Inject

class UpdateEventsUseCase @Inject constructor(
    private val repo: EventsRepositoryInterface
) {
    suspend operator fun invoke(
        begin: String?,
        end: String?
    ) = withContext(Dispatchers.IO) {
        repo.updateEvents(begin, end)
    }

    suspend fun pending() = withContext(Dispatchers.IO) {
        repo.updateEvents(begin = nowAsIso8601())
    }

    suspend fun user(
        userId: String,
        begin: String? = null,
        end: String? = null
    ) = withContext(Dispatchers.IO) {
        repo.updateUserEvents(userId, begin, end)
    }
}