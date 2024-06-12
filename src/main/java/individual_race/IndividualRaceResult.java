package individual_race;

import common.Category;
import common.RaceResult;

import java.time.Duration;

import static common.Race.DUMMY_DURATION;

public class IndividualRaceResult extends RaceResult {

    public IndividualRaceEntry entry;
    public boolean DNF;
    Duration finish_time; // Relative to start of leg 1.

    public IndividualRaceResult(final IndividualRace race) {

        super(race);
        this.DNF = true;
    }

    public Duration duration() {
        return DNF ? DUMMY_DURATION : finish_time;
    }

    @Override
    public int compareTo(RaceResult other) {
        return compare(this, other);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof IndividualRaceResult other && compareTo(other) == 0;
    }

    @Override
    public boolean sameEntrant(RaceResult other) {
        return entry.equals(((IndividualRaceResult) other).entry);
    }

    @Override
    public boolean completed() {
        return !DNF;
    }

    @Override
    public Category getCategory() {
        return entry.runner.category;
    }

    @Override
    public int comparePerformanceTo(RaceResult other) {
        return duration().compareTo(((IndividualRaceResult) other).duration());
    }

    public boolean dnf() {
        return DNF;
    }

    public static int compare(RaceResult r1, RaceResult r2) {

        // Where the time is the same, use the recording order.
        if (((IndividualRaceResult)r1).duration().equals(((IndividualRaceResult)r2).duration())) {

            IndividualRace individual_race = (IndividualRace) ((IndividualRaceResult) r1).race;

            final int this_recorded_position = individual_race.getRecordedPosition(((IndividualRaceResult)r1).entry.bib_number);
            final int other_recorded_position = individual_race.getRecordedPosition(((IndividualRaceResult)r2).entry.bib_number);

            return Integer.compare(this_recorded_position, other_recorded_position);
        }
        else
            return ((IndividualRaceResult)r1).duration().compareTo(((IndividualRaceResult)r2).duration());
    }
}
