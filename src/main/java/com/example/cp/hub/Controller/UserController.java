package com.example.cp.hub.Controller;

import com.example.cp.hub.DTO.UpdateProfileRequest;
import com.example.cp.hub.Service.UserService;
import com.example.cp.hub.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Get the currently logged-in user's profile
    @GetMapping("/me")
    public User getMyProfile() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.getByEmail(email);
    }

    // Update the currently logged-in user's profile (name, CP handles)
    @PutMapping("/me")
    public User updateMyProfile(@RequestBody UpdateProfileRequest request) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userService.updateProfile(email, request);
    }

    // Public profile view by username — for social/community feature
    @GetMapping("/{username}")
    public User getPublicProfile(@PathVariable String username) {
        return userService.getByUsername(username);
    }
}
