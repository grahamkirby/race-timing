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
package org.grahamkirby.race_timing_experimental.series_race;

import org.grahamkirby.race_timing_experimental.common.CategoriesProcessorImpl;
import org.grahamkirby.race_timing_experimental.common.CommonRace;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceFactory;
import org.grahamkirby.race_timing_experimental.individual_race.RaceFactory;

import java.io.IOException;
import java.nio.file.Path;

public class TourRaceFactory extends RaceFactory {

    public static void main(String[] args) {

        new TourRaceFactory().createAndProcessRace(args);
    }

    @Override
    public Race makeRace(final Path config_file_path) throws IOException {

        Race race = new CommonRace(config_file_path);

        race.setConfigProcessor(new TourRaceConfigProcessor());
        race.setSpecific(new TourRaceImpl());
        race.setCategoriesProcessor(new CategoriesProcessorImpl());
        race.setRaceDataProcessor(new TourRaceDataProcessorImpl());
        race.setResultsCalculator(new TourRaceResultsCalculatorImpl());
        race.setResultsOutput(new TourRaceResultsOutput());

        return race;
    }
}
