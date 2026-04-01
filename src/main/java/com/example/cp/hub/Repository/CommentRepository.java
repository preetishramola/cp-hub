package com.example.cp.hub.Repository;

import com.example.cp.hub.model.Blog;
import com.example.cp.hub.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Only fetch top-level comments (parent is null) for a blog
    // Replies are loaded via the Comment.replies relationship
    List<Comment> findAllByBlogAndParentIsNullOrderByCreatedAtAsc(Blog blog);
}
