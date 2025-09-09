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
package org.grahamkirby.race_timing.common;

import org.grahamkirby.race_timing.individual_race.IndividualRaceFactory;
import org.grahamkirby.race_timing.relay_race.RelayRaceFactory;
import org.grahamkirby.race_timing.series_race.GrandPrixRaceFactory;
import org.grahamkirby.race_timing.series_race.MidweekRaceFactory;
import org.grahamkirby.race_timing.series_race.TourRaceFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Config.*;

public class RaceFactory {

    public static void main(String[] args) {

        new RaceFactory().createAndProcessRace(args);
    }

    public void createAndProcessRace(String[] args) {

        try {
            final Race race = makeRace(Path.of(args[0]));

            race.processResults();
            race.outputResults();

        } catch (final Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public Race makeRace(final Path config_file_path) throws IOException {

        Properties properties = loadProperties(config_file_path);

        if (properties.containsKey(KEY_RACE_TEMPORAL_ORDER))
            return new GrandPrixRaceFactory().makeRace(config_file_path);

        if (properties.containsKey(KEY_SCORE_FOR_FIRST_PLACE))
            return new MidweekRaceFactory().makeRace(config_file_path);

        if (properties.containsKey(KEY_RACES))
            return new TourRaceFactory().makeRace(config_file_path);

        if (properties.containsKey(KEY_NUMBER_OF_LEGS))
            return new RelayRaceFactory().makeRace(config_file_path);

        return new IndividualRaceFactory().makeRace(config_file_path);
    }
}
