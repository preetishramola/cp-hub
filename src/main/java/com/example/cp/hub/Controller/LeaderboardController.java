package com.example.cp.hub.Controller;

import com.example.cp.hub.DTO.LeaderboardEntry;
import com.example.cp.hub.DTO.LeaderboardResponse;
import com.example.cp.hub.Service.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/leaderboard")
public class LeaderboardController {

    @Autowired
    private LeaderboardService leaderboardService;

    /**
     * GET /leaderboard
     * Returns all 4 leaderboards in one call:
     * global (combined), codeforces, leetcode, codechef
     *
     * Frontend can pick which tab to display.
     */
    @GetMapping
    public LeaderboardResponse getFullLeaderboard() {
        return leaderboardService.getLeaderboard();
    }

    /**
     * GET /leaderboard/global
     * Just the combined cross-platform leaderboard.
     */
    @GetMapping("/global")
    public List<LeaderboardEntry> getGlobalLeaderboard() {
        return leaderboardService.getLeaderboard().getGlobal();
    }

    /**
     * GET /leaderboard/codeforces
     * Ranked by Codeforces score only.
     */
    @GetMapping("/codeforces")
    public List<LeaderboardEntry> getCodeforcesLeaderboard() {
        return leaderboardService.getLeaderboard().getCodeforces();
    }

    /**
     * GET /leaderboard/leetcode
     * Ranked by LeetCode problems solved.
     */
    @GetMapping("/leetcode")
    public List<LeaderboardEntry> getLeetcodeLeaderboard() {
        return leaderboardService.getLeaderboard().getLeetcode();
    }

    /**
     * GET /leaderboard/codechef
     * Ranked by CodeChef score only.
     */
    @GetMapping("/codechef")
    public List<LeaderboardEntry> getCodechefLeaderboard() {
        return leaderboardService.getLeaderboard().getCodechef();
    }
}
