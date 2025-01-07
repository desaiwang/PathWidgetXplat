package com.sixbynine.transit.path.api.impl

import com.sixbynine.transit.path.api.Line
import com.sixbynine.transit.path.api.Line.Hoboken33rd
import com.sixbynine.transit.path.api.Line.HobokenWtc
import com.sixbynine.transit.path.api.Line.JournalSquare33rd
import com.sixbynine.transit.path.api.Line.NewarkWtc
import com.sixbynine.transit.path.api.Line.Wtc33rd
import com.sixbynine.transit.path.app.ui.ColorWrapper
import com.sixbynine.transit.path.app.ui.Colors

object LineComputer {
    fun computeLines(
        station: String,
        target: String,
        colors: List<ColorWrapper>
    ): Set<Line> {
        val lines = mutableSetOf<Line>()

        // First, make sure we match if there are any matching colors.
        colors.forEach {
            when (it) {
                Colors.NwkWtcSingle -> lines += NewarkWtc
                Colors.HobWtcSingle -> lines += HobokenWtc
                Colors.Hob33sSingle -> lines += Hoboken33rd
                Colors.Jsq33sSingle -> lines += JournalSquare33rd
                Colors.Wtc33sSingle -> lines += Wtc33rd
            }
        }

        if (colors.size == lines.size) {
            // All the colors matched a line, no need for any more logic.
            return lines
        }

        // This logic is an attempt to cover the case where schedules have changed to different
        // colors and different ending stations.
        when (station) {
            // Stations that only have one line.
            "NWK", "HAR" -> lines += NewarkWtc

            "JSQ" -> when (target) {
                "NWK", "HAR", "WTC", "EXP" -> lines += NewarkWtc
                "NEW", in NyNorthStations -> lines += JournalSquare33rd
            }

            "GRV" -> when (target) {
                "NWK", "HAR", "WTC", "EXP" -> lines += NewarkWtc
                "NEW", in NyNorthStations -> lines += JournalSquare33rd
                "JSQ" -> lines += listOf(NewarkWtc, JournalSquare33rd)
            }

            "EXP" -> when (target) {
                "NWK", "HAR", "WTC", "EXP", "GRV" -> lines += NewarkWtc
                "NEW", "HOB" -> lines += HobokenWtc
            }

            "NEW" -> when (target) {
                "EXP", "HOB" -> lines += HobokenWtc
                "GRV", "JSQ", in NyNorthStations -> lines += JournalSquare33rd
            }

            "HOB" -> when (target) {
                "EXP", "NEW" -> lines += HobokenWtc
                "GRV", "JSQ" -> lines += listOf(JournalSquare33rd, Hoboken33rd)
                in NyNorthStations -> lines += Hoboken33rd
            }

            "WTC" -> when (target) {
                "EXP" -> lines += listOf(NewarkWtc, HobokenWtc)
                "NWK", "HAR", "JSQ", "GRV" -> lines += NewarkWtc
                "NEW", "HOB" -> lines += HobokenWtc
                in NyNorthStations -> lines += Wtc33rd
            }

            in NyNorthStations -> when (target) {
                in NyNorthStations -> lines += listOf(JournalSquare33rd, Hoboken33rd)
                "HOB" -> lines += Hoboken33rd
                "GRV", "JSQ" -> lines += JournalSquare33rd
                "WTC" -> lines += Wtc33rd
            }
        }

        return lines
    }
}

private val NyNorthStations = setOf("CHR", "09S", "14S", "23S", "33S")