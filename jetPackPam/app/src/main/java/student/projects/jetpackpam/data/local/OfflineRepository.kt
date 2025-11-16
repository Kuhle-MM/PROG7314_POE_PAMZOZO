package student.projects.jetpackpam.data.local

import com.google.gson.Gson

class OfflineRepository(private val dao: OfflineDao) {

    suspend fun saveOffline(uid: String, key: String, data: Any) {
        val json = Gson().toJson(data)
        dao.insertData(
            OfflineData(
                uid = uid,
                dataKey = key,
                jsonData = json
            )
        )
    }

    suspend fun getUnsynced(uid: String) = dao.getUnsynced(uid)

    suspend fun markSynced(id: Int) = dao.markSynced(id)
}