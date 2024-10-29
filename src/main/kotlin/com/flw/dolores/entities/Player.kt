package com.flw.dolores.entities

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "players")
class Player(
    @Id
    @JsonSerialize(using = ToStringSerializer::class)
    val id: ObjectId = ObjectId.get(),
    val userName: String,
    val password: String,
    val role: String = "user",
    var status: Boolean = false,
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)

data class NewPlayerRequestBody(
    val userName: String,
    val password: String
)

data class LoginRequestBody(
    val userName: String,
    val password: String
)

data class PlayerResponse(
    val id: String,
    val userName: String,
    val role: String,
    var status: Boolean,
    var updatedAt: LocalDateTime
)


data class PlayerNameResponse(
    val userName: String
)

data class PlayerCountResponse(
    val count: Int,
)


data class LoginResponse(
    val userName: String,
    val role: String,
    val status: Boolean,
    val _id: String,
    val accessToken: String,
    val refreshToken: String,
)

data class JWTValidResponse(
    val status: Boolean
)