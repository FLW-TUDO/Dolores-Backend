package com.flw.dolores.calculator

import com.flw.dolores.entities.*
import com.flw.dolores.factory.MessageFactory

class EmployeeCalculator {

    /**
     * Checks if the employee was newly employed
     * @param employee current employee
     * @param roundNumber current round number
     * @returns employment cost for this round
     */
    private fun checkForNewEmployment(employee: Employee, roundNumber: Int): Int {
        if (employee.employmentRound != roundNumber) return 0
        if (employee.contractType == 2) return GameValues.new_employee_cost_temporary
        return GameValues.new_employee_cost_indefinite
    }

    /**
     * Checks if the employee is ready and can work this round
     * @param employee current employee
     * @param roundNumber current round number
     * @returns if the employee is ready
     */
    private fun isReady(employee: Employee, roundNumber: Int): Boolean {
        return employee.employmentRound < roundNumber && roundNumber <= employee.endRound + 1
    }

    /**
     * Checks if an employee has a valid contract
     * @param employee current employee
     * @returns contract validity of employee
     */
    private fun hasValidContract(employee: Employee): Boolean {
        return GameValues.valid_contract_types.indexOf(employee.contractType) != -1
    }

    /**
     * Calculates the error chance of an employee
     * @param dynamic EmployeeDynamic
     * @param works_with_conveyor is current employee working with conveyor
     * @returns error chance
     */
    private fun calculateErrorChance(dynamic: EmployeeDynamic, works_with_conveyor: Boolean = true): Double {
        return if (works_with_conveyor && !this.hasForkliftPermit(dynamic)) GameValues.error_chance_wofp_without_training
        else if (works_with_conveyor && !this.hasSecurityTraining(dynamic)) GameValues.error_chance_wfp_without_training
        else if (works_with_conveyor) GameValues.error_chance_wfp_with_training
        else if (this.hasQMSeminar(dynamic)) GameValues.error_chance_with_qm
        else GameValues.error_chance_without_qm
    }

    /**
     * Checks if an employee has a forklift permit
     * @param dynamic EmployeeDynamic
     * @returns employee has forklift permit
     */
    private fun hasForkliftPermit(dynamic: EmployeeDynamic): Boolean {
        return (dynamic.qualification % 2) == 1
    }

    /**
     * Checks if an employee has security training
     * @param dynamic EmployeeDynamic
     * @returns employee has security training
     */
    private fun hasSecurityTraining(dynamic: EmployeeDynamic): Boolean {
        return GameValues.security_qualifications.indexOf(dynamic.qualification) != -1
    }

    /**
     * Checks if an employee has had a quality management seminar
     * @param dynamic EmployeeDynamic
     * @returns employee has had quality measurement seminar
     */
    private fun hasQMSeminar(dynamic: EmployeeDynamic): Boolean {
        return dynamic.qualification > 3
    }

    /**
     * Calculates the work time of an employee based on his contract type and the current overtime hours
     * @param dynamic EmployeeDynamic
     * @param overtime_hours quantity of overtime hours
     * @returns total work time in seconds
     */
    private fun calculateWorkTime(dynamic: EmployeeDynamic, overtime_hours: Int = 0): Double {
        return if (dynamic.employee.contractType == 2)
            GameValues.working_time + overtime_hours * 3600 * GameValues.half_time_factor
        else
            (GameValues.working_time + overtime_hours * 3600).toDouble()
    }

    /**
     * Calculates the cost for the qualification training of an employee
     * @param dynamic EmployeeDynamic
     * @param roundNumber current round number
     * @returns cost for qualification training
     */
    private fun calculateQualificationTrainingCost(dynamic: EmployeeDynamic, roundNumber: Int): Int {
        if (dynamic.fpRound == roundNumber) return GameValues.forklift_training_cost
        if (dynamic.qmRound == roundNumber) return GameValues.qm_training_cost
        if (dynamic.secRound == roundNumber) return GameValues.securityTrainingCost
        return 0
    }

