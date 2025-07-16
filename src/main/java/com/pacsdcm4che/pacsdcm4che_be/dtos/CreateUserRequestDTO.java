package com.pacsdcm4che.pacsdcm4che_be.dtos;

import lombok.Data;

import java.util.Set;

@Data
public class CreateUserRequestDTO {
    private String username;
    private String password;
    private Set<String> role;
    private String phoneNumber;
    private String email;
}
