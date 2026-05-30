package global.auth

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
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
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { requests ->
                requests.anyRequest().permitAll()
            }.oauth2Login { oauth2 ->
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
