package com.flw.dolores.controller

import com.flw.dolores.entities.*
import com.flw.dolores.security.AuthorizationFilter
import com.flw.dolores.services.PlayerService
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.annotation.PostConstruct

@RestController
@RequestMapping("/api/v1/users")
class PlayerController(
    private val playerService: PlayerService,
    @Value("\${secret.key}") private val secretKey: String
) {

    @PostConstruct
    fun init() {
        playerService.initializeAdmin()
    }

    @GetMapping
    fun getAllPlayer(): ResponseEntity<List<PlayerResponse>> {
        val players = playerService.getAllPlayer()
        return ResponseEntity.ok(players)
    }

    @PostMapping
    fun createPlayer(@RequestBody body: NewPlayerRequestBody): ResponseEntity<Player> {
        val player = playerService.createPlayer(body)
        return if (player != null) {
            ResponseEntity.ok(player)
        } else {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    fun checkPlayerStatus(@RequestHeader("Authorization") requestHeader: String): ResponseEntity<JWTValidResponse> {
        val response = AuthorizationFilter.authenticateToken(requestHeader, secretKey)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{playerId}")
    fun getPlayerName(@PathVariable playerId: ObjectId): ResponseEntity<PlayerNameResponse> {
        val playerName = playerService.getPlayerNameById(playerId)
        return ResponseEntity.ok(playerName)
    }

    @GetMapping("/{playerId}/status")
    @ResponseStatus(HttpStatus.OK)
    fun changePlayerStatus(@PathVariable playerId: ObjectId): ResponseEntity<OkResponseMessage> {
        val response = playerService.changePlayerStatus(playerId)
        return ResponseEntity.ok(response)
    }


    @GetMapping("/{playerId}/count")
    fun getPlayerGameCount(@PathVariable playerId: ObjectId): ResponseEntity<PlayerCountResponse> {
        val response = playerService.getPlayerGameCount(playerId)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{playerId}")
    fun deletePlayer(@PathVariable playerId: ObjectId): ResponseEntity<OkResponseMessage> {
        val response = playerService.deletePlayer(playerId)
        return ResponseEntity.ok(response)
    }


}