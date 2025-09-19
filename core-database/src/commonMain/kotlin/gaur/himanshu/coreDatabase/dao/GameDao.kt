package gaur.himanshu.coreDatabase.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import gaur.himanshu.coreDatabase.entity.Game
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    
    @Query("SELECT * FROM game")
    fun getAllGames(): Flow<List<Game>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(game: Game)
    
    @Query("DELETE FROM game WHERE id = :id")
    suspend fun delete(id: Long)
}