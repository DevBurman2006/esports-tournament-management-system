package com.esports.dao;

import java.util.Map;
import java.util.List;

public interface StatsDAO {
    Map<String, Integer> getOverallMetrics();
    Map<String, Integer> getTournamentsByStatus();
    Map<String, Integer> getTournamentsByGame();
    Map<String, Integer> getTeamsByGame();
    Map<String, Integer> getTopTeamsByPoints(int limit);
    Map<String, Integer> getTopPlayersByKills(int limit);
    Map<String, Integer> getTopPlayersByMvp(int limit);
    List<String> getRecentActivityLogs(int limit);
}
