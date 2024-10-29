package com.flw.dolores.controller

import com.flw.dolores.entities.*
import com.flw.dolores.factory.*
import com.flw.dolores.repositories.GameStateRepository
import org.bson.types.ObjectId
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/gameState")
class GameStateController(
    private val gameStateRepository: GameStateRepository,
    private val websocket: WebsocketService,
    private val cacheManager: CacheManager
) {
    private val orderFactory: OrderFactory = OrderFactory()

    fun updateGameStateAndCache(gameState: GameState) {
        gameState.updatedAt = LocalDateTime.now()
        gameStateRepository.save(gameState)
        val cache = cacheManager.getCache("gameStates")
        cache?.put(gameState.id, ResponseEntity.ok(gameState))
        websocket.sendMessage(
            type = "state",
            gameId = gameState.gameInfoId,
            updatedAt = gameState.updatedAt
        )
    }

    @GetMapping("/{stateId}")
    @Cacheable("gameStates")
    fun getById(@PathVariable stateId: ObjectId): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/order")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun addOrder(@PathVariable stateId: ObjectId, @RequestBody body: OrderMessage): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        orderFactory.createOrder(gameState, body)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @GetMapping("/{stateId}/order/{orderId}")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun cancelOrder(
        @PathVariable stateId: ObjectId,
        @PathVariable orderId: ObjectId
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        orderFactory.cancelOrder(gameState, orderId)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @GetMapping("/{stateId}/employees/terminate/{employeeId}")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun terminateEmployee(
        @PathVariable stateId: ObjectId,
        @PathVariable employeeId: ObjectId
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.terminateEmployee(employeeId)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/employees/")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun hireEmployee(
        @PathVariable stateId: ObjectId,
        @RequestBody body: EmployeeHireMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.hireEmployee(body.employeeId, body.process, body.contractType)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/employees/train")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun trainEmployee(
        @PathVariable stateId: ObjectId,
        @RequestBody body: EmployeeTrainingMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.trainEmployee(body.employeeId, body.qualification)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/employees/process")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun updateEmployeeProcess(
        @PathVariable stateId: ObjectId,
        @RequestBody body: EmployeeProcessMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.updateEmployeeProcess(body.employeeId, body.process)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/overtime")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun setOvertime(@PathVariable stateId: ObjectId, @RequestBody body: OverTimeMessage): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.setOvertime(body.process, body.overtime)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/investment")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun setInvestment(
        @PathVariable stateId: ObjectId,
        @RequestBody body: InvestmentMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.setClimateInvestment(body.climateInvestment)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/conveyor/process")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun updateConveyorProcess(
        @PathVariable stateId: ObjectId,
        @RequestBody body: ConveyorProcessMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.updateConveyorProcess(body.conveyorId, body.process)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/conveyor/maintenance")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun updateConveyorMaintenance(
        @PathVariable stateId: ObjectId,
        @RequestBody body: ConveyorMaintenanceMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.updateConveyorMaintenance(body.conveyorId)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/conveyor/overhaul")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun overhaulConveyor(
        @PathVariable stateId: ObjectId,
        @RequestBody body: ConveyorOverhaulMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.overhaulConveyor(body.conveyorId)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/conveyor/sell")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun sellConveyor(
        @PathVariable stateId: ObjectId,
        @RequestBody body: ConveyorSellMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.sellConveyor(body.conveyorId)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/conveyor/buy")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun buyConveyor(
        @PathVariable stateId: ObjectId,
        @RequestBody body: ConveyorBuyMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.buyConveyor(body.conveyorId)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/information/lelevel")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun updateStorageEntranceLevel(
        @PathVariable stateId: ObjectId,
        @RequestBody body: StorageEntranceLevelMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.updateStorageEntranceLevel(body.conditionCost)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/information/service")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun updateServices(
        @PathVariable stateId: ObjectId,
        @RequestBody body: ServiceUpdateMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.updateServices(body.services)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/information/technology")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun updateTechnologies(
        @PathVariable stateId: ObjectId,
        @RequestBody body: TechnologyUpdateMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.updateTechnologies(body.technology)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }


    @PostMapping("/{stateId}/organisation/capacity")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun updateCapacityDistribution(
        @PathVariable stateId: ObjectId,
        @RequestBody body: StorageCapacityDistributionMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.updateStorageCapacityDistribution(body.distribution)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/organisation/inbound")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun updateInboundControl(
        @PathVariable stateId: ObjectId,
        @RequestBody body: StorageControlMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.updateStorageInboundControl(body.control)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/organisation/outbound")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun updateOutboundControl(
        @PathVariable stateId: ObjectId,
        @RequestBody body: StorageControlMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.updateStorageOutboundControl(body.control)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/organisation/unitsecurity")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun updateUnitSecurityDevices(
        @PathVariable stateId: ObjectId,
        @RequestBody body: UnitSecurityDeviceUpdateMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.updateUnitSecurityDevice(body.securityDevice)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/organisation/incstrategy")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun updateIncomingStrategy(
        @PathVariable stateId: ObjectId,
        @RequestBody body: StorageStrategyMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.updateIncomingStorageStrategy(body.strategy)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/organisation/storstrategy")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun updateStorageStrategy(
        @PathVariable stateId: ObjectId,
        @RequestBody body: StorageStrategyMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.updateStorageStrategy(body.strategy)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @PostMapping("/{stateId}/organisation/outstrategy")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun updateOutboundStrategy(
        @PathVariable stateId: ObjectId,
        @RequestBody body: StorageStrategyMessage
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.updateOutgoingStorageStrategy(body.strategy)
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @GetMapping("/{stateId}/organisation/analysis")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun initiateABCAnalysis(
        @PathVariable stateId: ObjectId
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.initiateABCAnalysis()
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }

    @GetMapping("/{stateId}/organisation/zoning")
    @CacheEvict("gameStates", key = "#stateId", beforeInvocation = true)
    fun initiateABCZoning(
        @PathVariable stateId: ObjectId
    ): ResponseEntity<GameState> {
        val gameState = gameStateRepository.findById(stateId)
        gameState.initiateABCZoning()
        this.updateGameStateAndCache(gameState)
        return ResponseEntity.ok(gameState)
    }
}