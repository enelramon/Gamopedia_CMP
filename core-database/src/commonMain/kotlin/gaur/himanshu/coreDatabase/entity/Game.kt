package gaur.himanshu.coreDatabase.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game")
data class Game(
    @PrimaryKey
    val id: Long,
    val image: String,
    val name: String
)