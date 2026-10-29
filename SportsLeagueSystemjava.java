package sportsleague;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;


class LeagueException extends Exception {
    LeagueException(String message) {
        super(message);
    }
}

class InvalidInputException extends LeagueException {
    InvalidInputException(String message) {
        super(message);
    }
}

class TeamLimitException extends LeagueException {
    TeamLimitException(String message) {
        super(message);
    }
}

class SchedulingConflictException extends LeagueException {
    SchedulingConflictException(String message) {
        super(message);
    }
}

abstract class Person {
    private int id;
    private String name;
    private int age;

    Person(int id, String name, int age) throws InvalidInputException {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Name cannot be empty.");
        }
        if (age < 15 || age > 60) {
            throw new InvalidInputException("Age must be between 15 and 60.");
        }
        this.id = id;
        this.name = name.trim();
        this.age = age;
    }

    int getId()      { return id; }
    String getName() { return name; }
    int getAge()     { return age; }

    abstract String getRole();

    void display() {
        System.out.printf("%-8s ID: %-4d Name: %-15s Age: %d%n",
                getRole(), id, name, age);
    }
}

class Player extends Person {
    private String position;
    private int goals;

    Player(int id, String name, int age, String position) throws InvalidInputException {
        super(id, name, age);
        if (position == null || position.trim().isEmpty()) {
            throw new InvalidInputException("Position cannot be empty.");
        }
        this.position = position.trim();
    }

    String getPosition() { return position; }
    int getGoals()       { return goals; }

    @Override
    String getRole() {
        return "Player";
    }

    @Override
    void display() {
        System.out.printf("%-8s ID: %-4d Name: %-15s Age: %-3d Position: %s%n",
                getRole(), getId(), getName(), getAge(), position);
    }
}

class Coach extends Person {

    Coach(int id, String name, int age) throws InvalidInputException {
        super(id, name, age);
    }

    @Override
    String getRole() {
        return "Coach";
    }
}


class Team {
    static final int MIN_PLAYERS = 3;
    static final int MAX_PLAYERS = 11;

    private int teamId;
    private String name;
    private Coach coach;
    private ArrayList<Player> players = new ArrayList<>();

    private int played, won, drawn, lost;
    private int goalsFor, goalsAgainst, points;

    Team(int teamId, String name, Coach coach) {
        this.teamId = teamId;
        this.name = name;
        this.coach = coach;
    }

    int getTeamId()               { return teamId; }
    String getName()              { return name; }
    int getPoints()               { return points; }
    int getPlayerCount()          { return players.size(); }
    int getGoalDifference()       { return goalsFor - goalsAgainst; }
    int getGoalsFor()             { return goalsFor; }

    void addPlayer(Player p) throws TeamLimitException {
        if (players.size() >= MAX_PLAYERS) {
            throw new TeamLimitException(name + " already has " + MAX_PLAYERS + " players.");
        }
        players.add(p);
    }

    boolean isReadyToPlay() {
        return players.size() >= MIN_PLAYERS;
    }

    // Called by Match after a result is recorded
    void updateStats(int scored, int conceded, int pointsEarned) {
        played++;
        goalsFor += scored;
        goalsAgainst += conceded;
        points += pointsEarned;
        if (scored > conceded) {
            won++;
        } else if (scored == conceded) {
            drawn++;
        } else {
            lost++;
        }
    }

    void displayTableRow(int rank) {
        System.out.printf("%-4d%-16s%-4d%-4d%-4d%-4d%-5d%-5d%-5d%-4d%n",
                rank, name, played, won, drawn, lost,
                goalsFor, goalsAgainst, getGoalDifference(), points);
    }

    void display() {
        System.out.println("\n--- TEAM DETAILS ---");
        System.out.println("Team ID: " + teamId);
        System.out.println("Team Name: " + name);
        System.out.println("Coach: " + coach.getName());
        System.out.println("Players (" + players.size() + "/" + MAX_PLAYERS + "):");
        if (players.isEmpty()) {
            System.out.println("  No players registered yet.");
        }
        for (Player p : players) {
            System.out.print("  ");
            p.display();
        }
    }
}


