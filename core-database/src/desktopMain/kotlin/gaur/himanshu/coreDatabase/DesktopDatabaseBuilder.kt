package gaur.himanshu.coreDatabase

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<AppDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "AppDatabase.db")
    return Room.databaseBuilder<AppDatabase>(
        name = dbFile.absolutePath
    )
}