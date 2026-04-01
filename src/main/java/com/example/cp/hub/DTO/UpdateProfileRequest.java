package com.example.cp.hub.DTO;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String name;
    private String codeforcesHandle;
    private String leetcodeHandle;
    private String codechefHandle;
}
