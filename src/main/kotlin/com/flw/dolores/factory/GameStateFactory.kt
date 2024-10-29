package com.flw.dolores.factory

import com.flw.dolores.calculator.*
import com.flw.dolores.entities.GameInfo
import com.flw.dolores.entities.GameState
import com.flw.dolores.entities.RoundValues
import com.flw.dolores.repositories.GameStateRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bson.types.ObjectId
import java.time.LocalDateTime

internal inline fun <reified T> Gson.fromJson(json: String) = fromJson<T>(json, object : TypeToken<T>() {}.type)


class GameStateFactory(
    private val repository: GameStateRepository
) {
    private val employeeFactory: EmployeeFactory = EmployeeFactory()
    private val conveyorFactory: ConveyorFactory = ConveyorFactory()
    private val articleFactory: ArticleFactory = ArticleFactory()
    private val messageFactory: MessageFactory = MessageFactory()
    private val orderFactory: OrderFactory = OrderFactory()
    private val storageFactory: StorageFactory = StorageFactory()
    private val customerJobFactory: CustomerJobFactory = CustomerJobFactory()
    private val capacityCalculator: CapacityCalculator = CapacityCalculator()
    private val conveyorCalculator: ConveyorCalculator = ConveyorCalculator()
    private val costIncomeCalculator: CostIncomeCalculator = CostIncomeCalculator()
    private val customerJobCalculator: CustomerJobCalculator = CustomerJobCalculator()
    private val employeeCalculator: EmployeeCalculator = EmployeeCalculator()
    private val postThroughputCalculator: PostThroughputCalculator = PostThroughputCalculator()
    private val statisticCalculator: StatisticCalculator = StatisticCalculator()
    private val throughputCalculator: ThroughputCalculator = ThroughputCalculator()

    fun createGameState(gameInfo: GameInfo, playerId: ObjectId): GameState {
        val articleDynamics = articleFactory.loadDynamics()
        var gameState = GameState(
            gameInfoId = gameInfo.id,
            playerId = playerId,
            employeeDynamics = employeeFactory.loadDynamics(),
            conveyorDynamics = conveyorFactory.loadDynamics(),
            articleDynamics = articleDynamics,
            messages = messageFactory.loadDynamics(),
            orders = orderFactory.loadDynamics(),
            customerJobs = customerJobFactory.loadDynamics(),
            conveyorStore = conveyorFactory.loadStore(),
            employeeStore = MutableList(10) { employeeFactory.createNewDynamic() },
            storage = storageFactory.loadStorage(articleDynamics),
            roundValues = RoundValues(),
            updatedAt = LocalDateTime.now()
        )
        gameState = repository.save(gameState)

        return gameState
    }

    fun nextRound(gameState: GameState): GameState {
        var currentGameState = gameState.copy()
        currentGameState.roundNumber += 1

        employeeCalculator.calculate(currentGameState)
        conveyorCalculator.calculate(currentGameState)
        capacityCalculator.calculate(currentGameState)
        throughputCalculator.calculate(currentGameState)
        postThroughputCalculator.calculate(currentGameState)
        costIncomeCalculator.calculate(currentGameState)
        customerJobCalculator.calculate(currentGameState)
        statisticCalculator.calculate(currentGameState)

        conveyorCalculator.prepareNextRound(currentGameState)
        employeeCalculator.prepareNextRound(currentGameState)
        throughputCalculator.prepareNextRound(currentGameState)
        costIncomeCalculator.prepareNextRound(currentGameState)

        this.fillEmployeeStore(currentGameState)
        currentGameState = repository.save(currentGameState)
        return currentGameState
    }

    private fun fillEmployeeStore(gameState: GameState) {
        for (i in gameState.employeeStore.size..9) {
            gameState.employeeStore.add(employeeFactory.createNewDynamic())
        }
    }

    fun getPreviousRound(gameInfoId: ObjectId, roundNumber: Int): GameState {
        return repository.findByGameInfoIdAndRoundNumber(gameInfoId, roundNumber)
    }
}