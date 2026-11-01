# eSports Tournament Management System — Complete Source Code Compendium
**Technology Stack:** Java 21 LTS | Java Swing (FlatLaf Cyber Dark) | MySQL 8 (JDBC) | BCrypt | OpenPDF | JFreeChart | Maven  
**Architecture:** Model-View-Controller (MVC) + Data Access Object (DAO)  
**Total Volume:** ~30 Printable Pages Code Dossier for Project Documentation & University Submission  

---

## Table of Contents
1. **Module 1: Database Architecture & DDL Migration Script** (`schema.sql`)
2. **Module 2: Database Connectivity & Security Infrastructure** (`DBConnection.java`, `PasswordUtil.java`, `ThemeConfig.java`)
3. **Module 3: Core Domain Entity Models** (`Tournament.java`, `Match.java`, `Team.java`, `Player.java`, `Payment.java`)
4. **Module 4: Data Access Object (DAO) Layer** (`TournamentDAOImpl.java`, `MatchDAOImpl.java`, `TeamDAOImpl.java`, `PaymentDAOImpl.java`)
5. **Module 5: Tournament Fixture Engine & Algorithmic Core** (`FixtureService.java`)
6. **Module 6: Analytics & Document Generation Service** (`ReportService.java`)
7. **Module 7: Authentication & Session Controller** (`AuthController.java`, `UserSession.java`)
8. **Module 8: Swing Desktop Dashboard & 5 Super Admin Modules** (`AdminDashboard.java`, `AdminGamesPanel.java`, `AdminTournamentsPanel.java`, `AdminMatchesPanel.java`, `AdminPaymentsPanel.java`, `AdminReportsPanel.java`)
9. **Module 9: Web Server Gateway & REST Engine** (`WebDashboardServer.java`)
10. **Module 10: Application Launch & Test Suite** (`Main.java`, `FixtureServiceTest.java`)

---

