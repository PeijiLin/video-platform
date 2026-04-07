package com.lpjpro.model.user.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {
    @NotNull
    private String account;
    @NotNull
    private String password;
    @NotNull
    private String checkPassword;
    @NotNull
    private String email;
    @NotNull
    private String code;
}
