package com.example.cp.hub.DTO;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String username;
    private String email;
    private String password;

    // Optional at signup — user can fill these in their profile later
    private String codeforcesHandle;
    private String leetcodeHandle;
    private String codechefHandle;
}
