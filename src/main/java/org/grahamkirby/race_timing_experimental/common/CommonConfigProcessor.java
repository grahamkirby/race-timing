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
package org.grahamkirby.race_timing_experimental.common;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.grahamkirby.race_timing_experimental.common.Config.*;
import static org.grahamkirby.race_timing_experimental.common.Config.KEY_YEAR;

public class CommonConfigProcessor {

    final Race race;
    final Path config_file_path;
    final Properties properties;

    public CommonConfigProcessor(Race race, Path config_file_path, Properties properties) {

        this.race = race;
        this.config_file_path = config_file_path;
        this.properties = properties;
    }

    public Map<String, Object> getCommonConfig() {

        final Map<String, Object> config_values = new HashMap<>();

        addRequiredStringProperty(KEY_YEAR, config_values);
        addRequiredStringProperty(KEY_RACE_NAME_FOR_RESULTS, config_values);
        addRequiredStringProperty(KEY_RACE_NAME_FOR_FILENAMES, config_values);
        addRequiredPathProperty(KEY_ENTRY_CATEGORIES_PATH, config_values);
        addRequiredPathProperty(KEY_PRIZE_CATEGORIES_PATH, config_values);

        return config_values;
    }

    private void addRequiredStringProperty(final String key, final Map<String, Object> config_values) {

        addRequiredProperty(key, config_values, s -> s);
    }

    private void addRequiredPathProperty(final String key, final Map<String, Object> config_values) {

        addRequiredProperty(key, config_values, s -> race.interpretPath(Path.of(s)));
    }

    private void addRequiredProperty(final String key, final Map<String, Object> config_values, final Function<String, Object> makeProperty) {

        final String value = properties.getProperty(key);
        if (value == null) throw new RuntimeException(STR."no entry for key '\{key}' in file '\{config_file_path.getFileName()}'");
        config_values.put(key, makeProperty.apply(value));
    }
}
