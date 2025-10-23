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

import org.grahamkirby.race_timing.categories.CategoriesProcessor;
import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.IndividualRaceConfigAdjuster;
import org.grahamkirby.race_timing.individual_race.IndividualRaceConfigValidator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Config.KEY_RACE_TEMPORAL_ORDER;

public class GrandPrixRaceFactory implements SpecialisedRaceFactory {

    public static final String KEY_INDICATIVE_OF_GRAND_PRIX_RACE = KEY_RACE_TEMPORAL_ORDER;

    @Override
    public Race2 makeRace(final Path config_file_path) throws IOException {

        final GrandPrixRace race = new GrandPrixRace(makeGrandPrixRaceConfig(config_file_path));

        race.setCategoriesProcessor(new CategoriesProcessor());
        race.setResultsCalculator(new GrandPrixRaceResultsCalculator());
        race.setResultsOutput(new GrandPrixRaceOutput());

        return race;
    }

    @Override
    public boolean isValidFor(final Properties properties) {

        return properties.containsKey(KEY_INDICATIVE_OF_GRAND_PRIX_RACE);
    }

    private Config makeGrandPrixRaceConfig(final Path config_file_path) throws IOException {

        final Config config = new Config(config_file_path);

        config.addConfigProcessor(new RaceConfigAdjuster());
        config.addConfigProcessor(new SeriesRaceConfigAdjuster());
        config.addConfigProcessor(new GrandPrixRaceConfigAdjuster());
        config.addConfigProcessor(new RaceConfigValidator());
        config.addConfigProcessor(new SeriesRaceConfigValidator());
        config.addConfigProcessor(new GrandPrixRaceConfigValidator());
        config.processConfig();

        return config;
    }
}
