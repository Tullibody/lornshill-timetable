package com.example.simplebutton.wear.sync

import com.example.simplebutton.sync.TimetableSyncContract
import com.example.simplebutton.wear.data.WatchTimetableRepository
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import java.nio.charset.StandardCharsets

class WatchWearableListenerService : WearableListenerService() {

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        val repository = WatchTimetableRepository(applicationContext)

        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val uri = event.dataItem.uri
                if (uri.path == TimetableSyncContract.PATH_TIMETABLE_DATA) {
                    val dataMapItem = DataMapItem.fromDataItem(event.dataItem)
                    WatchSyncManager.handleIncomingDataMap(dataMapItem, repository)
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == TimetableSyncContract.PATH_TIMETABLE_DATA) {
            val payload = String(messageEvent.data, StandardCharsets.UTF_8)
            val repository = WatchTimetableRepository(applicationContext)
            repository.saveFromJson(payload, null, System.currentTimeMillis())
        }
    }
}
