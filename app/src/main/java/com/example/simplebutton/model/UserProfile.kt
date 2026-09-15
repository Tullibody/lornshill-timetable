package com.example.simplebutton.model

data class UserProfile(
    val name: String = "",
    val year: String = "S1",
    val house: String = "Devon",
    val className: String = "",
    val completedOnboarding: Boolean = false
) {
    val displayName: String
        get() = name.ifBlank { "Student" }

    val formattedBadge: String
        get() {
            val parts = mutableListOf<String>()
            if (year.isNotBlank()) parts.add(year)
            if (house.isNotBlank()) parts.add(house)
            if ((year == "S1" || year == "S2") && className.isNotBlank()) {
                parts.add(className)
            }
            return parts.joinToString(" · ")
        }

    companion object {
        val YEARS = listOf("S1", "S2", "S3", "S4", "S5", "S6")
        val HOUSES = listOf("Devon", "Forebraes", "Grange", "Ochil")
    }
}
