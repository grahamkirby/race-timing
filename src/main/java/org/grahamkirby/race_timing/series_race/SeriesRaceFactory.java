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
package org.grahamkirby.race_timing.series_race;

import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Config.*;

public class SeriesRaceFactory extends RaceFactory {

    public static final String KEY_INDICATIVE_OF_SERIES_RACE = KEY_RACES;
    private static final String KEY_INDICATIVE_OF_SERIES_RACE_USING_INDIVIDUAL_TIMES = KEY_SCORE_FOR_MEDIAN_POSITION;
    private static final String KEY_INDICATIVE_OF_SERIES_RACE_USING_INDIVIDUAL_POSITIONS = KEY_SCORE_FOR_FIRST_PLACE;

    @Override
    public Race makeRace(final Path config_file_path) throws IOException {

        final Config config = makeSeriesRaceConfig(config_file_path);
        final SeriesRace race = new SeriesRace(config);

        try {
            config.processConfigAdjusters();
            config.processConfigValidators();
            race.initialise();
        }
        catch (final Exception e) {

            // Reset output in case config validation failed, before output initialisation.
            race.setOutput(new SeriesRaceOutput(config));
            race.getNotesProcessor().appendToNotes(e.getMessage() + LINE_SEPARATOR);
        }

        return race;
    }

    @Override
    public boolean isValidFor(final Properties properties) {

        return properties.containsKey(KEY_INDICATIVE_OF_SERIES_RACE);
    }

    private Config makeSeriesRaceConfig(final Path config_file_path) throws IOException {

        final Config config = new Config(config_file_path);

        config.addConfigAdjuster(RaceConfigAdjuster::new);
        config.addConfigAdjuster(SeriesRaceConfigAdjuster::new);

        config.addConfigValidator(RaceConfigValidator::new);
        config.addConfigValidator(SeriesRaceConfigValidator::new);

        if (config.containsKey(KEY_INDICATIVE_OF_SERIES_RACE_USING_INDIVIDUAL_TIMES)) {

            config.addConfigAdjuster(IndividualTimesConfigAdjuster::new);
            config.addConfigValidator(IndividualTimesConfigValidator::new);
        }

        if (config.containsKey(KEY_INDICATIVE_OF_SERIES_RACE_USING_INDIVIDUAL_POSITIONS)) {

            config.addConfigAdjuster(IndividualPositionsConfigAdjuster::new);
        }

        return config;
    }

    private static class IndividualPositionsConfigAdjuster extends ConfigProcessor {

        public IndividualPositionsConfigAdjuster(final Config config) {

            super(config);
        }

        @Override
        public void processConfig() {

            config.replace(KEY_SCORE_FOR_FIRST_PLACE, Integer::parseInt);
        }
    }

    private static class IndividualTimesConfigAdjuster extends ConfigProcessor {

        public IndividualTimesConfigAdjuster(final Config config) {

            super(config);
        }

        @Override
        public void processConfig() {

            config.replaceIfPresent(KEY_RACE_CATEGORIES_PATH, s -> config.interpretPath(Path.of(s)));
            config.replaceIfPresent(KEY_SCORE_FOR_MEDIAN_POSITION, Integer::parseInt);
        }
    }

    private static class IndividualTimesConfigValidator extends ConfigProcessor {

        public IndividualTimesConfigValidator(final Config config) {

            super(config);
        }

        public void processConfig() {

            checkAllPresent(List.of(KEY_RACE_CATEGORIES_PATH, KEY_SCORE_FOR_MEDIAN_POSITION));
            checkAllFilesExist(List.of(KEY_RACE_CATEGORIES_PATH));
        }
    }
}
