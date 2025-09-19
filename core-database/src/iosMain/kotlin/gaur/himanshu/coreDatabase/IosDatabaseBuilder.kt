package gaur.himanshu.coreDatabase

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSHomeDirectory() + "/AppDatabase.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath
    )
}