package com.gringotts.testcontainerplayground.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableJpaRepositories({"com.gringotts"})
@EnableCaching
public class AppConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration(ObjectMapper objectMapper) {
        return RedisCacheConfiguration.defaultCacheConfig()
                                      .entryTtl(Duration.ofMinutes(60))
                                      .disableCachingNullValues()
                                      .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                                              new GenericJackson2JsonRedisSerializer(objectMapper)));
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                         .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true)
                         .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
                         .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                         .configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true)
                         .addModule(new JavaTimeModule())
                         .findAndAddModules()
                         .build();
    }
}
