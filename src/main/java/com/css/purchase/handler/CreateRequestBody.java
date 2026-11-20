package com.css.purchase.handler;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRequestBody(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "business key must not be blank")
        String businessKey,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 120)
        @NotBlank(message = "title must not be blank")
        @Size(max = 120)
        String title) {
}
