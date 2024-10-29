package com.flw.dolores.calculator

import com.flw.dolores.entities.*
import com.flw.dolores.factory.MessageFactory
import kotlin.random.Random

class ConveyorCalculator {
    /**
     * Calculates the profit if the current conveyor is sold this round
     * @param dynamic current conveyor dynamic
     * @returns optional profit from sale else 0
     */
    private fun calculateProfitConveyorSold(dynamic: ConveyorDynamic): Double {
        var profit = 0.0
        if (dynamic.sold) {
            dynamic.status = -1
            profit = dynamic.currentValue
        }
        return profit
    }

    /**
     * Calculates the costs if the current conveyor is bought this round
     * @param dynamic current conveyor dynamic
     * @param roundNumber current round number
     * @returns optional costs for new bought conveyor
     */
    private fun calculateCostsConveyorBought(dynamic: ConveyorDynamic, roundNumber: Int): Double {
        var costs = 0.0
        if (this.getRoundForDelivery(dynamic) == roundNumber)
            costs = dynamic.conveyor.price
        return costs
    }

    /**
     * Checks if the current conveyor is ready for use this round
     * @param dynamic current conveyor dynamic
     * @param roundNumber current roundNumber
     * @returns if the current conveyor is ready for use
     */
    private fun isReady(dynamic: ConveyorDynamic, roundNumber: Int): Boolean {
        val deliveryRound = this.getRoundForDelivery(dynamic)
        val isDelivered = deliveryRound < roundNumber
        val isWorking = dynamic.condition > GameValues.conveyor_Scrap_Limit
        val notSold = !dynamic.sold

        if (isDelivered && !isWorking)
            dynamic.status = 2

        return isDelivered && isWorking && notSold
    }

    /**
     * Calculates the round in which the conveyor is bound for delivery
     * @param dynamic current conveyor dynamic
     * @returns the delivery round of the current conveyor
     */
    private fun getRoundForDelivery(dynamic: ConveyorDynamic): Int {
        return dynamic.roundBought + dynamic.conveyor.timeToDelivery
    }

    /**
     * Checks if the current conveyor suffered a breakdown
     * @param dynamic current conveyor dynamic
     * @returns true if breakdown has happened
     */
    private fun checkConveyorBreakdown(dynamic: ConveyorDynamic): Boolean {
        val isSold = dynamic.sold
        val condition = dynamic.condition
        val breakdownLimit = GameValues.conveyor_Breakdown_Limit
        val breakdownOdds = Random.nextDouble()

        if (!isSold && condition <= breakdownLimit && breakdownOdds < 0.5) {
            dynamic.status = 1
            return true
        }
        return false
    }

    /**
     * Calculates the breakdown time of the current conveyor
     * @param dynamic current conveyor dynamic
     * @returns the breakdown time of the current conveyor
     */
    private fun calculateConveyorBreakdownTime(dynamic: ConveyorDynamic): Int {
        return dynamic.conveyor.timeToRepair
    }

    /**
     * Calculates cost of a breakdown of the current conveyor
     * @param dynamic current conveyor dynamic
     * @returns the cost of the breakdown
     */
    private fun calculateConveyorBreakdownCosts(dynamic: ConveyorDynamic): Double {
        val conveyor = dynamic.conveyor
        val condition = dynamic.condition
        return kotlin.math.floor(conveyor.price * (condition / 100) * GameValues.conveyor_Repair_Cost)
    }

    /**
     * Updates conveyor and round properties (no test)
     * @param dynamic current conveyor dynamic
     * @param roundValues current roundValues
     * @param repairTime repair time of the current conveyor
     */
    private fun updateConveyorProperties(dynamic: ConveyorDynamic, roundValues: RoundValues, repairTime: Int) {
        val processID: Int = dynamic.process.toInt()
        val conveyor = dynamic.conveyor
        if (conveyor.needsForkliftPermit) {
            roundValues.conv_capacity_wfp_processes[processID] += (GameValues.working_time - repairTime).toDouble()
        } else {
            roundValues.conv_capacity_wofp_processes[processID] += (GameValues.working_time - repairTime).toDouble()
        }

        dynamic.currentValue = conveyor.price * (dynamic.condition.toDouble() / 100) * GameValues.conveyor_Sale_Factor
        roundValues.avg_speed_processes[processID] += conveyor.speed
        roundValues.conv_count_processes[processID] += 1
        roundValues.current_conv_value += dynamic.currentValue
    }

    /**
     * Calculates the costs for the overhaul of the current conveyor
     * @param dynamic current conveyor dynamic
     * @param roundNumber current roundNumber
     * @returns the costs of the overhaul
     */
    private fun calculateConveyorOverhaulCosts(dynamic: ConveyorDynamic, roundNumber: Int): Double {
        var costs = 0.0
        if (dynamic.overhaul) {
            val age = roundNumber - dynamic.roundBought
            val maxCondition = 100 - age * GameValues.conveyor_Damage_With_Maintenance
            val minCondition = maxCondition - 10
            dynamic.overhaul = false
            if (minCondition < dynamic.condition) return 0.0
            dynamic.condition =
                kotlin.math.round(Random.nextDouble() * (maxCondition - minCondition) + minCondition).toInt()
            costs = dynamic.overhaul_cost
        }
        return costs
    }


