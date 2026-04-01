package com.example.cp.hub.tracker;

import com.example.cp.hub.DTO.StatsResponse;
import com.example.cp.hub.model.Platform;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * CodeChef has no official public API, so we scrape the public profile page.
 * This is a best-effort approach — if CodeChef changes their HTML structure,
 * this will need to be updated.
 *
 * NOTE: Add Jsoup to pom.xml:
 *   <dependency>
 *     <groupId>org.jsoup</groupId>
 *     <artifactId>jsoup</artifactId>
 *     <version>1.17.2</version>
 *   </dependency>
 */
@Service
public class CodeChefService {

    public StatsResponse fetchStats(String handle) {
        try {
            String url = "https://www.codechef.com/users/" + handle;

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (compatible; cp-hub-bot/1.0)")
                    .timeout(10_000)
                    .get();

            // Rating is inside <div class="rating-number">
            int rating = 0;
            Element ratingEl = doc.selectFirst(".rating-number");
            if (ratingEl != null) {
                rating = Integer.parseInt(ratingEl.text().trim());
            }

            // Stars/rank title is inside <div class="rating-star"> or <span class="rating">
            String rank = null;
            Element rankEl = doc.selectFirst(".rating-ranks strong");
            if (rankEl != null) {
                rank = rankEl.text().trim();
            }

            // Global rank: inside <div class="rating-ranks"> — first <strong> is global, second is country
            int globalRank = 0;
            Element globalRankEl = doc.selectFirst(".rating-ranks ul li:first-child strong");
            if (globalRankEl != null) {
                String rankText = globalRankEl.text().replaceAll("[^0-9]", "");
                if (!rankText.isEmpty()) globalRank = Integer.parseInt(rankText);
            }

            // Problems solved: inside <section class="rating-data-section problems-solved"> h3
            int problemsSolved = 0;
            Element solvedEl = doc.selectFirst(".problems-solved h3");
            if (solvedEl != null) {
                // Text looks like "Total Problems Solved: 123"
                String text = solvedEl.text().replaceAll("[^0-9]", "");
                if (!text.isEmpty()) problemsSolved = Integer.parseInt(text);
            }

            return new StatsResponse(
                    Platform.CODECHEF,
                    handle,
                    rating,
                    rank,
                    globalRank == 0 ? null : globalRank,
                    problemsSolved,
                    LocalDateTime.now()
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch CodeChef stats for handle: " + handle + " — " + e.getMessage());
        }
    }
}