abstract class Match {
    private int matchId;
    private Team home;
    private Team away;
    private LocalDate date;
    private String venue;
    private boolean played;
    private int homeScore, awayScore;

    Match(Team home, Team away, LocalDate date, String venue) {
        this.home = home;
        this.away = away;
        this.date = date;
        this.venue = venue;
    }

    int getMatchId()    { return matchId; }
    void setMatchId(int id) { this.matchId = id; }
    Team getHome()      { return home; }
    Team getAway()      { return away; }
    LocalDate getDate() { return date; }
    String getVenue()   { return venue; }
    boolean isPlayed()  { return played; }

    abstract String getMatchType();
    abstract int pointsForWin();
    abstract int pointsForDraw();
    abstract boolean allowsDraw();

    boolean involves(Team t) {
        return home == t || away == t;
    }

    void recordResult(int h, int a) throws LeagueException {
        if (played) {
            throw new LeagueException("Result already recorded for match " + matchId + ".");
        }
        if (h < 0 || a < 0) {
            throw new InvalidInputException("Scores cannot be negative.");
        }
        if (h == a && !allowsDraw()) {
            throw new InvalidInputException(getMatchType() + " matches cannot end in a draw.");
        }
        homeScore = h;
        awayScore = a;
        played = true;

        int homePts = h > a ? pointsForWin() : (h == a ? pointsForDraw() : 0);
        int awayPts = a > h ? pointsForWin() : (h == a ? pointsForDraw() : 0);
        home.updateStats(h, a, homePts);
        away.updateStats(a, h, awayPts);

        System.out.println("\n--- MATCH RESULT RECEIPT ---");
        System.out.println("Match ID: " + matchId + " (" + getMatchType() + ")");
        System.out.println(home.getName() + " " + h + " - " + a + " " + away.getName());
        if (h == a) {
            System.out.println("Result: DRAW");
        } else {
            System.out.println("Winner: " + (h > a ? home.getName() : away.getName()));
        }
        System.out.println("Points awarded: " + home.getName() + " +" + homePts
                + ", " + away.getName() + " +" + awayPts);
        System.out.println("Status: SUCCESS");
    }

    void displayFixture() {
        String result = played ? (homeScore + " - " + awayScore) : "Not played";
        System.out.printf("%-6d%-10s%-12s%-15s vs %-15s%-18s%s%n",
                matchId, getMatchType(), date, home.getName(),
                away.getName(), venue, result);
    }
}

class LeagueMatch extends Match {
    LeagueMatch(Team home, Team away, LocalDate date, String venue) {
        super(home, away, date, venue);
    }

    @Override String getMatchType() { return "League"; }
    @Override int pointsForWin()    { return 3; }
    @Override int pointsForDraw()   { return 1; }
    @Override boolean allowsDraw()  { return true; }
}

class KnockoutMatch extends Match {
    KnockoutMatch(Team home, Team away, LocalDate date, String venue) {
        super(home, away, date, venue);
    }

    @Override String getMatchType() { return "Knockout"; }
    @Override int pointsForWin()    { return 3; }
    @Override int pointsForDraw()   { return 0; }
    @Override boolean allowsDraw()  { return false; }
}

/* ---------- League (Aggregation: League HAS Teams and Matches) ---------- */
class League {
    private String name;
    private ArrayList<Team> teams = new ArrayList<>();
    private ArrayList<Match> matches = new ArrayList<>();
    private int nextMatchId = 101;

    League(String name) {
        this.name = name;
    }

    String getName() { return name; }

    Team findTeam(int id) {
        for (Team t : teams) {
            if (t.getTeamId() == id) {
                return t;
            }
        }
        return null;
    }

    Match findMatch(int id) {
        for (Match m : matches) {
            if (m.getMatchId() == id) {
                return m;
            }
        }
        return null;
    }

