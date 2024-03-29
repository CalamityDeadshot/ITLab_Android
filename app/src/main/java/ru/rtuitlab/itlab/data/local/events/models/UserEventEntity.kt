package ru.rtuitlab.itlab.data.local.events.models

import androidx.room.*
import ru.rtuitlab.itlab.data.local.users.models.UserEntity
import ru.rtuitlab.itlab.data.remote.api.events.models.EventRoleModel
import ru.rtuitlab.itlab.data.remote.api.events.models.EventTypeModel
import ru.rtuitlab.itlab.data.remote.api.users.models.UserEventModel

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = EventTypeModel::class,
            parentColumns = ["id"],
            childColumns = ["typeId"]
        ),
        ForeignKey(
            entity = EventRoleModel::class,
            parentColumns = ["id"],
            childColumns = ["roleId"]
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"]
        )
    ],
    indices = [
        Index("typeId"),
        Index("roleId"),
        Index("userId"),
    ]
)
data class UserEventEntity(
    @PrimaryKey val id: String,
    val userId: String, // FK
    val address: String,
    val title: String,
    val typeId: String,
    val beginTime: String,
    val endTime: String,
    val roleId: String
)

data class UserEventWithTypeAndRole(
    @Embedded val userEvent: UserEventEntity,
    @Relation(
        parentColumn = "typeId",
        entityColumn = "id"
    )
    val eventType: EventTypeModel,
    @Relation(
        parentColumn = "roleId",
        entityColumn = "id"
    )
    val role: EventRoleModel
) {
    fun toUserEventModel() = UserEventModel(
        id = userEvent.id,
        address = userEvent.address,
        title = userEvent.title,
        beginTime = userEvent.beginTime,
        endTime = userEvent.endTime,
        eventType = eventType,
        role = role
    )
}
