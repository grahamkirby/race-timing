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
import java.util.List;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Config.*;

public class RaceFactory {

    private final List<SpecialisedRaceFactory> specialised_factories = List.of(
        new IndividualRaceFactory(),
        new RelayRaceFactory(),
        new GrandPrixRaceFactory(),
        new MidweekRaceFactory(),
        new TourRaceFactory()
    );

    public static void main(final String[] args) {

        new RaceFactory().createAndProcessRace(args);
    }

    public void createAndProcessRace(final String[] args) {

        try {
            final Race race = makeRace(Path.of(args[0]));

            race.processResults();
            race.outputResults();

        } catch (final Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public Race makeRace(final Path config_file_path) throws IOException {

        final Properties properties = loadProperties(config_file_path);

        for (final SpecialisedRaceFactory specialised_factory : specialised_factories)
            if (specialised_factory.isValidFor(properties))
                return specialised_factory.makeRace(config_file_path);

        throw new RuntimeException("No applicable race type for config file " + config_file_path);
    }
}
