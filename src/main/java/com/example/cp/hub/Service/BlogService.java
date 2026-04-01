package com.example.cp.hub.Service;

import com.example.cp.hub.DTO.*;
import com.example.cp.hub.Repository.*;
import com.example.cp.hub.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlogService {

    @Autowired private BlogRepository blogRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private BlogUpvoteRepository upvoteRepository;
    @Autowired private UserRepository userRepository;

    // ---- Blog CRUD ----

    public BlogResponse createBlog(String email, BlogRequest request) {
        User author = getUser(email);
        Blog blog = new Blog();
        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blog.setAuthor(author);
        blogRepository.save(blog);
        return toResponse(blog, author);
    }

    public List<BlogResponse> getAllBlogs(String email) {
        User viewer = email != null ? userRepository.findByEmail(email).orElse(null) : null;
        return blogRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(b -> toResponse(b, viewer))
                .collect(Collectors.toList());
    }

    public BlogResponse getBlogById(Long id, String email) {
        Blog blog = getBlog(id);
        User viewer = email != null ? userRepository.findByEmail(email).orElse(null) : null;
        return toResponseWithComments(blog, viewer);
    }

    public BlogResponse updateBlog(Long id, String email, BlogRequest request) {
        Blog blog = getBlog(id);
        User user = getUser(email);

        if (!blog.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("You can only edit your own blogs");
        }

        blog.setTitle(request.getTitle());
        blog.setContent(request.getContent());
        blogRepository.save(blog);
        return toResponse(blog, user);
    }

    public void deleteBlog(Long id, String email) {
        Blog blog = getBlog(id);
        User user = getUser(email);

        if (!blog.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own blogs");
        }
        blogRepository.delete(blog);
    }

    public List<BlogResponse> getBlogsByUser(String username, String viewerEmail) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        User viewer = viewerEmail != null ? userRepository.findByEmail(viewerEmail).orElse(null) : null;
        return blogRepository.findAllByAuthorOrderByCreatedAtDesc(author)
                .stream()
                .map(b -> toResponse(b, viewer))
                .collect(Collectors.toList());
    }

    // ---- Upvotes ----

    public BlogResponse toggleUpvote(Long blogId, String email) {
        Blog blog = getBlog(blogId);
        User user = getUser(email);

        if (upvoteRepository.existsByBlogAndUser(blog, user)) {
            // Already upvoted — remove it (toggle off)
            BlogUpvote existing = upvoteRepository.findByBlogAndUser(blog, user).get();
            upvoteRepository.delete(existing);
            blog.setUpvoteCount(Math.max(0, blog.getUpvoteCount() - 1));
        } else {
            // New upvote
            BlogUpvote upvote = new BlogUpvote();
            upvote.setBlog(blog);
            upvote.setUser(user);
            upvoteRepository.save(upvote);
            blog.setUpvoteCount(blog.getUpvoteCount() + 1);
        }

        blogRepository.save(blog);
        return toResponse(blog, user);
    }

    // ---- Comments ----

    public CommentResponse addComment(Long blogId, String email, CommentRequest request) {
        Blog blog = getBlog(blogId);
        User author = getUser(email);

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setAuthor(author);
        comment.setBlog(blog);

        // If parentId is provided, this is a reply
        if (request.getParentId() != null) {
            Comment parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));

            // Make sure the parent belongs to the same blog
            if (!parent.getBlog().getId().equals(blogId)) {
                throw new RuntimeException("Parent comment does not belong to this blog");
            }
            comment.setParent(parent);
        }

        commentRepository.save(comment);
        return toCommentResponse(comment);
    }

    public void deleteComment(Long commentId, String email) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        User user = getUser(email);

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own comments");
        }
        commentRepository.delete(comment);
    }

    // ---- Mappers ----

    private BlogResponse toResponse(Blog blog, User viewer) {
        boolean upvotedByMe = viewer != null && upvoteRepository.existsByBlogAndUser(blog, viewer);
        return new BlogResponse(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getAuthor().getUsername(),
                blog.getAuthor().getName(),
                blog.getUpvoteCount(),
                upvotedByMe,
                blog.getCreatedAt(),
                blog.getUpdatedAt(),
                null // no comments in list view — saves DB calls
        );
    }

    private BlogResponse toResponseWithComments(Blog blog, User viewer) {
        boolean upvotedByMe = viewer != null && upvoteRepository.existsByBlogAndUser(blog, viewer);
        List<CommentResponse> comments = commentRepository
                .findAllByBlogAndParentIsNullOrderByCreatedAtAsc(blog)
                .stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());

        return new BlogResponse(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getAuthor().getUsername(),
                blog.getAuthor().getName(),
                blog.getUpvoteCount(),
                upvotedByMe,
                blog.getCreatedAt(),
                blog.getUpdatedAt(),
                comments
        );
    }

    // Recursively maps comment + its replies
    private CommentResponse toCommentResponse(Comment comment) {
        List<CommentResponse> replies = comment.getReplies()
                .stream()
                .map(this::toCommentResponse)
                .collect(Collectors.toList());

        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getAuthor().getUsername(),
                comment.getAuthor().getName(),
                comment.getCreatedAt(),
                replies
        );
    }

    // ---- Helpers ----

    private Blog getBlog(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
