/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.individual_race.TimedIndividualRace;
import org.grahamkirby.race_timing.individual_race.UntimedIndividualRace;
import org.grahamkirby.race_timing.single_race.SingleRace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.grahamkirby.race_timing.common.Race.loadProperties;
import static org.grahamkirby.race_timing_experimental.common.Config.*;

public class SeriesRaceInput extends RaceInput {

    private List<String> race_config_paths;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public SeriesRaceInput(final Race race) {

        super(race);
        readProperties();
    }

    @Override
    protected void validateRequiredPropertiesPresent() {

        super.validateRequiredPropertiesPresent();

        race.getRequiredProperty(KEY_RACES);
        race.getRequiredProperty(KEY_NUMBER_OF_RACES_IN_SERIES);
        race.getRequiredProperty(KEY_MINIMUM_NUMBER_OF_RACES);
    }

    List<SingleRace> loadRaces() throws IOException {

        final int number_of_race_in_series = ((SeriesRace) race).getNumberOfRacesInSeries();
        if (number_of_race_in_series != race_config_paths.size())
            throw new RuntimeException(STR."invalid number of races specified in file '\{race.config_file_path.getFileName()}'");

        final List<SingleRace> races = new ArrayList<>();
        final List<String> config_paths_seen = new ArrayList<>();

        for (int i = 0; i < number_of_race_in_series; i++) {

            final String race_config_path = race_config_paths.get(i);

            if (race_config_path.isBlank())
                races.add(null);
            else {
                if (config_paths_seen.contains(race_config_path))
                    throw new RuntimeException(STR."duplicate races specified in file '\{race.config_file_path.getFileName()}'");
                config_paths_seen.add(race_config_path);
                races.add(getIndividualRace(race_config_path, i + 1));
            }
        }

        return races;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void readProperties() {

        race_config_paths = Arrays.asList(race.getRequiredProperty(KEY_RACES).split(",", -1));
    }

    private SingleRace getIndividualRace(final String race_config_path, final int race_number) throws IOException {

        final SingleRace individual_race;
        final Path config_path = race.getPath(race_config_path);
        if (!Files.exists(config_path))
            throw new RuntimeException(STR."invalid config for race \{race_number} in file '\{race.config_file_path.getFileName()}'");

        if (loadProperties(config_path).containsKey(KEY_RAW_RESULTS_PATH))
            individual_race = new TimedIndividualRace(config_path);
        else
            individual_race = new UntimedIndividualRace(config_path);

        configureIndividualRace(individual_race, race_number);
        individual_race.calculateResults();

        return individual_race;
    }

    protected void configureIndividualRace(final SingleRace individual_race, final int race_number) {
    }
}
