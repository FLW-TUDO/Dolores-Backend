package com.flw.dolores.factory

import com.flw.dolores.entities.GameState
import com.flw.dolores.entities.GameValues
import com.flw.dolores.entities.Order
import com.flw.dolores.entities.OrderMessage
import com.google.gson.Gson
import org.bson.types.ObjectId
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import java.io.BufferedReader
import kotlin.random.Random


class OrderFactory {
    private val resource: Resource = ClassPathResource("entity_base/base_order.json")
    private val orderBaseFile: BufferedReader = BufferedReader(resource.inputStream.reader(Charsets.UTF_8))
    private val jsonString: String = orderBaseFile.readText()
    private val orderList: MutableList<Order> = Gson().fromJson(jsonString)


    fun loadDynamics(): MutableList<Order> {
        val orders: MutableList<Order> = mutableListOf()
        for (order: Order in orderList) {
            val newOrder = Order(
                id = ObjectId.get(),
                orderNumber = order.orderNumber,
                orderRound = order.orderRound,
                deliveryRound = order.deliveryRound,
                deliveryWishRound = order.deliveryWishRound,
                articleNumber = order.articleNumber,
                realPurchasePrice = order.realPurchasePrice,
                quantity = order.quantity,
                deliveredQuantity = order.deliveredQuantity,
                fixCosts = order.fixCosts,
                deliveryCosts = order.deliveryCosts
            )
            orders.add(newOrder)
        }
        return orders
    }

    fun createOrder(gameState: GameState, order: OrderMessage) {
        val deliveredQuantity = order.deliveredQuantity - Random.nextInt(5)
        val newOrder = Order(
            orderNumber = order.orderNumber,
            orderRound = order.orderRound,
            deliveryRound = order.deliveryRound,
            deliveryWishRound = order.deliveryWishRound,
            articleNumber = order.articleNumber,
            realPurchasePrice = order.realPurchasePrice,
            quantity = order.quantity,
            deliveredQuantity = deliveredQuantity,
            fixCosts = order.fixCosts,
            deliveryCosts = order.deliveryCosts,
        )
        gameState.orders.add(newOrder)
    }

    fun cancelOrder(gameState: GameState, orderId: ObjectId) {
        try {
            val order = gameState.orders.first { order -> order.id == orderId }
            val timeToDelivery = order.deliveryRound - gameState.roundNumber
            val cost =
                GameValues.order_cancel_cost[timeToDelivery] * order.quantity * order.realPurchasePrice + order.fixCosts
            gameState.roundValues.accountBalance -= cost
            gameState.orders.remove(order)
        } catch (exception: Exception) {
            println("Current order $orderId could not be found! $exception")
        }
    }

}