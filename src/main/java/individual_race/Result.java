package individual_race;

import lap_race.LapRace;
import lap_race.LegResult;
import lap_race.Team;

import java.time.Duration;

import static common.Race.DUMMY_DURATION;

public class Result implements Comparable<Result> {

    final Runner runner;

    final IndividualRace race;
    boolean DNF;

    String position_string;

    Duration finish_time; // Relative to start of leg 1.

    public Result(final Runner runner, final IndividualRace race) {

        this.runner = runner;
        this.race = race;
        this.DNF = true;
    }

    public Duration duration() {
        return DNF ? DUMMY_DURATION : finish_time;
    }

    public boolean dnf() {

        return DNF;
    }
    @Override
    public int compareTo(Result o) {

        // Where the time is the same, use the recording order.
        if (duration().equals(o.duration())) {

            final int this_recorded_position = race.getRecordedPosition(runner.bib_number);
            final int other_recorded_position = race.getRecordedPosition(o.runner.bib_number);

            return Integer.compare(this_recorded_position, other_recorded_position);
        }
        else
            return duration().compareTo(o.duration());
    }
}
