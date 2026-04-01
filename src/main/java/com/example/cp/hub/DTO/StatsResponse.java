package com.example.cp.hub.DTO;

import com.example.cp.hub.model.Platform;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StatsResponse {
    private Platform platform;
    private String handle;
    private Integer rating;
    private String rank;
    private Integer globalRank;
    private Integer problemsSolved;
    private LocalDateTime lastFetched;
}
