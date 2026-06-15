package global.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import server.auth.infrastructure.Oauth2AuthorizationRequestStorage
import server.auth.presentation.Oauth2FailureHandler
import server.auth.presentation.Oauth2Handler
import server.auth.presentation.Oauth2SuccessHandler

@Configuration
class SecurityConfig {
    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        oauth2Handler: Oauth2Handler,
        oauth2SuccessHandler: Oauth2SuccessHandler,
        oauth2FailureHandler: Oauth2FailureHandler,
        oauth2AuthorizationRequestStorage: Oauth2AuthorizationRequestStorage,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors {}
            .authorizeHttpRequests { requests ->
                requests.anyRequest().permitAll()
            }.oauth2Login { oauth2 ->
                oauth2.authorizationEndpoint { endpoint ->
                    endpoint.authorizationRequestRepository(oauth2AuthorizationRequestStorage)
                }
                oauth2.redirectionEndpoint { endpoint ->
                    endpoint.baseUri("/api/v1/auth/callback/*")
                }
                oauth2.userInfoEndpoint { endpoint ->
                    endpoint.userService(oauth2Handler)
                }
                oauth2.successHandler(oauth2SuccessHandler)
                oauth2.failureHandler(oauth2FailureHandler)
            }

        return http.build()
    }
}
