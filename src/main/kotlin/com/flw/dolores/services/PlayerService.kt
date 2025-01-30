package com.flw.dolores.services

import com.flw.dolores.entities.*
import com.flw.dolores.factory.PasswordHashEncoder
import com.flw.dolores.repositories.GameInfoRepository
import com.flw.dolores.repositories.PlayerRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

@Component
class PlayerService(
    private val playerRepository: PlayerRepository,
    private val encoder: PasswordHashEncoder,
    private val gameInfoRepository: GameInfoRepository
) {

    fun findById(playerId: ObjectId): Player {
        return playerRepository.findById(playerId)
    }

    fun getPlayerByName(name: String): Player? {
        return playerRepository.findByUserName(name)
    }

    fun getPlayerNameById(playerId: ObjectId): PlayerNameResponse {
        val player = this.findById(playerId)
        return PlayerNameResponse(userName = player.userName)
    }

    fun getAllPlayer(): List<PlayerResponse> {
        val players = playerRepository.findAll()
        val response: MutableList<PlayerResponse> = mutableListOf()
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
        return response
    }

    fun createPlayer(player: NewPlayerRequestBody): Player? {
        val existingPlayer: Player? = this.getPlayerByName(player.userName)
        return if (existingPlayer == null) {
            val password = encoder.encode(player.password)
            var newPlayer = Player(userName = player.userName, password = password)
            newPlayer = playerRepository.save(newPlayer)
            newPlayer
        } else {
            null
        }
    }

    fun initializeAdmin() {
        val player: Player? = this.getPlayerByName("admin")
        if (player == null) {
            val password = encoder.encode("dolores_admin")
            val admin = Player(userName = "admin", password = password, status = true, role = "admin")
            playerRepository.save(admin)
        }
    }

    fun changePlayerStatus(playerId: ObjectId): OkResponseMessage {
        val player: Player = this.findById(playerId)
        player.status = !player.status
        playerRepository.save(player)
        return OkResponseMessage(message = "ok")
    }


    fun getPlayerGameCount(playerId: ObjectId): PlayerCountResponse {
        val count = gameInfoRepository.countAllByPlayerId(playerId)
        return PlayerCountResponse(count = count)
    }

    fun deletePlayer(playerId: ObjectId): OkResponseMessage {
        val player: Player = this.findById(playerId)
        if (player.role == "admin") {
            return OkResponseMessage(message = "You cannot delete the Admin")
        }
        playerRepository.deleteById(playerId)
        return OkResponseMessage(message = "ok")
    }
}