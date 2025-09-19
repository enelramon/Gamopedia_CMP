package gaur.himanshu.coreNetwork.di

import gaur.himanshu.coreNetwork.apiService.ApiService
import gaur.himanshu.coreNetwork.client.KtorfitClient
import org.koin.dsl.module

fun getCoreNetworkModule() = module {
    single { KtorfitClient.getInstance() }
    single { ApiService(gameApi = get()) }
}