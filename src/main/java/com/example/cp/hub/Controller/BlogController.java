package com.example.cp.hub.Controller;

import com.example.cp.hub.DTO.*;
import com.example.cp.hub.Service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/blogs")
public class BlogController {

    @Autowired
    private BlogService blogService;

    // ---- Public endpoints (no auth needed) ----

    // GET /blogs — all blogs, newest first
    @GetMapping
    public List<BlogResponse> getAllBlogs() {
        String email = getEmailIfLoggedIn();
        return blogService.getAllBlogs(email);
    }

    // GET /blogs/{id} — single blog with full comments tree
    @GetMapping("/{id}")
    public BlogResponse getBlog(@PathVariable Long id) {
        String email = getEmailIfLoggedIn();
        return blogService.getBlogById(id, email);
    }

    // GET /blogs/user/{username} — all blogs by a specific user
    @GetMapping("/user/{username}")
    public List<BlogResponse> getBlogsByUser(@PathVariable String username) {
        String email = getEmailIfLoggedIn();
        return blogService.getBlogsByUser(username, email);
    }

    // ---- Auth required endpoints ----

    // POST /blogs — create a new blog
    @PostMapping
    public BlogResponse createBlog(@RequestBody BlogRequest request) {
        return blogService.createBlog(getEmail(), request);
    }

    // PUT /blogs/{id} — edit your own blog
    @PutMapping("/{id}")
    public BlogResponse updateBlog(@PathVariable Long id, @RequestBody BlogRequest request) {
        return blogService.updateBlog(id, getEmail(), request);
    }

    // DELETE /blogs/{id} — delete your own blog
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlog(@PathVariable Long id) {
        blogService.deleteBlog(id, getEmail());
        return ResponseEntity.noContent().build();
    }

    // POST /blogs/{id}/upvote — toggle upvote on a blog
    @PostMapping("/{id}/upvote")
    public BlogResponse upvote(@PathVariable Long id) {
        return blogService.toggleUpvote(id, getEmail());
    }

    // POST /blogs/{id}/comments — add a comment or reply
    // For a reply, set parentId in the request body
    @PostMapping("/{id}/comments")
    public CommentResponse addComment(@PathVariable Long id,
                                      @RequestBody CommentRequest request) {
        return blogService.addComment(id, getEmail(), request);
    }

    // DELETE /blogs/comments/{commentId} — delete your own comment
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        blogService.deleteComment(commentId, getEmail());
        return ResponseEntity.noContent().build();
    }

    // ---- Helpers ----

    // Returns email of logged-in user (throws if not authenticated)
    private String getEmail() {
        return (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    // Returns email if logged in, null if request is unauthenticated
    // Used for public endpoints that show "upvotedByMe" correctly when logged in
    private String getEmailIfLoggedIn() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof String) {
                return (String) auth.getPrincipal();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
