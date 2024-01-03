package lap_race;

import java.time.Duration;

public class OverallResult implements Comparable<OverallResult> {

    final Team team;
    final LegResult[] leg_results;
    final Results results;

    public OverallResult(final Team team, final int number_of_legs, final Results results) {

        this.team = team;
        this.results = results;
        leg_results = new LegResult[number_of_legs];

        for (int i = 0; i < number_of_legs; i++)
            leg_results[i] = new LegResult(team, i+1, results);
    }

    public Duration duration() {

        Duration overall = Results.ZERO_TIME;

        for (final LegResult leg_result : leg_results) {

            if (leg_result.DNF) return Results.DNF_DUMMY_LEG_TIME;

            overall = overall.plus(leg_result.duration());
        }

        return overall;
    }

    public boolean dnf() {

        for (final LegResult leg_result : leg_results)
            if (leg_result.DNF) return true;

        return false;
    }

    public String toString() {
        return team.bib_number + "," + team.name + "," + team.category + "," + (dnf() ? "DNF" : format(duration()));
    }

    @Override
    public int compareTo(final OverallResult o) {

        if (duration().equals(o.duration())) {

            if (dnf()) return Integer.compare(team.bib_number, o.team.bib_number);

            return results.getRecordedLegPosition(team.bib_number, results.number_of_legs).compareTo(results.getRecordedLegPosition(o.team.bib_number, results.number_of_legs));
        }

        return duration().compareTo(o.duration());
    }

    public static String format(final Duration duration) {

        final long s = duration.getSeconds();
        return String.format("0%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
    }
}
