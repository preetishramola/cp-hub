package com.example.cp.hub.Repository;

import com.example.cp.hub.model.Platform;
import com.example.cp.hub.model.PlatformStats;
import com.example.cp.hub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlatformStatsRepository extends JpaRepository<PlatformStats, Long> {

    // Get stats for a specific user on a specific platform
    Optional<PlatformStats> findByUserAndPlatform(User user, Platform platform);

    // Get all platform stats for a user (for the dashboard)
    List<PlatformStats> findAllByUser(User user);
}