    void addTeam(Team t) throws LeagueException {
        for (Team existing : teams) {
            if (existing.getName().equalsIgnoreCase(t.getName())) {
                throw new LeagueException("A team named '" + t.getName() + "' already exists.");
            }
        }
        teams.add(t);
    }

    void scheduleMatch(Match m) throws LeagueException {
        Team h = m.getHome();
        Team a = m.getAway();

        if (h == a) {
            throw new SchedulingConflictException("A team cannot play against itself.");
        }
        if (!h.isReadyToPlay() || !a.isReadyToPlay()) {
            throw new SchedulingConflictException("Each team needs at least "
                    + Team.MIN_PLAYERS + " players to be scheduled.");
        }
        for (Match existing : matches) {
            if (!existing.getDate().equals(m.getDate())) {
                continue;
            }
            if (existing.involves(h) || existing.involves(a)) {
                throw new SchedulingConflictException(
                        "A team already has a match on " + m.getDate() + ".");
            }
            if (existing.getVenue().equalsIgnoreCase(m.getVenue())) {
                throw new SchedulingConflictException(
                        "Venue '" + m.getVenue() + "' is already booked on " + m.getDate() + ".");
            }
        }
        m.setMatchId(nextMatchId++);
        matches.add(m);
        System.out.println("\n--- MATCH SCHEDULED ---");
        m.displayFixture();
    }

    int getTeamCount() { return teams.size(); }

    void displayTeams() {
        if (teams.isEmpty()) {
            System.out.println("No teams registered.");
            return;
        }
        for (Team t : teams) {
            System.out.println("  " + t.getTeamId() + ". " + t.getName()
                    + " (" + t.getPlayerCount() + " players)");
        }
    }

    void displayFixtures() {
        System.out.println("\n--- FIXTURES & RESULTS ---");
        if (matches.isEmpty()) {
            System.out.println("No matches scheduled yet.");
            return;
        }
        ArrayList<Match> sorted = new ArrayList<>(matches);
        sorted.sort((x, y) -> x.getDate().compareTo(y.getDate()));
        System.out.printf("%-6s%-10s%-12s%-37s%-18s%s%n",
                "ID", "Type", "Date", "Match", "Venue", "Result");
        for (Match m : sorted) {
            m.displayFixture();
        }
    }

    void displayPointsTable() {
        System.out.println("\n--- POINTS TABLE: " + name + " ---");
        if (teams.isEmpty()) {
            System.out.println("No teams registered.");
            return;
        }
        ArrayList<Team> sorted = new ArrayList<>(teams);
        sorted.sort((x, y) -> {
            if (y.getPoints() != x.getPoints()) {
                return y.getPoints() - x.getPoints();
            }
            if (y.getGoalDifference() != x.getGoalDifference()) {
                return y.getGoalDifference() - x.getGoalDifference();
            }
            return y.getGoalsFor() - x.getGoalsFor();
        });
        System.out.printf("%-4s%-16s%-4s%-4s%-4s%-4s%-5s%-5s%-5s%-4s%n",
                "Pos", "Team", "P", "W", "D", "L", "GF", "GA", "GD", "Pts");
        int rank = 1;
        for (Team t : sorted) {
            t.displayTableRow(rank++);
        }
    }
}

public class SportsLeagueSystem {

    static Scanner sc = new Scanner(System.in);
    static League league = new League("Premier Cricket League");

