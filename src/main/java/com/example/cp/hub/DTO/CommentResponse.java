package com.example.cp.hub.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private String authorUsername;
    private String authorName;
    private LocalDateTime createdAt;
    private List<CommentResponse> replies; // nested replies (recursive)
}
