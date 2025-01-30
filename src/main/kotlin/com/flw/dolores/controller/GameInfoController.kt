package com.flw.dolores.controller

import com.flw.dolores.entities.*
import com.flw.dolores.services.GameInfoService
import org.bson.types.ObjectId
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.cache.annotation.Caching
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/gameInfos")
class GameInfoController(
    private val gameInfoService: GameInfoService,
) {

    @PostMapping
    @Caching(
        evict = [
            CacheEvict(value = ["player_games"], key = "#body.playerId"),
        ]
    )
    fun createGame(@RequestBody body: NewGameInfoBody): ResponseEntity<GameInfo> {
        val game = gameInfoService.createGame(body)
        return ResponseEntity.ok(game)
    }

    @GetMapping("/{gameId}/nextRound")
    @CacheEvict(
        value = ["games", "game_information", "game_balance", "game_satisfaction", "game_stocks", "game_statistics"],
        key = "#gameId"
    )
    fun nextRound(@PathVariable gameId: ObjectId): ResponseEntity<GameInfo> {
        val gameInfo = gameInfoService.calculateNextRound(gameId)
        return ResponseEntity.ok(gameInfo)
    }

    @GetMapping("/{gameId}/previousRound")
    @CacheEvict(
        value = ["games", "game_information", "game_balance", "game_satisfaction", "game_stocks", "game_statistics"],
        key = "#gameId"
    )
    fun previousRound(@PathVariable gameId: ObjectId): ResponseEntity<GameInfo> {
        val gameInfo = gameInfoService.revertToPreviousRound(gameId)
        return ResponseEntity.ok(gameInfo)
    }

    @GetMapping
    fun getAllGames(): ResponseEntity<List<GameInfo>> {
        val games = gameInfoService.getAllGames()
        return ResponseEntity.ok(games)
    }

    @GetMapping("/{gameId}")
    @Cacheable("games", key = "#gameId")
    fun getGameById(@PathVariable gameId: ObjectId): ResponseEntity<GameInfo> {
        val game = gameInfoService.getGameById(gameId)
        return ResponseEntity.ok(game)
    }

    @GetMapping("/user/{playerId}")
    @Cacheable("player_games", key = "#playerId")
    fun getAllGameIdsByPlayer(@PathVariable playerId: ObjectId): ResponseEntity<List<GameIdMessage>> {
        val gameIds = gameInfoService.getAllGameIdsByPlayer(playerId)
        return ResponseEntity.ok(gameIds)
    }

    @GetMapping("/{playerId}/count")
    @Cacheable("game_counts", key = "#playerId")
    fun countGameInfosByPlayer(@PathVariable playerId: ObjectId): ResponseEntity<Int> {
        val count = gameInfoService.countGameInfosByPlayer(playerId)
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
        val responseMessage = gameInfoService.deleteGameById(gameId)
        return ResponseEntity.ok(responseMessage)
    }

    @DeleteMapping
    @CacheEvict(value = ["games", "gameStates", "player_games", "game_counts"], allEntries = true)
    fun deleteAllGames(): ResponseEntity<OkResponseMessage> {
        val returnMessage = gameInfoService.deleteAllGames()
        return ResponseEntity.ok(returnMessage)
    }

    @DeleteMapping("/user/{playerId}")
    @Caching(
        evict = [
            CacheEvict(value = ["player_games", "game_counts"], key = "#playerId"),
            CacheEvict(value = ["games", "gameStates"], allEntries = true)
        ]
    )
    fun deleteAllGamesByPlayer(@PathVariable playerId: ObjectId): ResponseEntity<OkResponseMessage> {
        val returnMessage = gameInfoService.deleteAllGamesByPlayer(playerId)
        return ResponseEntity.ok(returnMessage)
    }


    @GetMapping("/{gameId}/export")
    fun export(@PathVariable gameId: ObjectId): ResponseEntity<GameInfoExportMessage> {
        val gameInfoExportMessage = gameInfoService.exportGame(gameId)
        return ResponseEntity.ok(gameInfoExportMessage)
    }

    @GetMapping("/{gameId}/download")
    fun download(@PathVariable gameId: ObjectId): ResponseEntity<GameInfoDownloadMessage> {
        val gameInfoDownloadMessage = gameInfoService.downloadGame(gameId)
        return ResponseEntity.ok(gameInfoDownloadMessage)
    }

    @PostMapping("/import")
    @CacheEvict("player_games", allEntries = true)
    fun importGame(@RequestBody body: GameInfoDownloadMessage): ResponseEntity<OkResponseMessage> {
        val responseMessage = gameInfoService.importGame(body)
        return ResponseEntity.ok(responseMessage)
    }

    @GetMapping("/{gameId}/information")
    @Cacheable("game_information", key = "#gameId")
    fun getGameInformation(@PathVariable gameId: ObjectId): ResponseEntity<GameInformationMessage> {
        val gameInformationMessage = gameInfoService.exportGameInformation(gameId)
        return ResponseEntity.ok(gameInformationMessage)
    }

    @GetMapping("/{gameId}/statisiticsinfo")
    @Cacheable("game_statistics", key = "#gameId")
    fun getGameBalanceAndSatisfactionStatistics(@PathVariable gameId: ObjectId): ResponseEntity<GameStatisticsMessage> {
        val gameBalanceAndSatisfactionMessage = gameInfoService.exportGameStatistics(gameId)
        return ResponseEntity.ok(gameBalanceAndSatisfactionMessage)
    }

    @GetMapping("/{gameId}/articles/{articleId}/paststock")
    @Cacheable("game_stocks", key = "#gameId")
    fun getPreviousStocks(
        @PathVariable gameId: ObjectId,
        @PathVariable articleId: Int
    ): ResponseEntity<GameStockHistoryMessage> {
        val gameStockHistoryMessage = gameInfoService.exportPreviousStocks(gameId, articleId)
        return ResponseEntity.ok(gameStockHistoryMessage)
    }

    @GetMapping("/{gameId}/balance")
    @Cacheable("game_balance", key = "#gameId")
    fun getPreviousBalances(@PathVariable gameId: ObjectId): ResponseEntity<GameBalanceMessage> {
        val gameBalanceMessage = gameInfoService.exportPreviousBalance(gameId)
        return ResponseEntity.ok(gameBalanceMessage)
    }

    @GetMapping("/{gameId}/satisfaction")
    @Cacheable("game_satisfaction", key = "#gameId")
    fun getPreviousSatisfaction(@PathVariable gameId: ObjectId): ResponseEntity<GameSatisfactionMessage> {
        val gameSatisfactionMessage = gameInfoService.exportPreviousSatisfaction(gameId)
        return ResponseEntity.ok(gameSatisfactionMessage)
    }
}