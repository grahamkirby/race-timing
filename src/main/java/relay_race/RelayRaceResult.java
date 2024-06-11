package relay_race;

import common.Category;
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

    public Duration duration() {

        Duration overall = Duration.ZERO;

        for (final LegResult leg_result : leg_results) {

            if (leg_result.DNF) return RelayRace.DUMMY_DURATION;
            overall = overall.plus(leg_result.duration());
        }

        return overall;
    }

    public boolean dnf() {

        for (final LegResult leg_result : leg_results)
            if (leg_result.DNF) return true;

        return false;
    }

    @Override
    public int compareTo(final RaceResult other) {
        return compare(this, other);
    }

    @Override
    public boolean sameEntrant(RaceResult other) {
        return entry.equals(((RelayRaceResult) other).entry);
    }

    @Override
    public boolean completed() {
        return !dnf();
    }

    @Override
    public Category getCategory() {
        return entry.team.category;
    }

    public static int compare(final RaceResult r1, final RaceResult r2) {

        RelayRaceResult o = (RelayRaceResult) r2;

        // DNF results are sorted in increasing order of bib number.
        // Otherwise sort in order of increasing overall team time.
        // Where two teams have the same overall time, the order in which their last leg runners were recorded is preserved.

        if (!((RelayRaceResult)r1).dnf() && o.dnf()) return -1;
        if (((RelayRaceResult)r1).dnf() && !o.dnf()) return 1;
        if (((RelayRaceResult)r1).dnf() && o.dnf()) return Integer.compare(((RelayRaceResult)r1).entry.bib_number, o.entry.bib_number);

        if (((RelayRaceResult)r1).duration().equals(o.duration())) {

            RelayRace relay_race = (RelayRace)r1.race;

            final int this_last_leg_position = relay_race.getRecordedLegPosition(((RelayRaceResult)r1).entry.bib_number, relay_race.number_of_legs);
            final int other_last_leg_position = relay_race.getRecordedLegPosition(o.entry.bib_number, relay_race.number_of_legs);

            return Integer.compare(this_last_leg_position, other_last_leg_position);
        }

        return ((RelayRaceResult)r1).duration().compareTo(o.duration());
    }
}