    /**
     * Calculates the current motivation factor based on the over time hours
     * @param overtime_hours current over time hours
     * @returns motivation factor based on over time hours
     */
    private fun calculateMotivationImpactOfOvertime(overtime_hours: Int): Double {
        val borders = GameValues.overtimeMotivationBorders
        for (index in borders.indices) {
            val border = borders[index]
            if (overtime_hours <= border) return GameValues.overtimeMotivationFactor[index]
        }
        return GameValues.overtimeMotivationFactor[borders.size - 1]
    }

    /**
     * Calculates the motivation based on the factor of temporary employee and total employee
     * @param temporary_factor factor between temporary employee and total employee in one process
     * @returns motivation factor based on the temporary factor
     */
    private fun calculateMotivationOfTemporaryEmployee(temporary_factor: Double): Double {
        val borders = GameValues.temporaryMotivationBorders
        for (index in borders.indices) {
            val border = borders[index]
            if (temporary_factor <= border)
                return GameValues.temporaryMotivationFactor[index]
        }
        return GameValues.temporaryMotivationFactor[borders.size]
    }

    /**
     * Calculates the motivation factor based on the salary an employee receives
     * @param dynamic EmployeeDynamic
     * @returns motivation factor based on salary
     */
    fun calculateMotivationSalary(dynamic: EmployeeDynamic): Double {
        val baseSalary = GameValues.salary[dynamic.qualification]
        val borders = GameValues.salary_bonus_border
        for (index in borders.indices) {
            val border = borders[index]
            if (dynamic.salary - baseSalary <= border)
                return GameValues.salary_bonus_factor[index]
        }
        return GameValues.salary_bonus_factor[borders.size + 1]
    }

    /**
     * Calculates the cost for laid off employee
     * @param dynamic EmployeeDynamic
     * @param roundNumber current round number
     * @returns compensation cost for laid off employee
     */
    private fun calculateEmployeeCompensation(dynamic: EmployeeDynamic, roundNumber: Int): Double {
        val employee = dynamic.employee
        if (employee.endRound != roundNumber) return 0.0
        val employmentDuration = roundNumber - employee.employmentRound
        return kotlin.math.floor((dynamic.salary * employmentDuration)) * GameValues.compensation_factor
    }

    /**
     * Calculates the average error chance for all processes
     * @returns average error chance for all processes
     */
    private fun calculateAvgErrorChance(roundValues: RoundValues, employeeDynamics: List<EmployeeDynamic>) {
        val avgErrorChance = Array(5) { 0.0 }
        val employeeCount = Array(5) { 0 }
        for (dynamic in employeeDynamics) {
            val process = dynamic.process
            val isProcessWithConveyor = process.isProcessWithConveyors()
            val errorChance = calculateErrorChance(dynamic, isProcessWithConveyor)
            avgErrorChance[process.toInt()] += errorChance
            employeeCount[process.toInt()] += 1
        }

        roundValues.avg_error_chance_processes = avgErrorChance
            .mapIndexed { idx, value -> if (employeeCount[idx] > 0) value / employeeCount[idx] else value }
            .toTypedArray()
    }

    /**
     * Updates the employee dynamic if the employee has had a recent qualification measure
     * @param dynamic EmployeeDynamic
     * @param roundNumber current round number
     */
    private fun checkEmployeeQualificationMeasure(dynamic: EmployeeDynamic, roundNumber: Int) {
        if (dynamic.fpRound == roundNumber) {
            dynamic.qualification += 1
            dynamic.salary = GameValues.salary[dynamic.qualification]
        }
        if (dynamic.secRound == roundNumber) {
            dynamic.qualification += 2
            dynamic.salary = GameValues.salary[dynamic.qualification]
        }
        if (dynamic.qmRound == roundNumber) {
            dynamic.qualification += 4
            dynamic.salary = GameValues.salary[dynamic.qualification]
        }
    }

    /**
     * Calculates the count of employee for each contract type
     * @param employeeDynamics EmployeeDynamics
     * @returns count of employee for each contract type
     */
    private fun countEmployeeContractTypes(employeeDynamics: List<EmployeeDynamic>): Array<Int> {
        val employeeContractCount = Array(3) { 0 }
        for (dynamic in employeeDynamics) {
            employeeContractCount[dynamic.employee.contractType] += 1
        }
        return employeeContractCount
    }

