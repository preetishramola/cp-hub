package com.example.cp.hub.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// ---- Blog Response ----
@Data
@AllArgsConstructor
public class BlogResponse {
    private Long id;
    private String title;
    private String content;         // raw markdown
    private String authorUsername;
    private String authorName;
    private int upvoteCount;
    private boolean upvotedByMe;    // did the currently logged-in user upvote this?
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CommentResponse> comments;
}
