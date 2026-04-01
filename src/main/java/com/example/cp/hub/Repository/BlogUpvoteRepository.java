package com.example.cp.hub.Repository;

import com.example.cp.hub.model.Blog;
import com.example.cp.hub.model.BlogUpvote;
import com.example.cp.hub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlogUpvoteRepository extends JpaRepository<BlogUpvote, Long> {
    Optional<BlogUpvote> findByBlogAndUser(Blog blog, User user);
    boolean existsByBlogAndUser(Blog blog, User user);
}
