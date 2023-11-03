package lap_race;

import java.time.Duration;
import java.util.Map;

public class OverallResult {

    int bib_number;
    String team_name;
    Category team_category;

    Duration overall_time;
    String position_string;    // Use string to cater for = positions.

    public OverallResult(int bib_number, Duration overall_time, Map<Integer, Team> entries) {

        this.bib_number = bib_number;
        this.overall_time = overall_time;

        team_name = getTeamName(bib_number, entries);
        team_category = getTeamCategory(bib_number, entries);
    }

     String getTeamName(final int bib_number, final Map<Integer, Team> entries) {
        for (Map.Entry<Integer, Team> entry : entries.entrySet()) {
            if (entry.getKey() == bib_number) return entry.getValue().name;
        }
        throw new RuntimeException("undefined team: " + bib_number);
    }

     Category getTeamCategory(final int bib_number, final Map<Integer, Team> entries) {
        for (Map.Entry<Integer, Team> entry : entries.entrySet()) {
            if (entry.getKey() == bib_number) return entry.getValue().category;
        }
        throw new RuntimeException("undefined team: " + bib_number);
    }

    public String toString() {
        return bib_number + "," + team_name + "," + team_category + "," + format(overall_time);
    }

    public static String format(Duration duration) {

        long s = duration.getSeconds();
        return String.format("0%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }
}
