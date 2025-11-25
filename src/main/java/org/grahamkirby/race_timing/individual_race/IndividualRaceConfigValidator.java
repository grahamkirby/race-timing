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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.Config;
import org.grahamkirby.race_timing.common.ConfigProcessor;

import java.nio.file.Path;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.*;

public class IndividualRaceConfigValidator extends ConfigProcessor {

    public IndividualRaceConfigValidator(final Config config) {

        super(config);
    }

    public void processConfig() {

        checkExactlyOnePresent(List.of(
            KEY_RAW_RESULTS_PATH,
            KEY_RESULTS_PATH));

        checkNonePresent(List.of(
            KEY_RACES,
            KEY_INDIVIDUAL_LEG_STARTS,
            KEY_MASS_START_TIMES,
            KEY_MINIMUM_NUMBER_OF_RACES,
            KEY_NUMBER_OF_LEGS,
            KEY_NUMBER_OF_RACES_IN_SERIES,
            KEY_PAIRED_LEGS,
            KEY_PAPER_RESULTS_PATH,
            KEY_RACE_TEMPORAL_ORDER));

        checkAllOrNonePresent(List.of(
            KEY_TEAM_PRIZE_GENDER_CATEGORIES,
            KEY_TEAM_PRIZE_NUMBER_TO_COUNT));

        checkAllOrNonePresent(List.of(
            KEY_TIME_TRIAL_RUNNERS_PER_WAVE,
            KEY_TIME_TRIAL_INTER_WAVE_INTERVAL));

        checkAtMostOnePresent(List.of(
            KEY_CATEGORY_START_OFFSETS,
            KEY_TIME_TRIAL_RUNNERS_PER_WAVE));

        checkAtMostOnePresent(List.of(
            KEY_CATEGORY_START_OFFSETS,
            KEY_TIME_TRIAL_INTER_WAVE_INTERVAL));

        validateDNFRecords((String) config.get(KEY_DNF_FINISHERS));
    }

    public void validateDNFRecords(final String dnf_string) {

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(","))
                try {
                    // Each DNF string contains single bib number.
                    Integer.parseInt(individual_dnf_string);

                } catch (final NumberFormatException e) {
                    throw new RuntimeException("invalid entry '" + dnf_string +"' for key '" + KEY_DNF_FINISHERS + "' in file '" + config.getConfigPath().getFileName() + "'", e);
                }
    }
}
