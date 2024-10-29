package com.flw.dolores.entities

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "gamestates")
class GameState(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    val id: ObjectId = ObjectId.get(),
    var roundNumber: Int = 11,

    var updatedAt: LocalDateTime,

    @JsonSerialize(using = ToStringSerializer::class)
    val gameInfoId: ObjectId,
    @JsonSerialize(using = ToStringSerializer::class)
    var playerId: ObjectId,
    val employeeDynamics: MutableList<EmployeeDynamic> = mutableListOf(),
    val conveyorDynamics: MutableList<ConveyorDynamic> = mutableListOf(),
    val articleDynamics: MutableList<ArticleDynamic> = mutableListOf(),
    val messages: MutableList<Message> = mutableListOf(),
    val orders: MutableList<Order> = mutableListOf(),
    val customerJobs: MutableList<CustomerJob> = mutableListOf(),
    val conveyorStore: MutableList<ConveyorDynamic> = mutableListOf(),
    val employeeStore: MutableList<EmployeeDynamic> = mutableListOf(),
    val storage: Storage,

    val roundValues: RoundValues,
) {
    fun copy(): GameState {
        return GameState(
            gameInfoId = this.gameInfoId,
            roundNumber = this.roundNumber,
            playerId = this.playerId,
            employeeDynamics = this.employeeDynamics.toMutableList(),
            employeeStore = this.employeeStore.toMutableList(),
            articleDynamics = this.articleDynamics.toMutableList(),
            conveyorDynamics = this.conveyorDynamics.toMutableList(),
            conveyorStore = this.conveyorStore.toMutableList(),
            orders = this.orders.toMutableList(),
            customerJobs = this.customerJobs.toMutableList(),
            messages = this.messages.toMutableList(),
            roundValues = this.roundValues.copy(),
            storage = this.storage.copy(),
            updatedAt = LocalDateTime.now()
        )
    }

    fun setOvertime(process: Int, overtime: Int) {
        this.roundValues.overtime_process[process] = overtime
    }

    fun setClimateInvestment(climateInvestment: Int) {
        this.roundValues.work_climate_invest = climateInvestment
    }

    fun updateServices(services: List<Int>) {
        this.roundValues.module_order_quantity = services.contains(0)
        this.roundValues.module_reorder_level = services.contains(1)
        this.roundValues.module_safety_stock = services.contains(2)
        this.roundValues.module_status_report = services.contains(3)
        this.roundValues.module_look_in_storage = services.contains(4)
    }

    fun updateTechnologies(technologyUpdate: Int) {
        val technologyCost = GameValues.technology_cost
        this.roundValues.itCosts = technologyCost[technologyUpdate]
        when (technologyUpdate) {
            0 -> {
                this.roundValues.back_to_basic_storage = 0
                this.roundValues.back_to_it_level1 = 0
                this.roundValues.back_to_it_level2 = 0
            }

            1 -> {
                this.roundValues.back_to_basic_storage = 7
                this.roundValues.back_to_it_level1 = 0
                this.roundValues.back_to_it_level2 = 0
            }

            2 -> {
                this.roundValues.back_to_basic_storage = 7
                this.roundValues.back_to_it_level1 = 5
                this.roundValues.back_to_it_level2 = 0
            }

            else -> {
                this.roundValues.back_to_basic_storage = 7
                this.roundValues.back_to_it_level1 = 5
                this.roundValues.back_to_it_level2 = 3

            }
        }
    }

    fun updateStorageEntranceLevel(conditionCost: Int) {
        this.roundValues.loading_equipment_level = conditionCost
    }

    fun updateStorageCapacityDistribution(distribution: Double) {
        this.roundValues.storage_factor = distribution
    }

    fun updateStorageInboundControl(control: Double) {
        this.roundValues.pallet_we_factor = control
    }

    fun updateStorageOutboundControl(control: Double) {
        this.roundValues.pallet_wa_factor = control
    }

    fun updateUnitSecurityDevice(securityDevice: Boolean) {
        this.roundValues.unit_security_devices_used = securityDevice
    }

    fun updateIncomingStorageStrategy(strategy: Int) {
        this.roundValues.strategy_incoming = strategy
    }

    fun updateStorageStrategy(strategy: Int) {
        this.roundValues.strategy_storage = strategy
    }

    fun updateOutgoingStorageStrategy(strategy: Int) {
        this.roundValues.strategy_outgoing = strategy
    }

    fun initiateABCAnalysis() {
        this.roundValues.abc_analysis_round = this.roundNumber
    }

    fun initiateABCZoning() {
        this.roundValues.abc_zoning_round = this.roundNumber
    }

    fun terminateEmployee(employeeId: ObjectId) {
        val employeeDynamicList = this.employeeDynamics
        val employeeDynamic: EmployeeDynamic = employeeDynamicList.first { it.employee.id == employeeId }
        if (employeeDynamic.employee.contractType == 2) {
            employeeDynamic.employee.endRound = this.roundNumber + 1
        } else {
            employeeDynamic.employee.endRound = this.roundNumber + 3
        }
    }

    fun hireEmployee(employeeId: ObjectId, processID: Int, contractType: Int) {
        val roundNumber = this.roundNumber
        val employeeDynamic = this.employeeStore.first { it.employee.id == employeeId }
        this.employeeStore.remove(employeeDynamic)
        employeeDynamic.process = Process.fromInt(processID)
        employeeDynamic.employee.employmentRound = roundNumber + 3
        employeeDynamic.employee.contractType = contractType
        if (contractType == 1) {
            employeeDynamic.salary *= 0.6
        } else if (contractType == 2) {
            employeeDynamic.employee.endRound = roundNumber + 4
            employeeDynamic.employee.employmentRound = roundNumber + 1
        }
        this.employeeDynamics.add(employeeDynamic)
    }

    fun trainEmployee(employeeId: ObjectId, qualification: Int) {
        val roundNumber = this.roundNumber
        val employeeDynamic = this.employeeDynamics.first { it.employee.id == employeeId }
        when (qualification) {
            1 -> employeeDynamic.fpRound = roundNumber + 2
            2 -> employeeDynamic.secRound = roundNumber + 1
            else -> employeeDynamic.qmRound = roundNumber + 2
        }
    }

    fun updateEmployeeProcess(employeeId: ObjectId, processID: Int) {
        val employeeDynamic = this.employeeDynamics.first { it.employee.id == employeeId }
        employeeDynamic.process = Process.fromInt(processID)
    }

    fun updateConveyorProcess(conveyorId: ObjectId, processID: Int) {
        val conveyorDynamic = this.conveyorDynamics.first { it.conveyor.id == conveyorId }
        conveyorDynamic.process = Process.fromInt(processID)
    }

    fun updateConveyorMaintenance(conveyorId: ObjectId) {
        val conveyorDynamic = this.conveyorDynamics.first { it.conveyor.id == conveyorId }
        conveyorDynamic.maintenanceEnabled = !conveyorDynamic.maintenanceEnabled
    }

    fun overhaulConveyor(conveyorId: ObjectId) {
        val conveyorDynamic = this.conveyorDynamics.first { it.conveyor.id == conveyorId }
        conveyorDynamic.overhaul = true
    }

    fun sellConveyor(conveyorId: ObjectId) {
        val conveyorDynamic = this.conveyorDynamics.first { it.conveyor.id == conveyorId }
        conveyorDynamic.sold = true
    }

    fun buyConveyor(conveyorId: ObjectId) {
        val conveyorDynamic = this.conveyorStore.first { it.conveyor.id == conveyorId }
        conveyorDynamic.id = ObjectId.get()
        conveyorDynamic.conveyor.id = ObjectId.get()
        conveyorDynamic.roundBought = this.roundNumber
        this.conveyorDynamics.add(conveyorDynamic)
    }
}

data class OverTimeMessage(
    val process: Int,
    val overtime: Int
)

data class InvestmentMessage(
    val climateInvestment: Int
)

data class ServiceUpdateMessage(
    val services: List<Int>
)

data class TechnologyUpdateMessage(
    val technology: Int
)

data class StorageEntranceLevelMessage(
    val conditionCost: Int
)

data class StorageCapacityDistributionMessage(
    val distribution: Double
)

data class StorageControlMessage(
    val control: Double
)

data class UnitSecurityDeviceUpdateMessage(
    val securityDevice: Boolean
)

data class StorageStrategyMessage(
    val strategy: Int
)