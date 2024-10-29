package com.flw.dolores.calculator

import com.flw.dolores.factory.MessageFactory
import com.flw.dolores.entities.GameState
import com.flw.dolores.entities.GameValues
import com.flw.dolores.entities.RoundValues

class CostIncomeCalculator {
    /**
     * Calculates the module cost from usage of different logistic technologies
     * @param roundValues current game round values
     * @returns overall cost from technologies
     */
    private fun calculateOverallModuleCost(roundValues: RoundValues): Double {
        var overallCost = 0.0

        if (roundValues.module_order_quantity)
            overallCost += GameValues.module_order_quantity_costs
        if (roundValues.module_reorder_level)
            overallCost += GameValues.module_reorder_level_costs
        if (roundValues.module_safety_stock)
            overallCost += GameValues.module_safety_stock_costs
        if (roundValues.module_look_in_storage)
            overallCost += GameValues.module_look_in_storage_costs
        if (roundValues.module_status_report)
            overallCost += GameValues.module_status_report_costs

        return overallCost
    }

    /**
     * Calculates the cost for an ABC analysis
     * @param roundValues current game round values
     * @returns cost for ABC analysis
     */
    private fun calculateABCCost(roundValues: RoundValues, roundNumber: Int): Double {
        var abcCost = 0.0
        if (roundValues.abc_analysis_round == roundNumber)
            abcCost += GameValues.costs_abc_analysis
        if (roundValues.abc_zoning_round == roundNumber)
            abcCost += GameValues.costs_abc_zoning
        return abcCost
    }

    /**
     * Calculates the cost for inventory
     * @param stock_value current inventory value
     * @returns inventory cost per round
     */
    private fun calculateInventoryCost(stock_value: Double): Double {
        return kotlin.math.floor(GameValues.storage_cost_factor * stock_value)
    }

    /**
     * Calculates the debit interest of the (possible) loan
     * @param balance current game balance
     * @returns the debit interest
     */
    private fun calculateDebitInterest(balance: Double): Double {
        return if (balance < 0) -1 * balance * GameValues.factor_debit_interest else 0.0
    }

    /**
     * Calculates the credit interest of the deposited balance
     * @param balance current game balance
     * @returns the credit interest
     */
    private fun calculateCreditInterest(balance: Double): Double {
        return if (balance > 0) balance * GameValues.factor_credit_interest else 0.0
    }

    /**
     * Calculates the overall costs due this round
     * @param roundValues current round values
     * @returns combined cost for this round
     */
    private fun calculateOverallCost(roundValues: RoundValues): Double {
        val overallModuleCost = this.calculateOverallModuleCost(roundValues)
        return (roundValues.current_order_costs + overallModuleCost + roundValues.employee_cost
                + roundValues.costs_new + roundValues.costs_repair + roundValues.costs_maintenance
                + roundValues.costs_overhaul + roundValues.work_climate_invest + roundValues.itCosts
                + roundValues.loading_equipment_level + roundValues.costs_usd + roundValues.costs_abc
                + roundValues.storage_cost + roundValues.debit_interest_cost + roundValues.costs_qualification_measure)
    }

    /**
     * Calculates the overall income in this round
     * @param roundValues current round values
     * @returns overall income this round
     */
    private fun calculateOverallIncome(roundValues: RoundValues): Double {
        return roundValues.sales_income + roundValues.credit_interest_income + roundValues.income_conveyor_sale
    }

    /**
     * Checks if the game state is critical
     * @param companyValue current value of the company
     * @param gameBalance current balance of the company
     * @returns if game state critical
     */
    private fun checkGameCritical(companyValue: Double, gameBalance: Double): Boolean {
        return companyValue < gameBalance * (-1)
    }

    /**
     * Calculates all cost, income and balance properties and updates the round values
     * @param roundValues current round values
     */
    private fun calculateCostAndUpdateRoundValues(roundValues: RoundValues, roundNumber: Int) {
        roundValues.costs_abc = this.calculateABCCost(roundValues, roundNumber)

        roundValues.storage_cost = this.calculateInventoryCost(roundValues.stock_value_processes[2])

        roundValues.sales_income = roundValues.sales_income_article.sum()

        val balance = roundValues.accountBalance

        roundValues.debit_interest_cost = this.calculateDebitInterest(balance)
        roundValues.credit_interest_income = this.calculateCreditInterest(balance)

        val overallCost = this.calculateOverallCost(roundValues)
        roundValues.costs_round = overallCost

        val overallIncome = this.calculateOverallIncome(roundValues)
        roundValues.income_round = overallIncome

        roundValues.accountBalance = kotlin.math.round(balance + overallIncome - overallCost)

    }

