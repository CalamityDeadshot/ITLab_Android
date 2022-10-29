package ru.rtuitlab.itlab.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import ru.rtuitlab.itlab.common.Resource
import ru.rtuitlab.itlab.common.ResponseHandler
import ru.rtuitlab.itlab.data.local.AppDatabase
import ru.rtuitlab.itlab.data.local.events.models.EventWithShiftsAndSalary
import ru.rtuitlab.itlab.data.local.events.models.EventWithType
import ru.rtuitlab.itlab.data.remote.api.events.EventsApi
import ru.rtuitlab.itlab.data.remote.api.users.models.UserEventModel
import ru.rtuitlab.itlab.data.repository.util.tryUpdate
import ru.rtuitlab.itlab.domain.repository.EventsRepositoryInterface
import ru.rtuitlab.itlab.presentation.ui.extensions.nowAsIso8601
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventsRepositoryImpl @Inject constructor(
    private val eventsApi: EventsApi,
    private val handler: ResponseHandler,
    private val coroutineScope: CoroutineScope,
    db: AppDatabase
): EventsRepositoryInterface {

    private val dao = db.eventsDao

    override suspend fun updatePendingEvents() = updateEvents(
        begin = nowAsIso8601(),
        end = null
    )

    override fun getEvents(): Flow<List<EventWithType>> = dao.getAllEvents()

    override fun searchEvents(query: String): Flow<List<EventWithType>> = dao.searchEvents(query)

    override fun getEventDetail(eventId: String): Flow<EventWithShiftsAndSalary?> = dao.getEventDetail(eventId)

    override suspend fun updateEvents(begin: String?, end: String?) = tryUpdate(
        inScope = coroutineScope,
        withHandler = handler,
        from = { eventsApi.getEvents(begin, end) },
        into = {
            dao.upsertEvents(
                it.map { it.toEventEntity() }
            )
        }
    )

    override suspend fun updateUserEvents(
        userId: String,
        begin: String?,
        end: String?
    ): Resource<List<UserEventModel>> = tryUpdate(
        inScope = coroutineScope,
        withHandler = handler,
        from = { eventsApi.getUserEvents(userId, begin, end) },
        into = {
            dao.upsertEventRoles(
                it.map { it.role }
            )
            dao.upsertEventTypes(
                it.map { it.eventType }
            )
            dao.upsertUserEvents(
                it.map { it.toEntity() }
            )
        }
    )

    override suspend fun fetchEvent(eventId: String) = tryUpdate(
        inScope = coroutineScope,
        withHandler = handler,
        from = { eventsApi.getEvent(eventId) },
        into = {
            updateEventRoles()
            dao.upsertEventDetail(
                event = it.toEventDetailEntity(),
                shifts = it.extractShiftEntities(),
                places = it.extractPlaceEntities(),
                roles = it.extractRoles()
            )
            updateEventSalary(eventId)
        }
    )

    override suspend fun updateEventSalary(eventId: String) = tryUpdate(
        inScope = coroutineScope,
        withHandler = handler,
        from = { eventsApi.getEventSalary(eventId) },
        into = {
            dao.upsertFullEventSalary(
                eventSalary = it.toEventSalaryEntity(),
                shiftSalaries = it.shiftSalaries,
                placeSalaries = it.placeSalaries
            )
        }
    )

    override suspend fun applyForPlace(placeId: String, roleId: String) = handler {
        eventsApi.applyForPlace(placeId, roleId)
    }

    override suspend fun updateEventRoles() = tryUpdate(
        inScope = coroutineScope,
        withHandler = handler,
        from = { eventsApi.getEventRoles() },
        into = {
            dao.upsertEventRoles(it)
        }
    )

    override suspend fun updateInvitations() = tryUpdate(
        inScope = coroutineScope,
        withHandler = handler,
        from = { eventsApi.getInvitations() },
        into = {
            updateEventRoles()
            dao.upsertInvitations(
                it.map {
                    it.toInvitationEntity()
                }
            )
        }
    )

    override suspend fun updateEventTypes() = tryUpdate(
        inScope = coroutineScope,
        withHandler = handler,
        from = { eventsApi.getEventTypes() },
        into = {
            dao.upsertEventTypes(it)
        }
    )

    override suspend fun rejectInvitation(placeId: String) = handler {
        eventsApi.rejectInvitation(placeId)
    }

    override suspend fun acceptInvitation(placeId: String) = handler {
        eventsApi.acceptInvitation(placeId)
    }

}