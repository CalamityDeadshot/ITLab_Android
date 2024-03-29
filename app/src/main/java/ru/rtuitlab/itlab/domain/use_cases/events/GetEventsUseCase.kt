package ru.rtuitlab.itlab.domain.use_cases.events

import kotlinx.coroutines.flow.map
import ru.rtuitlab.itlab.domain.repository.EventsRepository
import ru.rtuitlab.itlab.common.extensions.endOfTimes
import ru.rtuitlab.itlab.common.extensions.nowAsIso8601
import javax.inject.Inject

class GetEventsUseCase @Inject constructor(
    private val repo: EventsRepository
) {
    operator fun invoke() = repo.getEvents().map {
        it.map {
            it.toEventModel()
        }
    }

    fun search(
        query: String,
        begin: String = nowAsIso8601(),
        end: String = endOfTimes
    ) = repo.searchEvents(query, begin, end).map {
        it.map {
            it.toEventModel()
        }
    }
}