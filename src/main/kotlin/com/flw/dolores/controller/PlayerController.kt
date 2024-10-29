package com.flw.dolores.controller

import com.flw.dolores.entities.*
import com.flw.dolores.factory.PasswordHashEncoder
import com.flw.dolores.repositories.GameInfoRepository
import com.flw.dolores.repositories.PlayerRepository
import com.flw.dolores.security.AuthorizationFilter
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.ArrayList
import javax.annotation.PostConstruct

@RestController
@RequestMapping("/api/v1/users")
class PlayerController(
    private val playerRepository: PlayerRepository,
    private val gameInfoRepository: GameInfoRepository,
    @Value("\${secret.key}") private val secretKey: String
) {
    private val encoder: PasswordHashEncoder = PasswordHashEncoder()

    @PostConstruct
    fun init() {
        val player: Player? = playerRepository.findByUserName("admin")
        if (player == null) {
            val password = encoder.encode("dolores_admin")
            val admin = Player(userName = "admin", password = password, status = true, role = "admin")
            playerRepository.save(admin)
        }
    }

    @GetMapping
    fun getAllPlayer(): ResponseEntity<List<PlayerResponse>> {
        val players = playerRepository.findAll()
        val response: ArrayList<PlayerResponse> = arrayListOf()
        for (player in players) {
            response.add(
                PlayerResponse(
                    id = player.id.toString(),
                    userName = player.userName,
                    role = player.role,
                    status = player.status,
                    updatedAt = player.updatedAt
                )
            )
        }
        return ResponseEntity.ok(response)
    }

    @PostMapping
    fun createPlayer(@RequestBody body: NewPlayerRequestBody): ResponseEntity<Player> {
        val player: Player? = playerRepository.findByUserName(body.userName)
        return if (player == null) {
            val password = encoder.encode(body.password)
            var newPlayer = Player(userName = body.userName, password = password)
            newPlayer = playerRepository.save(newPlayer)
            ResponseEntity.ok(newPlayer)
        } else {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/check")
    @ResponseStatus(HttpStatus.OK)
    fun checkPlayerStatus(@RequestHeader("Authorization") requestHeader: String): ResponseEntity<JWTValidResponse> {
        val status = AuthorizationFilter.authenticateToken(requestHeader, secretKey)
        return ResponseEntity.ok(JWTValidResponse(status = status))
    }

    @GetMapping("/{playerId}")
    fun getPlayerName(@PathVariable playerId: ObjectId): ResponseEntity<PlayerNameResponse> {
        val player: Player = playerRepository.findById(playerId)
        return ResponseEntity.ok(PlayerNameResponse(userName = player.userName))
    }

    @GetMapping("/{playerId}/status")
    @ResponseStatus(HttpStatus.OK)
    fun changePlayerStatus(@PathVariable playerId: ObjectId): ResponseEntity<OkResponseMessage> {
        val player: Player = playerRepository.findById(playerId)
        player.status = !player.status
        playerRepository.save(player)
        return ResponseEntity.ok(OkResponseMessage(message = "ok"))
    }


    @GetMapping("/{playerId}/count")
    fun getPlayerGameCount(@PathVariable playerId: ObjectId): ResponseEntity<PlayerCountResponse> {
        val count = gameInfoRepository.countAllByPlayerId(playerId)
        val response = PlayerCountResponse(count = count)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{playerId}")
    fun deletePlayer(@PathVariable playerId: ObjectId): ResponseEntity<OkResponseMessage> {
        val player: Player = playerRepository.findById(playerId)
        if (player.role == "admin") {
            return ResponseEntity.ok(OkResponseMessage(message = "ok"))
        }
        playerRepository.deleteById(playerId)
        return ResponseEntity.ok(OkResponseMessage(message = "ok"))
    }


}