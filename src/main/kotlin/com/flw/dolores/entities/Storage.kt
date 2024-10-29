package com.flw.dolores.entities

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

class Pallet(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    var id: ObjectId = ObjectId.get(),
    val articleNumber: Int,
    @JsonSerialize(using = ToStringSerializer::class)
    var process: Process = Process.fromInt(0),
    val usedUnitSecurityDevices: Boolean = false,
    val error: Int = -1,
    var isStored: Boolean = false,
    var demandRound: Int = -1
)

class StockGround(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    val id: ObjectId = ObjectId.get(),
    val distSource: Double,
    val distDrain: Double,
    val distAvg: Double,
    val level: Int,
    val abc: String,
    val articleNumber: Int,
    var pallet: Pallet? = null
)


class Storage(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    val id: ObjectId = ObjectId.get(),
    val freeStocks: MutableList<StockGround> = mutableListOf(),
    val occStocks: MutableList<StockGround> = mutableListOf(),
    val pallets_not_in_storage: MutableList<Pallet> = mutableListOf()
) {
    fun copy(
        freeStocks: MutableList<StockGround> = this.freeStocks,
        occStocks: MutableList<StockGround> = this.occStocks,
        pallets_not_in_storage: MutableList<Pallet> = this.pallets_not_in_storage
    ): Storage {
        return Storage(
            freeStocks = freeStocks.toMutableList(),
            occStocks = occStocks.toMutableList(),
            pallets_not_in_storage = pallets_not_in_storage.toMutableList()
        )
    }
}