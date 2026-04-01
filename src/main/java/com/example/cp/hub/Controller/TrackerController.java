package com.example.cp.hub.Controller;

import com.example.cp.hub.DTO.StatsResponse;
import com.example.cp.hub.Service.TrackerService;
import com.example.cp.hub.model.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tracker")
public class TrackerController {

    @Autowired
    private TrackerService trackerService;

    /**
     * GET /tracker/stats
     * Returns cached stats for all platforms the logged-in user has handles set for.
     */
    @GetMapping("/stats")
    public List<StatsResponse> getMyStats() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return trackerService.getAllStats(email);
    }

    /**
     * POST /tracker/refresh/{platform}
     * Force-refreshes stats for a specific platform, bypassing the cache.
     * e.g. POST /tracker/refresh/CODEFORCES
     */
    @PostMapping("/refresh/{platform}")
    public StatsResponse refreshStats(@PathVariable Platform platform) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return trackerService.refreshStats(email, platform);
    }
}
