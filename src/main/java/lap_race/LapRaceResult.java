package lap_race;

import java.time.Duration;

public class LapRaceResult implements Comparable<LapRaceResult> {

    final Team team;
    final LegResult[] leg_results;
    final LapRace race;

    public LapRaceResult(final Team team, final int number_of_legs, final LapRace race) {

        this.team = team;
        this.race = race;
        leg_results = new LegResult[number_of_legs];

        for (int i = 0; i < number_of_legs; i++)
            leg_results[i] = new LegResult(team, race);
    }

    public Duration duration() {

        Duration overall = LapRace.ZERO_TIME;

        for (final LegResult leg_result : leg_results) {

            if (leg_result.DNF) return LapRace.DUMMY_DURATION;
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
    public int compareTo(final LapRaceResult o) {

        // Sort in order of increasing overall team time.
        // DNF results are sorted in increasing order of bib number.
        // Where two teams have the same overall time, the order in which their last leg runners were recorded is preserved.

        if (duration().equals(o.duration())) {

            if (dnf()) return Integer.compare(team.bib_number, o.team.bib_number);

            final int this_last_leg_position = race.getRecordedLegPosition(team.bib_number, race.number_of_legs);
            final int other_last_leg_position = race.getRecordedLegPosition(o.team.bib_number, race.number_of_legs);

            return Integer.compare(this_last_leg_position, other_last_leg_position);
        }

        return duration().compareTo(o.duration());
    }
}
