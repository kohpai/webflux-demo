package io.kohpai.webfluxdemo

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableReactiveMethodSecurity
class SecurityConfig {
    @Bean
    fun passwordEncoder() = BCryptPasswordEncoder()

    @Bean
    fun userDetailsService(passwordEncoder: BCryptPasswordEncoder): ReactiveUserDetailsService {
        val reader = User.withUsername("jokereader")
            .password(passwordEncoder.encode("topsecret"))
            .roles("READ")
            .build()
        val writer = User.withUsername("jokewriter")
            .password(passwordEncoder.encode("topsecret"))
            .roles("READ", "WRITE")
            .build()

        return MapReactiveUserDetailsService(reader, writer)
    }

    @Bean
    fun securityWebFilterChain(httpSecurity: ServerHttpSecurity): SecurityWebFilterChain =
        httpSecurity
            .csrf()
            .disable()
            .httpBasic().and()
            .authorizeExchange()
            // order of rules is important
            .pathMatchers(HttpMethod.GET, "/", "/resources/jokes/live")
            .permitAll()
            .pathMatchers(HttpMethod.GET, "/resources/jokes")
            .hasRole("READ")
            .pathMatchers("/resources/jokes")
            .hasRole("WRITE")
            .anyExchange().denyAll()
            .and()
            .build()
}