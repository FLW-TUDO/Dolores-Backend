package com.flw.dolores.calculator

import com.flw.dolores.entities.*
import kotlin.math.roundToInt

class PostThroughputCalculator {

    /**
     * Calculates the workload for conveyor and employee for all processes
     * @param roundValues current round values
     */
    private fun calculateWorkloadProcess(roundValues: RoundValues) {
        for (processID in GameValues.processes.indices) {
            val employeeCapacity = roundValues.emp_capacity_processes[processID]
            val conveyorCapacity = roundValues.conv_capacity_processes[processID]
            val overallCapacity = roundValues.capacity_overall_processes[processID]
            val currentCapacity = roundValues.capacity_processes[processID]

            roundValues.workload_employee[processID] =
                if (employeeCapacity > 0) kotlin.math.round((overallCapacity - currentCapacity) / employeeCapacity * 100) else 0.0
            roundValues.workload_conveyor[processID] =
                if (conveyorCapacity > 0) kotlin.math.round((overallCapacity - currentCapacity) / conveyorCapacity * 100) else 0.0

            if (processID == 2) {
                //TODO include in normal process workload calculation
                val overallStorageInCapacity = overallCapacity * roundValues.storage_factor
                val overallStorageOutCapacity = overallCapacity - overallStorageInCapacity
                val currentStorageInCapacity = roundValues.capacity_storage_in
                val currentStorageOutCapacity = roundValues.capacity_storage_out
                val employeeStorageInCapacity = employeeCapacity * roundValues.storage_factor
                val employeeStorageOutCapacity = employeeCapacity - employeeStorageInCapacity
                roundValues.workload_employee_storage_in =
                    if (employeeCapacity > 0) kotlin.math.round((overallStorageInCapacity - currentStorageInCapacity) / employeeStorageInCapacity * 100) else 0.0
                roundValues.workload_employee_storage_out =
                    if (employeeCapacity > 0) kotlin.math.round((overallStorageOutCapacity - currentStorageOutCapacity) / employeeStorageOutCapacity * 100) else 0.0

                val conveyorStorageInCapacity = conveyorCapacity * roundValues.storage_factor
                val conveyorStorageOutCapacity = conveyorCapacity - conveyorStorageInCapacity
                roundValues.workload_conveyor_storage_in =
                    if (employeeCapacity > 0) kotlin.math.round((overallStorageInCapacity - currentStorageInCapacity) / conveyorStorageInCapacity * 100) else 0.0
                roundValues.workload_conveyor_storage_out =
                    if (employeeCapacity > 0) kotlin.math.round((overallStorageOutCapacity - currentStorageOutCapacity) / conveyorStorageOutCapacity * 100) else 0.0
            }
        }
    }

    /**
     * Calculates the overall stock value of all articles
     * @param articleDynamics all articles
     * @returns overall stock value
     */
    private fun getOverallArticleValue(articleDynamics: MutableList<ArticleDynamic>): Double {
        var overallArticleValue = 0.0
        for (dynamic in articleDynamics) {
            val article = dynamic.article
            val palletCountPerProcess = dynamic.pallet_count_processes.sum()
            overallArticleValue += palletCountPerProcess * article.purchasePrice
        }
        return overallArticleValue
    }

    /**
     * Calculates the current value of all articles in a process
     * @param articleDynamics all article dynamics
     * @param processID current process
     * @returns value of all pallets in the process
     */
    private fun getProcessArticleValue(articleDynamics: MutableList<ArticleDynamic>, processID: Int): Double {
        var valueInProcess = 0.0
        for (dynamic in articleDynamics) {
            val article = dynamic.article
            valueInProcess += dynamic.pallet_count_processes[processID] * article.purchasePrice
        }
        return valueInProcess
    }

    /**
     * Calculates the current stock value of all articles.
     * @param roundValues current round values
     * @param articleDynamics all article dynamics
     */
    private fun calculateCurrentArticleValue(
        roundValues: RoundValues,
        articleDynamics: MutableList<ArticleDynamic>
    ) {
        val overallArticleValue = this.getOverallArticleValue(articleDynamics)
        roundValues.stock_value = overallArticleValue
        roundValues.company_value = overallArticleValue + roundValues.current_conv_value

        for (processID in GameValues.processes.indices) {
            roundValues.stock_value_processes[processID] = getProcessArticleValue(articleDynamics, processID)
        }
    }

