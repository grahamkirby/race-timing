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

            if (leg_result.DNF) return Results.DUMMY_DURATION;
            overall = overall.plus(leg_result.duration());
        }

        return overall;
    }

    public boolean dnf() {

        for (final LegResult leg_result : leg_results)
            if (leg_result.DNF) return true;

        return false;
    }

    @Override
    public int compareTo(final OverallResult o) {

        // Sort in order of increasing overall team time.
        // DNF results are sorted in increasing order of bib number.
        // Where two teams have the same overall time, the order in which their last leg runners were recorded is preserved.

        if (duration().equals(o.duration())) {

            if (dnf()) return Integer.compare(team.bib_number, o.team.bib_number);

            return results.getRecordedLegPosition(team.bib_number, results.number_of_legs).compareTo(results.getRecordedLegPosition(o.team.bib_number, results.number_of_legs));
        }

        return duration().compareTo(o.duration());
    }
}
