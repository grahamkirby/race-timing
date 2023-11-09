package lap_race;

import java.time.Duration;
import java.util.Map;

public class LegResult implements Comparable<LegResult> {

    Team team;
    int leg_number;
    boolean DNF;

    Duration start_time;  // Relative to start of leg 1.
    Duration finish_time; // Relative to start of leg 1.

    public LegResult(Team team, int leg_number, boolean DNF) {

        this.team = team;
        this.leg_number = leg_number;
        this. DNF = DNF;
    }

    public Duration duration() {
        return DNF ? Results.DNF_DUMMY_LEG_TIME : finish_time.minus(start_time);
    }

    public String toString() {
        return team.runners[leg_number-1] + "," + (DNF ? "DNF" : OverallResult.format(duration()));
    }

    @Override
    public int compareTo(LegResult o) {
        return duration().compareTo(o.duration());
    }
}
