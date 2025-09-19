package gaur.himanshu.coreNetwork.apiService

import gaur.himanshu.coreNetwork.api.GameApi
import gaur.himanshu.coreNetwork.model.game.GameResponse
import gaur.himanshu.coreNetwork.model.gameDetails.GameDetailsResponse

class ApiService(
    private val gameApi: GameApi
) {

    suspend fun getGames(): Result<GameResponse> {
        return try {
            val response = gameApi.getGames()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun search(q: String): Result<GameResponse> {
        return try {
            val response = gameApi.searchGames(query = q)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDetails(id: Int): Result<GameDetailsResponse> {
        return try {
            val response = gameApi.getGameDetails(id = id)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}