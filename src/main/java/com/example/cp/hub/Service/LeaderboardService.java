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
import java.util.function.ToDoubleFunction;
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
     *   LeetCode   : problemsSolved × 1.0 as the score (contest rating is shown
     *                but not scored, since many users never do contests)
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
            Integer cfMax     = cf != null ? cf.getMaxRating()     : null;
            String  cfRank    = cf != null ? cf.getRank()           : null;
            Integer cfSolved  = cf != null ? cf.getProblemsSolved() : null;

            Integer lcRating     = lc != null ? lc.getRating()         : null;
            Integer lcMax        = lc != null ? lc.getMaxRating()      : null;
            Integer lcSolved     = lc != null ? lc.getProblemsSolved() : null;
            Integer lcGlobalRank = lc != null ? lc.getGlobalRank()     : null;

            Integer ccRating  = cc != null ? cc.getRating()        : null;
            Integer ccMax     = cc != null ? cc.getMaxRating()     : null;
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
                    cfRating, cfMax, cfRank, cfSolved,
                    lcRating, lcMax, lcSolved, lcGlobalRank,
                    ccRating, ccMax, ccRank, ccSolved,
                    round(combinedScore),
                    round(cfScore),
                    round(lcScore),
                    round(ccScore)
            ));
        }

        // --- Build sorted leaderboards and assign ranks ---
        List<LeaderboardEntry> global     = rankBy(entries, LeaderboardEntry::getCombinedScore);
        List<LeaderboardEntry> codeforces = rankBy(entries, LeaderboardEntry::getCfScore);
        List<LeaderboardEntry> leetcode   = rankBy(entries, LeaderboardEntry::getLcScore);
        List<LeaderboardEntry> codechef   = rankBy(entries, LeaderboardEntry::getCcScore);

        return new LeaderboardResponse(global, codeforces, leetcode, codechef, LocalDateTime.now());
    }

    // --- Helpers ---

    private double computePlatformScore(Integer rating, Integer solved) {
        double r = rating != null ? rating : 0.0;
        double s = solved != null ? solved : 0.0;
        return (r * RATING_WEIGHT) + (s * SOLVED_WEIGHT);
    }

    /**
     * Sorts the entries by the given score (highest first) and assigns
     * rank numbers (1-indexed), handling ties —
     * if two users have the same score they get the same rank.
     * Each board gets its own copies so ranks don't overwrite each other.
     */
    private List<LeaderboardEntry> rankBy(List<LeaderboardEntry> entries,
                                          ToDoubleFunction<LeaderboardEntry> score) {
        List<LeaderboardEntry> sorted = entries.stream()
                .sorted(Comparator.comparingDouble(score).reversed())
                .collect(Collectors.toList());

        List<LeaderboardEntry> ranked = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < sorted.size(); i++) {
            // Only increment rank if score actually differs
            if (i > 0 && score.applyAsDouble(sorted.get(i)) != score.applyAsDouble(sorted.get(i - 1))) {
                rank = i + 1;
            }
            ranked.add(sorted.get(i).withRank(rank));
        }
        return ranked;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
