package gaur.himanshu.coreDatabase.di

import gaur.himanshu.coreDatabase.AppDatabase
import gaur.himanshu.coreDatabase.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun getCoreDatabaseModule(): Module {
    return module {
        single {
            getDatabaseBuilder().build()
        }
        single { get<AppDatabase>().gameDao() }
    }
}