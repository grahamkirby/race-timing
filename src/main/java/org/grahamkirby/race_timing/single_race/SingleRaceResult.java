package org.grahamkirby.race_timing.single_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.time.Duration;
import java.util.Set;

public abstract class SingleRaceResult extends RaceResult {

    public SingleRaceEntry entry;
    public Duration finish_time;
    public boolean dnf;

    protected SingleRaceResult(final Race race, final SingleRaceEntry entry, final Duration finish_time) {

        super(race);
        this.entry = entry;
        this.finish_time = finish_time;
    }

    public abstract Duration duration();

    protected abstract String getClub();

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

    private boolean isResultEligibleForPrizeCategoryByClub(final PrizeCategory prize_category) {

        final String club = getClub();
        final Set<String> eligible_clubs = prize_category.getEligibleClubs();

        if (club == null || eligible_clubs.isEmpty()) return true;

        return eligible_clubs.contains(club);
    }
}
