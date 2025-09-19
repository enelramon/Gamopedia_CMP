package gaur.himanshu.coreDatabase.di

import android.content.Context
import gaur.himanshu.coreDatabase.AppDatabase
import gaur.himanshu.coreDatabase.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getCoreDatabaseModule(): Module {
    return module {
        single {
            getDatabaseBuilder(get<Context>()).build()
        }
        single { get<AppDatabase>().gameDao() }
    }
}