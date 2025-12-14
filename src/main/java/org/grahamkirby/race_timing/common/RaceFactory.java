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
import org.grahamkirby.race_timing.series_race.SeriesRaceFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import static org.grahamkirby.race_timing.common.Config.loadProperties;

public class RaceFactory {

    private final List<Supplier<RaceFactory>> specialised_factories = List.of(
        IndividualRaceFactory::new,
        RelayRaceFactory::new,
        SeriesRaceFactory::new
    );

    public static void main(final String[] args) {

        new RaceFactory().createAndProcessRace(args);
    }

    public void createAndProcessRace(final String[] args) {

        try {
            final Race race = makeRace(Path.of(args[0]));

            if (race.configIsValid()) {

                final RaceResults results = race.processResults();

                // If results is null then an error has occurred during processing. Details will have been recorded
                // in the notes.
                if (results != null) {
                    if (results.getOverallResults().isEmpty())
                        race.outputRacerList();
                    else
                        race.outputResults(results);
                }
            }

            race.outputNotes();

        } catch (final Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public Race makeRace(final Path config_file_path) throws IOException {

        final Properties properties = loadProperties(config_file_path);

        for (final Supplier<RaceFactory> specialised_factory : specialised_factories) {

            final RaceFactory factory = specialised_factory.get();
            if (factory.isValidFor(properties)) return factory.makeRace(config_file_path);
        }

        throw new RuntimeException("No applicable race type for config file " + config_file_path);
    }

    // Only used in subclasses.
    public boolean isValidFor(final Properties properties) {
        return true;
    }
}
