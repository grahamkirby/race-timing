package lap_race;

import java.time.Duration;
import java.util.Map;

public class LegResult implements Comparable<LegResult> {

    final Team team;
    final int leg_number;
    final Results results;
    boolean DNF;
    String position_string;
    boolean in_mass_start = false;

    Duration start_time;  // Relative to start of leg 1.
    Duration finish_time; // Relative to start of leg 1.

    public LegResult(final Team team, final int leg_number, final Results results) {

        this.team = team;
        this.leg_number = leg_number;
        this.results = results;
        this.DNF = true;
    }

    public Duration duration() {
        return DNF ? Results.DNF_DUMMY_LEG_TIME : finish_time.minus(start_time);
    }

    public String toString() {
        return team.runners[leg_number-1] + "," + (DNF ? "DNF" : OverallResult.format(duration()));
    }

    @Override
    public int compareTo(LegResult o) {

        if (duration().equals(o.duration())) {
            return results.getRecordedLegPosition(team.bib_number, leg_number).compareTo(results.getRecordedLegPosition(o.team.bib_number, leg_number));
        }

        return duration().compareTo(o.duration());
    }
}
