package com.example.simplebutton.wear

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import com.example.simplebutton.model.TimetableRepository
import com.example.simplebutton.sync.TimetableSyncContract
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets

object MobileWearableSyncManager {

    val connectedWatchCount = mutableIntStateOf(0)
    val connectedWatchNames = mutableStateListOf<String>()
    val lastSyncTimestamp = mutableLongStateOf(0L)

    suspend fun refreshConnectedWatches(context: Context): Int = withContext(Dispatchers.IO) {
        try {
            val nodeClient = Wearable.getNodeClient(context)
            val nodes = nodeClient.connectedNodes.await()
            val names = nodes.map { it.displayName }

            withContext(Dispatchers.Main) {
                connectedWatchCount.intValue = nodes.size
                connectedWatchNames.clear()
                connectedWatchNames.addAll(names)
            }
            nodes.size
        } catch (_: Exception) {
            0
        }
    }

    suspend fun syncTimetableToWatches(context: Context, repository: TimetableRepository): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val timetableJson = TimetableSyncContract.periodsToJson(repository.periods)
            val profileJson = TimetableSyncContract.profileToJson(repository.userProfile.value)
            val now = System.currentTimeMillis()

            // 1. Put DataMap item (persistent sync across Bluetooth/Wi-Fi/Cloud)
            val putDataMapReq = PutDataMapRequest.create(TimetableSyncContract.PATH_TIMETABLE_DATA).apply {
                dataMap.putString(TimetableSyncContract.KEY_TIMETABLE_JSON, timetableJson)
                dataMap.putString(TimetableSyncContract.KEY_PROFILE_JSON, profileJson)
                dataMap.putLong(TimetableSyncContract.KEY_TIMESTAMP, now)
                setUrgent()
            }
            val putDataReq = putDataMapReq.asPutDataRequest().setUrgent()
            Wearable.getDataClient(context).putDataItem(putDataReq).await()

            // 2. Direct message to connected nodes for instant foreground update
            val nodes = try {
                Wearable.getNodeClient(context).connectedNodes.await()
            } catch (_: Exception) {
                emptyList()
            }

            val messageClient = Wearable.getMessageClient(context)
            val payload = timetableJson.toByteArray(StandardCharsets.UTF_8)
            var sentMessages = 0

            for (node in nodes) {
                try {
                    messageClient.sendMessage(node.id, TimetableSyncContract.PATH_TIMETABLE_DATA, payload).await()
                    sentMessages++
                } catch (_: Exception) {
                    // Ignore individual failure
                }
            }

            withContext(Dispatchers.Main) {
                lastSyncTimestamp.longValue = now
                connectedWatchCount.intValue = nodes.size
                connectedWatchNames.clear()
                connectedWatchNames.addAll(nodes.map { it.displayName })
            }

            Result.success(nodes.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun syncAsync(context: Context, repository: TimetableRepository, onComplete: ((Boolean) -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            val res = syncTimetableToWatches(context, repository)
            withContext(Dispatchers.Main) {
                onComplete?.invoke(res.isSuccess)
            }
        }
    }
}
