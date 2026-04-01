package com.example.cp.hub.Service;

import com.example.cp.hub.DTO.LeaderboardEntry;
import com.example.cp.hub.DTO.LeaderboardResponse;
import com.example.cp.hub.Repository.PlatformStatsRepository;
import com.example.cp.hub.Repository.UserRepository;
import com.example.cp.hub.model.Platform;
import com.example.cp.hub.model.PlatformStats;
import com.example.cp.hub.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {

    /*
     * SCORING FORMULA
     * ---------------
     * Combined Score = (normalizedRating × 0.7) + (totalSolved × 0.3)
     *
     * Per-platform Score:
     *   Codeforces : (cfRating × 0.7) + (cfSolved × 0.3)
     *   LeetCode   : LeetCode has no rating — we use problemsSolved × 1.0 as the score
     *                (globalRank is excluded from score since lower = better, which
     *                 would invert the ranking logic)
     *   CodeChef   : (ccRating × 0.7) + (ccSolved × 0.3)
     *
     * For the COMBINED score we average the per-platform scores of whichever
     * platforms the user has connected, so users with only 1 platform aren't
     * unfairly penalized.
     */

    private static final double RATING_WEIGHT  = 0.7;
    private static final double SOLVED_WEIGHT  = 0.3;

    @Autowired private UserRepository userRepository;
    @Autowired private PlatformStatsRepository statsRepository;

    public LeaderboardResponse getLeaderboard() {
        List<User> allUsers = userRepository.findAll();

        List<LeaderboardEntry> entries = new ArrayList<>();

        for (User user : allUsers) {
            List<PlatformStats> statsList = statsRepository.findAllByUser(user);
            if (statsList.isEmpty()) continue; // skip users with no stats yet

            Map<Platform, PlatformStats> statsMap = statsList.stream()
                    .collect(Collectors.toMap(PlatformStats::getPlatform, s -> s));

            // --- Extract raw values ---
            PlatformStats cf = statsMap.get(Platform.CODEFORCES);
            PlatformStats lc = statsMap.get(Platform.LEETCODE);
            PlatformStats cc = statsMap.get(Platform.CODECHEF);

            Integer cfRating  = cf != null ? cf.getRating()        : null;
            String  cfRank    = cf != null ? cf.getRank()           : null;
            Integer cfSolved  = cf != null ? cf.getProblemsSolved() : null;

            Integer lcSolved     = lc != null ? lc.getProblemsSolved() : null;
            Integer lcGlobalRank = lc != null ? lc.getGlobalRank()     : null;

            Integer ccRating  = cc != null ? cc.getRating()        : null;
            String  ccRank    = cc != null ? cc.getRank()           : null;
            Integer ccSolved  = cc != null ? cc.getProblemsSolved() : null;

            // --- Compute per-platform scores ---
            double cfScore = computePlatformScore(cfRating, cfSolved);
            double lcScore = lcSolved != null ? lcSolved : 0.0; // LeetCode: solved count only
            double ccScore = computePlatformScore(ccRating, ccSolved);

            // --- Compute combined score (average of connected platforms) ---
            List<Double> activePlatformScores = new ArrayList<>();
            if (cf != null) activePlatformScores.add(cfScore);
            if (lc != null) activePlatformScores.add(lcScore);
            if (cc != null) activePlatformScores.add(ccScore);

            double combinedScore = activePlatformScores.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);

            entries.add(new LeaderboardEntry(
                    0, // rank assigned after sorting
                    user.getId(),
                    user.getUsername(),
                    user.getName(),
                    cfRating, cfRank, cfSolved,
                    lcSolved, lcGlobalRank,
                    ccRating, ccRank, ccSolved,
                    round(combinedScore),
                    round(cfScore),
                    round(lcScore),
                    round(ccScore)
            ));
        }

        // --- Build sorted leaderboards and assign ranks ---
        List<LeaderboardEntry> global     = rankBy(entries, Comparator.comparingDouble(LeaderboardEntry::getCombinedScore).reversed());
        List<LeaderboardEntry> codeforces = rankBy(entries, Comparator.comparingDouble(LeaderboardEntry::getCfScore).reversed());
        List<LeaderboardEntry> leetcode   = rankBy(entries, Comparator.comparingDouble(LeaderboardEntry::getLcScore).reversed());
        List<LeaderboardEntry> codechef   = rankBy(entries, Comparator.comparingDouble(LeaderboardEntry::getCcScore).reversed());

        return new LeaderboardResponse(global, codeforces, leetcode, codechef, LocalDateTime.now());
    }

    // --- Helpers ---

    private double computePlatformScore(Integer rating, Integer solved) {
        double r = rating != null ? rating : 0.0;
        double s = solved != null ? solved : 0.0;
        return (r * RATING_WEIGHT) + (s * SOLVED_WEIGHT);
    }

    /**
     * Sorts a copy of the entries list by the given comparator,
     * then assigns rank numbers (1-indexed), handling ties —
     * if two users have the same score they get the same rank.
     */
    private List<LeaderboardEntry> rankBy(List<LeaderboardEntry> entries,
                                          Comparator<LeaderboardEntry> comparator) {
        List<LeaderboardEntry> sorted = entries.stream()
                .sorted(comparator)
                .collect(Collectors.toList());

        int rank = 1;
        for (int i = 0; i < sorted.size(); i++) {
            if (i > 0) {
                LeaderboardEntry prev = sorted.get(i - 1);
                LeaderboardEntry curr = sorted.get(i);
                // Only increment rank if score actually differs
                if (curr.getCombinedScore() != prev.getCombinedScore()) {
                    rank = i + 1;
                }
            }
            sorted.get(i).setRank(rank);
        }
        return sorted;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
