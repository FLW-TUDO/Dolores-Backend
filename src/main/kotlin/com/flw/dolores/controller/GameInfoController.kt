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
    cacheManager: CacheManager
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
        var message =
            "\uFEFFRound;Balance;Customer Satisfaction;Current Stock;;;;Customer_Jobs;;;;Delayed_Jobs;;;;" +
                    "Current_Orders;;;;Pallets_process_processed;Pallets_process_unprocessed;;;;;;;;;;;;;" +
                    "Employee_process_with_forklift_licence;Employee_process_without_forklift_licence;Employee_process_total;;;;;;;;;" +
                    "Conveyor_process_with_forklift_licence;Conveyor_process_without_forklift_licence;Conveyor_process_total;;;;;;;" +
                    "Workload_employee_process;;;;;Workload_conveyor_process;;;;;" +
                    "Storage_incoming_capacity_distr;Storage_out_cap_distr;Timestamp;\n"
        message += ";;;Article 1;Article 2;Article 3;Article 4;Article 1;Article 2;Article 3;Article 4;Article 1;Article 2;Article 3;Article 4;" +
                "Article 1;Article 2;Article 3;Article 4;Unloading;Unloading;Receipt of goods;Receipt of goods;Storing;Storing;Storage;Storage;Shipping;Shipping;Outgoing goods inspection;Outgoing goods inspection;Shipping;Shipping;" +
                "Unloading;;;Receipt of goods;Storing;;;Outgoing goods inspection;Shipping;;;Unloading;;;Storing;;;Shipping;;;" +
                "Unloading;Receipt of goods;Storing;Outgoing goods inspection;Shipping;Unloading;Receipt of goods;Storing;Outgoing goods inspection;Shipping;\n"


        for (gameState in gameStates) {
            message += "${gameState.roundNumber};"
            message += "${gameState.roundValues.accountBalance}€;"
            message += "${gameState.roundValues.customer_satisfaction}%;"

            val time = gameState.updatedAt.toString().split('T')
            message += "${time[0]};"
            message += "${time[1]};\n"


            for (dynamic in gameState.articleDynamics) {
                message += "${dynamic.currentStock};"
            }

            val demand: MutableList<Int> = MutableList(4) { 0 }
            val delayed: MutableList<Int> = MutableList(4) { 0 }

            for (job in gameState.customerJobs) {
                val articleId = job.articleNumber - 100101

                if (job.demandRound == gameState.roundNumber) {
                    demand[articleId] += job.quantity
                } else {
                    delayed[articleId] += job.quantity
                }
            }


            for (quantity in demand) {
                message += "${quantity};"
            }

            for (quantity in delayed) {
                message += "${quantity};"
            }

            val orders: MutableList<Int> = MutableList(4) { 0 }

            for (order in gameState.orders) {
                if (order.deliveryRound == gameState.roundNumber) {
                    val articleId = order.articleNumber - 100101
                    orders[articleId] += order.deliveredQuantity
                }
            }


            for (quantity in orders) {
                message += "${quantity};"
            }

            val palletsTransported = gameState.roundValues.pallets_transported_process
            val palletsNotTransported = gameState.roundValues.pallets_not_transported_process

            message += "${palletsTransported[0]};${palletsNotTransported[0]};"
            message += "${palletsTransported[1]};${palletsNotTransported[1]};"
            message += "${gameState.roundValues.pallets_transported_la_in};${gameState.roundValues.not_transported_pallets_la_in};"
            message += "${palletsTransported[2]};${palletsNotTransported[2]};"
            message += "${gameState.roundValues.pallets_transported_la_out};${gameState.roundValues.not_transported_pallets_la_out};"
            message += "${palletsTransported[3]};${palletsNotTransported[3]};"
            message += "${palletsTransported[4]};${palletsNotTransported[4]};"

            var employeeENFKL = 0
            var employeeENNFKL = 0
            var conveyorENFKL = 0
            var conveyorENNFKL = 0

            var employeeWV = 0

            var employeeSTFLK = 0
            var employeeSTNFKL = 0
            var conveyorSTFKL = 0
            var conveyorSTNFKL = 0

            var employeeWK = 0

            var employeeVLFKL = 0
            var employeeVLNFKL = 0
            var conveyorVLFKL = 0
            var conveyorVLNFKL = 0

            for (conveyor in gameState.conveyorDynamics) {
                if (gameState.roundNumber < conveyor.roundBought)
                    when (conveyor.process) {
                        Process.UNLOADING -> {
                            if (conveyor.conveyor.needsForkliftPermit) {
                                conveyorENFKL += 1
                            } else {
                                conveyorENNFKL += 1
                            }
                        }

                        Process.STORAGE -> {
                            if (conveyor.conveyor.needsForkliftPermit) {
                                conveyorSTFKL += 1
                            } else {
                                conveyorSTNFKL += 1
                            }
                        }

                        Process.LOADING -> {
                            if (conveyor.conveyor.needsForkliftPermit) {
                                conveyorVLFKL += 1
                            } else {
                                conveyorVLNFKL += 1
                            }
                        }

                        else -> {}
                    }
            }

            for (employee in gameState.employeeDynamics) {
                if (gameState.roundNumber < employee.employee.employmentRound
                    || gameState.roundNumber >= employee.employee.endRound
                )
                    when (employee.process) {
                        Process.UNLOADING -> {
                            if (employee.qualification % 2 == 1) {
                                employeeENFKL += 1
                            } else {
                                employeeENNFKL += 1
                            }
                        }

                        Process.COLLECTION -> {
                            employeeWV += 1
                        }

                        Process.STORAGE -> {
                            if (employee.qualification % 2 == 1) {
                                employeeSTFLK += 1
                            } else {
                                employeeSTNFKL += 1
                            }
                        }

                        Process.CONTROL -> {
                            employeeWK += 1
                        }

                        Process.LOADING -> {
                            if (employee.qualification % 2 == 1) {
                                employeeVLFKL += 1
                            } else {
                                employeeVLNFKL += 1
                            }
                        }

                        else -> {}
                    }
            }

            message += "${employeeENFKL};${employeeENNFKL};${employeeENFKL + employeeENNFKL};"
            message += "${employeeWV};"
            message += "${employeeSTFLK};${employeeSTNFKL};${employeeSTFLK + employeeSTNFKL};"
            message += "${employeeWK};"
            message += "${employeeVLFKL};${employeeVLNFKL};${employeeVLFKL + employeeVLNFKL};"

            message += "${conveyorENFKL};${conveyorENNFKL};${conveyorENFKL + conveyorENNFKL};"
            message += "${conveyorSTFKL};${conveyorSTNFKL};${employeeSTFLK + conveyorSTNFKL};"
            message += "${conveyorVLFKL};${conveyorVLNFKL};${conveyorVLFKL + conveyorVLNFKL};"


            for (workload in gameState.roundValues.workload_employee) {
                message += "${workload}%;"
            }

            for (workload in gameState.roundValues.workload_conveyor) {
                message += "${workload}%;"
            }

            message += "${gameState.roundValues.storage_factor * 100}%;"
            message += "${(1 - gameState.roundValues.storage_factor) * 100}%;\n"
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