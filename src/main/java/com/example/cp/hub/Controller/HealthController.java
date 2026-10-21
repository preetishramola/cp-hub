package com.example.cp.hub.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    /**
     * GET /health
     * Lightweight uptime check for monitors like UptimeRobot.
     * Doesn't touch the database, so pinging it keeps the server
     * awake without keeping the database awake.
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }
}