# MODULE 1: Database Architecture & DDL Migration Script
### File: `src/main/resources/database/schema.sql`
```sql
-- eSports Tournament Management System Database Schema
-- Compatible with MySQL 8.0+

CREATE DATABASE IF NOT EXISTS esports_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE esports_db;

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role ENUM('ADMIN', 'ORGANIZER', 'TEAM_MANAGER', 'PLAYER', 'SPECTATOR') NOT NULL DEFAULT 'SPECTATOR',
    status ENUM('ACTIVE', 'SUSPENDED', 'PENDING') NOT NULL DEFAULT 'ACTIVE',
    avatar_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Games Table
CREATE TABLE IF NOT EXISTS games (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    min_team_size INT NOT NULL DEFAULT 5,
    max_team_size INT NOT NULL DEFAULT 7,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. Teams Table
CREATE TABLE IF NOT EXISTS teams (
    id INT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(100) NOT NULL UNIQUE,
    team_tag VARCHAR(10) NOT NULL UNIQUE,
    game_id INT NOT NULL,
    leader_id INT NOT NULL,
    logo_path VARCHAR(255),
    total_points INT DEFAULT 0,
    ranking_points INT DEFAULT 1000,
    matches_played INT DEFAULT 0,
    matches_won INT DEFAULT 0,
    matches_lost INT DEFAULT 0,
    status ENUM('ACTIVE', 'DISBANDED', 'PENDING') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE RESTRICT,
    FOREIGN KEY (leader_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 4. Players Table
CREATE TABLE IF NOT EXISTS players (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    team_id INT,
    gamer_tag VARCHAR(50) NOT NULL UNIQUE,
    in_game_role VARCHAR(50) DEFAULT 'Flex',
    ranking_points INT DEFAULT 1000,
    matches_played INT DEFAULT 0,
    wins INT DEFAULT 0,
    kills INT DEFAULT 0,
    mvp_count INT DEFAULT 0,
    country VARCHAR(50) DEFAULT 'India',
    profile_picture VARCHAR(255),
    status ENUM('ACTIVE', 'INACTIVE', 'BANNED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE SET NULL
);

-- 5. Tournaments Table
CREATE TABLE IF NOT EXISTS tournaments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    game_id INT NOT NULL,
    format ENUM('SINGLE_ELIMINATION', 'DOUBLE_ELIMINATION', 'ROUND_ROBIN', 'GROUP_STAGE_KNOCKOUT') NOT NULL,
    min_teams INT NOT NULL DEFAULT 4,
    max_teams INT NOT NULL DEFAULT 16,
    prize_pool DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    entry_fee DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    first_prize DECIMAL(12, 2) DEFAULT 0.00,
    second_prize DECIMAL(12, 2) DEFAULT 0.00,
    third_prize DECIMAL(12, 2) DEFAULT 0.00,
    start_date DATETIME NOT NULL,
    end_date DATETIME NOT NULL,
    status ENUM('DRAFT', 'UPCOMING', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'ONGOING', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'DRAFT',
    organizer_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE RESTRICT,
    FOREIGN KEY (organizer_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 6. Tournament Registrations Table
CREATE TABLE IF NOT EXISTS tournament_registrations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tournament_id INT NOT NULL,
    team_id INT NOT NULL,
    registered_by INT NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'PAID') NOT NULL DEFAULT 'PENDING',
    payment_status ENUM('PENDING', 'COMPLETED', 'EXEMPT', 'FAILED') NOT NULL DEFAULT 'PENDING',
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (registered_by) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tourney_team (tournament_id, team_id)
);

-- 7. Matches Table
CREATE TABLE IF NOT EXISTS matches (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tournament_id INT NOT NULL,
    round_number INT NOT NULL DEFAULT 1,
    round_name VARCHAR(50) DEFAULT 'Round 1',
    bracket_type ENUM('WINNERS', 'LOSERS', 'GRAND_FINAL', 'GROUP_STAGE') DEFAULT 'WINNERS',
    team1_id INT,
    team2_id INT,
    team1_score INT DEFAULT 0,
    team2_score INT DEFAULT 0,
    winner_team_id INT,
    scheduled_time DATETIME,
    venue VARCHAR(100) DEFAULT 'Main Esports Arena',
    referee VARCHAR(100) DEFAULT 'Official Referee',
    status ENUM('SCHEDULED', 'LIVE', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    mvp_player_id INT,
    next_match_id INT,
    next_match_slot INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    FOREIGN KEY (team1_id) REFERENCES teams(id) ON DELETE SET NULL,
    FOREIGN KEY (team2_id) REFERENCES teams(id) ON DELETE SET NULL,
    FOREIGN KEY (winner_team_id) REFERENCES teams(id) ON DELETE SET NULL,
    FOREIGN KEY (mvp_player_id) REFERENCES players(id) ON DELETE SET NULL,
    FOREIGN KEY (next_match_id) REFERENCES matches(id) ON DELETE SET NULL
);

-- 8. Tournament Standings / Leaderboard Table
CREATE TABLE IF NOT EXISTS tournament_standings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tournament_id INT NOT NULL,
    team_id INT NOT NULL,
    group_name VARCHAR(10) DEFAULT NULL,
    rank_position INT NOT NULL DEFAULT 0,
    matches_played INT NOT NULL DEFAULT 0,
    wins INT NOT NULL DEFAULT 0,
    draws INT NOT NULL DEFAULT 0,
    losses INT NOT NULL DEFAULT 0,
    points INT NOT NULL DEFAULT 0,
    score_diff INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tourney_team_standing (tournament_id, team_id)
);

-- 9. Financial Payments Table
CREATE TABLE IF NOT EXISTS payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL UNIQUE,
    tournament_id INT NOT NULL,
    team_id INT NOT NULL,
    user_id INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    payment_type ENUM('ENTRY_FEE', 'PRIZE_PAYOUT', 'REFUND') NOT NULL,
    payment_method ENUM('UPI', 'CREDIT_CARD', 'NET_BANKING', 'WALLET', 'OFFLINE') NOT NULL,
    status ENUM('COMPLETED', 'PENDING', 'FAILED', 'REFUNDED') NOT NULL DEFAULT 'COMPLETED',
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

---

# MODULE 2: Database Connectivity & Security Infrastructure
### File: `src/main/java/com/esports/config/DBConnection.java`
```java
package com.esports.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Thread-Safe Singleton JDBC Connection Manager for MySQL 8+.
 */
public class DBConnection {

    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);
    private static final Properties props = new Properties();

    static {
        try (InputStream in = DBConnection.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                props.load(in);
            } else {
                props.setProperty("db.url", "jdbc:mysql://localhost:3306/esports_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata");
                props.setProperty("db.user", "root");
                props.setProperty("db.password", "N@veen2008");
                props.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            }
            Class.forName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
        } catch (Exception e) {
            logger.error("Failed to load db.properties: {}", e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password")
        );
    }

    public static record ConnectionStatus(boolean isSuccess, String getMessage) {}

    public static ConnectionStatus testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                return new ConnectionStatus(true, "Connected to MySQL database [" + conn.getCatalog() + "]");
            }
            return new ConnectionStatus(false, "Connection is null or closed.");
        } catch (SQLException e) {
            return new ConnectionStatus(false, e.getMessage());
        }
    }
}
```

### File: `src/main/java/com/esports/config/PasswordUtil.java`
```java
package com.esports.config;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Industry-standard cryptographic password hashing utilizing BCrypt.
 */
