package com.flw.dolores.calculator

import com.flw.dolores.entities.GameState
import com.flw.dolores.entities.Process
import com.flw.dolores.entities.RoundValues

class CapacityCalculator {
    /**
     * Calculates the current available capacity based on the
     * capacity available by the employee and conveyor
     * @param emp_cap_wfp employee capacity (with forklift licence)
     * @param conv_cap_wfp conveyor capacity (with forklift licence required)
     * @param emp_cap_wofp employee capacity (without forklift licence)
     * @param conv_cap_wofp conveyor capacity (without forklift licence required)
     * @returns Total capacity
     */
    private fun calculateProcessCapacity(
        emp_cap_wfp: Double,
        conv_cap_wfp: Double,
        emp_cap_wofp: Double,
        conv_cap_wofp: Double
    ): Double {
        val capacityWfp: Double = kotlin.math.min(emp_cap_wfp, conv_cap_wfp)
        val unneededEmpCapWfp: Double = if (emp_cap_wfp > conv_cap_wfp) emp_cap_wfp - conv_cap_wfp else 0.0
        val capacityWofp: Double = kotlin.math.min(emp_cap_wofp + unneededEmpCapWfp, conv_cap_wofp)

        return capacityWfp + capacityWofp
    }

    /**
     * Calculates the current available capacity for the outgoing storage
     * @param capacity_storage current available storage capacity
     * @param storage_factor player determined storage workload balance
     * @return current capacity for outgoing storage
     *  */
    private fun calculateStorageOutCapacity(capacity_storage: Double, storage_factor: Double): Double {
        return kotlin.math.floor(capacity_storage * storage_factor)
    }

    /**
     * Updates the capacities for each process
     * @param roundValues current round values
     * @param process current process
     */
    private fun updateOverallProcessCapacity(roundValues: RoundValues, process: Process) {
        val processID = process.toInt()
        val employeeCapacity = roundValues.emp_capacity_processes[processID]
        val employeeCapacityWfp = roundValues.emp_capacity_wfp_processes[processID]
        val conveyorCapacityWfp = roundValues.conv_capacity_wfp_processes[processID]
        val employeeCapacityWofp = roundValues.emp_capacity_wofp_processes[processID]
        val conveyorCapacityWofp = roundValues.conv_capacity_wofp_processes[processID]

        if (process.isProcessWithConveyors()) {
            val capacity = calculateProcessCapacity(
                employeeCapacityWfp,
                conveyorCapacityWfp,
                employeeCapacityWofp,
                conveyorCapacityWofp
            )
            roundValues.capacity_processes[processID] = capacity
            roundValues.capacity_overall_processes[processID] = capacity
        } else {
            roundValues.capacity_processes[processID] = employeeCapacity
            roundValues.capacity_overall_processes[processID] = employeeCapacity
        }
    }

    /**
     * Updates the storage in and out capacity
     * @param roundValues current round values
     */
    private fun updateStorageCapacity(roundValues: RoundValues) {
        val storageCapacity = roundValues.capacity_processes[2]
        val storageFactor = roundValues.storage_factor
        val storageOutCapacity = calculateStorageOutCapacity(storageCapacity, 1 - storageFactor)

        roundValues.capacity_storage_out = storageOutCapacity
        roundValues.capacity_storage_in = storageCapacity - storageOutCapacity
    }

    /**
     * Calculates capacity related values for the next round
     * @param currentState currentState object
     */
    fun calculate(currentState: GameState) {
        val roundValues = currentState.roundValues

        for (processID in 0..4) {
            val process = Process.fromInt(processID)
            updateOverallProcessCapacity(roundValues, process)
        }
        updateStorageCapacity(roundValues)
    }
}