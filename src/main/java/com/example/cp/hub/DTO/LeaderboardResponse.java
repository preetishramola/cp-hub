package com.example.cp.hub.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class LeaderboardResponse {
    private List<LeaderboardEntry> global;      // sorted by combinedScore
    private List<LeaderboardEntry> codeforces;  // sorted by cfScore
    private List<LeaderboardEntry> leetcode;    // sorted by lcScore
    private List<LeaderboardEntry> codechef;    // sorted by ccScore
    private LocalDateTime generatedAt;
}
