package org.elysia.api.DTO.phone;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PhoneCreateRequest(
        @Size(min = 1, max = 50)
        String name,

        @Pattern(regexp = "^[0-9]{10,20}$")
        @NotNull
        String number)
{}
