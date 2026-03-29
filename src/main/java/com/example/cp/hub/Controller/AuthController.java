package com.example.cp.hub.Controller;

import com.example.cp.hub.DTO.LoginRequest;
import com.example.cp.hub.jwt.JwtUtil;
import com.example.cp.hub.Service.UserService;
import com.example.cp.hub.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {


    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public User signup(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody LoginRequest request) {

        User user = userService.login(request.getEmail(), request.getPassword());

        String token = jwtUtil.generateToken(user.getEmail());

        return Map.of(
                "token", token
        );
    }

    @GetMapping("/test")
    public String test(HttpServletRequest request) {
        String email = (String) request.getAttribute("email");
        return "Hello " + email;
    }
}