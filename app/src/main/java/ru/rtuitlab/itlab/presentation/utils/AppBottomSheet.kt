package ru.rtuitlab.itlab.presentation.utils

import ru.rtuitlab.itlab.data.remote.api.events.models.detail.Shift

sealed class AppBottomSheet {
	class EventShift(val shift: Shift, val salaries: List<Int>): AppBottomSheet()
	class EventDescription(val markdown: String): AppBottomSheet()
	object ProfileEquipment: AppBottomSheet()
	object ProfileSettings: AppBottomSheet()
	object ProfileEvents: AppBottomSheet()
	object Equipment: AppBottomSheet()
	object Unspecified: AppBottomSheet()

	override fun equals(other: Any?): Boolean {
		return false
	}
}