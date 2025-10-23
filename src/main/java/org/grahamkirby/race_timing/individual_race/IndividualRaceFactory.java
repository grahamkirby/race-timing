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

import org.grahamkirby.race_timing.categories.CategoriesProcessor;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Config.KEY_RACES;
import static org.grahamkirby.race_timing.relay_race.RelayRaceFactory.KEY_INDICATIVE_OF_RELAY_RACE;

public class IndividualRaceFactory implements SpecialisedRaceFactory {

    public static final String KEY_INDICATIVE_OF_SERIES_RACE = KEY_RACES;

    public SingleRaceInternal makeRace(final Path config_file_path) throws IOException {

        final IndividualRace race = new IndividualRace(makeIndividualRaceConfig(config_file_path));

        race.setCategoriesProcessor(new CategoriesProcessor(race));
        race.setResultsCalculator(new IndividualRaceResultsCalculator(race));
        race.setResultsOutput(new IndividualRaceOutput(race));

        return race;
    }

    public boolean isValidFor(final Properties properties) {

        // Must be an individual race if it's not a relay race or series race.
        return !(properties.containsKey(KEY_INDICATIVE_OF_RELAY_RACE) || properties.containsKey(KEY_INDICATIVE_OF_SERIES_RACE));
    }

    private Config makeIndividualRaceConfig(final Path config_file_path) throws IOException {

        final Config config = new Config(config_file_path);

        config.addConfigProcessor(new RaceConfigAdjuster());
        config.addConfigProcessor(new IndividualRaceConfigAdjuster());
        config.addConfigProcessor(new RaceConfigValidator());
        config.addConfigProcessor(new IndividualRaceConfigValidator());
        config.processConfig();

        return config;
    }
}
