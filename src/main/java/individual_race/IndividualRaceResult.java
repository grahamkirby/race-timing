package individual_race;

import java.time.Duration;

import static common.Race.DUMMY_DURATION;

public class IndividualRaceResult implements Comparable<IndividualRaceResult> {

    public IndividualRaceEntry entry;

    final IndividualRace race;
    boolean DNF;

    Duration finish_time; // Relative to start of leg 1.

    public IndividualRaceResult(final IndividualRace race) {

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
    public int compareTo(IndividualRaceResult o) {

        // Where the time is the same, use the recording order.
        if (duration().equals(o.duration())) {

            final int this_recorded_position = race.getRecordedPosition(entry.bib_number);
            final int other_recorded_position = race.getRecordedPosition(o.entry.bib_number);

            return Integer.compare(this_recorded_position, other_recorded_position);
        }
        else
            return duration().compareTo(o.duration());
    }
}
