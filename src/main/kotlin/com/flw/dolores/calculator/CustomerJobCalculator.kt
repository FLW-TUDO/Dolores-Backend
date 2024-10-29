package com.flw.dolores.calculator

import com.flw.dolores.factory.CustomerJobFactory
import com.flw.dolores.entities.CustomerJob
import com.flw.dolores.entities.GameState
import com.flw.dolores.entities.GameValues
import com.flw.dolores.entities.RoundValues
import kotlin.random.Random

class CustomerJobCalculator {
    /**
     * Calculates the customer satisfaction factor based on the current customer satisfaction.
     * Used for calculate the maximum job pallet quantity.
     * @param current_customer_satisfaction current customer satisfaction
     * @returns satisfaction factor [-0.5, 1]
     */
    private fun getCustomerSatisfactionFactor(current_customer_satisfaction: Double): Double {
        for (index in GameValues.customerSatisfactionLevel.indices) {
            if (current_customer_satisfaction <= GameValues.customerSatisfactionLevel[index])
                return GameValues.customer_satisfaction_factor[index]
        }
        return GameValues.customer_satisfaction_factor[GameValues.customer_satisfaction_factor.size - 1]
    }

    /**
     * Increases/decreases the current maximal job pallet quantity based on the current customer satisfaction
     * @param roundValues current game values
     */
    private fun updatePalletMax(roundValues: RoundValues) {
        val satisfactionFactor = getCustomerSatisfactionFactor(roundValues.customer_satisfaction)
        val change = kotlin.math.floor(satisfactionFactor * GameValues.pallet_increase)
        roundValues.pMax += kotlin.math.min(change, 20.0).toInt()
    }

    /**
     * Calculates a random article number based on distribution of articles
     * @returns article number (100101 - 100104)
     */
    private fun getArticleDistributionForCustomerJob(): Int {
        val random = Random.nextDouble()
        for (index in GameValues.job_article_probability.indices) {
            if (random <= GameValues.job_article_probability[index])
                return 100101 + index
        }
        return 100104
    }

    /**
     * Calculates a random quantity of pallets based on distribution of job quantities
     * @returns job quantity (1 - 5 pallet(s))
     */
    private fun getQuantityDistributionForCustomerJob(): Int {
        val random = Random.nextDouble()
        for (index in GameValues.job_quantity_probability.indices) {
            if (random <= GameValues.job_quantity_probability[index])
                return index + 1
        }
        return GameValues.job_quantity_probability.size
    }

    /**
     * Creates new customer jobs with distributed quantity and based on distribution of articles
     * @param customerJobs  CustomerJobs
     * @param pMax current maximal pallet sum of all customer jobs
     * @param roundNumber current round number
     */
    private fun createNewJobs(customerJobs: MutableList<CustomerJob>, pMax: Int, roundNumber: Int) {
        var palletSum = 0
        while (palletSum < pMax) {
            val articleId = this.getArticleDistributionForCustomerJob()
            val quantity = this.getQuantityDistributionForCustomerJob()
            customerJobs.add(CustomerJobFactory.createNewCustomerJobs(articleId, quantity, roundNumber))
            palletSum += quantity
        }
    }

    /**
     * Main calculate method for customer jobs.
     * Updates the current maximum quantity of ordered pallets.
     * Creates new customer jobs based on this quantity.
     * @param currentState current game state
     */
    fun calculate(currentState: GameState) {
        val roundValues = currentState.roundValues
        val customerJobs = currentState.customerJobs

        this.updatePalletMax(roundValues)
        this.createNewJobs(customerJobs, roundValues.pMax, currentState.roundNumber)
    }


}