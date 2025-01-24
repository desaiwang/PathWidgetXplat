package com.sixbynine.transit.path

import com.sixbynine.transit.path.api.Stations.ExchangePlace
import com.sixbynine.transit.path.api.Stations.FourteenthStreet
import com.sixbynine.transit.path.api.Stations.GroveStreet
import com.sixbynine.transit.path.api.Stations.Hoboken
import com.sixbynine.transit.path.api.Stations.NinthStreet
import com.sixbynine.transit.path.api.Stations.TwentyThirdStreet
import com.sixbynine.transit.path.api.Stations.WorldTradeCenter
import com.sixbynine.transit.path.api.alerts.Alert
import com.sixbynine.transit.path.api.alerts.AlertText
import com.sixbynine.transit.path.api.alerts.Schedule
import com.sixbynine.transit.path.api.alerts.TrainFilter
import com.sixbynine.transit.path.api.alerts.github.GithubAlerts
import com.sixbynine.transit.path.api.alerts.hidesTrain
import com.sixbynine.transit.path.api.alerts.isActiveAt
import com.sixbynine.transit.path.util.JsonFormat
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.DayOfWeek.FRIDAY
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.DayOfWeek.SATURDAY
import kotlinx.datetime.DayOfWeek.SUNDAY
import kotlinx.datetime.DayOfWeek.WEDNESDAY
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Month.APRIL
import kotlinx.datetime.Month.DECEMBER
import kotlinx.datetime.Month.FEBRUARY
import kotlinx.datetime.Month.JANUARY
import kotlinx.datetime.Month.JULY
import kotlinx.datetime.Month.JUNE
import kotlinx.datetime.Month.MAY
import kotlinx.datetime.atTime
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GithubAlertsTest {
    @Test
    fun `current alerts text`() {
        val alerts = GithubAlerts(
            GeneralOvernightCleaning,
            FourteenthStreetOvernight,
            ChristmasOvernightCleaning,
            HobokenClosure,
        )

        val json = JsonFormat.encodeToString(alerts)

        assertEquals(alerts, JsonFormat.decodeFromString(json))

        println(json)
    }

    @Test
    fun `active at repeating weekly`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            schedule = Schedule.repeatingWeekly(
                startDay = SATURDAY,
                startTime = LocalTime(6, 0),
                endDay = MONDAY,
                endTime = LocalTime(0, 0),
                from = LocalDate(2024, APRIL, 6),
                to = LocalDate(2024, JUNE, 30),
            ),
            trains = TrainFilter.headSigns("33rd", "World Trade"),
        )

        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 6, 6, 0)))
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 7, 10, 0)))
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 7, 14, 0)))
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 20, 14, 0)))
        assertFalse(alert.isActiveAt(LocalDateTime(2024, APRIL, 11, 14, 0)))
        assertFalse(alert.isActiveAt(LocalDateTime(2024, JULY, 6, 14, 0)))
    }

    @Test
    fun `active at repeating daily`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            schedule = Schedule.repeatingDaily(
                days = listOf(WEDNESDAY),
                start = LocalTime(6, 0),
                end = LocalTime(10, 0),
                from = LocalDate(2024, APRIL, 6),
                to = LocalDate(2024, JUNE, 30),
            ),
            trains = TrainFilter.headSigns("33rd", "World Trade"),
        )

        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 10, 6, 0)))
        assertFalse(alert.isActiveAt(LocalDateTime(2024, APRIL, 10, 10, 0)))
    }

    @Test
    fun `active at same start and end time`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            schedule = Schedule.repeatingDaily(
                days = listOf(SATURDAY),
                start = LocalTime(0, 0),
                end = LocalTime(0, 0),
                from = LocalDate(2024, APRIL, 6),
                to = LocalDate(2024, JUNE, 30),
            ),
            trains = TrainFilter.headSigns("33rd", "World Trade"),
        )

        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 6, 0, 0)))
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 6, 6, 0)))
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 6, 23, 0)))
        assertFalse(alert.isActiveAt(LocalDateTime(2024, APRIL, 5, 23, 0)))
        assertFalse(alert.isActiveAt(LocalDateTime(2024, APRIL, 7, 0, 0)))
    }

    @Test
    fun `active at same start and end time not midnight`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            schedule = Schedule.repeatingDaily(
                days = listOf(SATURDAY),
                start = LocalTime(4, 0),
                end = LocalTime(4, 0),
                from = LocalDate(2024, APRIL, 6),
                to = LocalDate(2024, JUNE, 30),
            ),
            trains = TrainFilter.headSigns("33rd", "World Trade"),
        )

        assertFalse(alert.isActiveAt(LocalDateTime(2024, APRIL, 5, 23, 0)))
        assertFalse(alert.isActiveAt(LocalDateTime(2024, APRIL, 6, 0, 0)))
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 6, 6, 0)))
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 6, 23, 0)))
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 7, 0, 0)))
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 7, 3, 0)))
        assertFalse(alert.isActiveAt(LocalDateTime(2024, APRIL, 7, 4, 0)))
    }

    @Test
    fun `active at repeating daily overnight`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            schedule = Schedule.repeatingDaily(
                days = listOf(WEDNESDAY, FRIDAY),
                start = LocalTime(22, 0),
                end = LocalTime(10, 0),
                from = LocalDate(2024, APRIL, 6),
                to = LocalDate(2024, JUNE, 30),
            ),
            trains = TrainFilter.headSigns("33rd", "World Trade"),
        )

        // Wednesday at 23:00
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 10, 23, 0)))
        // Thursday at 09:00
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 11, 9, 0)))
        // Thursday at 10:00
        assertFalse(alert.isActiveAt(LocalDateTime(2024, APRIL, 11, 10, 0)))
        // Thursday at 23:00
        assertFalse(alert.isActiveAt(LocalDateTime(2024, APRIL, 11, 23, 0)))
        // Friday at 23:00
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 12, 23, 0)))
        // Saturday at midnight
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 13, 0, 0)))
        // Sunday at midnight
        assertFalse(alert.isActiveAt(LocalDateTime(2024, APRIL, 14, 0, 0)))
    }

    @Test
    fun `active at once`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            schedule = Schedule.once(
                from = LocalDateTime(2024, APRIL, 6, 10, 0),
                to = LocalDateTime(2024, JUNE, 30, 10, 0),
            ),
            trains = TrainFilter.headSigns("33rd", "World Trade"),
        )

        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 10, 6, 0)))
        assertTrue(alert.isActiveAt(LocalDateTime(2024, APRIL, 15, 10, 0)))
        assertTrue(alert.isActiveAt(LocalDateTime(2024, JUNE, 30, 8, 0)))
        assertFalse(alert.isActiveAt(LocalDateTime(2024, JUNE, 30, 11, 0)))
    }

    @Test
    fun `hidesTrain with headsign`() {
        val alert = Alert(
            stations = listOf(GroveStreet),
            schedule = Schedule(),
            trains = TrainFilter.headSigns("33rd", "World Trade"),
        )

        assertFalse(alert.hidesTrain("JSQ", "33rd"))
        assertFalse(alert.hidesTrain("GRV", "Newark"))
        assertTrue(alert.hidesTrain("GRV", "33rd"))
        assertTrue(alert.hidesTrain("GRV", "33RD"))
        assertTrue(alert.hidesTrain("GRV", "33rd St via Hoboken"))
        assertTrue(alert.hidesTrain("GRV", "World Trade Center"))
    }

    @Test
    fun `hidesTrain with all`() {
        val alert = Alert(
            stations = listOf(NinthStreet, TwentyThirdStreet),
            schedule = Schedule(),
            trains = TrainFilter.all(),
        )

        assertFalse(alert.hidesTrain("14S", "33rd St"))
        assertTrue(alert.hidesTrain("09S", "33rd St"))
        assertTrue(alert.hidesTrain("23S", "33rd St"))
        assertTrue(alert.hidesTrain("23S", "Hoboken"))
    }

    private companion object {
        val MoreTrains = Alert(
            stations = listOf(ExchangePlace, WorldTradeCenter),
            schedule = Schedule(),
            displaySchedule = Schedule.repeatingDaily(
                days = listOf(SATURDAY, SUNDAY),
                start = LocalTime(0, 0),
                end = LocalTime(0, 0),
                from = LocalDate(2024, APRIL, 17),
                to = LocalDate(2024, JUNE, 30),
            ),
            trains = TrainFilter(),
            message = AlertText(
                en = "Additional service is being provided between World Trade Center and Exchange Place during weekends until June 30",
                es = "Hay servicio adicional entre World Trade Center y Exchange Place durante los fines de semana hasta el 30 de junio"
            ),
            url = AlertText(
                en = "https://x.com/PATHAlerts/status/1788917634077007880",
            )
        )

        val MemorialDayGroveStAlert = Alert(
            stations = listOf(GroveStreet),
            schedule = Schedule(),
            displaySchedule = Schedule.once(
                from = LocalDateTime(2024, MAY, 24, 19, 0),
                to = LocalDateTime(2024, MAY, 28, 3, 0),
            ),
            trains = TrainFilter(),
            message = AlertText(
                en = "Grove Street has service in both directions during Memorial Day Weekend",
                es = "Grove Street tiene servicio en ambas direcciones durante el fin de semana del Memorial Day"
            ),
            url = AlertText(
                en = "https://www.panynj.gov/path/en/planned-service-changes.html"
            ),
            level = "INFO"
        )

        val GeneralGroveStAlert = Alert(
            stations = listOf(GroveStreet),
            schedule = Schedule.repeatingWeekly(
                startDay = SATURDAY,
                startTime = LocalTime(6, 0),
                endDay = MONDAY,
                endTime = LocalTime(0, 0),
                from = LocalDate(2024, MAY, 28),
                to = LocalDate(2024, JUNE, 30),
            ),
            displaySchedule = Schedule.repeatingDaily(
                days = listOf(SATURDAY, SUNDAY),
                start = LocalTime(0, 0),
                end = LocalTime(0, 0),
                from = LocalDate(2024, MAY, 28),
                to = LocalDate(2024, JUNE, 30),
            ),
            trains = TrainFilter.headSigns("33rd", "World Trade"),
            message = AlertText(
                en = "Due to construction, World Trade Center- and 33 St.-bound trains will not stop at Grove Street Station from 6 AM Saturday - 11:59 PM Sunday.",
                es = "Debido a construcción, los trenes con destino al World Trade Center y a 33 Street no pararán en la estación Grove Street entre el 6 de abril y el 30 de junio, desde las 6 a. m. del sábado hasta las 11:59 p. m. del domingo."
            ),
            url = AlertText(
                en = "https://www.panynj.gov/path/en/modernizing-path/grove-st-improvements.html"
            ),
            level = "WARN",
        )

        val ChristmasOvernightCleaning = Alert(
            stations = listOf(NinthStreet, TwentyThirdStreet),
            schedule = Schedule(),
            displaySchedule = Schedule.repeatingDaily(
                days = DayOfWeek.values().toList(),
                start = LocalTime(17, 0),
                end = LocalTime(5, 0),
                from = LocalDate(2024, DECEMBER, 23),
                to = LocalDate(2025, JANUARY, 5),
            ),
            trains = TrainFilter(),
            message = AlertText(
                en = "9 St. & 23 St. stations will remain open overnight during the holiday season until January 6",
                es = "Las estaciones de 9 St. y 23 St. permanecerán abiertas durante la noche hasta el 6 de enero"
            ),
            url = AlertText(
                en = "https://www.panynj.gov/path/en/schedules-maps/weekend-schedules.html"
            ),
            level = "INFO"
        )

        val GeneralOvernightCleaning = Alert(
            stations = listOf(NinthStreet, TwentyThirdStreet),
            schedule = Schedule.repeatingDaily(
                days = DayOfWeek.values().toList(),
                start = LocalTime(0, 0),
                end = LocalTime(5, 0),
                from = LocalDate(2025, JANUARY, 6),
                to = LocalDate(2025, DECEMBER, 31),
            ),
            displaySchedule = Schedule.repeatingDaily(
                days = DayOfWeek.values().toList(),
                start = LocalTime(17, 0),
                end = LocalTime(5, 0),
                from = LocalDate(2025, JANUARY, 5),
                to = LocalDate(2025, DECEMBER, 30),
            ),
            trains = TrainFilter.all(),
            message = AlertText(
                en = "9 St. & 23 St. stations are closed daily from 11:59 PM – 5 AM for maintenance-related activity. Please use Christopher St., 14 St., or 33 St. station.",
                es = "Las estaciones de 9 St. y 23 St. están cerradas diariamente de 11:59 p. m. a 5 a. m. para actividades relacionadas con el mantenimiento. Utilice las estaciones de Christopher St., 14 St. o 33 St."
            ),
            url = AlertText(
                en = "https://www.panynj.gov/path/en/schedules-maps.html"
            ),
            level = "WARN"
        )

        val FourteenthStreetOvernight = Alert(
            stations = listOf(FourteenthStreet),
            schedule = Schedule(),
            displaySchedule = Schedule.repeatingDaily(
                days = DayOfWeek.values().toList(),
                start = LocalTime(22, 0),
                end = LocalTime(7, 0),
                from = LocalDate(2024, JANUARY, 5),
                to = LocalDate(2025, DECEMBER, 31),
            ),
            trains = TrainFilter(),
            message = AlertText(
                en = "During overnight hours all PATH service at the 14 Street Station may operate from the station’s uptown or downtown track.  Signage is posted at the entrance of the station when this single-track operation is in effect.",
            ),
            url = AlertText(
                en = "https://www.panynj.gov/path/en/planned-service-changes.html",
            )
        )

        val HobokenClosure = Alert(
            stations = listOf(Hoboken),
            schedule = Schedule.once(
                from = LocalDate(2025, JANUARY, 30).atTime(23, 59),
                to = LocalDate(2025, FEBRUARY, 25).atTime(5, 0)
            ),
            displaySchedule = Schedule.once(
                from = LocalDate(2025, JANUARY, 30).atTime(7, 0),
                to = LocalDate(2025, FEBRUARY, 25).atTime(5, 0)
            ),
            trains = TrainFilter.all(),
            message = AlertText(
                en = "Hoboken Station is closed until February 25 for significant track and station upgrades. ",
                es = "La estación de Hoboken está cerrada hasta el 25 de febrero"
            ),
            url = AlertText(
                en = "https://www.panynj.gov/HobokenClosure"
            ),
            level = "WARN"
        )
    }
}