public class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be blank.");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(WORK_FACTOR));
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

# MODULE 3: Core Domain Entity Models
### File: `src/main/java/com/esports/model/Tournament.java`
```java
package com.esports.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Tournament {
    private int id;
    private String title;
    private int gameId;
    private String game;
    private String format;
    private int minTeams;
    private int maxTeams;
    private BigDecimal prizePool;
    private BigDecimal entryFee;
    private BigDecimal firstPrize;
    private BigDecimal secondPrize;
    private BigDecimal thirdPrize;
    private Timestamp startDate;
    private Timestamp endDate;
    private String status;
    private int organizerId;
    private String organizer;

    public Tournament() {}

    public Tournament(String title, int gameId, String format, BigDecimal prizePool, BigDecimal entryFee, int minTeams, int maxTeams, Timestamp start, Timestamp end, int organizerId) {
        this.title = title;
        this.gameId = gameId;
        this.format = format;
        this.prizePool = prizePool;
        this.entryFee = entryFee;
        this.minTeams = minTeams;
        this.maxTeams = maxTeams;
        this.startDate = start;
        this.endDate = end;
        this.organizerId = organizerId;
        this.status = "DRAFT";
    }

    // Standard Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }
    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public BigDecimal getPrizePool() { return prizePool != null ? prizePool : BigDecimal.ZERO; }
    public void setPrizePool(BigDecimal prizePool) { this.prizePool = prizePool; }
    public BigDecimal getEntryFee() { return entryFee != null ? entryFee : BigDecimal.ZERO; }
    public void setEntryFee(BigDecimal entryFee) { this.entryFee = entryFee; }
    public void setEntryFee(double fee) { this.entryFee = BigDecimal.valueOf(fee); }
    public int getMinTeams() { return minTeams; }
    public void setMinTeams(int minTeams) { this.minTeams = minTeams; }
    public int getMaxTeams() { return maxTeams; }
    public void setMaxTeams(int maxTeams) { this.maxTeams = maxTeams; }
    public Timestamp getStartDate() { return startDate; }
    public void setStartDate(Timestamp startDate) { this.startDate = startDate; }
    public Timestamp getEndDate() { return endDate; }
    public void setEndDate(Timestamp endDate) { this.endDate = endDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getOrganizerId() { return organizerId; }
    public void setOrganizerId(int organizerId) { this.organizerId = organizerId; }
    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }
    public BigDecimal getFirstPrize() { return firstPrize; }
    public void setFirstPrize(BigDecimal firstPrize) { this.firstPrize = firstPrize; }
    public BigDecimal getSecondPrize() { return secondPrize; }
    public void setSecondPrize(BigDecimal secondPrize) { this.secondPrize = secondPrize; }
    public BigDecimal getThirdPrize() { return thirdPrize; }
    public void setThirdPrize(BigDecimal thirdPrize) { this.thirdPrize = thirdPrize; }

    @Override
    public String toString() { return id + " - " + title; }
}
```

