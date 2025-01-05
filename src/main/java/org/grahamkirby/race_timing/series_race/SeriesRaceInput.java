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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeriesRaceInput extends RaceInput {

    // Configuration file keys.
    private static final String KEY_RACES = "RACES";

    private List<String> race_config_paths;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public SeriesRaceInput(final Race race) {

        super(race);
        readProperties();
    }

    List<IndividualRace> loadRaces() throws IOException {

        final List<IndividualRace> races = new ArrayList<>();

        for (int i = 0; i < race_config_paths.size(); i++)
            races.add(getIndividualRace(i));

        return races;
    }

    private IndividualRace getIndividualRace(final int i) throws IOException {

        final String race_config_path = race_config_paths.get(i);

        return getIndividualRace(race_config_path, i + 1);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void readProperties() {

        race_config_paths = Arrays.asList(race.getProperty(KEY_RACES).split(",", -1));
    }

    private IndividualRace getIndividualRace(final String race_config_path, final int race_number) throws IOException {

        final IndividualRace individual_race = new IndividualRace(race.getPath(race_config_path));

        configureIndividualRace(individual_race, race_number);
        individual_race.calculateResults();

        return individual_race;
    }

    protected void configureIndividualRace(final IndividualRace individual_race, final int race_number) {
    }
}
