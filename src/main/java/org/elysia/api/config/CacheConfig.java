package org.elysia.api.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.elysia.api.DTO.phone.PhoneDTO;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static java.time.Duration.ofDays;
import static org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public RedisCacheManagerBuilderCustomizer redisBuilderCustomizer(ObjectMapper objectMapper) {
        ObjectMapper cacheObjectMapper = objectMapper.copy();
        cacheObjectMapper.registerModules(new JavaTimeModule(), new Jdk8Module(), new ParameterNamesModule());

        GenericJackson2JsonRedisSerializer defaultSerializer = new GenericJackson2JsonRedisSerializer(cacheObjectMapper);
        Jackson2JsonRedisSerializer<PhoneDTO> phoneDTOJackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(cacheObjectMapper, PhoneDTO.class);

        JavaType phoneDTOlistType = cacheObjectMapper.getTypeFactory().constructCollectionType(List.class, PhoneDTO.class);
        Jackson2JsonRedisSerializer<List<PhoneDTO>> phoneDTOlistSerializer = new Jackson2JsonRedisSerializer<>(cacheObjectMapper, phoneDTOlistType);

        return builder -> builder.cacheDefaults(defaultCacheConfig()
                        .prefixCacheNameWith("defaults")
                        .serializeValuesWith(fromSerializer(defaultSerializer))
                        .entryTtl(ofDays(1)))
                .withCacheConfiguration("PhoneNumbers", defaultCacheConfig()
                        .serializeValuesWith(fromSerializer(phoneDTOJackson2JsonRedisSerializer))
                        .entryTtl(ofDays(1)))
                .withCacheConfiguration("PhoneNumbersList", defaultCacheConfig()
                        .serializeValuesWith(fromSerializer(phoneDTOlistSerializer))
                        .entryTtl(ofDays(1))
                );
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(javaTimeModule)
                .registerModule(new Jdk8Module())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));

        template.afterPropertiesSet();
        return template;
    }


//    @Bean
//    public RedisCacheConfiguration redisCacheConfiguration() {
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//
//        JavaTimeModule javaTimeModule = new JavaTimeModule();
//        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
//        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
//
//        ObjectMapper objectMapper = new ObjectMapper()
//                .registerModule(javaTimeModule)
//                .registerModule(new Jdk8Module())
//                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
//                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
//
//        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
//
//        // 🔹 Use GenericJackson2JsonRedisSerializer (safe, no type metadata required)
//        RedisSerializer<Object> serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
//
//        return RedisCacheConfiguration.defaultCacheConfig()
//                .entryTtl(Duration.ofMinutes(10))
//                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
//                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
//    }
}
