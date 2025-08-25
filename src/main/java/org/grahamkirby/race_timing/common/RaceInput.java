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

import static org.grahamkirby.race_timing_experimental.common.Config.*;

public abstract class RaceInput {

    protected final Race race;

    protected RaceInput(final Race race) {
        this.race = race;
    }

    protected void validateConfig() {

        validateRequiredPropertiesPresent();
    }

    protected void validateRequiredPropertiesPresent() {

        race.getRequiredProperty(KEY_YEAR);
        race.getRequiredProperty(KEY_RACE_NAME_FOR_RESULTS);
        race.getRequiredProperty(KEY_RACE_NAME_FOR_FILENAMES);
        race.getRequiredProperty(KEY_ENTRY_CATEGORIES_PATH);
        race.getRequiredProperty(KEY_PRIZE_CATEGORIES_PATH);
    }
}