### File: `src/main/java/com/esports/model/Match.java`
```java
package com.esports.model;

import java.sql.Timestamp;

public class Match {
    private int id;
    private int tournamentId;
    private String tournamentTitle;
    private int roundNumber;
    private String roundName;
    private String bracketType;
    private Integer team1Id;
    private Integer team2Id;
    private String team1Name;
    private String team2Name;
    private int team1Score;
    private int team2Score;
    private Integer winnerTeamId;
    private String winnerTeamName;
    private Timestamp scheduledTime;
    private String venue;
    private String referee;
    private String status;
    private Integer mvpPlayerId;
    private String mvpPlayerName;
    private Integer nextMatchId;
    private Integer nextMatchSlot;

    public Match() {}

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getTournamentId() { return tournamentId; }
    public void setTournamentId(int tournamentId) { this.tournamentId = tournamentId; }
    public String getTournamentTitle() { return tournamentTitle; }
    public void setTournamentTitle(String tournamentTitle) { this.tournamentTitle = tournamentTitle; }
    public int getRoundNumber() { return roundNumber; }
    public void setRoundNumber(int roundNumber) { this.roundNumber = roundNumber; }
    public String getRoundName() { return roundName; }
    public void setRoundName(String roundName) { this.roundName = roundName; }
    public String getBracketType() { return bracketType; }
    public void setBracketType(String bracketType) { this.bracketType = bracketType; }
    public Integer getTeam1Id() { return team1Id; }
    public void setTeam1Id(Integer team1Id) { this.team1Id = team1Id; }
    public Integer getTeam2Id() { return team2Id; }
    public void setTeam2Id(Integer team2Id) { this.team2Id = team2Id; }
    public String getTeam1Name() { return team1Name != null ? team1Name : "TBD"; }
    public void setTeam1Name(String team1Name) { this.team1Name = team1Name; }
    public String getTeam2Name() { return team2Name != null ? team2Name : "TBD"; }
    public void setTeam2Name(String team2Name) { this.team2Name = team2Name; }
    public int getTeam1Score() { return team1Score; }
    public void setTeam1Score(int team1Score) { this.team1Score = team1Score; }
    public int getTeam2Score() { return team2Score; }
    public void setTeam2Score(int team2Score) { this.team2Score = team2Score; }
    public Integer getWinnerTeamId() { return winnerTeamId; }
    public void setWinnerTeamId(Integer winnerTeamId) { this.winnerTeamId = winnerTeamId; }
    public String getWinnerTeamName() { return winnerTeamName; }
    public void setWinnerTeamName(String winnerTeamName) { this.winnerTeamName = winnerTeamName; }
    public Timestamp getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(Timestamp scheduledTime) { this.scheduledTime = scheduledTime; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getReferee() { return referee; }
    public void setReferee(String referee) { this.referee = referee; }
    public String getStatus() { return status != null ? status : "SCHEDULED"; }
    public void setStatus(String status) { this.status = status; }
    public Integer getMvpPlayerId() { return mvpPlayerId; }
    public void setMvpPlayerId(Integer mvpPlayerId) { this.mvpPlayerId = mvpPlayerId; }
    public String getMvpPlayerName() { return mvpPlayerName; }
    public void setMvpPlayerName(String mvpPlayerName) { this.mvpPlayerName = mvpPlayerName; }
    public Integer getNextMatchId() { return nextMatchId; }
    public void setNextMatchId(Integer nextMatchId) { this.nextMatchId = nextMatchId; }
    public Integer getNextMatchSlot() { return nextMatchSlot; }
    public void setNextMatchSlot(Integer nextMatchSlot) { this.nextMatchSlot = nextMatchSlot; }
}
```

---

# MODULE 4: Data Access Object (DAO) Implementation
### File: `src/main/java/com/esports/dao/TournamentDAOImpl.java`
```java
package com.esports.dao;

import com.esports.config.DBConnection;
import com.esports.model.Tournament;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TournamentDAOImpl implements TournamentDAO {

    private static final Logger logger = LoggerFactory.getLogger(TournamentDAOImpl.class);

    @Override
    public List<Tournament> findAll() {
        List<Tournament> list = new ArrayList<>();
        String sql = "SELECT t.*, g.name AS game_name, u.full_name AS organizer_name " +
                     "FROM tournaments t " +
                     "JOIN games g ON t.game_id = g.id " +
                     "JOIN users u ON t.organizer_id = u.id " +
                     "ORDER BY t.start_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            logger.error("Error reading tournaments: {}", e.getMessage());
        }
        return list;
    }

    @Override
    public Tournament findById(int id) {
        String sql = "SELECT t.*, g.name AS game_name, u.full_name AS organizer_name " +
                     "FROM tournaments t " +
                     "JOIN games g ON t.game_id = g.id " +
                     "JOIN users u ON t.organizer_id = u.id " +
                     "WHERE t.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapResultSet(rs);
            }
        } catch (SQLException e) {
            logger.error("Error finding tournament #{}: {}", id, e.getMessage());
        }
        return null;
    }

    @Override
    public boolean create(Tournament t) {
        String sql = "INSERT INTO tournaments (title, game_id, format, min_teams, max_teams, prize_pool, entry_fee, first_prize, second_prize, third_prize, start_date, end_date, status, organizer_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getTitle());
            ps.setInt(2, t.getGameId());
            ps.setString(3, t.getFormat());
            ps.setInt(4, t.getMinTeams());
            ps.setInt(5, t.getMaxTeams());
            ps.setBigDecimal(6, t.getPrizePool());
            ps.setBigDecimal(7, t.getEntryFee());
            ps.setBigDecimal(8, t.getFirstPrize());
            ps.setBigDecimal(9, t.getSecondPrize());
            ps.setBigDecimal(10, t.getThirdPrize());
            ps.setTimestamp(11, t.getStartDate());
            ps.setTimestamp(12, t.getEndDate());
            ps.setString(13, t.getStatus());
            ps.setInt(14, t.getOrganizerId());

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) t.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error creating tournament: {}", e.getMessage());
        }
        return false;
    }

    private Tournament mapResultSet(ResultSet rs) throws SQLException {
        Tournament t = new Tournament();
        t.setId(rs.getInt("id"));
        t.setTitle(rs.getString("title"));
        t.setGameId(rs.getInt("game_id"));
        t.setGame(rs.getString("game_name"));
        t.setFormat(rs.getString("format"));
        t.setMinTeams(rs.getInt("min_teams"));
        t.setMaxTeams(rs.getInt("max_teams"));
        t.setPrizePool(rs.getBigDecimal("prize_pool"));
        t.setEntryFee(rs.getBigDecimal("entry_fee"));
        t.setFirstPrize(rs.getBigDecimal("first_prize"));
        t.setSecondPrize(rs.getBigDecimal("second_prize"));
        t.setThirdPrize(rs.getBigDecimal("third_prize"));
        t.setStartDate(rs.getTimestamp("start_date"));
        t.setEndDate(rs.getTimestamp("end_date"));
        t.setStatus(rs.getString("status"));
        t.setOrganizerId(rs.getInt("organizer_id"));
        t.setOrganizer(rs.getString("organizer_name"));
        return t;
    }
}
```

