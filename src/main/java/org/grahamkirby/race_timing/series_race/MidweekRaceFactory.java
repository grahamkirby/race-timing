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

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Config.*;

public class MidweekRaceFactory implements SpecialisedRaceFactory {

    public static final String KEY_INDICATIVE_OF_MIDWEEK_RACE = KEY_SCORE_FOR_FIRST_PLACE;

    @Override
    public Race makeRace(final Path config_file_path) throws IOException {

        final MidweekRace race = new MidweekRace(makeMidweekRaceConfig(config_file_path));

        race.setCategoriesProcessor(new CategoriesProcessor(race));
        race.setResultsCalculator(new MidweekRaceResultsCalculator(race));
        race.setResultsOutput(new MidweekRaceOutput(race));

        return race;
    }

    @Override
    public boolean isValidFor(final Properties properties) {

        return properties.containsKey(KEY_INDICATIVE_OF_MIDWEEK_RACE);
    }

    private Config makeMidweekRaceConfig(final Path config_file_path) throws IOException {

        final Config config = new Config(config_file_path);

        config.addConfigProcessor(new RaceConfigAdjuster());
        config.addConfigProcessor(new SeriesRaceConfigAdjuster());
        config.addConfigProcessor(new MidweekRaceConfigAdjuster());
        config.addConfigProcessor(new RaceConfigValidator());
        config.addConfigProcessor(new SeriesRaceConfigValidator());
        config.addConfigProcessor(new MidweekRaceConfigValidator());
        config.processConfig();

        return config;
    }
}
