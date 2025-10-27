package org.elysia.api.DTO.phone;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PhoneUpdateRequest(
        @Size(min = 1, max = 50)
        @Nullable
        String name,

        @Pattern(regexp = "^[0-9]{10,20}$")
        @Nullable
        String number)
{ }
