package com.flw.dolores.entities

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id

class Order(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    var id: ObjectId = ObjectId.get(),
    val orderNumber: Int,
    val orderRound: Int,
    val deliveryRound: Int,
    val deliveryWishRound: Int,
    val articleNumber: Int,
    val realPurchasePrice: Double,
    val quantity: Int,
    val deliveredQuantity: Int,
    val fixCosts: Double,
    val deliveryCosts: Double
)

data class OrderMessage(
    val orderNumber: Int,
    val orderRound: Int,
    val deliveryRound: Int,
    val deliveryWishRound: Int,
    val articleNumber: Int,
    val realPurchasePrice: Double,
    val quantity: Int,
    val deliveredQuantity: Int,
    val fixCosts: Double,
    val deliveryCosts: Double
)