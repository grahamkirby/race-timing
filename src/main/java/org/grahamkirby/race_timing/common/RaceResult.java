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

import java.util.List;

/**
 * Abstraction over types of race result.
 */
public interface RaceResult extends Comparable<RaceResult> {

    /**
     * Gets the race in which this is a result.
     * @return the race
     */
    RaceInternal getRace();

    /**
     * Gets the participant for the result.
     * @return the participant
     */
    Participant getParticipant();

    /**
     * Gets the name of the participant.
     * @return the participant name
     */
    String getParticipantName();

    /**
     * Gets the entry category for the participant.
     * @return the entry category
     */
    EntryCategory getEntryCategory();

    /**
     * Gets the performance for the result.
     * @return the performance
     */
    Performance getPerformance();

    /**
     * Gets a string denoting the position in the race. This is a string rather than an integer
     * to allow equal positions such as "3=".
     * @return the position
     */
    String getPositionString();

    /**
     * Sets the position string for the result.
     * @param position_string the position string
     */
    void setPositionString(final String position_string);

    /**
     * Gets the categories of prize to be awarded to this participant. They may receive
     * multiple prizes if categories are non-exclusive.
     * @return a list containing the prize categories
     */
    List<PrizeCategory> getCategoriesOfPrizesAwarded();

    /**
     * Compares the performance of this result to that of another result.
     * @param other the other result
     * @return a negative number if the performance of this result is better than that of the other,
     * zero if they are the same, and a positive number if the performance of this result is worse
     */
    int comparePerformanceTo(RaceResult other);

    /**
     * Tests whether this participant has completed the race (single races) or whether it is possible
     * to complete the race (series races).
     * @return true if the race has been, or can be, completed
     */
    boolean canOrHasCompleted();
}
