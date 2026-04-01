package com.example.cp.hub.Service;

import com.example.cp.hub.DTO.RegisterRequest;
import com.example.cp.hub.DTO.UpdateProfileRequest;
import com.example.cp.hub.Repository.UserRepository;
import com.example.cp.hub.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCodeforcesHandle(request.getCodeforcesHandle());
        user.setLeetcodeHandle(request.getLeetcodeHandle());
        user.setCodechefHandle(request.getCodechefHandle());

        return userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with that email"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Incorrect password");
        }

        return user;
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User updateProfile(String email, UpdateProfileRequest request) {
        User user = getByEmail(email);

        if (request.getName() != null) user.setName(request.getName());
        if (request.getCodeforcesHandle() != null) user.setCodeforcesHandle(request.getCodeforcesHandle());
        if (request.getLeetcodeHandle() != null) user.setLeetcodeHandle(request.getLeetcodeHandle());
        if (request.getCodechefHandle() != null) user.setCodechefHandle(request.getCodechefHandle());

        return userRepository.save(user);
    }
}
