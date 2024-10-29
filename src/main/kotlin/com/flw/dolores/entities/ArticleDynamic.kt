package com.flw.dolores.entities

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

class DiscountLevel(
    val level: Int,
    val minQuantity: Int,
    val purchasePrice: Double
)

class DeliveryType(
    val duration: Int,
    val price: Double
)

class Article(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    var id: ObjectId = ObjectId.get(),
    val name: String,
    val abc_classification: String,
    val articleNumber: Int,
    val purchasePrice: Double,
    val salesPrice: Double,
    val minOrder: Int,
    val fixOrderCost: Double,
    val discount: MutableList<DiscountLevel> = mutableListOf(),
    val delivery: MutableList<DeliveryType> = mutableListOf()
)

class ArticleDynamic(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    var id: ObjectId = ObjectId.get(),
    val article: Article,
    var currentStock: Int = 0,
    var averageConsumption: Double = 30.0,
    val pastConsumption: MutableList<Int> = mutableListOf(30),
    val pallet_count_processes: Array<Int> = Array(5) { 0 },
    var estimatedRange: Int = 1,
    var optimalOrderQuantity: Int = 1
)