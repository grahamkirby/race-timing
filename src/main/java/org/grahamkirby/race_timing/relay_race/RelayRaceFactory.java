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
package org.grahamkirby.race_timing.relay_race;

import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import static org.grahamkirby.race_timing.common.Config.KEY_NUMBER_OF_LEGS;
import static org.grahamkirby.race_timing.common.Config.LINE_SEPARATOR;

public class RelayRaceFactory extends RaceFactory {

    public static final String KEY_INDICATIVE_OF_RELAY_RACE = KEY_NUMBER_OF_LEGS;

    @Override
    public Race makeRace(final Path config_file_path) throws IOException {

        final Config config = makeRelayRaceConfig(config_file_path);

        final RelayRace race = new RelayRace(config);

        try {
            config.processConfigAdjusters();
            config.processConfigValidators();
            race.initialise();
        }
        catch (final Exception e) {

            // Reset output in case config validation failed, before output initialisation.
            race.setOutput(new RelayRaceOutput(config));
            race.getNotesProcessor().appendToNotes(e.getMessage() + LINE_SEPARATOR);
        }

        return race;
    }

    @Override
    public boolean isValidFor(final Properties properties) {

        return properties.containsKey(KEY_INDICATIVE_OF_RELAY_RACE);
    }

    private Config makeRelayRaceConfig(final Path config_file_path) throws IOException {

        final Config config = new Config(config_file_path);

        config.addConfigAdjuster(RaceConfigAdjuster::new);
        config.addConfigAdjuster(RelayRaceConfigAdjuster::new);

        config.addConfigValidator(RaceConfigValidator::new);
        config.addConfigValidator(RelayRaceConfigValidator::new);

        return config;
    }
}
