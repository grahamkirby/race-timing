/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2026 Graham Kirby (race-timing@kirby-family.net)
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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {

    @Test
    public void createDirectory() throws IOException {

        final Path root_directory = Files.createTempDirectory(null);

        final Path inner_directory1 = root_directory.resolve("dir1");
        final Path inner_directory2 = inner_directory1.resolve("dir2");

        Config.setUpDirectory(inner_directory2);

        assertTrue(Files.exists(inner_directory2));
        assertTrue(Files.isDirectory(inner_directory2));
        assertTrue(Files.isWritable(inner_directory2));
    }

    @Test
    public void createDirectoryClashingWithFile() throws IOException {

        final Path root_directory = Files.createTempDirectory(null);

        final Path inner_directory1 = root_directory.resolve("dir1");

        Config.setUpDirectory(inner_directory1);
        final Path conflicted_path = inner_directory1.resolve("abc");

        Files.createFile(conflicted_path);

        assertThrows(
            RuntimeException.class,
            () -> Config.setUpDirectory(conflicted_path)
        );
    }

    @Test
    public void createDirectoryClashingWithinNonWriteableParent() throws IOException {

        final Path root_directory = Files.createTempDirectory(null);

        final Path inner_directory1 = root_directory.resolve("dir1");

        Config.setUpDirectory(inner_directory1);

        final Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(inner_directory1);
        permissions.remove(PosixFilePermission.OWNER_WRITE);
        Files.setPosixFilePermissions(inner_directory1, permissions);

        final Path inner_directory2 = inner_directory1.resolve("dir2");

        assertThrows(
            RuntimeException.class,
            () -> Config.setUpDirectory(inner_directory2)
        );
    }

    @Test
    public void createDirectoryClashingWithinNonTraversableParent() throws IOException {

        final Path root_directory = Files.createTempDirectory(null);

        final Path inner_directory1 = root_directory.resolve("dir1");

        Config.setUpDirectory(inner_directory1);

        final Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(inner_directory1);
        permissions.remove(PosixFilePermission.OWNER_EXECUTE);
        Files.setPosixFilePermissions(inner_directory1, permissions);

        final Path inner_directory2 = inner_directory1.resolve("dir2");

        assertThrows(
            RuntimeException.class,
            () -> Config.setUpDirectory(inner_directory2)
        );
    }
}