    /**
     * Main cost and income calculation
     * @param currentState current game State
     */
    fun calculate(currentState: GameState) {
        val roundValues = currentState.roundValues

        this.calculateCostAndUpdateRoundValues(roundValues, currentState.roundNumber)
    }

    /**
     * Prepare next round method
     * Checks the status of the game
     * Informs player if any critical state has been reached
     * @param currentState current game state
     */
    fun prepareNextRound(currentState: GameState) {
        val roundValues = currentState.roundValues
        val roundNumber = currentState.roundNumber

        val companyValue = roundValues.company_value
        val gameBalance = roundValues.accountBalance
        val currentCustomerSatisfaction = roundValues.customer_satisfaction

        // Game is critical
        if (checkGameCritical(companyValue, gameBalance)) {
            //Game was critical before
            if (roundValues.game_state == "CRITICAL") {
                //Game over
                if (roundNumber - roundValues.status_change_round > GameValues.max_critical_state_duration) {
                    roundValues.game_state = "END"
                    val textDE = "DAS SPIEL WURDE BEENDET, DA SIE KONKURS ANMELDEN MUSSTEN."
                    val textEN = "THE GAME WAS STOPPED BECAUSE YOU HAD TO DECLARE BANKRUPTCY."
                    val message = MessageFactory.createMessage(textDE, textEN, currentState.roundNumber)
                    currentState.messages.add(0, message)
                } else {
                    val textDE = "IHRE FINANZIELLE SITUATION IST NACH WIE VOR KRITISCH."
                    val textEN = "YOUR FINANCIAL SITUATION IS STILL CRITICAL."
                    val message = MessageFactory.createMessage(textDE, textEN, currentState.roundNumber)
                    currentState.messages.add(0, message)
                }
            } else {
                //GAME is now critical
                roundValues.status_change_round = roundNumber - 1
                roundValues.game_state = "CRITICAL"
                val textDE = "IHRE FINANZIELLE SITUATION IST KRITISCH GEWORDEN."
                val textEN = "YOUR FINANCIAL SITUATION HAS BECOME CRITICAL."
                val message = MessageFactory.createMessage(textDE, textEN, currentState.roundNumber)
                currentState.messages.add(0, message)
            }
        }

        if (currentCustomerSatisfaction < GameValues.min_customer_satisfaction) {
            if (roundValues.game_state === "CRITICAL") {
                if (roundNumber - roundValues.status_change_round > GameValues.max_critical_state_duration) {
                    roundValues.game_state = "END"
                    val textDE = "DAS SPIEL WURDE BEENDET, DA SIE KEINE KUNDEN MEHR HABEN."
                    val textEN = "THE GAME WAS STOPPED BECAUSE YOU DON'T HAVE CUSTOMERS ANYMORE."
                    val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
                    currentState.messages.add(0, message)
                } else {
                    val textDE = "SIE HABEN KAUM NOCH KUNDEN."
                    val textEN = "YOUR NUMBER OF CUSTOMERS IS ALARMING."
                    val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
                    currentState.messages.add(0, message)
                }
            } else {
                //GAME is now critical
                roundValues.status_change_round = roundNumber - 1
                roundValues.game_state = "CRITICAL"
                val textDE = "DIE KUNDENZUFRIEDENHEIT IST KRITISCH GEWORDEN."
                val textEN = "THE CUSTOMER SATISFACTION HAS BECOME CRITICAL."
                val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
                currentState.messages.add(0, message)
            }
        }

        if (roundValues.game_state === "CRITICAL" && !checkGameCritical(
                companyValue,
                gameBalance
            ) && currentCustomerSatisfaction >= GameValues.min_customer_satisfaction
        ) {
            roundValues.status_change_round = roundNumber - 1
            roundValues.game_state = "OK"
            val textDE = "IHRE SITUATION HAT SICH WIEDER VERBESSERT."
            val textEN = "THE SITUATION HAS IMPROVED."
            val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
            currentState.messages.add(0, message)
        }

    }
}