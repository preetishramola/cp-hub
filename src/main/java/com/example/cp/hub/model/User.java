package com.example.cp.hub.model;

import jakarta.persistence.*;
import lombok.*;

import com.example.cp.hub.model.Role; // ✅ CORRECT
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    // CP Platform Handles
    private String codeforcesHandle;
    private String leetcodeHandle;
    private String codechefHandle;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
