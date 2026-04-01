package com.example.cp.hub.DTO;

import lombok.Data;

// ---- Requests ----

@Data
public class BlogRequest {
    private String title;
    private String content; // markdown
}