    /**
     * Calculates the cost for conveyor maintenance and the damage to the conveyor
     * @param dynamic current conveyor dynamic
     * @returns the cost for conveyor maintenance
     */
    private fun calculateConveyorMaintenanceCosts(dynamic: ConveyorDynamic): Double {
        val conveyor = dynamic.conveyor
        val maintenanceEnabled = dynamic.maintenanceEnabled
        val damage: Int
        var cost = 0.0
        if (maintenanceEnabled) {
            damage = GameValues.conveyor_Damage_With_Maintenance
            cost = conveyor.maintenanceCost
        } else {
            damage = GameValues.conveyor_Damage_Without_Maintenance
        }
        dynamic.condition = kotlin.math.max(dynamic.condition - damage, 0)
        return cost
    }

    /**
     * Checks the status of the conveyor and creates messages for the player (no test)
     * @param dynamic current conveyor dynamic
     * @param roundNumber current round number
     * @param messages current game messages
     */
    private fun checkConveyorStatus(dynamic: ConveyorDynamic, roundNumber: Int, messages: MutableList<Message>) {
        val conveyorName = dynamic.conveyor.name

        // Inform player of insufficient conveyor condition
        val minCondition = (GameValues.conveyor_Breakdown_Limit + GameValues.conveyor_Scrap_Limit) / 2
        if (dynamic.condition < minCondition && dynamic.status != 1) {
            val textDE =
                "Der technische Zustand des Fördermittels $conveyorName hat einen kritischen Wert erreicht."
            val textEN = "The technical condition of the conveyor $conveyorName has reached a critical level."
            val message: Message = MessageFactory.createMessage(textDE, textEN, roundNumber)
            messages.add(0, message)
        }

        // Inform player of breakdown
        if (dynamic.status == 1) {
            val textDE = "Das Fördermittel $conveyorName ist ausgefallen und musste repariert werden."
            val textEN = "The conveyor $conveyorName is down and needs to be repaired."
            val message: Message = MessageFactory.createMessage(textDE, textEN, roundNumber)
            messages.add(0, message)
        }

        // Inform player of scraped conveyor
        if (dynamic.status == 2) {
            val textDE =
                "Das Fördermittel $conveyorName ist endgültig ausgefallen. Es steht nun nicht mehr zur Verfügung."
            val textEN = "The conveyor $conveyorName is down and is not available any longer."
            val message: Message = MessageFactory.createMessage(textDE, textEN, roundNumber)
            messages.add(0, message)
        }
    }

    /**
     * Calculates the average speed of all conveyors in a process
     * @param roundValues current roundValues
     * @param processID id of current process
     */
    private fun calculateConveyorAvgSpeed(roundValues: RoundValues, processID: Int) {
        val conveyorCount = roundValues.conv_count_processes[processID]
        roundValues.avg_speed_processes[processID] =
            if (conveyorCount > 0) roundValues.avg_speed_processes[processID] / conveyorCount else 0.0
    }

    /**
     * Calculates the handicap factor if more than one conveyor operate in a process
     * @param capacity current capacity in process
     * @param conveyor_count current count of all conveyor in process
     * @returns the capacity reduction based on the handicap factor
     */
    private fun calculateConveyorHandicapFactor(capacity: Double, conveyor_count: Int): Double {
        return kotlin.math.max(
            kotlin.math.floor(capacity * (conveyor_count - 1) * GameValues.conveyor_disability_factor),
            0.0
        )
    }

    /**
     * Removes all sold conveyor from the conveyor dynamics
     * @param conveyorDynamics current conveyor dynamics
     */
    private fun removeSoldConveyorDynamics(conveyorDynamics: MutableList<ConveyorDynamic>) {
        conveyorDynamics.removeAll { it.sold }
    }

