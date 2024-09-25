package relay_race;

import common.categories.Category;
import common.Race;
import common.RaceResult;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class RelayRaceResult extends RaceResult {

    public final RelayRaceEntry entry;
    final List<LegResult> leg_results;

    public RelayRaceResult(final RelayRaceEntry entry, final int number_of_legs, final Race race) {

        super(race);
        this.entry = entry;
        leg_results = new ArrayList<>();

        for (int i = 0; i < number_of_legs; i++)
            leg_results.add(new LegResult(entry, race));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public int compareTo(final RaceResult other) {
        return compare(this, other);
    }

    @Override
    public boolean sameEntrant(final RaceResult other) {
        return entry.equals(((RelayRaceResult) other).entry);
    }

    @Override
    public boolean completed() {
        return !dnf();
    }

    @Override
    public Category getCategory() {
        return entry.team.category();
    }

    protected Duration duration() {

        Duration overall = Duration.ZERO;

        for (final LegResult leg_result : leg_results) {

            if (leg_result.DNF) return RelayRace.DUMMY_DURATION;
            overall = overall.plus(leg_result.duration());
        }

        return overall;
    }

    protected boolean dnf() {

        for (final LegResult leg_result : leg_results)
            if (leg_result.DNF) return true;

        return false;
    }

    protected static int compare(final RaceResult r1, final RaceResult r2) {

        final RelayRaceResult result1 = (RelayRaceResult) r1;
        final RelayRaceResult result2 = (RelayRaceResult) r2;

        final int completion_comparison = compareCompletion(!result1.dnf(), !result2.dnf());
        final int bib_comparison = Integer.compare(result1.entry.bib_number, result2.entry.bib_number);
        final int duration_comparison = result1.duration().compareTo(result2.duration());
        final int last_leg_position_comparison = Integer.compare(getRecordedLastLegPosition(result1), getRecordedLastLegPosition(result2));

        if (completion_comparison != 0) return completion_comparison;   // If one has completed and the other has not, order by completion first.
        if (result1.dnf()) return bib_comparison;                       // If the first has not completed (then implicitly neither has the second), order by bib number.
        if (duration_comparison != 0) return duration_comparison;       // If the durations are different, order by duration.

        return last_leg_position_comparison;                            // Both completed, with same overall duration, order by last leg finish position.
    }

    private static int compareCompletion(final boolean completed1, final boolean completed2) {

        if (completed1 && !completed2) return -1;
        if (!completed1 && completed2) return 1;
        return 0;
    }

    private static int getRecordedLastLegPosition(RelayRaceResult result) {

        final RelayRace race = (RelayRace) result.race;
        return race.getRecordedLegPosition(result.entry.bib_number, race.number_of_legs);
    }
}
