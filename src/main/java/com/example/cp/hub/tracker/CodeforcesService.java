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
import java.util.HashSet;
import java.util.Set;

@Service
public class CodeforcesService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    // Fetches rating + rank from Codeforces official API
    public StatsResponse fetchStats(String handle) {
        try {
            // Step 1: Get user info (rating, rank)
            String userInfoUrl = "https://codeforces.com/api/user.info?handles=" + handle;
            JsonNode userInfo = get(userInfoUrl).get("result").get(0);

            int rating = userInfo.has("rating") ? userInfo.get("rating").asInt() : 0;
            String rank = userInfo.has("rank") ? userInfo.get("rank").asText() : "unrated";

            // Step 2: Get solved problems count via user.status
            // We count unique problems with verdict "OK"
            int solved = fetchSolvedCount(handle);

            return new StatsResponse(
                    Platform.CODEFORCES,
                    handle,
                    rating,
                    rank,
                    null,         // Codeforces doesn't expose global rank via API
                    solved,
                    LocalDateTime.now()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch Codeforces stats for handle: " + handle + " — " + e.getMessage());
        }
    }

    private int fetchSolvedCount(String handle) throws Exception {
        // user.status returns all submissions; we count unique solved problems
        String url = "https://codeforces.com/api/user.status?handle=" + handle + "&from=1&count=10000";
        JsonNode submissions = get(url).get("result");

        Set<String> solvedProblems = new HashSet<>();
        for (JsonNode sub : submissions) {
            if ("OK".equals(sub.get("verdict").asText())) {
                JsonNode problem = sub.get("problem");
                // Unique key = contestId + problemIndex
                String key = problem.get("contestId").asText() + problem.get("index").asText();
                solvedProblems.add(key);
            }
        }
        return solvedProblems.size();
    }

    private JsonNode get(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(response.body());

        if (!"OK".equals(root.get("status").asText())) {
            throw new RuntimeException(root.has("comment") ? root.get("comment").asText() : "Codeforces API error");
        }
        return root;
    }
}