    /**
     * Calculates the average consumption of all articles.
     * Updates the articles with this information.
     * @param articleDynamics all article dynamics
     */
    private fun updateAverageConsumptionAndRange(articleDynamics: MutableList<ArticleDynamic>) {
        for (dynamic in articleDynamics) {
            val pastPalletConsumption =
                dynamic.pastConsumption.filterIndexed { index, _ -> index >= dynamic.pastConsumption.size - GameValues.history_time } as MutableList<Int>
            val avgPalletConsumption =
                kotlin.math.round(pastPalletConsumption.sum().toDouble() / pastPalletConsumption.size)
            dynamic.averageConsumption = avgPalletConsumption
            dynamic.estimatedRange =
                if (avgPalletConsumption > 0) kotlin.math.floor(dynamic.pallet_count_processes[2] / avgPalletConsumption)
                    .toInt() else 0
        }
    }

    /**
     * Calculates the average consumption of an article based on the past consumption
     * @param pastConsumption article consumption over the last rounds
     * @returns average consumption
     */
    private fun calculateAvgPalletConsumption(pastConsumption: MutableList<Int>): Double {
        val history = pastConsumption.slice(
            kotlin.math.max(
                pastConsumption.size - GameValues.history_time,
                0
            ) until pastConsumption.size
        )
        return history.sum().toDouble() / GameValues.history_time
    }

    /**
     * Calculates the optimal order quantity of the article in question based
     * on the average consumption, and the desired discount level
     * @param article article in question
     * @param pastConsumption past consumption of the article
     * @param discountLevel desired discount level [-1 .. 2]
     * @returns optimal order quantity
     */
    private fun calculateOrderQuantityByDiscount(
        article: Article,
        pastConsumption: MutableList<Int>,
        discountLevel: Int = -1
    ): Int {
        val averageConsumption = calculateAvgPalletConsumption(pastConsumption)
        val orderFixCost = article.fixOrderCost
        val costPerPallet: Double = if (discountLevel == -1) {
            article.purchasePrice
        } else {
            article.discount[discountLevel].purchasePrice
        }

        var optimalQuantity: Int =
            kotlin.math.round(kotlin.math.sqrt((2 * averageConsumption * orderFixCost) / (costPerPallet * GameValues.stockCarryingFactor)))
                .toInt()

        if (discountLevel != -1 && optimalQuantity < article.discount[discountLevel].minQuantity) {
            optimalQuantity = article.discount[discountLevel].minQuantity
        }

        return optimalQuantity
    }

    /**
     * Calculates the order cost based on the article in question,
     * the consumption of this article, the order quantity, and the discount level
     * @param article the article in question
     * @param pastConsumption average consumption of the article
     * @param quantity desired order quantity
     * @param discountLevel selected discount level [-1 ... 2]
     * @returns order cost based on the provided quantity and discount level
     */
    private fun calculateOrderCost(
        article: Article,
        pastConsumption: MutableList<Int>,
        quantity: Int,
        discountLevel: Int = -1
    ): Double {
        val averageConsumption = calculateAvgPalletConsumption(pastConsumption)
        val orderFixCost = article.fixOrderCost

        val costPerPallet = if (discountLevel == -1)
            article.purchasePrice
        else
            article.discount[discountLevel].purchasePrice

        return kotlin.math.round(
            ((orderFixCost / quantity) + (costPerPallet * averageConsumption)
                    + kotlin.math.round(quantity * costPerPallet * GameValues.stockCarryingFactor)) / 2
        )
    }

    /**
     * Calculates the optimal order quantity for each article and updates the article properties
     * The optimal order quantity can be one of three cases:
     * 1. No discount quantity is reached
     * 2. The first required discount quantity is reached
     * 3. The quantity is bigger than the second required discount quantity
     * @param articleDynamics all article dynamics
     */
    private fun updateOptimalOrderQuantity(articleDynamics: MutableList<ArticleDynamic>) {
        for (dynamic in articleDynamics) {
            val quantityNormal =
                this.calculateOrderQuantityByDiscount(dynamic.article, dynamic.pastConsumption, -1)
            val quantityDiscountOne =
                this.calculateOrderQuantityByDiscount(dynamic.article, dynamic.pastConsumption, 0)
            val quantityDiscountTwo =
                this.calculateOrderQuantityByDiscount(dynamic.article, dynamic.pastConsumption, 1)


            val orderCostNormal = calculateOrderCost(dynamic.article, dynamic.pastConsumption, quantityNormal, -1)
            val orderCostDiscountOne =
                calculateOrderCost(dynamic.article, dynamic.pastConsumption, quantityDiscountOne, 0)
            val orderCostDiscountTwo =
                calculateOrderCost(dynamic.article, dynamic.pastConsumption, quantityDiscountTwo, 1)

            when (listOf(orderCostNormal, orderCostDiscountOne, orderCostDiscountTwo).minOrNull()) {
                orderCostNormal -> dynamic.optimalOrderQuantity = quantityNormal
                orderCostDiscountOne -> dynamic.optimalOrderQuantity = quantityDiscountOne
                else -> dynamic.optimalOrderQuantity = quantityDiscountTwo
            }
        }
    }

