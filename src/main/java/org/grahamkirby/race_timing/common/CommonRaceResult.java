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

import java.util.*;

public abstract class CommonRaceResult implements RaceResult {

    protected final RaceInternal race;
    protected Participant participant;
    protected String position_string;
    protected List<PrizeCategory> categories_of_prizes_awarded = new ArrayList<>();

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected CommonRaceResult(final RaceInternal race, final Participant participant) {

        this.race = race;
        this.participant = participant;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public abstract Comparator<RaceResult> getComparator();

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public RaceInternal getRace() {
        return race;
    }

    @Override
    public Participant getParticipant() {
        return participant;
    }

    @Override
    public String getParticipantName() {
        return participant.name;
    }

    @Override
    public EntryCategory getCategory() {
        return participant.category;
    }

    @Override
    public String getPositionString() {
        return position_string;
    }

    @Override
    public void setPositionString(final String position_string) {
        this.position_string  = position_string;
    }

    @Override
    public List<PrizeCategory> getCategoriesOfPrizesAwarded() {
        return categories_of_prizes_awarded;
    }

    @Override
    public int compareTo(final RaceResult other) {

        return getComparator().compare(this, other);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Combines multiple comparators into a single comparator. */
    @SafeVarargs
    public static <T extends Comparable<T>> Comparator<T> consecutiveComparator(final Comparator<T>... comparators) {

        return Arrays.stream(comparators).
            reduce((_, _) -> 0, Comparator::thenComparing);
    }

    protected static <T extends Comparable<T>> Comparator<T> conditionalComparator(final ComparatorPredicate<T> predicate, final Comparator<T> comparator_to_use_if_true, final Comparator<T> comparator_to_use_if_false) {

        return (result1, result2) -> predicate.apply(result1, result2) ? comparator_to_use_if_true.compare(result1, result2) : comparator_to_use_if_false.compare(result1, result2);
    }

    protected static int comparePossibleCompletion(final RaceResult r1, final RaceResult r2) {

        return Boolean.compare(r2.canComplete(), r1.canComplete());
    }

    /** Compares two results based on their performances, which may be based on a single or aggregate time,
     *  or a score. Gives a negative result if the first result has a better performance than the second. */
    protected static int comparePerformance(final RaceResult r1, final RaceResult r2) {

        return r1.comparePerformanceTo(r2);
    }

    /** Compares two results based on alphabetical ordering of the runners' first names. */
    protected static int compareRunnerFirstName(final RaceResult r1, final RaceResult r2) {

        return getFirstNameOfFirstRunner(r1.getParticipantName()).compareTo(getFirstNameOfFirstRunner(r2.getParticipantName()));
    }

    /** Compares two results based on alphabetical ordering of the runners' last names. */
    protected static int compareRunnerLastName(final RaceResult r1, final RaceResult r2) {

        return getLastNameOfFirstRunner(r1.getParticipantName()).compareTo(getLastNameOfFirstRunner(r2.getParticipantName()));
    }

    /** Gets the first name of the given runner, or of the first runner if it's a pair. */
    private static String getFirstNameOfFirstRunner(final String s) {

        final String runner = s.contains(" & ") ? s.split(" & ")[0] : s;
        return runner.split(" ")[0];
    }

    /** Gets the last name of the given runner, or of the first runner if it's a pair. */
    private static String getLastNameOfFirstRunner(final String s) {

        final String runner = s.contains(" & ") ? s.split(" & ")[0] : s;
        return Arrays.stream(runner.split(" ")).toList().getLast();
    }
}
