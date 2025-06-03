package org.grahamkirby.race_timing.single_race;

import org.grahamkirby.race_timing.common.Participant;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.time.Duration;
import java.util.Comparator;
import java.util.Set;

public class SingleRaceResult extends RaceResult {

    public SingleRaceEntry entry;
    public Duration finish_time;
    public boolean dnf;

    public SingleRaceResult(final Race race, final SingleRaceEntry entry, final Duration finish_time) {

        super(race);
        this.entry = entry;
        this.finish_time = finish_time;
    }

    @Override
    protected String getParticipantName() {

        return entry.participant.name;
    }

    @Override
    public boolean shouldDisplayPosition() {
        return canComplete();
    }

    protected String getClub() {

        // The participant field could hold a team rather than a runner, but this method is overridden in RelayRaceResult.
        return ((Runner) entry.participant).club;
    }

    public Duration duration() {
        return finish_time;
    }

    @Override
    public int comparePerformanceTo(final RaceResult other) {

        final Duration duration = duration();
        final Duration other_duration = ((SingleRaceResult) other).duration();

        return Comparator.nullsLast(Duration::compareTo).compare(duration, other_duration);
    }

    @Override
    public EntryCategory getCategory() {
        return entry.participant.category;
    }

    @Override
    public boolean canComplete() {
        return !dnf;
    }

    /** Tests whether the given entry category is eligible for the given prize category. */
    @Override
    public boolean isResultEligibleForPrizeCategory(final PrizeCategory prize_category) {

        return super.isResultEligibleForPrizeCategory(prize_category) &&
            isResultEligibleForPrizeCategoryByClub( prize_category);
    }

    @Override
    public Participant getParticipant() {
        return entry.participant;
    }

    private boolean isResultEligibleForPrizeCategoryByClub(final PrizeCategory prize_category) {

        final String club = getClub();
        final Set<String> eligible_clubs = prize_category.getEligibleClubs();

        if (club == null || eligible_clubs.isEmpty()) return true;

        return eligible_clubs.contains(club);
    }
}
