package com.flw.dolores.entities

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

class Conveyor(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    var id: ObjectId = ObjectId.get(),
    val name: String,
    val needsForkliftPermit: Boolean,
    val conveyorId: Int,
    val capacity: Double,
    val maintenanceCost: Double,
    val price: Double,
    val speed: Double,
    val timeToDelivery: Int,
    val timeToRepair: Int,
    val useInStorage: Boolean
)

class ConveyorDynamic(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    var id: ObjectId = ObjectId.get(),
    val conveyor: Conveyor,
    var condition: Int,
    @JsonSerialize(using = ToStringSerializer::class)
    var process: Process,
    var overhaul: Boolean,
    var maintenanceEnabled: Boolean,
    var currentValue: Double,
    val overhaul_cost: Double,
    var status: Int,
    var roundBought: Int,
    var sold: Boolean
)


class ConveyorProcessMessage(
    val conveyorId: ObjectId,
    val process: Int
)

class ConveyorMaintenanceMessage(
    val conveyorId: ObjectId,
)

class ConveyorOverhaulMessage(
    val conveyorId: ObjectId,
)

class ConveyorSellMessage(
    val conveyorId: ObjectId,
)

class ConveyorBuyMessage(
    val conveyorId: ObjectId,
)