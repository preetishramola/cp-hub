package com.example.cp.hub.DTO;

import lombok.Data;

@Data
public class CommentRequest {
    private String content;
    private Long parentId; // null for top-level comment, parentComment's id for a reply
}