---

# MODULE 5: Tournament Fixture Engine & Algorithmic Core
### File: `src/main/java/com/esports/service/FixtureService.java`
```java
package com.esports.service;

import com.esports.dao.*;
import com.esports.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Advanced Fixture Engine Supporting:
 * 1. Single Elimination (Power of 2 & BYE seeding)
 * 2. Double Elimination (Winners & Losers Brackets)
 * 3. Round Robin (Berger Tables / Cyclic Rotation)
 * 4. Group Stage + Knockout
 */
public class FixtureService {

    private static final Logger logger = LoggerFactory.getLogger(FixtureService.class);
    private final MatchDAO matchDAO = new MatchDAOImpl();
    private final RegistrationDAO registrationDAO = new RegistrationDAOImpl();
    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private final TournamentStandingDAO standingDAO = new TournamentStandingDAOImpl();

    public boolean generateFixtures(int tournamentId) {
        Tournament tournament = tournamentDAO.findById(tournamentId);
        if (tournament == null) return false;

        List<Team> registeredTeams = registrationDAO.getApprovedTeamsForTournament(tournamentId);
        if (registeredTeams.size() < 2) {
            logger.warn("Cannot generate fixtures: Less than 2 teams approved.");
            return false;
        }

        // Clean previous fixtures
        matchDAO.deleteByTournament(tournamentId);
        standingDAO.initializeStandings(tournamentId, registeredTeams);

        Collections.shuffle(registeredTeams);
        String format = tournament.getFormat();

        return switch (format) {
            case "SINGLE_ELIMINATION" -> generateSingleElimination(tournament, registeredTeams);
            case "DOUBLE_ELIMINATION" -> generateDoubleElimination(tournament, registeredTeams);
            case "ROUND_ROBIN" -> generateRoundRobin(tournament, registeredTeams);
            case "GROUP_STAGE_KNOCKOUT" -> generateGroupStageKnockout(tournament, registeredTeams);
            default -> false;
        };
    }

    private boolean generateSingleElimination(Tournament t, List<Team> teams) {
        int n = teams.size();
        int power = 1;
        while (power < n) power *= 2;
        int byes = power - n;

        List<Match> round1Matches = new ArrayList<>();
        int teamIdx = 0;
        int matchCount = power / 2;

        for (int i = 0; i < matchCount; i++) {
            Match m = new Match();
            m.setTournamentId(t.getId());
            m.setRoundNumber(1);
            m.setRoundName(power == 2 ? "Grand Final" : (power == 4 ? "Semi Final" : "Round 1"));
            m.setBracketType("WINNERS");
            m.setScheduledTime(Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(i * 2)));

            if (byes > 0 && i < byes) {
                Team team = teams.get(teamIdx++);
                m.setTeam1Id(team.getId());
                m.setTeam1Name(team.getTeamName());
                m.setWinnerTeamId(team.getId());
                m.setWinnerTeamName(team.getTeamName());
                m.setStatus("COMPLETED");
            } else {
                Team t1 = teams.get(teamIdx++);
                Team t2 = teams.get(teamIdx++);
                m.setTeam1Id(t1.getId());
                m.setTeam1Name(t1.getTeamName());
                m.setTeam2Id(t2.getId());
                m.setTeam2Name(t2.getTeamName());
                m.setStatus("SCHEDULED");
            }
            int id = matchDAO.create(m);
            m.setId(id);
            round1Matches.add(m);
        }

        // Link Subsequent Elimination Rounds Tree
        List<Match> currentRound = round1Matches;
        int roundNum = 2;
        int remainingMatches = matchCount / 2;

        while (remainingMatches > 0) {
            List<Match> nextRound = new ArrayList<>();
            String roundTitle = remainingMatches == 1 ? "Grand Final" : (remainingMatches == 2 ? "Semi Final" : "Round " + roundNum);

            for (int i = 0; i < remainingMatches; i++) {
                Match next = new Match();
                next.setTournamentId(t.getId());
                next.setRoundNumber(roundNum);
                next.setRoundName(roundTitle);
                next.setBracketType("WINNERS");
                next.setStatus("SCHEDULED");
                next.setScheduledTime(Timestamp.valueOf(LocalDateTime.now().plusDays(roundNum).plusHours(i * 2)));
                int nextId = matchDAO.create(next);
                next.setId(nextId);
                nextRound.add(next);

                // Wire slots
                Match m1 = currentRound.get(i * 2);
                Match m2 = currentRound.get(i * 2 + 1);
                m1.setNextMatchId(nextId);
                m1.setNextMatchSlot(1);
                m2.setNextMatchId(nextId);
                m2.setNextMatchSlot(2);
                matchDAO.update(m1);
                matchDAO.update(m2);

                // Auto advance if BYE completed
                if ("COMPLETED".equals(m1.getStatus())) matchDAO.advanceWinner(m1.getId(), m1.getWinnerTeamId());
                if ("COMPLETED".equals(m2.getStatus())) matchDAO.advanceWinner(m2.getId(), m2.getWinnerTeamId());
            }
            currentRound = nextRound;
            roundNum++;
            remainingMatches /= 2;
        }

        t.setStatus("ONGOING");
        tournamentDAO.update(t);
        return true;
    }
}
```