    /**
     * Calculates the percentage of temporary employees compared to all employees
     * @param employeeDynamics EmployeeDynamics
     * @returns percentage of temporary employees
     */
    private fun calculateTemporaryEmployeeRatio(employeeDynamics: List<EmployeeDynamic>): Double {
        val employeeContractCount = this.countEmployeeContractTypes(employeeDynamics)
        val overallCount = employeeContractCount.sum()
        return if (overallCount != 0) employeeContractCount[2].toDouble() / overallCount.toDouble() else 0.0
    }

    /**
     * Calculates all employee related cost and updates the round values
     * @param roundValues current round values
     * @param employeeDynamics EmployeeDynamics
     */
    private fun updateEmployeeCost(
        roundValues: RoundValues,
        roundNumber: Int,
        employeeDynamics: MutableList<EmployeeDynamic>
    ) {
        var costsNew = 0.0
        var costsSalary = 0.0
        var costsOvertime = 0.0
        var costsCompensation = 0.0
        for (dynamic in employeeDynamics) {
            val processID = dynamic.process.toInt()
            val overtime = roundValues.overtime_process[processID]
            costsNew += this.checkForNewEmployment(dynamic.employee, roundNumber)
            costsCompensation += this.calculateEmployeeCompensation(dynamic, roundNumber)
            costsSalary += dynamic.salary
            costsOvertime += (dynamic.salary / GameValues.working_time) * overtime * 3600
        }
        roundValues.costs_new_employees = costsNew + costsCompensation
        roundValues.employee_cost = costsOvertime + costsSalary + costsCompensation + costsNew
        roundValues.workTimeCost = costsOvertime + costsSalary
    }

    /**
     * Calculates the motivation for each employee and updates the round values
     * @param roundValues current round values
     * @param employeeDynamics EmployeeDynamics
     */
    private fun updateEmployeeMotivation(
        roundValues: RoundValues,
        roundNumber: Int,
        employeeDynamics: List<EmployeeDynamic>
    ) {
        val temporaryFactor = calculateTemporaryEmployeeRatio(employeeDynamics)
        for (dynamic in employeeDynamics) {
            val processID = dynamic.process.toInt()

            roundValues.costs_qualification_measure += calculateQualificationTrainingCost(dynamic, roundNumber)

            //Motivation by work climate invest
            val climateLevel: Int = GameValues.work_climate_invest_level.indexOf(roundValues.work_climate_invest)
            val motivationFromWorkClimateInvest =
                GameValues.work_climate_factor[climateLevel] * GameValues.motivation_base

            //Motivation by overtime
            val overtime = roundValues.overtime_process[processID]
            val motivationOvertime = calculateMotivationImpactOfOvertime(overtime) * GameValues.motivation_base

            //Motivation temporary worker
            val motivationTemporary = if (dynamic.employee.contractType == 2)
                this.calculateMotivationOfTemporaryEmployee(temporaryFactor) * GameValues.motivation_base
            else
                GameValues.motivation_base

            //Motivation by salary
            // const motivation_salary = calculateMotivationSalary(dynamic) * gameValues["motivation_base"]
            // TODO revert motivation based on salary and raise issue #72
            val motivationFromSalary = GameValues.motivation_base * 0.9

            val noise = Math.random() * 0.1

            //calculates new motivation
            val newMotivation =
                motivationFromWorkClimateInvest + motivationTemporary + motivationFromSalary + motivationOvertime - noise
            dynamic.motivation = if (newMotivation < 1) kotlin.math.round(newMotivation * 100).toInt() else 100

            checkEmployeeQualificationMeasure(dynamic, roundNumber)
        }
    }

    /**
     * Calculates the overall average motivation of all employee
     * @param roundValues current round values
     * @param employeeDynamics all current available employee
     */
    private fun calculateAvgMotivation(roundValues: RoundValues, employeeDynamics: List<EmployeeDynamic>) {
        var overallMotivation = 0.0
        for (dynamic in employeeDynamics) {
            overallMotivation += dynamic.motivation
        }
        roundValues.avg_motivation = overallMotivation / employeeDynamics.size
    }

