package gaur.himanshu.favorite.data.repository

import gaur.himanshu.common.domain.model.Game
import gaur.himanshu.coreDatabase.dao.GameDao
import gaur.himanshu.coreDatabase.entity.Game as GameEntity
import gaur.himanshu.favorite.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteRepoImpl(
    private val gameDao: GameDao
) : FavoriteRepository {
    override fun getAllGames(): Flow<List<Game>> {
        return gameDao.getAllGames()
            .map { gameEntities ->
                gameEntities.map { entity ->
                    Game(
                        id = entity.id.toInt(),
                        name = entity.name,
                        imageUrl = entity.image
                    )
                }
            }
    }

    override suspend fun upsert(id: Int, image: String, name: String) {
        val gameEntity = GameEntity(
            id = id.toLong(),
            image = image,
            name = name
        )
        gameDao.upsert(gameEntity)
    }

    override suspend fun delete(id: Int) {
        gameDao.delete(id.toLong())
    }
}