    /**
     * Updates all properties related to customer complaints.
     * Customer Complaints are based on pallet errors of shipped pallets.
     * @param roundValues current round values
     */
    private fun updateCustomerComplaintProperties(roundValues: RoundValues) {
        val palletCountWithError =
            roundValues.pallet_quantity_per_errors.slice(1 until roundValues.pallet_quantity_per_errors.size)
        val sumPalletsWithError = palletCountWithError.sum()
        val processID = GameValues.processes.size - 1

        if (roundValues.pallets_transported_process[processID] == 0) return
        roundValues.overall_complaint_percentage =
            sumPalletsWithError.toDouble() / roundValues.pallets_transported_process[processID]
        roundValues.overall_complaint_damaged =
            palletCountWithError[0].toDouble() / roundValues.pallets_transported_process[processID]
        roundValues.overall_complaint_w_delivered =
            palletCountWithError[1].toDouble() / roundValues.pallets_transported_process[processID]
        roundValues.overall_complaint_w_retrieval =
            palletCountWithError[2].toDouble() / roundValues.pallets_transported_process[processID]
        roundValues.overall_complaint_w_pallets =
            (palletCountWithError[1] + palletCountWithError[2]).toDouble() / roundValues.pallets_transported_process[processID]
        roundValues.overall_complaint_e_en =
            palletCountWithError[3].toDouble() / roundValues.pallets_transported_process[processID]
        roundValues.overall_complaint_e_la =
            palletCountWithError[4].toDouble() / roundValues.pallets_transported_process[processID]
        roundValues.overall_complaint_e_ve =
            palletCountWithError[5].toDouble() / roundValues.pallets_transported_process[processID]
        roundValues.overall_complaint_e_transport =
            (palletCountWithError[3] + palletCountWithError[4] + palletCountWithError[5]).toDouble() / roundValues.pallets_transported_process[processID]
    }

    /**
     * Calculates the current quantity of ordered pallets
     * TODO: Move to CostsIncomeCalc
     * @param roundValues current round values
     * @param orders current open orders
     */
    private fun updateOrderedPalletQuantity(roundValues: RoundValues, orders: MutableList<Order>) {
        if (orders.size > 0)
            roundValues.current_ordered_pallets =
                orders.sumOf { it.quantity }
        else
            roundValues.current_ordered_pallets = 0
    }

    /**
     * Calculates the service level and customer satisfaction based on the finished jobs and jobs with complaints
     * @param roundValues current round values
     */
    private fun updateServiceLevelAndCustomerSatisfaction(roundValues: RoundValues) {
        val serviceLevel =
            if (roundValues.current_customerJobs > 0) roundValues.accurate_finished_jobs.toDouble() / roundValues.current_customerJobs else 1.0
        val customerSatisfaction = (1.0 - roundValues.overall_complaint_percentage) * serviceLevel * 100.0

        roundValues.service_level = serviceLevel
        roundValues.customer_satisfaction = customerSatisfaction.roundToInt().toDouble()
    }

    /**
     * Main calculation method for post throughput calculator
     * @param currentState current game state
     */
    fun calculate(currentState: GameState) {
        val roundValues = currentState.roundValues
        val articleDynamics = currentState.articleDynamics
        val orders = currentState.orders

        this.calculateWorkloadProcess(roundValues)
        this.calculateCurrentArticleValue(roundValues, articleDynamics)
        this.updateAverageConsumptionAndRange(articleDynamics)
        this.updateOptimalOrderQuantity(articleDynamics)
        this.updateCustomerComplaintProperties(roundValues)
        this.updateOrderedPalletQuantity(roundValues, orders)
        this.updateServiceLevelAndCustomerSatisfaction(roundValues)
    }
}