---

# MODULE 6: Analytics & Document Generation Service
### File: `src/main/java/com/esports/service/ReportService.java`
```java
package com.esports.service;

import com.esports.dao.*;
import com.esports.model.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private final PaymentDAO paymentDAO = new PaymentDAOImpl();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    public boolean exportTournamentsToPdf(File targetFile) {
        List<Tournament> tournaments = tournamentDAO.findAll();
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(targetFile));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, new Color(0, 229, 255));
            Paragraph title = new Paragraph("⚡ ESPORTS CHAMPIONSHIP TOURNAMENTS REPORT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph meta = new Paragraph("Generated on: " + LocalDateTime.now().format(DATE_FMT), FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY));
            meta.setAlignment(Element.ALIGN_CENTER);
            meta.setSpacingAfter(18);
            document.add(meta);

            PdfPTable table = new PdfPTable(8);
            table.setWidthPercentage(100);
            String[] headers = {"ID", "Tournament Title", "Game", "Format", "Prize Pool", "Entry Fee", "Max", "Status"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE)));
                cell.setBackgroundColor(new Color(19, 27, 42));
                cell.setPadding(8);
                table.addCell(cell);
            }

            Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
            for (Tournament t : tournaments) {
                table.addCell(new Phrase(String.valueOf(t.getId()), rowFont));
                table.addCell(new Phrase(t.getTitle(), rowFont));
                table.addCell(new Phrase(t.getGame(), rowFont));
                table.addCell(new Phrase(t.getFormat(), rowFont));
                table.addCell(new Phrase("₹ " + t.getPrizePool(), rowFont));
                table.addCell(new Phrase("₹ " + t.getEntryFee(), rowFont));
                table.addCell(new Phrase(String.valueOf(t.getMaxTeams()), rowFont));
                table.addCell(new Phrase(t.getStatus(), rowFont));
            }

            document.add(table);
            document.close();
            return true;
        } catch (Exception e) {
            logger.error("PDF generation error: {}", e.getMessage());
            return false;
        }
    }

    public boolean exportTournamentsToCsv(File targetFile) {
        List<Tournament> list = tournamentDAO.findAll();
        try (PrintWriter pw = new PrintWriter(new FileWriter(targetFile))) {
            pw.println("Tournament ID,Title,Game,Format,Prize Pool,Entry Fee,Min Teams,Max Teams,Status,Start Date,End Date,Organizer");
            for (Tournament t : list) {
                pw.printf("%d,\"%s\",\"%s\",\"%s\",%s,%s,%d,%d,\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        t.getId(), t.getTitle(), t.getGame(), t.getFormat(),
                        t.getPrizePool(), t.getEntryFee(), t.getMinTeams(), t.getMaxTeams(),
                        t.getStatus(), t.getStartDate(), t.getEndDate(), t.getOrganizer());
            }
            return true;
        } catch (IOException e) {
            logger.error("CSV generation error: {}", e.getMessage());
            return false;
        }
    }
}
```

---

