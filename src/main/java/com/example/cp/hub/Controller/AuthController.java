package com.example.cp.hub.Controller;

import com.example.cp.hub.DTO.AuthResponse;
import com.example.cp.hub.DTO.LoginRequest;
import com.example.cp.hub.DTO.RegisterRequest;
import com.example.cp.hub.Service.UserService;
import com.example.cp.hub.jwt.JwtUtil;
import com.example.cp.hub.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getUsername(), user.getEmail(), user.getRole());
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getEmail(), request.getPassword());
        String token = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(token, user.getId(), user.getName(), user.getUsername(), user.getEmail(), user.getRole());
    }

    // Protected test endpoint — proves JWT auth is working
    @GetMapping("/test")
    public String test() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "Hello, " + email + "! Your JWT is valid.";
    }
}
