package gaur.himanshu.coreDatabase

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual fun getDatabaseBuilder(context: Any?): RoomDatabase.Builder<AppDatabase> {
    val ctx = context as Context
    val appContext = ctx.applicationContext
    val dbFile = appContext.getDatabasePath("AppDatabase.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}