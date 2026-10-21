package com.example.cp.hub.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.With;

@Data
@AllArgsConstructor
public class LeaderboardEntry {
    @With
    private int rank;               // position on the leaderboard (1, 2, 3...)
    private Long userId;
    private String username;
    private String name;

    // Per-platform stats (null if user hasn't set that handle)
    private Integer cfRating;
    private Integer cfMaxRating;
    private String cfRank;
    private Integer cfSolved;

    private Integer lcRating;
    private Integer lcMaxRating;
    private Integer lcSolved;
    private Integer lcGlobalRank;

    private Integer ccRating;
    private Integer ccMaxRating;
    private String ccRank;
    private Integer ccSolved;

    // Computed scores
    private double combinedScore;   // weighted score across all platforms
    private double cfScore;         // per-platform score
    private double lcScore;
    private double ccScore;
}