    /**
     * Calculates the average employee motivation for a specific process
     * @param employeeDynamics EmployeeDynamics
     * @returns average employee motivation
     */
    private fun calculateAvgMotivation(employeeDynamics: List<EmployeeDynamic>): List<Int> {
        val avgMotivation = Array(5) { 0 }
        val employeeCount = Array(5) { 0 }
        for (dynamic in employeeDynamics) {
            val processID = dynamic.process.toInt()
            avgMotivation[processID] += dynamic.motivation
            employeeCount[processID] += 1
        }
//        return if (employee_count > 0) avgMotivation / employee_count else avgMotivation
        return avgMotivation.zip(employeeCount) { motivation, count -> if (count > 0) motivation / count else motivation }
    }

    /**
     * Calculates the overall work time per process and updates the round values
     * @param roundValues current round values
     * @param employeeDynamics EmployeeDynamics
     */
    private fun calculateEmployeeWorkingTime(roundValues: RoundValues, employeeDynamics: List<EmployeeDynamic>) {
        val avgMotivation = this.calculateAvgMotivation(employeeDynamics)
        for (dynamic in employeeDynamics) {
            val process = dynamic.process
            val processID = process.toInt()
            val workTime = this.calculateWorkTime(dynamic, roundValues.overtime_process[processID])
            val capacity = workTime * (avgMotivation[processID] / 100.0)
            if (process.isProcessWithConveyors()) {
                if (hasForkliftPermit(dynamic))
                    roundValues.emp_capacity_wfp_processes[processID] += capacity
                else
                    roundValues.emp_capacity_wofp_processes[processID] += capacity
            }
            roundValues.emp_capacity_processes[processID] += capacity
        }
    }

    /**
     * Filters all employee to extract currently available employee
     * @param employeeDynamics all current employee dynamics
     * @param roundNumber current round number
     * @returns all currently available employee
     */
    private fun getValidEmployee(
        employeeDynamics: MutableList<EmployeeDynamic>,
        roundNumber: Int
    ): List<EmployeeDynamic> {
        return employeeDynamics.filter {
            this.isReady(
                it.employee,
                roundNumber
            ) && this.hasValidContract(it.employee)
        }
    }

    /**
     * Filters all employee to extract currently available and new employee
     * @param  employeeDynamics all current employee dynamics
     * @param  roundNumber current round number
     * @returns  all currently available and new employee
     */
    private fun getValidAndNewEmployee(
        employeeDynamics: MutableList<EmployeeDynamic>,
        roundNumber: Int
    ): List<EmployeeDynamic> {
        return employeeDynamics.filter {
            it.employee.employmentRound <= roundNumber && roundNumber <= it.employee.endRound && this.hasValidContract(
                it.employee
            )
        }
    }

    /**
     * Updates the number of employee per process
     * @param roundValues current round values
     * @param employeeDynamics current available employee
     */
    private fun updateEmployeeCount(roundValues: RoundValues, employeeDynamics: List<EmployeeDynamic>) {
        for (dynamic in employeeDynamics) {
            val processID = dynamic.process.toInt()
            roundValues.emp_count_processes[processID] += 1
        }
    }

    /**
     * Removes all terminated employee from the list of current employee
     */
    private fun removeTerminatedEmployee(
        roundNumber: Int,
        employeeDynamics: MutableList<EmployeeDynamic>
    ) {
        employeeDynamics.removeAll { roundNumber > it.employee.endRound }
    }

    /**
     * Reset function to zero all roundValues that are summed throughout the calculations to avoid previous non-zero inits
     * @param roundValues current round values
     */
    private fun resetValues(roundValues: RoundValues) {
        roundValues.emp_count_processes = Array(5) { 0 }
        roundValues.emp_capacity_processes = Array(5) { 0.0 }
        roundValues.emp_capacity_wfp_processes = Array(5) { 0.0 }
        roundValues.emp_capacity_wofp_processes = Array(5) { 0.0 }
        roundValues.costs_qualification_measure = 0.0
    }

