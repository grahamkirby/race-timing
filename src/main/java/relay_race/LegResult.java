package relay_race;

import common.Race;
import common.RaceResult;

import java.time.Duration;

public class LegResult extends RaceResult {

    final Team team;
    int leg_number;
    boolean DNF;
    boolean in_mass_start;

    Duration start_time;  // Relative to start of leg 1.
    Duration finish_time; // Relative to start of leg 1.

    public LegResult(final Team team, final Race race) {

        super(race);
        this.team = team;
        this.DNF = true;
        this.in_mass_start = false;
    }

    public Duration duration() {
        return DNF ? RelayRace.DUMMY_DURATION : finish_time.minus(start_time);
    }

    @Override
    public int compareTo(final RaceResult other) {

        LegResult o = (LegResult) other;

        // Where the time is the same, use the recording order.
        if (duration().equals(o.duration())) {

            final int this_recorded_position = ((RelayRace)race).getRecordedLegPosition(team.bib_number, leg_number);
            final int other_recorded_position = ((RelayRace)race).getRecordedLegPosition(o.team.bib_number, leg_number);

            return Integer.compare(this_recorded_position, other_recorded_position);
        }
        else
            return duration().compareTo(o.duration());
    }

    @Override
    public int comparePerformanceTo(RaceResult other) {
        return duration().compareTo(((LegResult) other).duration());
    }
}
