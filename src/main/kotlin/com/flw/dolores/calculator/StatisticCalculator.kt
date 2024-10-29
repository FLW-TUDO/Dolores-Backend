package com.flw.dolores.calculator

import com.flw.dolores.entities.CustomerJob
import com.flw.dolores.entities.GameState
import com.flw.dolores.entities.RoundValues
import com.flw.dolores.entities.Storage

class StatisticCalculator {
    /**
     * Updates the quantity of due jobs which are not delivered yet
     * @param roundValues current round values
     * @param customerJobs all customer jobs
     */
    private fun updateLateJobCount(roundValues: RoundValues, roundNumber: Int, customerJobs: MutableList<CustomerJob>) {
        val lateJobs = customerJobs.filter { it.demandRound < roundNumber }
        roundValues.late_jobs = lateJobs.size
    }

    /**
     * Updates the quantity of free stocks in the storage
     * @param roundValues current round values
     * @param storage storage object of current state
     */
    private fun updateStockSpace(roundValues: RoundValues, storage: Storage) {
        roundValues.free_storage = storage.freeStocks.size
        roundValues.occ_storage = storage.occStocks.size
    }


    /**
     * Main calculate method for the statistics calculator.
     * @param currentState current game state
     */
    fun calculate(currentState: GameState) {
        val roundValues = currentState.roundValues
        val storage = currentState.storage
        val customerJobs = currentState.customerJobs

        this.updateLateJobCount(roundValues, currentState.roundNumber, customerJobs)
        this.updateStockSpace(roundValues, storage)

        // Further, processing which is maybe (probably) useless
    }
}