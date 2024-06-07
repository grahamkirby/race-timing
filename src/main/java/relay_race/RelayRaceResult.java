package relay_race;

import common.Race;
import common.RaceResult;

import java.time.Duration;

public class RelayRaceResult extends RaceResult {

    final Team team;
    final LegResult[] leg_results;

    public RelayRaceResult(final Team team, final int number_of_legs, final Race race) {

        super(race);
        this.team = team;
        leg_results = new LegResult[number_of_legs];

        for (int i = 0; i < number_of_legs; i++)
            leg_results[i] = new LegResult(team, race);
    }

    public Duration duration() {

        Duration overall = Duration.ZERO;

        for (final LegResult leg_result : leg_results) {

            if (leg_result.DNF) return RelayRace.DUMMY_DURATION;
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
    public int compareTo(final RaceResult other) {

        RelayRaceResult o = (RelayRaceResult) other;

        // DNF results are sorted in increasing order of bib number.
        // Otherwise sort in order of increasing overall team time.
        // Where two teams have the same overall time, the order in which their last leg runners were recorded is preserved.

        if (!dnf() && o.dnf()) return -1;
        if (dnf() && !o.dnf()) return 1;
        if (dnf() && o.dnf()) return Integer.compare(team.bib_number, o.team.bib_number);

        if (duration().equals(o.duration())) {

            final int this_last_leg_position = ((RelayRace)race).getRecordedLegPosition(team.bib_number, ((RelayRace)race).number_of_legs);
            final int other_last_leg_position = ((RelayRace)race).getRecordedLegPosition(o.team.bib_number, ((RelayRace)race).number_of_legs);

            return Integer.compare(this_last_leg_position, other_last_leg_position);
        }

        return duration().compareTo(o.duration());
    }

    @Override
    public int comparePerformanceTo(RaceResult other) {
        throw new UnsupportedOperationException();
    }
}
