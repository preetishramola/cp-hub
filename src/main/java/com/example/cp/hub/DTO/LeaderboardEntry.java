package com.example.cp.hub.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardEntry {
    private int rank;               // position on the leaderboard (1, 2, 3...)
    private Long userId;
    private String username;
    private String name;

    // Per-platform stats (null if user hasn't set that handle)
    private Integer cfRating;
    private String cfRank;
    private Integer cfSolved;

    private Integer lcSolved;
    private Integer lcGlobalRank;

    private Integer ccRating;
    private String ccRank;
    private Integer ccSolved;

    // Computed scores
    private double combinedScore;   // weighted score across all platforms
    private double cfScore;         // per-platform score
    private double lcScore;
    private double ccScore;
}
