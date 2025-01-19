package com.sixbynine.transit.path.widget

import android.content.Context
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.currentState
import com.sixbynine.transit.path.Logging
import com.sixbynine.transit.path.MobilePathApplication
import com.sixbynine.transit.path.api.Stations
import com.sixbynine.transit.path.api.TrainFilter
import com.sixbynine.transit.path.api.anyMatch
import com.sixbynine.transit.path.time.now
import com.sixbynine.transit.path.time.today
import com.sixbynine.transit.path.util.DataResult
import com.sixbynine.transit.path.util.map
import com.sixbynine.transit.path.widget.configuration.StoredWidgetConfiguration
import com.sixbynine.transit.path.widget.configuration.WidgetConfigurationManager
import com.sixbynine.transit.path.widget.configuration.needsSetup
import com.sixbynine.transit.path.widget.ui.WidgetContent
import com.sixbynine.transit.path.widget.ui.WidgetFooterHeight
import com.sixbynine.transit.path.widget.ui.WidgetState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import java.util.Locale

class DepartureBoardWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(SmallWidgetSize, getMediumWidgetSize()))

    override suspend fun provideGlance(context: Context, id: GlanceId) = coroutineScope {
        launch { WidgetRefreshWorker.schedule() }

        val data = AndroidWidgetDataRepository.getData()
        Logging.d("provideGlance")
        provideContent {
            val updateTime =
                currentState(key = LastUpdateKey)
                    ?.let { Instant.fromEpochMilliseconds(it) } ?: now()
            val configuration = with(WidgetConfigurationManager) { getWidgetConfiguration() }
            Logging.d("provideContent invoked, updateTime = $updateTime")

            val widgetState = if (configuration.needsSetup()) {
                WidgetState(
                    result = DataResult.loading(),
                    updateTime = updateTime,
                    needsSetup = true
                )
            } else {
                val result by data.collectAsState()
                WidgetState(
                    result = result.map { it.adjustForConfiguration(configuration) },
                    updateTime,
                    needsSetup = false
                )
            }
            WidgetContent(widgetState)
            Logging.d("composed widget content with data fetched at ${widgetState.result.data?.fetchTime}")
        }
    }

    private fun WidgetData.adjustForConfiguration(
        configuration: StoredWidgetConfiguration
    ): WidgetData {
        val newStations = stations.toMutableList()

        newStations.removeAll { it.id !in configuration.fixedStations.orEmpty() }

        newStations.sortWith(StationDataComparator(configuration.sortOrder))

        newStations.forEachIndexed { index, stationData ->
            newStations[index] = stationData.copy(
                trains = stationData.trains.filter { trainData ->
                    configuration.lines.anyMatch(trainData, stationId = stationData.id)
                },
                signs = stationData.signs.filter {
                    configuration.lines.anyMatch(it, stationId = stationData.id)
                }
            )
        }

        if (configuration.useClosestStation && closestStationId != null) {
            newStations.removeAll { it.id == closestStationId }
            stations.find { it.id == closestStationId }?.let {
                newStations.add(0, it)
            }
        }

        if (configuration.filter == TrainFilter.Interstate) {
            newStations.forEachIndexed { index, stationData ->
                val station =
                    Stations.All.find { it.pathApiName == stationData.id } ?: return@forEachIndexed
                newStations[index] = stationData.copy(
                    signs = stationData.signs.filter { sign ->
                        val destination = Stations.fromHeadSign(sign.title) ?: return@filter true
                        TrainFilter.matchesFilter(station, destination, TrainFilter.Interstate)
                    }
                )
            }
        }

        return copy(stations = newStations)
    }

    companion object {
        suspend fun onDataChanged() {
            updateAppWidgetStates<DepartureBoardWidget> { prefs, _ ->
                prefs[LastUpdateKey] = System.currentTimeMillis()
            }
            DepartureBoardWidget().updateAll(MobilePathApplication.instance)
        }
    }
}

val SmallWidgetSize = DpSize(1.dp, 1.dp)

private fun getMediumWidgetSize(): DpSize {
    val context = MobilePathApplication.instance

    val widestTime =
        WidgetDataFormatter.formatTime(
            today().atTime(22, 20).toInstant(TimeZone.currentSystemDefault())
        )

    val widestUpdatedAtText = when (Locale.getDefault().language) {
        "es" -> "Se actualizó a las $widestTime"
        else -> "Updated at $widestTime"
    }
    val updatedAtWidth = estimateTextWidth(context, widestUpdatedAtText, 12.sp)
    val requiredWidth = (WidgetFooterHeight * 2) + updatedAtWidth
    return DpSize(requiredWidth, 1.dp)
}

class DepartureBoardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = DepartureBoardWidget()
}

private val LastUpdateKey = longPreferencesKey("last_update")
