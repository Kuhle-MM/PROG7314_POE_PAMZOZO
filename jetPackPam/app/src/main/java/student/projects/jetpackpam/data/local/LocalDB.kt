package student.projects.jetpackpam.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [OfflineData::class], version = 1, exportSchema = false)
abstract class LocalDB : RoomDatabase() {
    abstract fun offlineDao(): OfflineDao
    companion object {
        @Volatile private var INSTANCE: LocalDB? = null
        fun getInstance(context: Context): LocalDB =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    LocalDB::class.java,
                    "local_db"
                ).build()
            }
    }
}