    /**
     * Main calculation method for all employee related aspects
     * @param currentState current game state
     */
    fun calculate(currentState: GameState) {
        val roundValues = currentState.roundValues
        val employeeDynamics = currentState.employeeDynamics

        this.resetValues(roundValues)


        val validEmployeeDynamics = this.getValidEmployee(employeeDynamics, currentState.roundNumber)
        this.updateEmployeeMotivation(roundValues, currentState.roundNumber, validEmployeeDynamics)
        this.calculateEmployeeWorkingTime(roundValues, validEmployeeDynamics)
        this.calculateAvgErrorChance(roundValues, validEmployeeDynamics)
        val validAndNewEmployee = this.getValidAndNewEmployee(employeeDynamics, currentState.roundNumber)
        this.updateEmployeeCount(roundValues, validAndNewEmployee)
        this.calculateAvgMotivation(roundValues, validEmployeeDynamics)
        this.updateEmployeeCost(roundValues, currentState.roundNumber, employeeDynamics) // includes invalid employee
        this.removeTerminatedEmployee(currentState.roundNumber, employeeDynamics)
    }

    /**
     * Prepare next round for all employee related variables.
     * Create messages to inform player about employee related events
     * @param currentState current game state
     */
    fun prepareNextRound(currentState: GameState) {
        val roundValues = currentState.roundValues
        val roundNumber = currentState.roundNumber

        if (roundValues.avg_motivation <= GameValues.motivation_warning) {
            val textDE = "Die Motivation ihrer Mitarbeiter ist derzeit bedenklich."
            val textEN = "Employees motivation is currently alarming."
            val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
            currentState.messages.add(0, message)
        }


        for (dynamic in currentState.employeeDynamics) {
            val employee = dynamic.employee

            if (!this.hasValidContract(employee)) {
                val textDE =
                    "${if (employee.gender) "Die Mitarbeiterin" else "Der Mitarbeiter"} ${employee.name} hat einen ungültigen Vertrag."
                val textEN = "The employee ${employee.name} has an invalid contract."
                val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
                currentState.messages.add(0, message)
            }

            if (isReady(dynamic.employee, roundNumber)) {
                if (dynamic.fpRound == roundNumber) {
                    val textDE =
                        "${if (employee.gender) "Die Mitarbeiterin" else "Der Mitarbeiter"} ${employee.name} hat die Staplerschein-Prüfung erfolgreich bestanden."
                    val textEN = "The employee ${employee.name} passed the forklift drivers license successfully."
                    val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
                    currentState.messages.add(0, message)
                }
                if (dynamic.qmRound == roundNumber) {
                    val textDE =
                        " ${if (dynamic.employee.gender) "Die Mitarbeiterin" else "Der Mitarbeiter"} ${employee.name} hat das QM-Seminar erfolgreich absolviert."
                    val textEN = "The employee ${employee.name} passed the qm-seminar successfully."
                    val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
                    currentState.messages.add(0, message)
                }
                if (dynamic.secRound == roundNumber) {
                    val textDE =
                        "${if (dynamic.employee.gender) "Die Mitarbeiterin" else "Der Mitarbeiter"} ${employee.name} hat das Sicherheitstraining erfolgreich absolviert."
                    val textEN = "The employee  ${employee.name} passed the safety training successfully."
                    val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
                    currentState.messages.add(0, message)
                }
                if (dynamic.employee.endRound == roundNumber) {
                    val textDE =
                        "${if (dynamic.employee.gender) "Die Mitarbeiterin" else "Der Mitarbeiter"} ${employee.name} verlässt Ihr Unternehmen zum Ende dieser Runde."
                    val textEN = "The employee ${employee.name} is leaving your company at the end of this round."
                    val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
                    currentState.messages.add(0, message)
                }
            } else {
                if (dynamic.employee.employmentRound == roundNumber) {
                    val textDE =
                        "${if (dynamic.employee.gender) "Die Mitarbeiterin" else "Der Mitarbeiter "} ${employee.name} beginnt in dieser Runde."
                    val textEN = "The employee  ${employee.name} beginns in this round."
                    val message = MessageFactory.createMessage(textDE, textEN, roundNumber)
                    currentState.messages.add(0, message)
                }
            }
        }
    }
}