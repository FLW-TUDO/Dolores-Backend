package com.flw.dolores.repositories

import com.flw.dolores.entities.GameState
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface GameStateRepository : MongoRepository<GameState, String> {
    fun deleteByPlayerId(id: ObjectId)
    fun deleteById(id: ObjectId)
    fun findById(id: ObjectId): GameState
    fun findAllByGameInfoId(id: ObjectId): List<GameState>

    fun findByGameInfoIdAndRoundNumber(id: ObjectId, roundNumber: Int): GameState
}