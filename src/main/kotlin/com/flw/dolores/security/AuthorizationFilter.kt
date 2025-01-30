package com.flw.dolores.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.flw.dolores.entities.JWTValidResponse
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.http.HttpStatus.FORBIDDEN
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.time.LocalTime
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class AuthorizationFilter(
    private val secretKey: String
) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // TODO change pathing to file
        if (request.servletPath.equals("/api/v1/users/login") || request.servletPath.contains("websocket")) {
            filterChain.doFilter(request, response)
        } else {
            val authorizationHeader: String? = request.getHeader(AUTHORIZATION)
            if ((authorizationHeader != null) && authorizationHeader.startsWith("Bearer ")) {
                try {
                    // remove bearer space
                    val token: String = authorizationHeader.substring("Bearer ".length)
                    // TODO replace secret with import
                    val algorithm: Algorithm =
                        Algorithm.HMAC512(secretKey)
                    val verifier: JWTVerifier = JWT.require(algorithm).build()
                    val decodedJWT: DecodedJWT = verifier.verify(token)
                    val userName: String = decodedJWT.subject
                    val role = decodedJWT.claims["roles"].toString()
                    val authorities: ArrayList<SimpleGrantedAuthority> = arrayListOf(SimpleGrantedAuthority(role))
                    val authenticationToken = UsernamePasswordAuthenticationToken(userName, null, authorities)
                    SecurityContextHolder.getContext().authentication = authenticationToken
                    filterChain.doFilter(request, response)
                } catch (exception: Exception) {
                    when (exception) {
                        is TokenExpiredException -> {
                            println("Token has expired for user.")
                        }
                        else -> {
                            response.sendError(INTERNAL_SERVER_ERROR.value())
                            println("${LocalTime.now()}/${request.servletPath}: ${exception.message}")
                        }
                    }

                }
            } else {
                response.sendError(FORBIDDEN.value())
            }
        }
    }

    companion object {
        fun authenticateToken(token: String, secretKey: String): JWTValidResponse {
            return try {
                val algorithm: Algorithm =
                    Algorithm.HMAC512(secretKey)
                val verifier: JWTVerifier = JWT.require(algorithm).build()
                verifier.verify(token.substring("Bearer ".length))
                JWTValidResponse(status = true)
            } catch (exception: Exception) {
                JWTValidResponse(status = false)
            }
        }
    }
}