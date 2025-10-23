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

public interface RaceResult extends Comparable<RaceResult> {

    SingleRaceInternal getRace();

    Participant getParticipant();

    String getParticipantName();

    EntryCategory getCategory();

    String getPositionString();

    void setPositionString(final String position_string);

    List<PrizeCategory> getCategoriesOfPrizesAwarded();

//    String getPrizeDetailPDF();
    String getPrizeDetail();

    int comparePerformanceTo(RaceResult other);
    boolean canComplete();
}
