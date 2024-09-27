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
package org.grahamkirby.race_timing;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.grahamkirby.race_timing.common.Race;
import org.junit.jupiter.api.AfterEach;
import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

import static java.nio.file.FileVisitResult.CONTINUE;
import static org.junit.jupiter.api.Assertions.*;

public abstract class RaceTest {

    private Path config_file_path;
    private Path expected_output_directory;
    private Path temp_directory;
    private Path temp_output_directory;

    // When enabled, test results are retained on the desktop.
    private static final boolean DEBUG = false;
    private static final String DEBUG_FILES_LOCATION = "/Users/gnck/Desktop/temp";

    protected abstract Race makeRace(Path config_file_path) throws IOException;
    protected abstract String getResourcesPath();

    @AfterEach
    public void tearDown() throws IOException {

        if (!DEBUG) FileManipulation.deleteDirectory(temp_directory);
    }

    protected void configureTest(final String test_resource_root) throws IOException {

        temp_directory = DEBUG ? Paths.get(DEBUG_FILES_LOCATION) : Files.createTempDirectory("temp");

        final Path resources_root = Paths.get("src/test/resources/").resolve(getResourcesPath()).resolve(test_resource_root);
        final Path resources_inputs = resources_root.resolve("input");
        expected_output_directory = resources_root.resolve("expected");

        temp_output_directory = Files.createDirectories(temp_directory.resolve("output"));

        final Path temp_input_directory = temp_directory.resolve("input");
        config_file_path = temp_input_directory.resolve("config.txt");

        copyDirectory(resources_inputs, temp_input_directory);
    }

    public void testExpectedException(final String configuration_name, final String expected_error_message) throws Exception {

        configureTest(configuration_name);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> makeRace(config_file_path).processResults()
        );

        assertEquals(expected_error_message, thrown.getMessage());
    }

    protected void testExpectedCompletion(final String configuration_name) throws Exception {

        configureTest(configuration_name);
        makeRace(config_file_path).processResults();

        assertThatDirectoryContainsAllExpectedContent(expected_output_directory, temp_output_directory);
    }

    private static void assertThatDirectoryContainsAllExpectedContent(final Path expected, final Path actual) throws IOException {

        final Set<String> directory_listing_expected = getDirectoryEntries(expected);

        for (final String file_name : directory_listing_expected) {

            if (!fileInExpectedDirectoryShouldBeIgnored(file_name)) {

                final Path path_expected = expected.resolve(file_name);
                final Path path_actual = actual.resolve(file_name);

                if (Files.isDirectory(path_expected)) {
                    assertTrue(Files.isDirectory(path_actual));
                    assertThatDirectoryContainsAllExpectedContent(path_expected, path_actual);
                } else {
                    assertFalse(Files.isDirectory(path_actual));
                    assertThatFilesHaveSameContentIgnoringWhitespace(path_expected, path_actual);
                }
            }
        }
    }

    private static Set<String> getDirectoryEntries(final Path directory) throws IOException {

        final Set<String> directory_listing = new HashSet<>();

        try (final Stream<Path> entries = Files.list(directory)) {
            for (final Iterator<Path> iterator = entries.iterator(); iterator.hasNext();)
                directory_listing.add(iterator.next().getFileName().toString());
        }

        return directory_listing;
    }

    private static void assertThatFilesHaveSameContentIgnoringWhitespace(final Path path1, final Path path2) throws IOException {

        final String file_content1 = removeWhiteSpace(getFileContent(path1));
        final String file_content2 = removeWhiteSpace(getFileContent(path2));

        if (!file_content1.equals(file_content2))
            fail("Files differ: " + path1 + ", " + path2);
    }

    private static String getFileContent(final Path path) throws IOException {

        if (!path.toString().endsWith(".pdf"))
            return Files.readAllLines(path).stream().reduce((s1, s2) -> s1 + s2).orElseThrow();
        else {

            final PdfReader reader = new PdfReader(path.toString());

            try (final PdfDocument pdfDocument = new PdfDocument(reader)) {

                final StringBuilder text = new StringBuilder();
                for (int i = 1; i <= pdfDocument.getNumberOfPages(); i++) {
                    text.append(PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i)));
                }

                return text.toString();
            }
        }
    }

    private static String removeWhiteSpace(final String s) {

        return s.replaceAll("\t", "").replaceAll("\n", "").replaceAll(" ", "");
    }

    private static boolean fileInExpectedDirectoryShouldBeIgnored(final String file_name) {
        return file_name.equals(".DS_Store");
    }

    private static void copyDirectory(final Path source_directory, final Path destination_directory) throws IOException {

        final FileVisitor<Path> copier = new FileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                Files.copy(dir, destination_directory.resolve(source_directory.relativize(dir)));
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                Files.copy(file, destination_directory.resolve(source_directory.relativize(file)));
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                return CONTINUE;
            }
        };

        Files.walkFileTree(source_directory, copier);
    }
}
