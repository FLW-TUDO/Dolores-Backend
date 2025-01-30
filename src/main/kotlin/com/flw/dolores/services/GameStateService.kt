package com.flw.dolores.services

import com.flw.dolores.entities.*
import com.flw.dolores.factory.GameStateFactory
import com.flw.dolores.repositories.GameStateRepository
import org.bson.types.ObjectId
import org.springframework.cache.CacheManager
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class GameStateService(
    private val gameStateRepository: GameStateRepository,
    private val gameStateFactory: GameStateFactory,
    private val websocketService: WebsocketService,
    private val cacheManager: CacheManager
) {
    fun updateGameStateAndCache(gameState: GameState) {
        gameState.updatedAt = LocalDateTime.now()
        gameStateRepository.save(gameState)
        val cache = cacheManager.getCache("gameStates")
        cache?.put(gameState.id, gameState)
        websocketService.sendMessage(
            type = "state",
            gameId = gameState.gameInfoId,
            updatedAt = gameState.updatedAt
        )
    }

    fun getGameStateById(stateId: ObjectId): GameState {
        return gameStateRepository.findById(stateId)
    }

    fun addOrderToGameState(stateId: ObjectId, order: OrderMessage): GameState {
        val gameState = this.getGameStateById(stateId)
        val newOrder = gameStateFactory.createOrder(order)
        gameState.orders.add(newOrder)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun cancelOrderById(stateId: ObjectId, orderId: ObjectId): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.cancelOrder(orderId)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun terminateEmployeeById(stateId: ObjectId, employeeId: ObjectId): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.terminateEmployee(employeeId)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun hireEmployeeById(stateId: ObjectId, employeeMessage: EmployeeHireMessage): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.hireEmployee(employeeMessage.employeeId, employeeMessage.process, employeeMessage.contractType)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun trainEmployee(stateId: ObjectId, trainingMessage: EmployeeTrainingMessage): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.trainEmployee(trainingMessage.employeeId, trainingMessage.qualification)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun updateEmployeeProcess(stateId: ObjectId, updateMessage: EmployeeProcessMessage): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.updateEmployeeProcess(updateMessage.employeeId, updateMessage.process)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun setOvertime(stateId: ObjectId, body: OverTimeMessage): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.setOvertime(body.process, body.overtime)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun setInvestment(
        stateId: ObjectId,
        body: InvestmentMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.setClimateInvestment(body.climateInvestment)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun updateConveyorProcess(
        stateId: ObjectId,
        body: ConveyorProcessMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.updateConveyorProcess(body.conveyorId, body.process)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun updateConveyorMaintenance(
        stateId: ObjectId,
        body: ConveyorMaintenanceMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.updateConveyorMaintenance(body.conveyorId)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun overhaulConveyor(
        stateId: ObjectId,
        body: ConveyorOverhaulMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.overhaulConveyor(body.conveyorId)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun sellConveyor(
        stateId: ObjectId,
        body: ConveyorSellMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.sellConveyor(body.conveyorId)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun buyConveyor(
        stateId: ObjectId,
        body: ConveyorBuyMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.buyConveyor(body.conveyorId)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun updateStorageEntranceLevel(
        stateId: ObjectId,
        body: StorageEntranceLevelMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.updateStorageEntranceLevel(body.conditionCost)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun updateServices(
        stateId: ObjectId,
        body: ServiceUpdateMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.updateServices(body.services)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun updateTechnologies(
        stateId: ObjectId,
        body: TechnologyUpdateMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.updateTechnologies(body.technology)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun updateCapacityDistribution(
        stateId: ObjectId,
        body: StorageCapacityDistributionMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.updateStorageCapacityDistribution(body.distribution)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun updateInboundControl(
        stateId: ObjectId,
        body: StorageControlMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.updateStorageInboundControl(body.control)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun updateOutboundControl(
        stateId: ObjectId,
        body: StorageControlMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.updateStorageOutboundControl(body.control)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun updateUnitSecurityDevices(
        stateId: ObjectId,
        body: UnitSecurityDeviceUpdateMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.updateUnitSecurityDevice(body.securityDevice)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun updateIncomingStrategy(
        stateId: ObjectId,
        body: StorageStrategyMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.updateIncomingStorageStrategy(body.strategy)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun updateStorageStrategy(
        stateId: ObjectId,
        body: StorageStrategyMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.updateStorageStrategy(body.strategy)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun updateOutboundStrategy(
        stateId: ObjectId,
        body: StorageStrategyMessage
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.updateOutgoingStorageStrategy(body.strategy)
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun initiateABCAnalysis(
        stateId: ObjectId
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.initiateABCAnalysis()
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun initiateABCZoning(
        stateId: ObjectId
    ): GameState {
        val gameState = this.getGameStateById(stateId)
        gameState.initiateABCZoning()
        this.updateGameStateAndCache(gameState)
        return gameState
    }

    fun getGameStatesByGameInfoId(gameId: ObjectId): List<GameState> {
        return gameStateRepository.findAllByGameInfoId(gameId).sortedBy { it.roundNumber }
    }

    fun saveGameState(gameState: GameState) {
        gameStateRepository.save(gameState)
    }

    fun deleteStateById(gameStateId: ObjectId) {
        gameStateRepository.deleteById(gameStateId)
    }

    fun deleteStateByPlayerId(playerId: ObjectId) {
        gameStateRepository.deleteByPlayerId(playerId)
    }

    fun deleteAllStates() {
        gameStateRepository.deleteAll()
    }
}