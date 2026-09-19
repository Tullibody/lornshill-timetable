package com.example.simplebutton.wear.tile

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DeviceParametersBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material.CompactChip
import androidx.wear.protolayout.material.ChipColors
import androidx.wear.protolayout.material.Text
import androidx.wear.protolayout.material.Typography
import androidx.wear.protolayout.material.layouts.PrimaryLayout
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.example.simplebutton.wear.WatchMainActivity
import com.example.simplebutton.wear.data.WatchTimetableRepository
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

private const val RESOURCES_VERSION = "1"

class LornshillTileService : TileService() {

    override fun onTileRequest(requestParams: RequestBuilders.TileRequest): ListenableFuture<TileBuilders.Tile> {
        val repository = WatchTimetableRepository(applicationContext)
        val (currentPeriod, nextPeriod) = repository.getCurrentAndNextPeriod()

        val headlineText: String
        val detailText: String
        val roomText: String

        if (currentPeriod != null) {
            val countdown = repository.getCountdownStatus(currentPeriod)
            headlineText = "${currentPeriod.formattedPeriodTitle}: ${currentPeriod.subject}"
            detailText = "In Progress • ${countdown.badgeText}"
            roomText = if (currentPeriod.room.isNotBlank()) "Room: ${currentPeriod.room}" else currentPeriod.teacher
        } else if (nextPeriod != null) {
            val countdown = repository.getCountdownStatus(nextPeriod)
            headlineText = "Next: ${nextPeriod.subject}"
            detailText = countdown.badgeText
            roomText = if (nextPeriod.room.isNotBlank()) "Room: ${nextPeriod.room}" else nextPeriod.teacher
        } else {
            headlineText = "Lornshill Timetable"
            detailText = if (repository.hasTimetable) "No active classes" else "No timetable loaded"
            roomText = "Tap to view schedule"
        }

        val rootLayout = buildTileLayout(requestParams.deviceConfiguration, headlineText, detailText, roomText)

        val timeline = TimelineBuilders.Timeline.Builder()
            .addTimelineEntry(
                TimelineBuilders.TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(rootLayout)
                            .build()
                    )
                    .build()
            )
            .build()

        val tile = TileBuilders.Tile.Builder()
            .setResourcesVersion(RESOURCES_VERSION)
            .setTileTimeline(timeline)
            .setFreshnessIntervalMillis(60_000L) // Refresh every minute
            .build()

        return Futures.immediateFuture(tile)
    }

    override fun onTileResourcesRequest(requestParams: RequestBuilders.ResourcesRequest): ListenableFuture<ResourceBuilders.Resources> {
        return Futures.immediateFuture(
            ResourceBuilders.Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()
        )
    }

    private fun buildTileLayout(
        deviceParams: DeviceParametersBuilders.DeviceParameters,
        headline: String,
        status: String,
        room: String
    ): LayoutElementBuilders.LayoutElement {
        val launchAction = ActionBuilders.LaunchAction.Builder()
            .setAndroidActivity(
                ActionBuilders.AndroidActivity.Builder()
                    .setPackageName(packageName)
                    .setClassName(WatchMainActivity::class.java.name)
                    .build()
            )
            .build()

        val clickModifier = ModifiersBuilders.Clickable.Builder()
            .setId("open_app")
            .setOnClick(launchAction)
            .build()

        val outerModifiers = ModifiersBuilders.Modifiers.Builder()
            .setClickable(clickModifier)
            .build()

        val contentColumn = LayoutElementBuilders.Column.Builder()
            .setWidth(DimensionBuilders.expand())
            .setHeight(DimensionBuilders.wrap())
            .setModifiers(outerModifiers)
            .addContent(
                Text.Builder(this, headline)
                    .setTypography(Typography.TYPOGRAPHY_TITLE3)
                    .setColor(ColorBuilders.argb(0xFFFFFFFF.toInt()))
                    .setMaxLines(2)
                    .build()
            )
            .addContent(
                Text.Builder(this, status)
                    .setTypography(Typography.TYPOGRAPHY_BODY2)
                    .setColor(ColorBuilders.argb(0xFF10B981.toInt()))
                    .build()
            )
            .addContent(
                Text.Builder(this, room)
                    .setTypography(Typography.TYPOGRAPHY_CAPTION2)
                    .setColor(ColorBuilders.argb(0xFF94A3B8.toInt()))
                    .build()
            )
            .build()

        val labelText = Text.Builder(this, "LORNSHILL")
            .setTypography(Typography.TYPOGRAPHY_CAPTION1)
            .setColor(ColorBuilders.argb(0xFF38BDF8.toInt()))
            .build()

        val chip = CompactChip.Builder(
            this,
            "Open App",
            clickModifier,
            deviceParams
        )
        .setChipColors(
            ChipColors(
                ColorBuilders.argb(0xFF1E40AF.toInt()),
                ColorBuilders.argb(0xFFFFFFFF.toInt())
            )
        )
        .build()

        return PrimaryLayout.Builder(deviceParams)
            .setPrimaryLabelTextContent(labelText)
            .setContent(contentColumn)
            .setPrimaryChipContent(chip)
            .build()
    }
}
