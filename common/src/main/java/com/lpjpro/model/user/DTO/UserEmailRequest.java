package com.lpjpro.model.user.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class UserEmailRequest {
    @NotNull
    private String email;
    @NotNull
    private String code;
}
