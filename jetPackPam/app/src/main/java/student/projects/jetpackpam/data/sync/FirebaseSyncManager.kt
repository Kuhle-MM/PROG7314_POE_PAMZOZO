package student.projects.jetpackpam.data.sync

import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import student.projects.jetpackpam.data.local.OfflineRepository

class FirebaseSyncManager(
    private val repo: OfflineRepository,
    private val db: DatabaseReference
) {
    /**
     * Sync unsynced items for the provided uid.
     * This is a suspend-friendly manager: it will push items to Firebase and mark them synced.
     * It's safe to call from a coroutine or via launch(Dispatchers.IO).
     */
    suspend fun sync(uid: String) {
        val unsyncedItems = repo.getUnsynced(uid)

        unsyncedItems.forEach { item ->
            val target = db.child("User").child(uid).child(item.dataKey)

            try {
                // setValue returns a Task<Void>, so we can await it if caller is within coroutine,
                // but here we use the Task listeners pattern to avoid blocking. We'll mark synced only on success.
                val parsed = Gson().fromJson(item.jsonData, Any::class.java)
                target.setValue(parsed)
                    .addOnSuccessListener {
                        // mark synced on IO dispatcher
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                repo.markSynced(item.id)
                            } catch (_: Exception) { /* ignore marking failures */ }
                        }
                    }
                    .addOnFailureListener {
                        // ignore individual failures; will retry next sync
                    }
            } catch (_: Exception) {
                // malformed JSON or other error — skip this item for now
            }
        }
    }
}
