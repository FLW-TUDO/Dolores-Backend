package com.flw.dolores.factory

import com.flw.dolores.entities.CustomerJob
import com.google.gson.Gson
import org.bson.types.ObjectId
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.io.BufferedReader

class CustomerJobFactory {
    private val resource: Resource = ClassPathResource("entity_base/base_customerJob.json")
    private val customerJobBaseFile: BufferedReader = BufferedReader(resource.inputStream.reader(Charsets.UTF_8))

    private val jsonString: String = customerJobBaseFile.readText()
    private val customerJobList: MutableList<CustomerJob> = Gson().fromJson(jsonString)


    fun loadDynamics(): MutableList<CustomerJob> {
        val customerJobs: MutableList<CustomerJob> = mutableListOf()
        for (customerJob: CustomerJob in customerJobList) {
            val newCustomerJob: CustomerJob = CustomerJob(
                id = ObjectId.get(),
                articleNumber = customerJob.articleNumber,
                quantity = customerJob.quantity,
                remainingQuantity = customerJob.remainingQuantity,
                demandRound = customerJob.demandRound
            )
            customerJobs.add(newCustomerJob)
        }
        return customerJobs
    }

    companion object {
        fun createNewCustomerJobs(articleNumber: Int, quantity: Int, demandRound: Int): CustomerJob {
            return CustomerJob(
                articleNumber = articleNumber,
                quantity = quantity,
                remainingQuantity = quantity,
                demandRound = demandRound
            )
        }
    }
}