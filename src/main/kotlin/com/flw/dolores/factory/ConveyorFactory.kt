package com.flw.dolores.factory

import com.flw.dolores.entities.Conveyor
import com.flw.dolores.entities.ConveyorDynamic
import com.google.gson.Gson
import org.bson.types.ObjectId
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.io.BufferedReader


class ConveyorFactory {
    private val resourceBase: Resource = ClassPathResource("entity_base/base_conveyor.json")
    private val conveyorBaseFile: BufferedReader = BufferedReader(resourceBase.inputStream.reader(Charsets.UTF_8))
    private val resourceStore: Resource = ClassPathResource("entity_base/store_conveyor.json")
    private val conveyorStoreFile: BufferedReader = BufferedReader(resourceStore.inputStream.reader(Charsets.UTF_8))

    private val jsonBaseString: String = conveyorBaseFile.readText()
    private val jsonStoreString: String = conveyorStoreFile.readText()
    private var conveyorDynamicList: MutableList<ConveyorDynamic> = Gson().fromJson(jsonBaseString)
    private var conveyorDynamicStoreList: MutableList<ConveyorDynamic> = Gson().fromJson(jsonStoreString)


    fun loadDynamics(): MutableList<ConveyorDynamic> {
        val conveyorDynamics: MutableList<ConveyorDynamic> = mutableListOf()
        for (conveyorDynamic: ConveyorDynamic in conveyorDynamicList) {
            val conveyor: Conveyor = Conveyor(
                id = ObjectId.get(),
                name = conveyorDynamic.conveyor.name,
                needsForkliftPermit = conveyorDynamic.conveyor.needsForkliftPermit,
                conveyorId = conveyorDynamic.conveyor.conveyorId,
                capacity = conveyorDynamic.conveyor.capacity,
                maintenanceCost = conveyorDynamic.conveyor.maintenanceCost,
                price = conveyorDynamic.conveyor.price,
                speed = conveyorDynamic.conveyor.speed,
                timeToDelivery = conveyorDynamic.conveyor.timeToDelivery,
                timeToRepair = conveyorDynamic.conveyor.timeToRepair,
                useInStorage = conveyorDynamic.conveyor.useInStorage,
            )

            val newConveyorDynamic: ConveyorDynamic = ConveyorDynamic(
                id = ObjectId.get(),
                conveyor = conveyor,
                condition = conveyorDynamic.condition,
                process = conveyorDynamic.process.copy(),
                overhaul = conveyorDynamic.overhaul,
                maintenanceEnabled = conveyorDynamic.maintenanceEnabled,
                currentValue = conveyorDynamic.currentValue,
                overhaul_cost = conveyorDynamic.overhaul_cost,
                status = conveyorDynamic.status,
                roundBought = conveyorDynamic.roundBought,
                sold = conveyorDynamic.sold,
            )
            conveyorDynamics.add(newConveyorDynamic)
        }
        return conveyorDynamics
    }

    fun loadStore(): MutableList<ConveyorDynamic> {
        val conveyorDynamics: MutableList<ConveyorDynamic> = mutableListOf()
        for (conveyorDynamic: ConveyorDynamic in conveyorDynamicStoreList) {
            val conveyor: Conveyor = Conveyor(
                id = ObjectId.get(),
                name = conveyorDynamic.conveyor.name,
                needsForkliftPermit = conveyorDynamic.conveyor.needsForkliftPermit,
                conveyorId = conveyorDynamic.conveyor.conveyorId,
                capacity = conveyorDynamic.conveyor.capacity,
                maintenanceCost = conveyorDynamic.conveyor.maintenanceCost,
                price = conveyorDynamic.conveyor.price,
                speed = conveyorDynamic.conveyor.speed,
                timeToDelivery = conveyorDynamic.conveyor.timeToDelivery,
                timeToRepair = conveyorDynamic.conveyor.timeToRepair,
                useInStorage = conveyorDynamic.conveyor.useInStorage,
            )

            val newConveyorDynamic: ConveyorDynamic = ConveyorDynamic(
                id = ObjectId.get(),
                conveyor = conveyor,
                condition = conveyorDynamic.condition,
                process = conveyorDynamic.process,
                overhaul = conveyorDynamic.overhaul,
                maintenanceEnabled = conveyorDynamic.maintenanceEnabled,
                currentValue = conveyorDynamic.currentValue,
                overhaul_cost = conveyorDynamic.overhaul_cost,
                status = conveyorDynamic.status,
                roundBought = conveyorDynamic.roundBought,
                sold = conveyorDynamic.sold,
            )
            conveyorDynamics.add(newConveyorDynamic)
        }
        return conveyorDynamics
    }
}
