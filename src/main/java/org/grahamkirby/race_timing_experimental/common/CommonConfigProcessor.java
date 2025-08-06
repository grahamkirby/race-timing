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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

public class CommonConfigProcessor {

    final Race race;
    final Path config_file_path;
    final Properties properties;
    final Map<String, Object> config_values;

    public CommonConfigProcessor(Race race, Path config_file_path, Properties properties) {

        this.race = race;
        this.config_file_path = config_file_path;
        this.properties = properties;

        config_values = new HashMap<>();
    }

    public Map<String, Object> getConfigValues() {
        return config_values;
    }

    public void addRequiredStringProperties(final List<String> keys) {

        for (final String key : keys)
            addRequiredProperty(key, s -> s);
    }

    public void addOptionalStringProperties(final List<String> keys) {

        for (final String key : keys)
            addOptionalProperty(key, s -> s);
    }

    public void addOptionalPathProperties(final List<String> keys) {

        for (final String key : keys)
            addOptionalProperty(key, s -> race.interpretPath(Path.of(s)));
    }

    public void addRequiredPathProperties(final List<String> keys) {

        for (final String key : keys)
            addRequiredProperty(key, s -> race.interpretPath(Path.of(s)));
    }

    public void addRequiredProperty(final String key, final Function<String, Object> make_value) {

        final String value = properties.getProperty(key);
        if (value == null) throw new RuntimeException(STR."no entry for key '\{key}' in file '\{config_file_path.getFileName()}'");
        config_values.put(key, make_value.apply(value));
    }

    private void addOptionalProperty(final String key, final Function<String, Object> make_value) {

        final String value = properties.getProperty(key);
        if (value != null) config_values.put(key, make_value.apply(value));
    }

    public void addOptionalStringProperties(final List<String> keys, final List<String> defaults) {

        if (keys.size() != defaults.size())
            throw new RuntimeException("Mismatch between number of keys and defaults");

        for (int i = 0; i < keys.size(); i++)
            addOptionalStringProperty(keys.get(i), defaults.get(i));
    }

    public void addOptionalPathProperties(final List<String> keys, final List<Path> defaults) {

        if (keys.size() != defaults.size())
            throw new RuntimeException("Mismatch between number of keys and defaults");

        for (int i = 0; i < keys.size(); i++)
            addOptionalPathProperty(keys.get(i), defaults.get(i));
    }

    private void addOptionalStringProperty(final String key, final String default_value) {

        final Function<String, Object> make_value = value -> value;
        final Function<String, Object> make_default_value = _ -> default_value;

        addOptionalProperty(key, make_value, make_default_value);
    }

    private void addOptionalPathProperty(final String key, final Path default_value) {

        final Function<String, Object> make_value = value -> race.interpretPath(Path.of(value));
        final Function<String, Object> make_default_value = _ -> race.interpretPath(default_value);

        addOptionalProperty(key, make_value, make_default_value);
    }

    public void addOptionalProperty(final String key, final Function<String, Object> make_value, final Function<String, Object> make_default_value) {

        final String value = properties.getProperty(key);

        if (value != null)
            config_values.put(key, make_value.apply(value));
        else
            config_values.put(key, make_default_value.apply(key));
    }
}
