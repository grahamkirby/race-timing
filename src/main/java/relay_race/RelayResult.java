package relay_race;

import java.time.Duration;

public class RelayResult implements Comparable<RelayResult> {

    final Team team;
    int leg_number;
    final RelayRace race;
    boolean DNF;
    boolean in_mass_start;
    String position_string;

    Duration start_time;  // Relative to start of leg 1.
    Duration finish_time; // Relative to start of leg 1.

    public RelayResult(final Team team, final RelayRace race) {

        this.team = team;
        this.race = race;
        this.DNF = true;
        this.in_mass_start = false;
    }

    public Duration duration() {
        return DNF ? RelayRace.DUMMY_DURATION : finish_time.minus(start_time);
    }

    @Override
    public int compareTo(final RelayResult o) {

        // Where the time is the same, use the recording order.
        if (duration().equals(o.duration())) {

            final int this_recorded_position = race.getRecordedLegPosition(team.bib_number, leg_number);
            final int other_recorded_position = race.getRecordedLegPosition(o.team.bib_number, leg_number);

            return Integer.compare(this_recorded_position, other_recorded_position);
        }
        else
            return duration().compareTo(o.duration());
    }
}
