package com.example.cp.hub.tracker;

import com.example.cp.hub.DTO.StatsResponse;
import com.example.cp.hub.model.Platform;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;

@Service
public class LeetCodeService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String GRAPHQL_URL = "https://leetcode.com/graphql";

    // LeetCode uses GraphQL — we query for profile, contest rating + solved stats in one call
    private static final String QUERY = """
            {
              "query": "query getUserProfile($username: String!) { matchedUser(username: $username) { profile { ranking } submitStatsGlobal { acSubmissionNum { difficulty count } } } userContestRanking(username: $username) { rating } userContestRankingHistory(username: $username) { attended rating } }",
              "variables": { "username": "%s" }
            }
            """;

    public StatsResponse fetchStats(String handle) {
        try {
            String body = QUERY.formatted(handle).strip();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GRAPHQL_URL))
                    .header("Content-Type", "application/json")
                    .header("Referer", "https://leetcode.com")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());

            JsonNode matchedUser = root.path("data").path("matchedUser");
            if (matchedUser.isMissingNode() || matchedUser.isNull()) {
                throw new RuntimeException("LeetCode user not found: " + handle);
            }

            int globalRank = matchedUser.path("profile").path("ranking").asInt(0);

            // Contest rating — userContestRanking is null if the user has never done a contest
            JsonNode contestRating = root.path("data").path("userContestRanking").path("rating");
            Integer rating = contestRating.isNumber() ? (int) Math.round(contestRating.asDouble()) : null;

            // Max rating = highest rating across attended contests
            Integer maxRating = null;
            for (JsonNode contest : root.path("data").path("userContestRankingHistory")) {
                if (contest.path("attended").asBoolean(false) && contest.path("rating").isNumber()) {
                    int r = (int) Math.round(contest.path("rating").asDouble());
                    if (maxRating == null || r > maxRating) maxRating = r;
                }
            }

            // acSubmissionNum has entries for "All", "Easy", "Medium", "Hard"
            int totalSolved = 0;
            JsonNode acStats = matchedUser.path("submitStatsGlobal").path("acSubmissionNum");
            for (JsonNode stat : acStats) {
                if ("All".equals(stat.path("difficulty").asText())) {
                    totalSolved = stat.path("count").asInt(0);
                    break;
                }
            }

            return new StatsResponse(
                    Platform.LEETCODE,
                    handle,
                    rating,         // contest rating (null if no contests)
                    maxRating,
                    null,           // No rank title
                    globalRank,
                    totalSolved,
                    LocalDateTime.now()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch LeetCode stats for handle: " + handle + " — " + e.getMessage());
        }
    }
}
