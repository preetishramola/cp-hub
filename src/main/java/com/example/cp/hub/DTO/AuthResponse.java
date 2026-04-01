package com.example.cp.hub.DTO;

import com.example.cp.hub.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;
    private String name;
    private String username;
    private String email;
    private Role role;
}
