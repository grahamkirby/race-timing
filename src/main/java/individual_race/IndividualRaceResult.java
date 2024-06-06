package individual_race;

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

        IndividualRaceResult o = (IndividualRaceResult) other;

        // Where the time is the same, use the recording order.
        if (duration().equals(o.duration())) {

            final int this_recorded_position = ((IndividualRace)race).getRecordedPosition(entry.bib_number);
            final int other_recorded_position = ((IndividualRace)race).getRecordedPosition(o.entry.bib_number);

            return Integer.compare(this_recorded_position, other_recorded_position);
        }
        else
            return duration().compareTo(o.duration());
    }
}
