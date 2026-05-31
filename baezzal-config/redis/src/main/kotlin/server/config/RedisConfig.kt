package server.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config

@Configuration
@EnableConfigurationProperties(RedisConnectionProperties::class)
class RedisConfig {
    @Bean
    fun redisConnectionFactory(properties: RedisConnectionProperties): RedisConnectionFactory {
        val standalone =
            RedisStandaloneConfiguration(
                properties.host,
                properties.port,
            ).apply {
                database = properties.database
                if (!properties.username.isNullOrBlank()) {
                    username =
                        properties.username
                }
                if (!properties.password.isNullOrBlank()) {
                    password =
                        RedisPassword.of(properties.password)
                }
            }
        val clientConfiguration =
            LettuceClientConfiguration
                .builder()
                .commandTimeout(properties.timeout)
                .build()
        return LettuceConnectionFactory(standalone, clientConfiguration).apply {
            afterPropertiesSet()
        }
    }

    @Bean
    fun stringRedisTemplate(redisConnectionFactory: RedisConnectionFactory): StringRedisTemplate =
        StringRedisTemplate(redisConnectionFactory)

    @Bean(destroyMethod = "shutdown")
    fun redissonClient(properties: RedisConnectionProperties): RedissonClient {
        val timeoutMillis = properties.timeout.toMillis().coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        val config =
            Config().apply {
                useSingleServer()
                    .setAddress("redis://${properties.host}:${properties.port}")
                    .setDatabase(properties.database)
                    .setTimeout(timeoutMillis)
                    .setConnectTimeout(timeoutMillis)
                    .apply {
                        if (!properties.username.isNullOrBlank()) {
                            username = properties.username
                        }
                        if (!properties.password.isNullOrBlank()) {
                            password = properties.password
                        }
                    }
            }
        return Redisson.create(config)
    }
}
