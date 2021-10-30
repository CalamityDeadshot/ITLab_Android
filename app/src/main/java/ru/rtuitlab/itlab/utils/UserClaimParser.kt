package ru.rtuitlab.itlab.utils

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ru.rtuitlab.itlab.api.users.models.UserClaimCategories

object UserClaimParser {
	fun parse(payload: String?): List<Any> {
		if (payload == null) return emptyList()

		// Sometimes "itlab" field is not an Array of Strings, but just String, so this check is needed
		val privileges =
			if (payload.substringAfter("\"itlab\":").startsWith('\"'))
				"[${payload.substringAfter("\"itlab\":").substringBefore(',')}]" // Obtaining single parameter as a list
			else
				"${payload.substringAfter("\"itlab\":").substringBefore(']')}]" // Obtaining a list of claims
		return Json.decodeFromString<List<String>>(privileges).mapNotNull {
			UserClaimCategories.obtainClaimFrom(it)
		}
	}
}