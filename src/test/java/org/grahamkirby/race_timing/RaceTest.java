/*
 * Copyright 2025 Graham Kirby:
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

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.file.FileVisitResult.CONTINUE;
import static org.grahamkirby.race_timing.common.Normalisation.SUFFIX_PDF;
import static org.junit.jupiter.api.Assertions.*;

public abstract class RaceTest {

    // File names that may be present in list of expected output files for a given test, but should be ignored.
    private static final List<String> ignored_file_names = loadIgnoredFileNames();

    //////////////////////////////////////////////////////////////////////////////////////////////////

    // Each test specifies a set of input files, located in test/resources, and checks either that the
    // expected output files are generated, or that the expected error occurs.
    //
    // If the DEBUG flag below is disabled then output files are generated within a temporary directory
    // and deleted at the end of each test.
    //
    // If DEBUG is enabled then output files are generated within a user-specified directory. If the
    // flag RETAIN_FIRST_OUTPUT is also enabled, then the output files generated by the first test are
    // retained for inspection at the end of the run. Otherwise, the output files generated by the
    // first failing test, if any, are retained.

    private static final boolean DEBUG = false;
    private static final boolean RETAIN_FIRST_OUTPUT = false;

    private static final String USER_TEST_DIRECTORY_PATH = "/Users/gnck/Desktop/tests";
    private static final String IGNORED_FILE_NAMES_PATH = "src/main/resources/configuration/test_ignored_file_names.csv";

    // Whether the current test is the first test in the run.
    private static boolean first_test = true;

    // Whether at least one test failed earlier in the run.
    private static boolean previous_failed_test = false;

    // Whether the current test failed.
    private boolean failed_test = true;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private Path config_file_path;
    private Path resources_input_directory;
    private Path test_directory;
    private Path test_input_directory;
    private Path test_output_directory;
    private Path retained_output_directory;
    private Path expected_output_directory;

    protected abstract Race makeRace(Path config_file_path) throws IOException;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @AfterEach
    public void tearDown() throws IOException {

        if (DEBUG) {

            // Whether this is the first test in the run to have failed.
            final boolean first_failed_test = failed_test && !previous_failed_test;

            // Whether the retained output directory is present from a previous run (in which case it should be deleted).
            final boolean output_directory_retained_from_previous_run = first_test && Files.exists(retained_output_directory);

            // Whether the output directory from this run should be retained, either because this is the first test
            // in the run to fail, or this is the first test in the run and RETAIN_FIRST_OUTPUT is set.
            final boolean output_directory_should_be_retained = first_failed_test || (first_test && RETAIN_FIRST_OUTPUT);

            // Delete the input and output directories but retain the parent test directory, to make it easier to
            // view the contents on repeated debugging runs.
            // Also retain a copy of the output directory for review if appropriate.
            cleanUpDirectories(output_directory_retained_from_previous_run, output_directory_should_be_retained);

            first_test = false;
            if (first_failed_test) previous_failed_test = true;

        } else
            deleteDirectory(test_directory);
    }

    private void cleanUpDirectories(final boolean output_directory_retained_from_previous_run, final boolean output_directory_should_be_retained) throws IOException {

        if (output_directory_retained_from_previous_run)
            deleteDirectory(retained_output_directory);

        if (output_directory_should_be_retained)
            copyDirectory(test_output_directory, retained_output_directory);

        deleteDirectory(test_input_directory);
        deleteDirectory(test_output_directory);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void configureTest(final String individual_test_resource_root) throws IOException {

        test_directory = DEBUG ? Paths.get(USER_TEST_DIRECTORY_PATH) : Files.createTempDirectory(null);

        configureDirectories(individual_test_resource_root);
        configureDirectoryContents(resources_input_directory);
    }

    void testExpectedException(final String configuration_name, final String expected_error_message) throws IOException {

        configureTest(configuration_name);

        final Exception exception = assertThrows(
            RuntimeException.class,
            () -> makeRace(config_file_path).processResults()
        );

        assertEquals(expected_error_message, exception.getMessage(), "Unexpected exception message");

        // Test has passed if this line is reached.
        failed_test = false;
    }

    protected void testExpectedCompletion(final String configuration_name) throws IOException {

        configureTest(configuration_name);
        makeRace(config_file_path).processResults();

        assertThatDirectoryContainsAllExpectedContent(expected_output_directory, test_output_directory);

        // Test has passed if this line is reached.
        failed_test = false;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void configureDirectories(final String individual_test_resource_root) {

        final Path resources_root_directory = Race.getTestResourcesRootPath(individual_test_resource_root);

        resources_input_directory = resources_root_directory.resolve("input");
        expected_output_directory = resources_root_directory.resolve("expected");

        test_input_directory = test_directory.resolve("input");
        test_output_directory = test_directory.resolve("output");
        retained_output_directory = test_directory.resolve("output_retained");

        config_file_path = test_input_directory.resolve("config.txt");
    }

    private void configureDirectoryContents(final Path resources_inputs) throws IOException {

        Files.createDirectories(test_output_directory);
        if (Files.exists(test_input_directory)) deleteDirectory(test_input_directory);

        copyDirectory(resources_inputs, test_input_directory);
    }

    private static void assertThatDirectoryContainsAllExpectedContent(final Path expected, final Path actual) throws IOException {

        for (final String expected_file_name : getDirectoryEntries(expected)) {

            if (!shouldFileInExpectedDirectoryBeIgnored(expected_file_name)) {

                final Path path_expected = expected.resolve(expected_file_name);
                final Path path_actual = actual.resolve(expected_file_name);

                if (Files.isDirectory(path_expected)) {

                    assertTrue(Files.isDirectory(path_actual), "Expected directory missing");
                    assertThatDirectoryContainsAllExpectedContent(path_expected, path_actual);

                } else {
                    assertFalse(Files.isDirectory(path_actual), "Unexpected directory");
                    assertThatFilesHaveSameContent(path_expected, path_actual);
                }
            }
        }
    }

    private static List<String> getDirectoryEntries(final Path directory) throws IOException {

        try (final Stream<Path> list = Files.list(directory)) {
            return list.map(path -> path.getFileName().toString()).toList();
        }
    }

    private static void assertThatFilesHaveSameContent(final Path path1, final Path path2) {

        if (!getFileContent(path1).equals(getFileContent(path2)))
            fail(STR."Files differ: \{path1}, \{path2}");
    }

    private static String getFileContent(final Path path) {

        try {
            if (path.toString().endsWith(SUFFIX_PDF)) {
                try (final PdfDocument document = new PdfDocument(new PdfReader(path.toString()))) {

                    final StringBuilder text = new StringBuilder();
                    for (int i = 1; i <= document.getNumberOfPages(); i++)
                        text.append(PdfTextExtractor.getTextFromPage(document.getPage(i)));

                    return text.toString();
                }
            } else return String.join("", Files.readAllLines(path));

        } catch (final IOException e) {
            fail(STR."Expected output file not found: \{path}");
            throw new RuntimeException(e);
        }
    }

    private static List<String> loadIgnoredFileNames() {

        try {
            return Files.readAllLines(Paths.get(IGNORED_FILE_NAMES_PATH)).stream().toList();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean shouldFileInExpectedDirectoryBeIgnored(final String file_name) {
        return ignored_file_names.contains(file_name);
    }

    private static void copyDirectory(final Path source_directory, final Path destination_directory) throws IOException {

        Files.walkFileTree(source_directory, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {

                Files.copy(dir, destination_directory.resolve(source_directory.relativize(dir)));
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {

                Files.copy(file, destination_directory.resolve(source_directory.relativize(file)));
                return CONTINUE;
            }
        });
    }

    private static void deleteDirectory(final Path directory) throws IOException {

        Files.walkFileTree(directory, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) throws IOException {

                Files.delete(dir);
                return CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {

                Files.delete(file);
                return CONTINUE;
            }
        });
    }
}
