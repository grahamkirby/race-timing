package lap_race;

import java.time.Duration;

public class LegResult implements Comparable<LegResult> {

    final Team team;
    final int leg_number;
    final LapRace race;
    boolean DNF;
    boolean in_mass_start;
    String position_string;

    Duration start_time;  // Relative to start of leg 1.
    Duration finish_time; // Relative to start of leg 1.

    public LegResult(final Team team, final int leg_number, final LapRace race) {

        this.team = team;
        this.leg_number = leg_number;
        this.race = race;
        this.DNF = true;
        this.in_mass_start = false;
    }

    public Duration duration() {
        return DNF ? LapRace.DUMMY_DURATION : finish_time.minus(start_time);
    }

    @Override
    public int compareTo(LegResult o) {

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