    static int nextTeamId = 1;
    static int nextPersonId = 1;

    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number! Please try again.");
            }
        }
    }

    static String readText(String prompt) {
        System.out.print(prompt);
        return sc.nextLine();
    }

    static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(sc.nextLine().trim());
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date! Use format YYYY-MM-DD.");
            }
        }
    }

    static Team selectTeam(String prompt) throws LeagueException {
        int id = readInt(prompt);
        Team t = league.findTeam(id);
        if (t == null) {
            throw new LeagueException("Team with ID " + id + " not found.");
        }
        return t;
    }

    static void addTeam() throws LeagueException {
        String teamName = readText("Enter team name: ");
        if (teamName.trim().isEmpty()) {
            throw new InvalidInputException("Team name cannot be empty.");
        }
        String coachName = readText("Enter coach name: ");
        int coachAge = readInt("Enter coach age: ");

        Coach coach = new Coach(nextPersonId, coachName, coachAge);
        Team team = new Team(nextTeamId, teamName.trim(), coach);
        league.addTeam(team);
        nextPersonId++;
        nextTeamId++;

        System.out.println("Team registered successfully!");
        System.out.println("Team ID: " + team.getTeamId());
    }

    static void addPlayer() throws LeagueException {
        if (league.getTeamCount() == 0) {
            throw new LeagueException("Register a team first.");
        }
        league.displayTeams();
        Team team = selectTeam("Enter team ID: ");

        String name = readText("Enter player name: ");
        int age = readInt("Enter player age: ");
        String position = readText("Enter player position: ");

        Player p = new Player(nextPersonId, name, age, position);
        team.addPlayer(p);
        nextPersonId++;

        System.out.println("Player added to " + team.getName() + " successfully!");
        System.out.println("Player ID: " + p.getId());
    }

    static void scheduleMatch() throws LeagueException {
        if (league.getTeamCount() < 2) {
            throw new LeagueException("At least 2 teams are required to schedule a match.");
        }
        league.displayTeams();
        Team home = selectTeam("Enter home team ID: ");
        Team away = selectTeam("Enter away team ID: ");
        LocalDate date = readDate("Enter match date (YYYY-MM-DD): ");
        String venue = readText("Enter venue: ");
        if (venue.trim().isEmpty()) {
            throw new InvalidInputException("Venue cannot be empty.");
        }

        int type = readInt("Match type (1. League  2. Knockout): ");
        Match m;
        if (type == 1) {
            m = new LeagueMatch(home, away, date, venue.trim());
        } else if (type == 2) {
            m = new KnockoutMatch(home, away, date, venue.trim());
        } else {
            throw new InvalidInputException("Invalid match type.");
        }
        league.scheduleMatch(m);
    }

    static void recordResult() throws LeagueException {
        league.displayFixtures();
        int id = readInt("Enter match ID: ");
        Match m = league.findMatch(id);
        if (m == null) {
            throw new LeagueException("Match with ID " + id + " not found.");
        }
        int h = readInt("Enter score of " + m.getHome().getName() + ": ");
        int a = readInt("Enter score of " + m.getAway().getName() + ": ");
        m.recordResult(h, a);
    }

    static void displayTeamDetails() throws LeagueException {
        if (league.getTeamCount() == 0) {
            throw new LeagueException("No teams registered.");
        }
        league.displayTeams();
        selectTeam("Enter team ID: ").display();
    }

    public static void main(String[] args) {
        int choice = 0;

        do {
            System.out.println("\n===== SPORTS LEAGUE & MATCH SCHEDULING =====");
            System.out.println("1. Add Team");
            System.out.println("2. Add Player");
            System.out.println("3. Schedule Match");
            System.out.println("4. Record Match Result");
            System.out.println("5. Display Points Table");
            System.out.println("6. Display Fixtures");
            System.out.println("7. Display Team Details");
            System.out.println("8. Exit");
            choice = readInt("Enter your choice: ");

            try {
                switch (choice) {
                    case 1: addTeam();            break;
                    case 2: addPlayer();          break;
                    case 3: scheduleMatch();      break;
                    case 4: recordResult();       break;
                    case 5: league.displayPointsTable(); break;
                    case 6: league.displayFixtures();    break;
                    case 7: displayTeamDetails(); break;
                    case 8: System.out.println("Thank you for using the League Manager!"); break;
                    default: System.out.println("Invalid choice!");
                }
            } catch (LeagueException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (choice != 8);

        sc.close();
    }
}
