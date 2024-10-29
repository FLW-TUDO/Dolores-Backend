package com.flw.dolores.factory

import com.flw.dolores.entities.*
import com.google.gson.Gson
import org.bson.types.ObjectId
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.io.BufferedReader
import kotlin.random.Random


class EmployeeFactory {
    private val resource: Resource = ClassPathResource("entity_base/base_employee.json")
    private val employeeBaseFile: BufferedReader = BufferedReader(resource.inputStream.reader(Charsets.UTF_8))
    private val jsonString: String = employeeBaseFile.readText()
    private val employeeDynamicList: MutableList<EmployeeDynamic> = Gson().fromJson(jsonString)


    fun loadDynamics(): MutableList<EmployeeDynamic> {
        val employeeDynamics: MutableList<EmployeeDynamic> = mutableListOf()
        for (employeeDynamic: EmployeeDynamic in employeeDynamicList) {
            val newEmployee: Employee = Employee(
                id = ObjectId.get(),
                name = employeeDynamic.employee.name,
                gender = employeeDynamic.employee.gender,
                employmentRound = employeeDynamic.employee.employmentRound,
                age = employeeDynamic.employee.age,
                contractType = employeeDynamic.employee.contractType,
                endRound = employeeDynamic.employee.endRound,
            )


            val newEmployeeDynamic: EmployeeDynamic = EmployeeDynamic(
                id = ObjectId.get(),
                employee = newEmployee,
                qualification = employeeDynamic.qualification,
                process = employeeDynamic.process.copy(),
                motivation = employeeDynamic.motivation,
                salary = employeeDynamic.salary,
                qmRound = employeeDynamic.qmRound,
                fpRound = employeeDynamic.fpRound,
                secRound = employeeDynamic.secRound
            )
            employeeDynamics.add(newEmployeeDynamic)
        }
        return employeeDynamics
    }

    fun createNewDynamic(): EmployeeDynamic {
        val qualification = Random.nextInt(8)
        val salary = listOf(85.0, 125.0, 125.0, 145.0, 110.0, 150.0, 160.0, 170.0)
        return EmployeeDynamic(
            employee = createNewEmployee(),
            qualification = qualification,
            process = Process.UNLOADING,
            motivation = 100,
            salary = salary[qualification],
            qmRound = 0,
            fpRound = 0,
            secRound = 0
        )
    }

    private fun createNewEmployee(): Employee {
        val gender = Random.nextBoolean()
        val firstname = if (gender) employee_male_names.random() else employee_female_names.random()
        val lastName = employee_last_names.random()

        return Employee(
            name = "$firstname $lastName",
            age = Random.nextInt(20, 50),
            gender = gender,
            employmentRound = -1,
            contractType = 0,
            endRound = 1000
        )
    }
}