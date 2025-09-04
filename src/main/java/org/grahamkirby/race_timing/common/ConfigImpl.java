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

import java.nio.file.Path;
import java.util.Map;

public class ConfigImpl implements Config {

    private final Map<String, Object> config_map;
    private final Path config_path;

    public ConfigImpl(Map<String, Object> config_map, final Path config_path) {

        this.config_map = config_map;
        this.config_path = config_path;
    }

    @Override
    public Object get(final String key) {
        return config_map.get(key);
    }

    @Override
    public boolean containsKey(final String key) {
        return config_map.containsKey(key);
    }

    @Override
    public Path getConfigPath() {
        return config_path;
    }
}