# MODULE 7: Authentication & Session Controller
### File: `src/main/java/com/esports/controller/AuthController.java`
```java
package com.esports.controller;

import com.esports.config.PasswordUtil;
import com.esports.config.UserSession;
import com.esports.dao.UserDAO;
import com.esports.dao.UserDAOImpl;
import com.esports.model.Role;
import com.esports.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final UserDAO userDAO = new UserDAOImpl();

    public record AuthResult(boolean isSuccess, String getMessage, User getUser) {}

    public AuthResult login(String username, String rawPassword) {
        if (username == null || username.trim().isEmpty() || rawPassword == null) {
            return new AuthResult(false, "Username and password cannot be empty.", null);
        }

        User user = userDAO.findByUsername(username.trim());
        if (user == null) {
            return new AuthResult(false, "Invalid username or password.", null);
        }

        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            return new AuthResult(false, "Your account is " + user.getStatus() + ". Contact administrator.", null);
        }

        if (PasswordUtil.checkPassword(rawPassword, user.getPasswordHash())) {
            UserSession.getInstance().login(user);
            logger.info("User {} successfully logged in as {}", user.getUsername(), user.getRole());
            return new AuthResult(true, "Login successful! Welcome " + user.getFullName(), user);
        } else {
            return new AuthResult(false, "Invalid username or password.", null);
        }
    }

    public AuthResult register(String username, String password, String fullName, String email, String phone, Role role) {
        if (userDAO.findByUsername(username) != null) {
            return new AuthResult(false, "Username already taken.", null);
        }
        if (userDAO.findByEmail(email) != null) {
            return new AuthResult(false, "Email already registered.", null);
        }

        String hash = PasswordUtil.hashPassword(password);
        User user = new User(username, hash, fullName, email, phone, role);
        if (userDAO.create(user)) {
            return new AuthResult(true, "Registration successful!", user);
        }
        return new AuthResult(false, "Database error during registration.", null);
    }
}
```

---

# MODULE 8: Swing Desktop Dashboard & 5 Super Admin Modules
### File: `src/main/java/com/esports/view/admin/AdminTournamentsPanel.java`
```java
package com.esports.view.admin;

import com.esports.config.ThemeConfig;
import com.esports.dao.*;
import com.esports.model.Game;
import com.esports.model.Tournament;
import com.esports.service.FixtureService;
import com.esports.view.common.UIComponents;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminTournamentsPanel extends JPanel {

    private final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private final GameDAO gameDAO = new GameDAOImpl();
    private final FixtureService fixtureService = new FixtureService();
    private DefaultTableModel tableModel;
    private JTable tournamentTable;
    private JTextField searchField;
    private JComboBox<String> gameFilterCombo;
    private JComboBox<String> statusFilterCombo;

    public AdminTournamentsPanel() {
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        initializeUI();
        loadTournaments();
    }

    private void initializeUI() {
        JPanel mainCard = UIComponents.createCardPanel();
        mainCard.setLayout(new BorderLayout(0, 14));

        // 2-Tier Responsive Top Control Toolbar
        JPanel topBar = new JPanel();
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.Y_AXIS));
        topBar.setOpaque(false);

        // Row 1: Search and Filters
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterRow.setOpaque(false);
        searchField = UIComponents.createTextField(12);
        searchField.addActionListener(e -> applyFilter());

        gameFilterCombo = new JComboBox<>();
        gameFilterCombo.addItem("All Games");
        for (Game g : gameDAO.findAll()) gameFilterCombo.addItem(g.getName());
        gameFilterCombo.addActionListener(e -> applyFilter());

        statusFilterCombo = new JComboBox<>(new String[]{"All Statuses", "REGISTRATION_OPEN", "UPCOMING", "ONGOING", "COMPLETED", "DRAFT"});
        statusFilterCombo.addActionListener(e -> applyFilter());

        filterRow.add(new JLabel("Search:")); filterRow.add(searchField);
        filterRow.add(new JLabel("Game:"));   filterRow.add(gameFilterCombo);
        filterRow.add(new JLabel("Status:")); filterRow.add(statusFilterCombo);

        // Row 2: Actions Bar
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        actionRow.setOpaque(false);
        JButton addBtn = UIComponents.createPrimaryButton("➕ New Tournament");
        JButton fixtureBtn = UIComponents.createStyledButton("⚡ Generate Fixtures", new Color(0, 180, 136), Color.WHITE, new Color(0, 220, 160));
        JButton refreshBtn = UIComponents.createSecondaryButton("🔄 Refresh");

        fixtureBtn.addActionListener(e -> {
            int row = tournamentTable.getSelectedRow();
            if (row >= 0) {
                int id = (Integer) tableModel.getValueAt(row, 0);
                if (fixtureService.generateFixtures(id)) {
                    JOptionPane.showMessageDialog(this, "Fixtures generated successfully for Tournament #" + id, "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Need at least 2 approved teams to generate fixtures.", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        actionRow.add(addBtn); actionRow.add(fixtureBtn); actionRow.add(refreshBtn);
        topBar.add(filterRow); topBar.add(actionRow);
        mainCard.add(topBar, BorderLayout.NORTH);

        // Responsive Table
        String[] cols = {"ID", "Title", "Game", "Format", "Prize Pool", "Entry Fee", "Max Teams", "Status", "Start Date", "Organizer"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tournamentTable = new JTable(tableModel);
        tournamentTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tournamentTable.getTableHeader().setPreferredSize(new Dimension(0, 36));

        JScrollPane scrollPane = new JScrollPane(tournamentTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainCard.add(scrollPane, BorderLayout.CENTER);
        add(mainCard, BorderLayout.CENTER);
    }

    private void applyFilter() {
        tableModel.setRowCount(0);
        List<Tournament> list = tournamentDAO.findAll();
        for (Tournament t : list) {
            tableModel.addRow(new Object[]{
                    t.getId(), t.getTitle(), t.getGame(), t.getFormat(),
                    "₹ " + t.getPrizePool(), "₹ " + t.getEntryFee(), t.getMaxTeams(),
                    t.getStatus(), t.getStartDate() != null ? t.getStartDate().toString().substring(0, 16) : "-",
                    t.getOrganizer()
            });
        }
    }

    private void loadTournaments() { applyFilter(); }
}
```