    /**
     * Calculates all profits and cost related to the conveyor
     * @param roundValues current round values
     * @param conveyorDynamics current conveyor dynamics
     * @param messages message list of current game state
     */
    private fun updateConveyorDynamicsAndRoundCost(
        roundValues: RoundValues,
        roundNumber: Int,
        conveyorDynamics: MutableList<ConveyorDynamic>,
        messages: MutableList<Message>
    ) {
        for (dynamic: ConveyorDynamic in conveyorDynamics) {
            // Calculate profit from sold conveyor
            val profit = this.calculateProfitConveyorSold(dynamic)
            roundValues.income_conveyor_sale += profit

            // Calculate cost for new purchased conveyor
            val purchaseCosts = this.calculateCostsConveyorBought(dynamic, roundNumber)
            roundValues.costs_new += purchaseCosts

            if (isReady(dynamic, roundNumber)) {

                // Check for conveyor breakdowns
                val breakdown = this.checkConveyorBreakdown(dynamic)
                var repairTime = 0
                if (breakdown) {
                    repairTime = this.calculateConveyorBreakdownTime(dynamic)
                    roundValues.costs_repair += calculateConveyorBreakdownCosts(dynamic)
                    roundValues.repair_duration += repairTime
                }

                // Update conveyor condition
                val maintenanceCosts = this.calculateConveyorMaintenanceCosts(dynamic)
                roundValues.costs_maintenance += maintenanceCosts

                // Handle conveyor overhaul
                val overhaulCosts = this.calculateConveyorOverhaulCosts(dynamic, roundNumber)
                roundValues.costs_overhaul += overhaulCosts

                // Create messages based on conveyor status
                checkConveyorStatus(dynamic, roundNumber, messages)

                // Updates all conveyor values and the roundValues
                updateConveyorProperties(dynamic, roundValues, repairTime)
            }
        }
    }

    /**
     * Updates all important conveyor properties in round values, including:
     * Count, average speed, capacity reduction due to interference
     * @param roundValues current round values
     * @param processID current process
     */
    private fun updateProcessConveyorProperties(roundValues: RoundValues, processID: Int) {
        val conveyorCount = roundValues.conv_count_processes[processID]

        // Calculate average conveyor speed
        calculateConveyorAvgSpeed(roundValues, processID)

        // Calculate conveyor capacity reduction due to interference
        val capacityWfp = roundValues.emp_capacity_wfp_processes[processID]
        val handicapCapacityWfp = this.calculateConveyorHandicapFactor(capacityWfp, conveyorCount)
        val capacityWofp = roundValues.emp_capacity_wofp_processes[processID]
        val handicapCapacityWofp = this.calculateConveyorHandicapFactor(capacityWofp, conveyorCount)
        roundValues.conv_capacity_wfp_processes[processID] -= kotlin.math.min(
            roundValues.conv_capacity_wfp_processes[processID],
            handicapCapacityWfp
        )
        roundValues.conv_capacity_wofp_processes[processID] -= kotlin.math.min(
            roundValues.conv_capacity_wofp_processes[processID],
            handicapCapacityWofp
        )
        roundValues.conv_capacity_processes[processID] =
            roundValues.conv_capacity_wfp_processes[processID] + roundValues.conv_capacity_wofp_processes[processID]
    }

    /**
     * Reset function to zero all roundValues that are summed throughout the calculations to avoid previous non-zero inits
     * @param roundValues current round values
     */
    private fun resetValues(roundValues: RoundValues) {
        roundValues.costs_overhaul = 0.0
        roundValues.costs_repair = 0.0
        roundValues.costs_maintenance = 0.0
        roundValues.repair_duration = 0.0
        roundValues.income_conveyor_sale = 0.0
        roundValues.costs_new = 0.0
        roundValues.current_conv_value = 0.0
        roundValues.conv_count_processes = Array(5) { 0 }
        roundValues.avg_speed_processes = Array(5) { 0.0 }
        roundValues.conv_capacity_wfp_processes = Array(5) { 0.0 }
        roundValues.conv_capacity_wofp_processes = Array(5) { 0.0 }
    }

    /**
     * Calculate and update all properties relating to conveyor (no test)
     * @param currentState current GameState
     */
    fun calculate(currentState: GameState) {
        val roundValues = currentState.roundValues
        val conveyorDynamics = currentState.conveyorDynamics

        this.resetValues(roundValues)

        this.updateConveyorDynamicsAndRoundCost(
            roundValues,
            currentState.roundNumber,
            conveyorDynamics,
            currentState.messages
        )
        this.removeSoldConveyorDynamics(conveyorDynamics)


        // Iterate through all process and update Variables
        for (processID in 0 until GameValues.processes.size) {
            this.updateProcessConveyorProperties(roundValues, processID)
        }
    }

    /**
     * Prepare next round method
     * Informs the player of all events regarding conveyor
     * @param currentState - current GameState
     */
    fun prepareNextRound(currentState: GameState) {
        val conveyorDynamics = currentState.conveyorDynamics
        for (dynamic: ConveyorDynamic in conveyorDynamics) {
            if (this.getRoundForDelivery(dynamic) == currentState.roundNumber) {
                val textDE =
                    "Ein in Runde ${dynamic.roundBought} bestelltes Fördermittel ist eingetroffen. Kosten: ${dynamic.conveyor.price} €"
                val textEN =
                    "In round ${dynamic.roundBought} a conveyor was ordered and it just arrived. Costs: ${dynamic.conveyor.price} € "
                val message = MessageFactory.createMessage(textDE, textEN, currentState.roundNumber)
                currentState.messages.add(0, message)
            }
        }
    }
}