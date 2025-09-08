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
import org.grahamkirby.race_timing.common.Race;

import java.io.IOException;
import java.nio.file.Path;

public class IndividualRaceFactory extends RaceFactory {

    public static void main(String[] args) {

        new IndividualRaceFactory().createAndProcessRace(args);
    }

    @Override
    public Race makeRace(final Path config_file_path) throws IOException {

        Race race = new Race(config_file_path);

        race.addConfigProcessor(new IndividualRaceConfigAdjuster());
        race.addConfigProcessor(new IndividualRaceConfigValidator());
        race.loadConfig();
        race.setSpecific(new IndividualRaceImpl());
        race.setCategoriesProcessor(new CategoriesProcessor());
        race.setRaceDataProcessor(new IndividualRaceDataProcessorImpl());
        race.setResultsCalculator(new IndividualRaceResultsCalculatorImpl());
        race.setResultsOutput(new IndividualRaceResultsOutput());

        return race;
    }
}
