package com.flw.dolores.controller

import com.flw.dolores.entities.GameJoinMessage
import com.flw.dolores.entities.PlayerJoinMessage
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller


@Controller
class WebsocketController {

    private val lobbies: HashMap<String, ArrayList<String>> = HashMap()

    @MessageMapping("/joinGame")
    @SendTo("/topic/updateGame")
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

    @MessageMapping("/leaveGame")
    @SendTo("/topic/updateGame")
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
}