package com.flw.dolores.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.flw.dolores.entities.LoginRequestBody
import com.flw.dolores.entities.LoginResponse
import com.flw.dolores.repositories.PlayerRepository
import com.google.gson.Gson
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.util.*
import java.util.stream.Collectors
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthenticationFilter(
    authenticationManager: AuthenticationManager,
    private val repository: PlayerRepository,
    private val secretKey: String
) :
    UsernamePasswordAuthenticationFilter() {

    init {
        this.authenticationManager = authenticationManager
    }

    override fun attemptAuthentication(request: HttpServletRequest, response: HttpServletResponse): Authentication {
        val mapper = jacksonObjectMapper()
        val body: LoginRequestBody = mapper.readValue(request.inputStream, LoginRequestBody::class.java)
        val userName = body.userName
        val password = body.password
        val authenticationToken = UsernamePasswordAuthenticationToken(userName, password)
        return this.authenticationManager.authenticate(authenticationToken)
    }

    override fun successfulAuthentication(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
        authentication: Authentication
    ) {
        val user: User = authentication.principal as User
        val algorithm: Algorithm =
            Algorithm.HMAC512(secretKey)
        val accessToken: String = JWT.create()
            .withSubject(user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 72 * 60 * 60 * 1000)) // 72 hours
            .withIssuer(request.requestURL.toString())
            .withClaim(
                "roles",
                user.authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining())
            )
            .sign(algorithm)
        val refreshToken: String = JWT.create()
            .withSubject(user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 14 * 24 * 60 * 60 * 1000)) // 14 days for refreshing
            .withIssuer(request.requestURL.toString())
            .sign(algorithm)

//        response.setHeader("access_token", accessToken)
//        response.setHeader("refresh_token", refreshToken)

        val player = repository.findByUserName(user.username) ?: throw Error("User not found but authenticated!!")

        val userResponse = LoginResponse(
            userName = player.userName,
            role = player.role,
            status = player.status,
            _id = player.id.toString(),
            accessToken = accessToken,
            refreshToken = refreshToken,
        )

        val json = Gson().toJson(userResponse)
        response.contentType = APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(json)
        response.writer.flush()

//        val content: HashMap<String, String> = HashMap()
//        content["access_token"] = accessToken
//        content["refresh_token"] = refreshToken
//        ObjectMapper().writeValue(response.outputStream, content)
    }
}