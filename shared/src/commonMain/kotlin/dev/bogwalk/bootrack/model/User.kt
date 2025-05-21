package dev.bogwalk.bootrack.model

import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Int,
    val fullName: String,
    val username: String,
    val settings: UserSettings
)

@Serializable
data class UserSettings(
    val avatarIcon: Int,
    val avatarTint: Int,
    val defaultProject: Project,
    val location: Location,
    val maxTravelDistance: Int,
    val dateFormat: String,
    val defaultSort: UserSort,
    val notifyOnSelfChanges: Boolean,
    val notifyOnMention: Boolean,
    val unstarOnIssueClose: Boolean,
    val starOnIssueCreate: Boolean,
    val starOnIssueUpdate: Boolean,
    val starOnIssueAssigned: Boolean,
    val starOnIssueUpvote: Boolean,
)

@Serializable
data class Location(
    val latitude: Double,
    val longitude: Double
) {
    override fun toString(): String = "$latitude $longitude"
}

enum class UserSort(val label: String) {
    RELEVANCE("Relevance"),
    UPDATED("Updated")
}

enum class UserDateFormat(
    val pattern: String,
    val format: DateTimeFormat<DateTimeComponents>
) {
    DAY_MONTH_YEAR_TIME_NAMED(
        "31 Dec 2000 23:59",
        DateTimeComponents.Format {
            dayOfMonth()
            char(' ')
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            char(' ')
            year()
            char(' ')
            hour()
            char(':')
            minute()
        }
    ),
    DAY_MONTH_YEAR_TIME(
        "31/12/2000 23:59",
        DateTimeComponents.Format {
            dayOfMonth()
            char('/')
            monthNumber()
            char('/')
            year()
            char(' ')
            hour()
            char(':')
            minute()
        }
    ),
    DAY_MONTH_YEAR_TIME_AM_PM(
        "31/12/2000 11:59PM",
        DateTimeComponents.Format {
            dayOfMonth()
            char('/')
            monthNumber()
            char('/')
            year()
            char(' ')
            amPmHour()
            char(':')
            minute()
            amPmMarker("AM", "PM")
        }
    ),
    DAY_MONTH_YEAR(
        "31 Dec 2000",
        DateTimeComponents.Format {
            dayOfMonth()
            char(' ')
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            char(' ')
            year()
        }
    ),
    MONTH_DAY_YEAR_TIME_NAMED(
        "Dec 31 2000 23:59",
        DateTimeComponents.Format {
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            char(' ')
            dayOfMonth()
            char(' ')
            year()
            char(' ')
            hour()
            char(':')
            minute()
        }
    ),
    MONTH_DAY_YEAR_TIME(
        "12/31/2000 23:59",
        DateTimeComponents.Format {
            monthNumber()
            char('/')
            dayOfMonth()
            char('/')
            year()
            char(' ')
            hour()
            char(':')
            minute()
        }
    ),
    MONTH_DAY_YEAR_TIME_AM_PM(
        "12/31/2000 11:59PM",
        DateTimeComponents.Format {
            monthNumber()
            char('/')
            dayOfMonth()
            char('/')
            year()
            char(' ')
            amPmHour()
            char(':')
            minute()
            amPmMarker("AM", "PM")
        }
    ),
    MONTH_DAY_YEAR(
        "Dec 31 2000",
        DateTimeComponents.Format {
            monthName(MonthNames.ENGLISH_ABBREVIATED)
            char(' ')
            dayOfMonth()
            char(' ')
            year()
        }
    )
}
