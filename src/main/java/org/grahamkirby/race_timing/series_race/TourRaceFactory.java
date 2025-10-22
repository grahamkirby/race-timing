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
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceConfigAdjuster;
import org.grahamkirby.race_timing.common.RaceConfigValidator;
import org.grahamkirby.race_timing.common.SpecialisedRaceFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static org.grahamkirby.race_timing.individual_race.IndividualRaceFactory.KEY_INDICATIVE_OF_SERIES_RACE;
import static org.grahamkirby.race_timing.series_race.GrandPrixRaceFactory.KEY_INDICATIVE_OF_GRAND_PRIX_RACE;
import static org.grahamkirby.race_timing.series_race.MidweekRaceFactory.KEY_INDICATIVE_OF_MIDWEEK_RACE;

public class TourRaceFactory implements SpecialisedRaceFactory {

    @Override
    public Race makeRace(final Path config_file_path) throws IOException {

        final Race race = new Race(config_file_path);

        race.addConfigProcessor(new RaceConfigAdjuster());
        race.addConfigProcessor(new SeriesRaceConfigAdjuster());
        race.addConfigProcessor(new TourRaceConfigAdjuster());
        race.addConfigProcessor(new RaceConfigValidator());
        race.addConfigProcessor(new SeriesRaceConfigValidator());
        race.loadConfig();

        race.setSpecific(new TourRace());
        race.setCategoriesProcessor(new CategoriesProcessor());
        race.setResultsCalculator(new TourRaceResultsCalculator());
        race.setResultsOutput(new TourRaceOutput());

        return race;
    }

    @Override
    public boolean isValidFor(final Properties properties) {

        return properties.containsKey(KEY_INDICATIVE_OF_SERIES_RACE) &&
            !properties.containsKey(KEY_INDICATIVE_OF_GRAND_PRIX_RACE) &&
            !properties.containsKey(KEY_INDICATIVE_OF_MIDWEEK_RACE);
    }
}
