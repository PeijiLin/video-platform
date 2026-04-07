package com.lpjpro.model.user.DTO;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {
    private String account;
    private String password;
}
