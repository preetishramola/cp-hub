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

    // LeetCode uses GraphQL — we query for profile + solved stats in one call
    private static final String QUERY = """
            {
              "query": "query getUserProfile($username: String!) { matchedUser(username: $username) { profile { ranking } submitStatsGlobal { acSubmissionNum { difficulty count } } } }",
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
                    null,           // LeetCode doesn't have a rating like CF
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
