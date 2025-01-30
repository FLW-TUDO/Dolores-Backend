package com.flw.dolores.entities

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import java.util.ArrayList

@Document(collection = "gameinfos")
class GameInfo(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    val id: ObjectId = ObjectId.get(),
    val gameName: String = "",
    @JsonSerialize(using = ToStringSerializer::class)
    var playerId: ObjectId,

    var updatedAt: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @DBRef
    var currentState: GameState? = null,

    @DBRef
    var previousState: GameState? = null
)

data class GameJoinMessage(
    val type: String,
    val count: Int,
    val playerId: ArrayList<String>,
    val gameId: String
)

data class PlayerJoinMessage(
    val gameId: ObjectId,
    val playerId: String
)

data class GameInfoUpdate(
    val gameId: String,
    val updatedAt: LocalDateTime,
    val type: String
)

data class GameInfoInfo(
    val gameId: String,
    val type: String
)

data class NewGameInfoBody(
    val gameName: String,
    val playerId: ObjectId
)

data class GameInfoExportMessage(
    val file: String
)

data class GameInfoDownloadMessage(
    val game: GameInfo,
    val states: List<GameState>
)

data class OkResponseMessage(
    val message: String
)

data class GameStatisticsMessage(
    val balances: MutableList<Double>,
    val satisfaction: MutableList<Double>,
    val labels: MutableList<Int>
)

data class GameInformationMessage(
    val gameName: String,
    val roundNumber: Number,
    val balance: Double,
    val satisfaction: Double,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class GameIdMessage(
    val id: String
)

data class GameBalanceMessage(
    val balances: MutableList<Double>,
    val labels: MutableList<Int>
)

data class GameSatisfactionMessage(
    val satisfaction: MutableList<Double>,
    val labels: MutableList<Int>
)

data class GameStockHistoryMessage(
    val data: MutableList<Int>,
    val labels: MutableList<Int>
)