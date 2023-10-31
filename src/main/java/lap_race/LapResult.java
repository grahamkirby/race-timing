package lap_race;

import java.time.Duration;
import java.util.Map;

public class LapResult {

    int lap;
    int bib_number;
    String team_name;
    Category team_category;
    boolean DNF;

    Duration recorded_split_time;
    Duration adjusted_split_time;
    Duration amount_this_lap_finish_later_than_next_lap_mass_start; // Zero if it isn't after mass start.
    Duration lap_time;

    public LapResult(int lap, int bib_number, Duration recorded_split_time, Duration adjusted_split_time, Duration amount_this_lap_finish_later_than_next_lap_mass_start, Duration lap_time, Map<Integer, Team> entries) {

        this.lap = lap;
        this.bib_number = bib_number;
        this.recorded_split_time = recorded_split_time;
        this.adjusted_split_time = adjusted_split_time;
        this.amount_this_lap_finish_later_than_next_lap_mass_start = amount_this_lap_finish_later_than_next_lap_mass_start;
        this.lap_time = lap_time;

        team_name = getTeamName(bib_number, entries);
        team_category = getTeamCategory(bib_number, entries);
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
        return bib_number + "," + team_name + "," + team_category + "," + OverallResult.format(recorded_split_time) + "," + OverallResult.format(amount_this_lap_finish_later_than_next_lap_mass_start) + "," + OverallResult.format(lap_time);
    }
}
