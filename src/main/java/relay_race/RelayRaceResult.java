package relay_race;

import java.time.Duration;

public class RelayRaceResult implements Comparable<RelayRaceResult> {

    final Team team;
    final RelayResult[] leg_results;
    final RelayRace race;

    public RelayRaceResult(final Team team, final int number_of_legs, final RelayRace race) {

        this.team = team;
        this.race = race;
        leg_results = new RelayResult[number_of_legs];

        for (int i = 0; i < number_of_legs; i++)
            leg_results[i] = new RelayResult(team, race);
    }

    public Duration duration() {

        Duration overall = Duration.ZERO;

        for (final RelayResult leg_result : leg_results) {

            if (leg_result.DNF) return RelayRace.DUMMY_DURATION;
            overall = overall.plus(leg_result.duration());
        }

        return overall;
    }

    public boolean dnf() {

        for (final RelayResult leg_result : leg_results)
            if (leg_result.DNF) return true;

        return false;
    }

    @Override
    public int compareTo(final RelayRaceResult o) {

        // DNF results are sorted in increasing order of bib number.
        // Otherwise sort in order of increasing overall team time.
        // Where two teams have the same overall time, the order in which their last leg runners were recorded is preserved.

        if (!dnf() && o.dnf()) return -1;
        if (dnf() && !o.dnf()) return 1;
        if (dnf() && o.dnf()) return Integer.compare(team.bib_number, o.team.bib_number);

        if (duration().equals(o.duration())) {

            final int this_last_leg_position = race.getRecordedLegPosition(team.bib_number, race.number_of_legs);
            final int other_last_leg_position = race.getRecordedLegPosition(o.team.bib_number, race.number_of_legs);

            return Integer.compare(this_last_leg_position, other_last_leg_position);
        }

        return duration().compareTo(o.duration());
    }
}
