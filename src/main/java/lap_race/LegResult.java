package lap_race;

import java.time.Duration;

public class LegResult implements Comparable<LegResult> {

    final Team team;
    final int leg_number;
    final LapRace results;
    boolean DNF;
    boolean in_mass_start;
    String position_string;

    Duration start_time;  // Relative to start of leg 1.
    Duration finish_time; // Relative to start of leg 1.

    public LegResult(final Team team, final int leg_number, final LapRace results) {

        this.team = team;
        this.leg_number = leg_number;
        this.results = results;
        this.DNF = true;
        this.in_mass_start = false;
    }

    public Duration duration() {
        return DNF ? LapRace.DUMMY_DURATION : finish_time.minus(start_time);
    }

    @Override
    public int compareTo(LegResult o) {

        if (duration().equals(o.duration()))
            return results.getRecordedLegPosition(team.bib_number, leg_number).compareTo(results.getRecordedLegPosition(o.team.bib_number, leg_number));
        else
            return duration().compareTo(o.duration());
    }
}