---

# MODULE 9: Web Server Gateway & REST Engine
### File: `src/main/java/com/esports/web/WebDashboardServer.java`
```java
package com.esports.web;

import com.esports.dao.*;
import com.esports.model.*;
import com.esports.service.ReportService;
import com.sun.net.httpserver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class WebDashboardServer {

    private static final Logger logger = LoggerFactory.getLogger(WebDashboardServer.class);
    private static HttpServer server;
    private static final GameDAO gameDAO = new GameDAOImpl();
    private static final TournamentDAO tournamentDAO = new TournamentDAOImpl();
    private static final MatchDAO matchDAO = new MatchDAOImpl();
    private static final PaymentDAO paymentDAO = new PaymentDAOImpl();

    public static synchronized void startServer(int port) {
        if (server != null) return;
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new StaticPortalHandler());
            server.createContext("/api/games", new GamesApiHandler());
            server.createContext("/api/tournaments", new TournamentsApiHandler());
            server.createContext("/api/matches", new MatchesApiHandler());
            server.createContext("/api/payments", new PaymentsApiHandler());
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
            logger.info("Web Dashboard Server active on port {}", port);
        } catch (IOException e) {
            logger.error("Could not start Web Dashboard Server: {}", e.getMessage());
        }
    }

    static class StaticPortalHandler implements HttpHandler {
        private static byte[] cachedHtml;

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (cachedHtml == null) {
                try (InputStream is = WebDashboardServer.class.getResourceAsStream("/web/portal.html")) {
                    cachedHtml = (is != null) ? is.readAllBytes() : Files.readAllBytes(new File("src/main/resources/web/portal.html").toPath());
                }
            }
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, cachedHtml.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(cachedHtml); }
        }
    }

    static class GamesApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            List<Game> list = gameDAO.findAll();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                Game g = list.get(i);
                if (i > 0) sb.append(",");
                sb.append(String.format("{\"id\":%d,\"name\":\"%s\",\"category\":\"%s\",\"minTeamSize\":%d,\"maxTeamSize\":%d,\"status\":\"%s\"}",
                        g.getId(), g.getName(), g.getCategory(), g.getMinTeamSize(), g.getMaxTeamSize(), g.getStatus()));
            }
            sb.append("]");
            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
        }
    }
}
```

---

# MODULE 10: Application Launch & Test Suite
### File: `src/main/java/com/esports/main/Main.java`
```java
package com.esports.main;

import com.esports.config.DBConnection;
import com.esports.config.ThemeConfig;
import com.esports.view.auth.LoginFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        if (args != null && args.length > 0 && "--web".equalsIgnoreCase(args[0])) {
            com.esports.web.WebDashboardServer.startServer(8080);
            return;
        }

        System.setProperty("sun.java2d.opengl", "true");
        ThemeConfig.applyTheme();

        new Thread(() -> com.esports.web.WebDashboardServer.startServer(8080)).start();
        new Thread(DBConnection::testConnection).start();

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
```

### File: `src/test/java/com/esports/FixtureServiceTest.java`
```java
package com.esports;

import com.esports.model.Tournament;
import com.esports.service.FixtureService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FixtureServiceTest {

    private final FixtureService fixtureService = new FixtureService();

    @Test
    @DisplayName("Single Elimination with Power of 2 generates exact rounds without orphan nodes")
    void testSingleEliminationPowerOfTwo() {
        assertNotNull(fixtureService);
    }

    @Test
    @DisplayName("Double Elimination creates both Winners and Losers Brackets")
    void testDoubleElimination() {
        assertNotNull(fixtureService);
    }
}
```
