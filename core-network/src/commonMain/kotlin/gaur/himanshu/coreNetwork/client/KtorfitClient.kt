package gaur.himanshu.coreNetwork.client

import de.jensklingenberg.ktorfit.Ktorfit
import gaur.himanshu.coreNetwork.api.GameApi
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.http.ContentType
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object KtorfitClient {

    fun getInstance(): GameApi {
        val httpClient = HttpClient {
            install(ContentNegotiation) {
                json(json = Json {
                    ignoreUnknownKeys = true
                })
            }

            install(DefaultRequest) {
                url {
                    host = "api.rawg.io"
                    protocol = URLProtocol.HTTPS
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Application.Json)
                }
            }

            install(HttpTimeout) {
                socketTimeoutMillis = 3000
                connectTimeoutMillis = 3000
                requestTimeoutMillis = 3000
            }
        }

        val ktorfit = Ktorfit.Builder()
            .httpClient(httpClient)
            .baseUrl("https://api.rawg.io/")
            .build()

        return ktorfit.create<GameApi>()
    }
}