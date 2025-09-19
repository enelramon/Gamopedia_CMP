package gaur.himanshu.coreNetwork.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import gaur.himanshu.coreNetwork.model.game.GameResponse
import gaur.himanshu.coreNetwork.model.gameDetails.GameDetailsResponse

interface GameApi {

    @GET("api/games")
    suspend fun getGames(
        @Query("key") apiKey: String = "1abb1867f52548a4aa9f54dd4946af2f"
    ): GameResponse

    @GET("api/games")
    suspend fun searchGames(
        @Query("key") apiKey: String = "1abb1867f52548a4aa9f54dd4946af2f",
        @Query("search") query: String
    ): GameResponse

    @GET("api/games/{id}")
    suspend fun getGameDetails(
        @Path("id") id: Int,
        @Query("key") apiKey: String = "1abb1867f52548a4aa9f54dd4946af2f"
    ): GameDetailsResponse
}