package com.flw.dolores.factory

import com.flw.dolores.entities.GameInfo
import com.flw.dolores.repositories.GameInfoRepository
import com.flw.dolores.repositories.GameStateRepository
import org.bson.types.ObjectId
import org.springframework.cache.CacheManager
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class GameInfoFactory(
    private val gameInfoRepository: GameInfoRepository,
    private val gameStateRepository: GameStateRepository,
    private val cacheManager: CacheManager
) {
    private val gameStateFactory: GameStateFactory = GameStateFactory(gameStateRepository)

    fun createNewGame(gameName: String, playerId: ObjectId): GameInfo {
        var gameInfo = GameInfo(
            gameName = gameName,
            playerId = playerId
        )
        val newGameState = gameStateFactory.createGameState(gameInfo, playerId)
        gameInfo.currentState = newGameState
        gameInfo.previousState = newGameState

        val stateCache = cacheManager.getCache("gameStates")
        stateCache?.put(newGameState.id, ResponseEntity.ok(newGameState))

        gameInfo = gameInfoRepository.save(gameInfo)

        val gameCache = cacheManager.getCache("games")
        gameCache?.put(gameInfo.id, ResponseEntity.ok(gameInfo))

        return gameInfo
    }

    fun nextRound(gameId: ObjectId): GameInfo {
        var gameInfo = gameInfoRepository.findById(gameId)
        gameInfo.previousState = gameInfo.currentState
        gameInfo.currentState = gameInfo.currentState?.let { gameStateFactory.nextRound(it) }
        gameInfo.updatedAt = LocalDateTime.now()
        gameInfo = gameInfoRepository.save(gameInfo)

        val stateCache = cacheManager.getCache("gameStates")
        gameInfo.currentState?.let { stateCache?.put(it.id, ResponseEntity.ok(gameInfo.currentState)) }

        return gameInfo
    }

    fun previousRound(gameId: ObjectId): GameInfo {
        var gameInfo = gameInfoRepository.findById(gameId)
        if (gameInfo.currentState?.roundNumber!! > 11) {
            gameInfo.currentState?.let { gameStateRepository.deleteById(it.id) }

            val cache = cacheManager.getCache("gameStates")
            gameInfo.currentState?.let { cache?.evict(it.id) }

            gameInfo.currentState = gameInfo.previousState
            if (gameInfo.previousState?.roundNumber!! > 11) {
                gameInfo.previousState =
                    gameStateFactory.getPreviousRound(gameInfo.id, gameInfo.currentState?.roundNumber!! - 1)
            }
        }
        gameInfo.updatedAt = LocalDateTime.now()
        gameInfo = gameInfoRepository.save(gameInfo)

        return gameInfo
    }
}