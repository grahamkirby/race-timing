package lap_race;

import java.time.Duration;
import java.util.Map;

public class LegResult implements Comparable<LegResult> {

    int leg_number;
    Team team;
    boolean DNF;

    Duration recorded_split_time;
    Duration adjusted_split_time;
    Duration adjustment_for_finishing_after_next_leg_mass_start; // Zero if it isn't after mass start.
    Duration leg_duration;
    Duration finish_time; // Relative to start of leg 1.

    public LegResult() {

    }

    public LegResult(int leg_number, Team team, Duration recorded_split_time, Duration adjusted_split_time, Duration adjustment_for_finishing_after_next_leg_mass_start, Duration leg_time, Map<Integer, Team> entries) {

        this.leg_number = leg_number;
        this.team = team;
        this.recorded_split_time = recorded_split_time;
        this.adjusted_split_time = adjusted_split_time;
        this.adjustment_for_finishing_after_next_leg_mass_start = adjustment_for_finishing_after_next_leg_mass_start;
        this.leg_duration = leg_time;

        DNF = false;
    }

    private String getTeamName(final int bib_number, final Map<Integer, Team> entries) {
        for (Map.Entry<Integer, Team> entry : entries.entrySet()) {
            if (entry.getKey() == bib_number) return entry.getValue().name;
        }
        throw new RuntimeException("undefined team: " + bib_number);
    }

    private Category getTeamCategory(final int bib_number, final Map<Integer, Team> entries) {
        for (Map.Entry<Integer, Team> entry : entries.entrySet()) {
            if (entry.getKey() == bib_number) return entry.getValue().category;
        }
        throw new RuntimeException("undefined team: " + bib_number);
    }

    public String toString() {
        return team.runners[leg_number - 1] + "," + OverallResult.format(leg_duration);
    }

    @Override
    public int compareTo(LegResult o) {
        return leg_duration.compareTo(o.leg_duration);
    }
}
