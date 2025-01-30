package com.flw.dolores.controller

import com.flw.dolores.entities.GameJoinMessage
import com.flw.dolores.entities.PlayerJoinMessage
import com.flw.dolores.services.WebsocketService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller


@Controller
class WebsocketController(
    private val websocketService: WebsocketService
) {
    
    @MessageMapping("/joinGame")
    @SendTo("/topic/updateGame")
    fun joinGame(message: PlayerJoinMessage): GameJoinMessage {
        return websocketService.joinGame(message)
    }

    @MessageMapping("/leaveGame")
    @SendTo("/topic/updateGame")
    fun leaveGame(message: PlayerJoinMessage): GameJoinMessage {
        return websocketService.leaveGame(message)
    }
}