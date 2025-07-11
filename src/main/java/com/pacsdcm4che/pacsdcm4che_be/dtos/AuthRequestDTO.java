package com.pacsdcm4che.pacsdcm4che_be.dtos;

import java.util.Set;

public class AuthRequestDTO {
    private String username;
    private String password;
    private Set<String> role;
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<String> getRole() {
        return role;
    }

    public void setRole(Set<String> role) {
        this.role = role;
    }
}
