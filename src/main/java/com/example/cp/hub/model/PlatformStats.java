package com.example.cp.hub.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "platform_stats")
public class PlatformStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which user these stats belong to
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Which platform: CODEFORCES, LEETCODE, CODECHEF
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    private String handle;         // the username on that platform

    private Integer rating;        // current rating (null for LeetCode which uses ranking)
    private String rank;           // rank title e.g. "Specialist", "Guardian"
    private Integer globalRank;    // global ranking number (LeetCode, CodeChef)
    private Integer problemsSolved; // total problems solved

    private LocalDateTime lastFetched; // when we last pulled from the external API

    @PrePersist
    @PreUpdate
    public void onSave() {
        this.lastFetched = LocalDateTime.now();
    }
}
