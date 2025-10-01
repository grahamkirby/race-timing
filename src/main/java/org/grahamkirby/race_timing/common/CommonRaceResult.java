/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.common;


import org.grahamkirby.race_timing.categories.EntryCategory;
import org.grahamkirby.race_timing.categories.PrizeCategory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.getFirstNameOfFirstRunner;
import static org.grahamkirby.race_timing.common.Normalisation.getLastNameOfFirstRunner;

@SuppressWarnings("IncorrectFormatting")
public abstract class CommonRaceResult implements RaceResult {

    public static final Duration VERY_LONG_DURATION = Duration.ofDays(Integer.MAX_VALUE);

    protected final Race race;
    protected Participant participant;
    protected String position_string;
    protected List<PrizeCategory> categories_of_prizes_awarded = new ArrayList<>();

    protected CommonRaceResult(final Race race, final Participant participant) {
        this.race = race;
        this.participant = participant;
    }

    public Race getRace() {
        return race;
    }

    public Participant getParticipant() {
        return participant;
    }

    public String getParticipantName() {
        return participant.name;
    }

    public EntryCategory getCategory() {
        return participant.category;
    }

    public String getPositionString() {
        return position_string;
    }

    public void setPositionString(final String position_string) {
        this.position_string  = position_string;
    }

    public List<PrizeCategory> getCategoriesOfPrizesAwarded() {
        return categories_of_prizes_awarded;
    }

    public abstract int comparePerformanceTo(CommonRaceResult other);
    public abstract boolean canComplete();

    protected static int comparePossibleCompletion(final CommonRaceResult r1, final CommonRaceResult r2) {

        return Boolean.compare(r2.canComplete(), r1.canComplete());
    }

    /** Compares two results based on their performances, which may be based on a single or aggregate time,
     *  or a score. Gives a negative result if the first result has a better performance than the second. */
    public static int comparePerformance(final CommonRaceResult r1, final CommonRaceResult r2) {

        return r1.comparePerformanceTo(r2);
    }

    /** Compares two results based on alphabetical ordering of the runners' first names. */
    public static int compareRunnerFirstName(final CommonRaceResult r1, final CommonRaceResult r2) {

        return getFirstNameOfFirstRunner(r1.getParticipantName()).compareTo(getFirstNameOfFirstRunner(r2.getParticipantName()));
    }

    /** Compares two results based on alphabetical ordering of the runners' last names. */
    public static int compareRunnerLastName(final CommonRaceResult r1, final CommonRaceResult r2) {

        return getLastNameOfFirstRunner(r1.getParticipantName()).compareTo(getLastNameOfFirstRunner(r2.getParticipantName()));
    }

    public static Comparator<CommonRaceResult> ignoreIfEitherResultIsDNF(final Comparator<? super CommonRaceResult> base_comparator) {

        return (r1, r2) -> {

            if (!r1.canComplete() || !r2.canComplete()) return 0;
            else return base_comparator.compare(r1, r2);
        };
    }

    @Override
    public int compareTo(final RaceResult o) {

        CommonRaceResult other = (CommonRaceResult)o;

        return combineComparators(getComparators()).compare(this, other);
    }

    /** Combines multiple comparators into a single comparator. */
    public static Comparator<CommonRaceResult> combineComparators(final Collection<Comparator<CommonRaceResult>> comparators) {

        return comparators.stream().
            reduce((_, _) -> 0, Comparator::thenComparing);
    }

    public abstract List<Comparator<CommonRaceResult>> getComparators();
}
