package com.flw.dolores.services

import com.flw.dolores.entities.GameInfoInfo
import com.flw.dolores.entities.GameInfoUpdate
import com.flw.dolores.entities.GameJoinMessage
import com.flw.dolores.entities.PlayerJoinMessage
import org.bson.types.ObjectId
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime


@Component
class WebsocketService(private val template: SimpMessagingTemplate) {

    private val lobbies: HashMap<String, ArrayList<String>> = HashMap()

    fun joinGame(message: PlayerJoinMessage): GameJoinMessage {
        val gameId = message.gameId
        val playerId = message.playerId
        if (lobbies.containsKey(gameId.toString())) {
            if (!lobbies[gameId.toString()]?.contains(playerId)!!)
                lobbies[gameId.toString()]?.add(playerId)
        } else {
            lobbies[gameId.toString()] = arrayListOf(playerId)
        }

        val size = lobbies[gameId.toString()]!!.size

        return GameJoinMessage(
            type = "lobbyJoin",
            count = size,
            playerId = lobbies[gameId.toString()]!!,
            gameId = gameId.toString()
        )
    }

    fun leaveGame(message: PlayerJoinMessage): GameJoinMessage {
        val gameId = message.gameId
        val playerId = message.playerId
        if (lobbies.containsKey(gameId.toString())) {
            lobbies[gameId.toString()]!!.remove(playerId)
        } else {
            lobbies[gameId.toString()] = arrayListOf()
        }

        val size = lobbies[gameId.toString()]!!.size


        return GameJoinMessage(
            type = "lobbyJoin",
            count = size,
            playerId = lobbies[gameId.toString()]!!,
            gameId = gameId.toString()
        )
    }

    fun sendMessage(
        type: String,
        gameId: ObjectId,
        updatedAt: LocalDateTime
    ) {
        template.convertAndSend(
            "/topic/updateGame",
            GameInfoUpdate(type = type, gameId = gameId.toString(), updatedAt = updatedAt)
        )
    }

    fun informPlayer(
        type: String,
        gameId: ObjectId,
    ) {
        template.convertAndSend(
            "/topic/updateGame",
            GameInfoInfo(type = type, gameId = gameId.toString())
        )
    }
}