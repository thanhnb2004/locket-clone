package com.locket.clone.reaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReactionRequest(
        @NotBlank @Size(max = 16) String emoji
) {
}
