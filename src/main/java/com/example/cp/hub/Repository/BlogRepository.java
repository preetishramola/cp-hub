package com.example.cp.hub.Repository;

import com.example.cp.hub.model.Blog;
import com.example.cp.hub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    // All blogs by a specific author, newest first
    List<Blog> findAllByAuthorOrderByCreatedAtDesc(User author);
    // All blogs, newest first (for the public feed)
    List<Blog> findAllByOrderByCreatedAtDesc();
}
