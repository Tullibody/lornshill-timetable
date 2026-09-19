package com.example.simplebutton.wear

import android.content.Intent
import com.example.simplebutton.MainActivity
import com.example.simplebutton.model.TimetableRepository
import com.example.simplebutton.sync.TimetableSyncContract
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MobileWearableListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            TimetableSyncContract.PATH_REQUEST_SYNC -> {
                val repository = TimetableRepository(applicationContext)
                CoroutineScope(Dispatchers.IO).launch {
                    MobileWearableSyncManager.syncTimetableToWatches(applicationContext, repository)
                }
            }
            TimetableSyncContract.PATH_OPEN_PHONE_APP -> {
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    putExtra(MainActivity.EXTRA_TARGET_TAB, "TIMETABLE")
                }
                startActivity(intent)
            }
        }
    }
}
