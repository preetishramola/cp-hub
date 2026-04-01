package com.example.cp.hub.Service;

import com.example.cp.hub.DTO.StatsResponse;
import com.example.cp.hub.Repository.PlatformStatsRepository;
import com.example.cp.hub.Repository.UserRepository;
import com.example.cp.hub.model.Platform;
import com.example.cp.hub.model.PlatformStats;
import com.example.cp.hub.model.User;
import com.example.cp.hub.tracker.CodeChefService;
import com.example.cp.hub.tracker.CodeforcesService;
import com.example.cp.hub.tracker.LeetCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TrackerService {

    // Cache TTL: only re-fetch from external API if data is older than 1 hour
    private static final int CACHE_TTL_HOURS = 1;

    @Autowired private UserRepository userRepository;
    @Autowired private PlatformStatsRepository statsRepository;
    @Autowired private CodeforcesService codeforcesService;
    @Autowired private LeetCodeService leetCodeService;
    @Autowired private CodeChefService codeChefService;

    /**
     * Returns stats for all platforms for a given user.
     * Serves from cache if fresh, otherwise fetches live and updates cache.
     */
    public List<StatsResponse> getAllStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<StatsResponse> results = new ArrayList<>();

        if (user.getCodeforcesHandle() != null) {
            try { results.add(getOrFetch(user, Platform.CODEFORCES, user.getCodeforcesHandle())); }
            catch (Exception e) { System.err.println("CF fetch failed: " + e.getMessage()); }
        }

        if (user.getLeetcodeHandle() != null) {
            try { results.add(getOrFetch(user, Platform.LEETCODE, user.getLeetcodeHandle())); }
            catch (Exception e) { System.err.println("LC fetch failed: " + e.getMessage()); }
        }

        if (user.getCodechefHandle() != null) {
            try { results.add(getOrFetch(user, Platform.CODECHEF, user.getCodechefHandle())); }
            catch (Exception e) { System.err.println("CC fetch failed: " + e.getMessage()); }
        }
        return results;
    }

    /**
     * Force-refresh stats for a specific platform (bypasses cache).
     * Called when user explicitly clicks "Refresh" on the dashboard.
     */
    public StatsResponse refreshStats(String email, Platform platform) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String handle = getHandle(user, platform);
        if (handle == null) throw new RuntimeException("No " + platform + " handle set on your profile");

        StatsResponse fresh = fetchFromPlatform(platform, handle);
        saveToCache(user, platform, handle, fresh);
        return fresh;
    }

    // --- Internal helpers ---

    private StatsResponse getOrFetch(User user, Platform platform, String handle) {
        Optional<PlatformStats> cached = statsRepository.findByUserAndPlatform(user, platform);

        // Use cache if it exists and is less than CACHE_TTL_HOURS old
        if (cached.isPresent()) {
            PlatformStats stats = cached.get();
            boolean isFresh = stats.getLastFetched()
                    .isAfter(LocalDateTime.now().minusHours(CACHE_TTL_HOURS));
            if (isFresh) {
                return toResponse(stats);
            }
        }

        // Cache miss or stale — fetch live
        StatsResponse fresh = fetchFromPlatform(platform, handle);
        saveToCache(user, platform, handle, fresh);
        return fresh;
    }

    private StatsResponse fetchFromPlatform(Platform platform, String handle) {
        return switch (platform) {
            case CODEFORCES -> codeforcesService.fetchStats(handle);
            case LEETCODE   -> leetCodeService.fetchStats(handle);
            case CODECHEF   -> codeChefService.fetchStats(handle);
        };
    }

    private void saveToCache(User user, Platform platform, String handle, StatsResponse data) {
        PlatformStats stats = statsRepository
                .findByUserAndPlatform(user, platform)
                .orElse(new PlatformStats());

        stats.setUser(user);
        stats.setPlatform(platform);
        stats.setHandle(handle);
        stats.setRating(data.getRating());
        stats.setRank(data.getRank());
        stats.setGlobalRank(data.getGlobalRank());
        stats.setProblemsSolved(data.getProblemsSolved());

        statsRepository.save(stats);
    }

    private StatsResponse toResponse(PlatformStats s) {
        return new StatsResponse(
                s.getPlatform(),
                s.getHandle(),
                s.getRating(),
                s.getRank(),
                s.getGlobalRank(),
                s.getProblemsSolved(),
                s.getLastFetched()
        );
    }

    private String getHandle(User user, Platform platform) {
        return switch (platform) {
            case CODEFORCES -> user.getCodeforcesHandle();
            case LEETCODE   -> user.getLeetcodeHandle();
            case CODECHEF   -> user.getCodechefHandle();
        };
    }

    /**
     * Scheduled job: refresh stats for ALL users every 6 hours automatically.
     * This keeps the cache warm without users having to manually trigger it.
     * Requires @EnableScheduling on CpHubApplication.
     */
    @Scheduled(fixedDelay = 6 * 60 * 60 * 1000)
    public void scheduledRefreshAll() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            try {
                if (user.getCodeforcesHandle() != null)
                    refreshStats(user.getEmail(), Platform.CODEFORCES);
                if (user.getLeetcodeHandle() != null)
                    refreshStats(user.getEmail(), Platform.LEETCODE);
                if (user.getCodechefHandle() != null)
                    refreshStats(user.getEmail(), Platform.CODECHEF);
            } catch (Exception e) {
                // Don't let one user's bad handle crash the whole job
                System.err.println("Scheduled refresh failed for user: " + user.getEmail() + " — " + e.getMessage());
            }
        }
    }
}
