package org.elysia.api.DTO.phone;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
// @RedisHash
public record PhoneDTO(
        int id,
        String name,
        String number,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime created_at)
{ }