package com.flw.dolores.repositories

import com.flw.dolores.entities.Player
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface PlayerRepository : MongoRepository<Player, String> {
    fun findById(id: ObjectId): Player
    fun findByUserName(userName: String): Player?
    fun deleteById(id: ObjectId)
}