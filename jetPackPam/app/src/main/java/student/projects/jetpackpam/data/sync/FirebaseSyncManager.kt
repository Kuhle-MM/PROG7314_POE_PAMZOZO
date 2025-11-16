package student.projects.jetpackpam.data.sync

import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import com.onesignal.common.threading.launchOnIO
import student.projects.jetpackpam.data.local.OfflineRepository

class FirebaseSyncManager(
    private val repo: OfflineRepository,
    private val db: DatabaseReference
) {
    suspend fun sync(uid: String) {
        val unsyncedItems = repo.getUnsynced(uid)

        unsyncedItems.forEach { item ->
            val target = db.child("User").child(uid).child(item.dataKey)

            target.setValue(
                Gson().fromJson(item.jsonData, Any::class.java)
            ).addOnSuccessListener {

                launchOnIO { repo.markSynced(item.id) }

            }
        }
    }
}