package com.flw.dolores.services

import com.flw.dolores.entities.*
import com.flw.dolores.factory.GameInfoFactory
import com.flw.dolores.repositories.GameInfoRepository
import org.bson.types.ObjectId
import org.springframework.stereotype.Component
import java.net.URLEncoder

@Component
class GameInfoService(
    private val gameInfoRepository: GameInfoRepository,
    private val gameStateService: GameStateService,
    private val playerService: PlayerService,
    private val gameInfoFactory: GameInfoFactory,
    private val websocket: WebsocketService,
) {

    fun createGame(newGame: NewGameInfoBody): GameInfo {
        return gameInfoFactory.createNewGame(newGame.gameName, newGame.playerId)
    }

    fun calculateNextRound(gameId: ObjectId): GameInfo {
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
        return gameInfo
    }

    fun revertToPreviousRound(gameId: ObjectId): GameInfo {
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
        return gameInfo
    }

    fun getAllGames(): List<GameInfo> {
        return gameInfoRepository.findAll()
    }

    fun getGameById(gameId: ObjectId): GameInfo {
        return gameInfoRepository.findById(gameId)
    }

    fun getAllGameIdsByPlayer(playerId: ObjectId): List<GameIdMessage> {
        val games = gameInfoRepository.findAllByPlayerId(playerId)
        return games.map { game -> GameIdMessage(game.id.toString()) }
    }

    fun countGameInfosByPlayer(playerId: ObjectId): Int {
        return gameInfoRepository.countAllByPlayerId(playerId)
    }

    fun deleteGameById(gameId: ObjectId): OkResponseMessage {
        return try {
            gameInfoRepository.deleteById(gameId)
            OkResponseMessage(message = "ok")
        } catch (e: Exception) {
            OkResponseMessage(message = "Failed to delete all games by gameId: $gameId")
        }
    }

    fun deleteAllGames(): OkResponseMessage {
        return try {
            gameInfoRepository.deleteAll()
            gameStateService.deleteAllStates()
            OkResponseMessage(message = "ok")
        } catch (e: Exception) {
            OkResponseMessage(message = "Failed to delete all games")
        }

    }

    fun deleteAllGamesByPlayer(playerId: ObjectId): OkResponseMessage {
        return try {
            gameInfoRepository.deleteByPlayerId(playerId)
            gameStateService.deleteStateByPlayerId(playerId)
            OkResponseMessage(message = "ok")
        } catch (e: Exception) {
            OkResponseMessage(message = "Failed to delete all games by playerId: $playerId")
        }
    }

    fun exportGame(gameId: ObjectId): GameInfoExportMessage {
        val gameStates = gameStateService.getGameStatesByGameInfoId(gameId)
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
        return GameInfoExportMessage(
            file = "data:text/csv;charset=utf-8," + URLEncoder.encode(message, "utf-8")
        )
    }

    fun downloadGame(gameId: ObjectId): GameInfoDownloadMessage {
        val currentGame = gameInfoRepository.findById(gameId)
        val gameStates = gameStateService.getGameStatesByGameInfoId(gameId)
        return GameInfoDownloadMessage(
            game = currentGame,
            states = gameStates
        )
    }

    fun importGame(gameInfoMessage: GameInfoDownloadMessage): OkResponseMessage {
        val currentGame = gameInfoMessage.game
        val states = gameInfoMessage.states
        val admin = playerService.getPlayerByName("admin")

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
                gameStateService.saveGameState(state)
            }
            if (admin != null) {
                currentGame.playerId = admin.id
            }
            gameInfoRepository.save(currentGame)
        } catch (e: Exception) {
            for (state: GameState in states) {
                gameStateService.deleteStateById(state.id)
            }
            gameInfoRepository.deleteById(currentGame.id)
            return OkResponseMessage(message = "Error during import")
        }

        return OkResponseMessage(message = "Imported successfully")
    }

    fun exportGameInformation(gameId: ObjectId): GameInformationMessage? {
        val gameInfo = getGameById(gameId)
        return gameInfo.currentState?.let {
            GameInformationMessage(
                gameName = gameInfo.gameName,
                roundNumber = it.roundNumber,
                balance = it.roundValues.accountBalance,
                satisfaction = it.roundValues.customer_satisfaction,
                createdAt = gameInfo.createdAt,
                updatedAt = it.updatedAt
            )
        }

    }

    fun exportGameStatistics(gameId: ObjectId): GameStatisticsMessage {
        val gameStates = gameStateService.getGameStatesByGameInfoId(gameId)
        val balances: MutableList<Double> = mutableListOf()
        val satisfaction: MutableList<Double> = mutableListOf()
        val rounds: MutableList<Int> = mutableListOf()
        for (gameState in gameStates) {
            balances.add(gameState.roundValues.accountBalance)
            satisfaction.add(gameState.roundValues.customer_satisfaction)
            rounds.add(gameState.roundNumber)
        }
        return GameStatisticsMessage(
            balances = balances,
            satisfaction = satisfaction,
            labels = rounds
        )
    }

    fun exportPreviousStocks(gameId: ObjectId, articleId: Int): GameStockHistoryMessage {
        val gameStates = gameStateService.getGameStatesByGameInfoId(gameId)
        val stocks: MutableList<Int> = mutableListOf()
        val rounds: MutableList<Int> = mutableListOf()
        for (gameState in gameStates) {
            val articleDynamic = gameState.articleDynamics.first { it.article.articleNumber == articleId }
            stocks.add(articleDynamic.currentStock)
            rounds.add(gameState.roundNumber)
        }
        return GameStockHistoryMessage(
            data = stocks,
            labels = rounds
        )
    }

    fun exportPreviousBalance(gameId: ObjectId): GameBalanceMessage {
        val gameStates = gameStateService.getGameStatesByGameInfoId(gameId)
        val balances: MutableList<Double> = mutableListOf()
        val rounds: MutableList<Int> = mutableListOf()
        for (gameState in gameStates) {
            balances.add(gameState.roundValues.accountBalance)
            rounds.add(gameState.roundNumber)
        }
        return GameBalanceMessage(
            balances = balances,
            labels = rounds
        )
    }

    fun exportPreviousSatisfaction(gameId: ObjectId): GameSatisfactionMessage {
        val gameStates = gameStateService.getGameStatesByGameInfoId(gameId)
        val satisfaction: MutableList<Double> = mutableListOf()
        val rounds: MutableList<Int> = mutableListOf()
        for (gameState in gameStates) {
            satisfaction.add(gameState.roundValues.customer_satisfaction)
            rounds.add(gameState.roundNumber)
        }
        return GameSatisfactionMessage(
            satisfaction = satisfaction,
            labels = rounds
        )
    }


}