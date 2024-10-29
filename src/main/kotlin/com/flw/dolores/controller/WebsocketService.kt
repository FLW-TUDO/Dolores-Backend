package com.flw.dolores.controller

import com.flw.dolores.entities.GameInfoInfo
import com.flw.dolores.entities.GameInfoUpdate
import org.bson.types.ObjectId
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime


@Component
class WebsocketService(private val template: SimpMessagingTemplate) {

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