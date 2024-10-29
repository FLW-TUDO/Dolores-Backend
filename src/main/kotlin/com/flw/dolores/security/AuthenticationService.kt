package com.flw.dolores.security

import com.flw.dolores.repositories.PlayerRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val repository: PlayerRepository
) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val player =
            repository.findByUserName(username) ?: throw UsernameNotFoundException("User not found in database")
        val authorities: Collection<SimpleGrantedAuthority> = arrayListOf(SimpleGrantedAuthority(player.role))
        return User(player.userName, player.password, authorities)
    }
}