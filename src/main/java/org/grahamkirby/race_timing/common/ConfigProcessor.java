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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class ConfigProcessor {

    protected final Config config;

    public ConfigProcessor(final Config config) {
        this.config = config;
    }

    public abstract void processConfig();

    protected void checkAllPresent(final List<String> keys) {

        for (final String key : keys) {
            if (!config.containsKey(key))
                throw new RuntimeException("no entry for key '" + key + "' in file '" + config.getConfigPath().getFileName() + "'");
        }
    }

    protected void checkAllFilesExist(final List<String> keys) {

        for (final String key : keys) {
            final Path path = config.getPath(key);

            if (!Files.exists(path))
                throw new RuntimeException("invalid entry '" + path.getFileName() + "' for key '" + key + "' in file '" + config.getConfigPath().getFileName() + "'");
        }
    }

    protected void checkExactlyOnePresent(final List<String> keys) {

        if (countKeysPresent(keys) != 1)
            throw new RuntimeException("should have exactly one key from {" + String.join(", ", keys) + "} in file '" + config.getConfigPath().getFileName() + "'");
    }

    protected void checkNonePresent(final List<String> keys) {

        if (countKeysPresent(keys) > 0)
            throw new RuntimeException("should have no keys from {" + String.join(", ", keys) + "} in file '" + config.getConfigPath().getFileName() + "'");
    }

    protected void checkAllOrNonePresent(final List<String> keys) {

        final int count = countKeysPresent(keys);

        if (count > 0 && count < keys.size())
            throw new RuntimeException("should have no or all keys from {" + String.join(", ", keys) + "} in file '" + config.getConfigPath().getFileName() + "'");
    }

    protected void checkAtMostOnePresent(final List<String> keys) {

        if (countKeysPresent(keys) > 1)
            throw new RuntimeException("should have no more than one key from {" + String.join(", ", keys) + "} in file '" + config.getConfigPath().getFileName() + "'");
    }

    private int countKeysPresent(final List<String> keys) {

        return (int) keys.stream().filter(config::containsKey).count();
    }
}
