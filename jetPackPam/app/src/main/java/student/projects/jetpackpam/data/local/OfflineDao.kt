package student.projects.jetpackpam.data.local

import androidx.room.*

@Dao
interface OfflineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertData(data: OfflineData)

    @Query("SELECT * FROM offline_storage WHERE uid = :uid AND synced = 0")
    suspend fun getUnsynced(uid: String): List<OfflineData>

    @Query("UPDATE offline_storage SET synced = 1 WHERE id = :id")
    suspend fun markSynced(id: Int)
}