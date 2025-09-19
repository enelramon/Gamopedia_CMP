package gaur.himanshu.coreDatabase

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import gaur.himanshu.coreDatabase.dao.GameDao
import gaur.himanshu.coreDatabase.entity.Game

@Database(
    entities = [Game::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
}

// Platform-specific database builders will be defined in each platform sourceSet
expect fun getDatabaseBuilder(context: Any? = null): RoomDatabase.Builder<AppDatabase>