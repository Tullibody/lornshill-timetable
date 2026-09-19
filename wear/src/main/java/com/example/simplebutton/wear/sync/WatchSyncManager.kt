package com.example.simplebutton.wear.sync

import android.content.Context
import com.example.simplebutton.sync.TimetableSyncContract
import com.example.simplebutton.wear.data.WatchTimetableRepository
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object WatchSyncManager {

    suspend fun requestSyncFromPhone(context: Context): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val nodeClient = Wearable.getNodeClient(context)
            val nodes: List<Node> = Tasks.await(nodeClient.connectedNodes)
            if (nodes.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("No connected phone found"))
            }

            val messageClient = Wearable.getMessageClient(context)
            var sentCount = 0
            for (node in nodes) {
                try {
                    Tasks.await(messageClient.sendMessage(node.id, TimetableSyncContract.PATH_REQUEST_SYNC, ByteArray(0)))
                    sentCount++
                } catch (_: Exception) {
                    // Ignore individual node failure
                }
            }

            if (sentCount > 0) {
                Result.success(sentCount)
            } else {
                Result.failure(IllegalStateException("Failed to deliver sync request to phone"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun openAppOnPhone(context: Context): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val nodeClient = Wearable.getNodeClient(context)
            val nodes: List<Node> = Tasks.await(nodeClient.connectedNodes)
            if (nodes.isEmpty()) {
                return@withContext Result.failure(IllegalStateException("No connected phone found"))
            }

            val messageClient = Wearable.getMessageClient(context)
            var triggered = false
            for (node in nodes) {
                try {
                    Tasks.await(messageClient.sendMessage(node.id, TimetableSyncContract.PATH_OPEN_PHONE_APP, ByteArray(0)))
                    triggered = true
                } catch (_: Exception) {
                    // Ignore individual node failure
                }
            }

            if (triggered) {
                Result.success(true)
            } else {
                Result.failure(IllegalStateException("Failed to send open request to phone"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun handleIncomingDataMap(dataMapItem: DataMapItem, repository: WatchTimetableRepository) {
        val map = dataMapItem.dataMap
        val timetableJson = map.getString(TimetableSyncContract.KEY_TIMETABLE_JSON) ?: return
        val profileJson = map.getString(TimetableSyncContract.KEY_PROFILE_JSON)
        val timestamp = map.getLong(TimetableSyncContract.KEY_TIMESTAMP, System.currentTimeMillis())

        repository.saveFromJson(timetableJson, profileJson, timestamp)
    }
}
