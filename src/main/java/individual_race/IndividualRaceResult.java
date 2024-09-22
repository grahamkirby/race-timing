package individual_race;

import common.RaceResult;
import common.categories.Category;

import java.time.Duration;

import static common.Race.DUMMY_DURATION;

public class IndividualRaceResult extends RaceResult {

    public IndividualRaceEntry entry;
    public boolean DNF;
    public Duration finish_time;

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

    protected int compareRunnerNameTo(final IndividualRaceResult o) {

        final int last_name_comparison = getLastName(entry.runner.name).compareTo(getLastName(o.entry.runner.name));
        return last_name_comparison != 0 ? last_name_comparison : getFirstName(entry.runner.name).compareTo(getFirstName(o.entry.runner.name));
    }

    private int compareRecordedPositionTo(IndividualRaceResult r2) {

        IndividualRace individual_race = (IndividualRace) race;

        final int this_recorded_position = individual_race.getRecordedPosition(entry.bib_number);
        final int other_recorded_position = individual_race.getRecordedPosition(r2.entry.bib_number);

        return Integer.compare(this_recorded_position, other_recorded_position);
    }

    public boolean dnf() {
        return DNF;
    }

    public static int compare(RaceResult r1, RaceResult r2) {

        final int compare_completion = r1.compareCompletionTo(r2);
        if (compare_completion != 0) return compare_completion;

        // Either both have completed or neither have completed.

        final int compare_performance = r1.comparePerformanceTo(r2);
        if (compare_performance != 0) return compare_performance;

        // Both have the same time. If they have completed, use the recording order,
        // otherwise use alphabetical order for DNFs.

        if (r1.completed())
            return ((IndividualRaceResult)r1).compareRecordedPositionTo((IndividualRaceResult) r2);
        else
            return ((IndividualRaceResult)r1).compareRunnerNameTo((IndividualRaceResult) r2);
    }
}
