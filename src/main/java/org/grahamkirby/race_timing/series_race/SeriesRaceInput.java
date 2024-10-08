/*
 * Copyright 2024 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.series_race;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.individual_race.IndividualRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.grahamkirby.race_timing.common.Race.KEY_RACES;

public class SeriesRaceInput extends RaceInput {

    public List<Path> race_config_paths;

    public SeriesRaceInput(final Race race) {

        super(race);
        readProperties();
    }

    public List<IndividualRace> loadRaces() throws IOException {

        final List<IndividualRace> races = new ArrayList<>();

        for (int i = 0; i < race_config_paths.size(); i++)
            races.add(getIndividualRace(i));

        return races;
    }

    private IndividualRace getIndividualRace(int i) throws IOException {
        final Path relative_path = race_config_paths.get(i);

        return relative_path.toString().isEmpty() ? null : getIndividualRace(relative_path, i + 1);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void readProperties() {

        race_config_paths = readRaceConfigPaths();
    }

    private List<Path> readRaceConfigPaths() {

        final String[] race_strings = race.getProperties().getProperty(KEY_RACES).split(",", -1);
        return Arrays.stream(race_strings).map(Paths::get).toList();
    }

    protected IndividualRace getIndividualRace(final Path relative_path, final int race_number) throws IOException {

        final Path individual_race_path = race.getWorkingDirectoryPath().resolve(relative_path);
        final IndividualRace individual_race = new IndividualRace(individual_race_path);

        configureIndividualRace(individual_race, race_number);
        individual_race.calculateResults();

        return individual_race;
    }

    protected void configureIndividualRace(final IndividualRace individual_race, final int race_number) {
    }
}
