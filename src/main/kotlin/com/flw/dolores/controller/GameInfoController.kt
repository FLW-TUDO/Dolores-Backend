package com.flw.dolores.controller

import com.flw.dolores.entities.*
import com.flw.dolores.factory.GameInfoFactory
import com.flw.dolores.repositories.GameInfoRepository
import com.flw.dolores.repositories.GameStateRepository
import com.flw.dolores.repositories.PlayerRepository
import org.bson.types.ObjectId
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URLEncoder


@RestController
@RequestMapping("/api/v1/gameInfos")
class GameInfoController(
    private val gameInfoRepository: GameInfoRepository,
    private val gameStateRepository: GameStateRepository,
    private val playerRepository: PlayerRepository,
    private val websocket: WebsocketService,
    private val cacheManager: CacheManager
) {
    private val gameInfoFactory: GameInfoFactory =
        GameInfoFactory(gameInfoRepository, gameStateRepository, cacheManager)

    @PostMapping
    @Caching(
        evict = [
            CacheEvict(value = ["player_games"], key = "#body.playerId"),
        ]
    )
    fun createGame(@RequestBody body: NewGameInfoBody): ResponseEntity<GameInfo> {
        val game = gameInfoFactory.createNewGame(body.gameName, body.playerId)
        return ResponseEntity.ok(game)
    }

    @GetMapping("/{gameId}/nextRound")
    @CacheEvict(
        value = ["games", "game_information", "game_balance", "game_satisfaction", "game_stocks", "game_statistics"],
        key = "#gameId"
    )
    fun nextRound(@PathVariable gameId: ObjectId): ResponseEntity<GameInfo> {
        websocket.informPlayer(
            type = "calculation",
            gameId = gameId,
        )
        val gameInfo = gameInfoFactory.nextRound(gameId)
        websocket.sendMessage(
            type = "game",
            gameId = gameInfo.id,
            updatedAt = gameInfo.updatedAt
        )
        return ResponseEntity.ok(gameInfo)

    }

    @GetMapping("/{gameId}/previousRound")
    @CacheEvict(
        value = ["games", "game_information", "game_balance", "game_satisfaction", "game_stocks", "game_statistics"],
        key = "#gameId"
    )
    fun previousRound(@PathVariable gameId: ObjectId): ResponseEntity<GameInfo> {
        websocket.informPlayer(
            type = "calculation",
            gameId = gameId,
        )
        val gameInfo = gameInfoFactory.previousRound(gameId)
        websocket.sendMessage(
            type = "game",
            gameId = gameInfo.id,
            updatedAt = gameInfo.updatedAt
        )
        return ResponseEntity.ok(gameInfo)
    }

    @GetMapping
    fun getAllGames(): ResponseEntity<List<GameInfo>> {
        val games = gameInfoRepository.findAll()
        return ResponseEntity.ok(games)
    }

    @GetMapping("/{gameId}")
    @Cacheable("games", key = "#gameId")
    fun getGameById(@PathVariable gameId: ObjectId): ResponseEntity<GameInfo> {
        val game = gameInfoRepository.findById(gameId)
        return ResponseEntity.ok(game)
    }

    @GetMapping("/user/{playerId}")
    @Cacheable("player_games", key = "#playerId")
    fun getAllGamesByPlayer(@PathVariable playerId: ObjectId): ResponseEntity<List<GameIdMessage>> {
        val games = gameInfoRepository.findAllByPlayerId(playerId)
        val gameIds = games.map { game -> GameIdMessage(game.id.toString()) }
        return ResponseEntity.ok(gameIds)
    }

    @GetMapping("/{playerId}/count")
    @Cacheable("game_counts", key = "#playerId")
    fun countGameInfosByPlayer(@PathVariable playerId: ObjectId): ResponseEntity<Int> {
        val count = gameInfoRepository.countAllByPlayerId(playerId)
        return ResponseEntity.ok(count)
    }

    @DeleteMapping("/{gameId}")
    @Caching(
        evict = [
            CacheEvict(value = ["games"], key = "#gameId"),
            CacheEvict(value = ["gameStates"], allEntries = true),
            CacheEvict(value = ["player_games", "game_counts"], allEntries = true)
        ]
    )
    fun deleteGameById(@PathVariable gameId: ObjectId): ResponseEntity<OkResponseMessage> {
        gameInfoRepository.deleteById(gameId)
        return ResponseEntity.ok(OkResponseMessage(message = "ok"))
    }

    @DeleteMapping
    @CacheEvict(value = ["games", "gameStates", "player_games", "game_counts"], allEntries = true)
    fun deleteAllGames(): ResponseEntity<OkResponseMessage> {
        gameInfoRepository.deleteAll()
        gameStateRepository.deleteAll()
        return ResponseEntity.ok(OkResponseMessage(message = "ok"))
    }

    @DeleteMapping("/user/{playerId}")
    @Caching(
        evict = [
            CacheEvict(value = ["player_games", "game_counts"], key = "#playerId"),
            CacheEvict(value = ["games", "gameStates"], allEntries = true)
        ]
    )
    fun deleteAllGamesByPlayer(@PathVariable playerId: ObjectId): ResponseEntity<OkResponseMessage> {
        gameInfoRepository.deleteByPlayerId(playerId)
        gameStateRepository.deleteByPlayerId(playerId)
        return ResponseEntity.ok(OkResponseMessage(message = "ok"))
    }


    @GetMapping("/{gameId}/export")
    fun export(@PathVariable gameId: ObjectId): ResponseEntity<GameInfoExportMessage> {
        val gameStates = gameStateRepository.findAllByGameInfoId(gameId).sortedBy { it.roundNumber }
        var message = "\uFEFFRunde;Kontostand;Kundenzufriedenheit;Zeitpunkt;\n"
        for (gameState in gameStates) {
            val time = gameState.updatedAt.toString().split('T')
            message += "${gameState.roundNumber};"
            message += "${gameState.roundValues.accountBalance}€;"
            message += "${gameState.roundValues.customer_satisfaction}%;"
            message += "${time[0]};"
            message += "${time[1]};\n"
        }
        return ResponseEntity.ok(
            GameInfoExportMessage(
                file = "data:text/csv;charset=utf-8," + URLEncoder.encode(message, "utf-8")
            )
        )
    }

    @GetMapping("/{gameId}/download")
    fun download(@PathVariable gameId: ObjectId): ResponseEntity<GameInfoDownloadMessage> {
        val currentGame = gameInfoRepository.findById(gameId)
        val gameStates = gameStateRepository.findAllByGameInfoId(gameId).sortedBy { it.roundNumber }

        return ResponseEntity.ok(
            GameInfoDownloadMessage(
                game = currentGame,
                states = gameStates
            )
        )
    }

    @PostMapping("/import")
    @CacheEvict("player_games", allEntries = true)
    fun importGame(@RequestBody body: GameInfoDownloadMessage): ResponseEntity<OkResponseMessage> {
        val currentGame = body.game
        val states = body.states
        val admin = playerRepository.findByUserName("admin")

        try {
            for (state: GameState in states) {
                if (admin != null) {
                    state.playerId = admin.id
                }
                for (dynamic in state.employeeDynamics) {
                    dynamic.process = Process.fromInt(dynamic.process.value + 1)
                }
                for (dynamic in state.conveyorDynamics) {
                    dynamic.process = Process.fromInt(dynamic.process.value + 1)
                }
                for (stockGround in state.storage.occStocks) {
                    stockGround.pallet?.process = Process.fromInt(stockGround.pallet?.process?.value?.plus(1) ?: 0)
                }
                for (pallet in state.storage.pallets_not_in_storage) {
                    pallet.process = Process.fromInt(pallet.process.value + 1)
                }
                gameStateRepository.save(state)
            }
            if (admin != null) {
                currentGame.playerId = admin.id
            }
            gameInfoRepository.save(currentGame)
        } catch (e: Exception) {
            for (state: GameState in states) {
                gameStateRepository.deleteById(state.id)
            }
            gameInfoRepository.deleteById(currentGame.id)
            return ResponseEntity.ok(OkResponseMessage(message = "Error during import"))
        }

        return ResponseEntity.ok(OkResponseMessage(message = "Imported successfully"))
    }

    @GetMapping("/{gameId}/information")
    @Cacheable("game_information", key = "#gameId")
    fun getGameInformation(@PathVariable gameId: ObjectId): ResponseEntity<GameInformationMessage> {
        val gameInfo = gameInfoRepository.findById(gameId)
        return ResponseEntity.ok(
            gameInfo.currentState?.let {
                GameInformationMessage(
                    gameName = gameInfo.gameName,
                    roundNumber = it.roundNumber,
                    balance = it.roundValues.accountBalance,
                    satisfaction = it.roundValues.customer_satisfaction,
                    createdAt = gameInfo.createdAt,
                    updatedAt = it.updatedAt
                )
            }
        )
    }

    @GetMapping("/{gameId}/statisiticsinfo")
    @Cacheable("game_statistics", key = "#gameId")
    fun getGameBalanceAndSatisfactionStatistics(@PathVariable gameId: ObjectId): ResponseEntity<GameBalanceAndSatisfactionMessage> {
        val gameStates = gameStateRepository.findAllByGameInfoId(gameId).sortedBy { it.roundNumber }
        val balances: MutableList<Double> = mutableListOf()
        val satisfaction: MutableList<Double> = mutableListOf()
        val rounds: MutableList<Int> = mutableListOf()
        for (gameState in gameStates) {
            balances.add(gameState.roundValues.accountBalance)
            satisfaction.add(gameState.roundValues.customer_satisfaction)
            rounds.add(gameState.roundNumber)
        }
        return ResponseEntity.ok(
            GameBalanceAndSatisfactionMessage(
                balances = balances,
                satisfaction = satisfaction,
                labels = rounds
            )
        )
    }

    @GetMapping("/{gameId}/articles/{articleId}/paststock")
    @Cacheable("game_stocks", key = "#gameId")
    fun getPreviousStocks(
        @PathVariable gameId: ObjectId,
        @PathVariable articleId: Int
    ): ResponseEntity<GameStockHistoryMessage> {
        val gameStates = gameStateRepository.findAllByGameInfoId(gameId).sortedBy { it.roundNumber }
        val stocks: MutableList<Int> = mutableListOf()
        val rounds: MutableList<Int> = mutableListOf()
        for (gameState in gameStates) {
            val articleDynamic = gameState.articleDynamics.first { it.article.articleNumber == articleId }
            stocks.add(articleDynamic.currentStock)
            rounds.add(gameState.roundNumber)
        }
        return ResponseEntity.ok(
            GameStockHistoryMessage(
                data = stocks,
                labels = rounds
            )
        )
    }

    @GetMapping("/{gameId}/balance")
    @Cacheable("game_balance", key = "#gameId")
    fun getPreviousBalances(@PathVariable gameId: ObjectId): ResponseEntity<GameBalanceMessage> {
        val gameStates = gameStateRepository.findAllByGameInfoId(gameId).sortedBy { it.roundNumber }
        val balances: MutableList<Double> = mutableListOf()
        val rounds: MutableList<Int> = mutableListOf()
        for (gameState in gameStates) {
            balances.add(gameState.roundValues.accountBalance)
            rounds.add(gameState.roundNumber)
        }
        return ResponseEntity.ok(
            GameBalanceMessage(
                balances = balances,
                labels = rounds
            )
        )
    }

    @GetMapping("/{gameId}/satisfaction")
    @Cacheable("game_satisfaction", key = "#gameId")
    fun getPreviousSatisfaction(@PathVariable gameId: ObjectId): ResponseEntity<GameSatisfactionMessage> {
        val gameStates = gameStateRepository.findAllByGameInfoId(gameId).sortedBy { it.roundNumber }
        val satisfaction: MutableList<Double> = mutableListOf()
        val rounds: MutableList<Int> = mutableListOf()
        for (gameState in gameStates) {
            satisfaction.add(gameState.roundValues.customer_satisfaction)
            rounds.add(gameState.roundNumber)
        }
        return ResponseEntity.ok(
            GameSatisfactionMessage(
                satisfaction = satisfaction,
                labels = rounds
            )
        )
    }
}