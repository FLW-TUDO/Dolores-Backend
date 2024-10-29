package com.flw.dolores.repositories

import com.flw.dolores.entities.GameInfo
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface GameInfoRepository : MongoRepository<GameInfo, String> {
    fun deleteByPlayerId(id: ObjectId)
    fun findAllByPlayerId(id: ObjectId): List<GameInfo>
    fun findById(id: ObjectId): GameInfo
    fun deleteById(id: ObjectId)
    fun countAllByPlayerId(id: ObjectId): Int
}