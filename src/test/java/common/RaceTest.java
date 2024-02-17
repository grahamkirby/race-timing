package common;

import org.junit.jupiter.api.AfterEach;
import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public abstract class RaceTest {

    private Properties properties;
    private Path resources_expected_outputs;
    private Path temp_directory;
    private Path temp_output_sub_directory;

    protected abstract Race makeRace(Properties properties) throws IOException;
    protected abstract String getResourcesPath();

    @AfterEach
    public void tearDown() throws IOException {

        // Disable this when debugging and you don't want the test results to be immediately deleted.

        FileManipulation.deleteDirectory(temp_directory);
    }

    protected void configureTest(final String test_resource_root) throws IOException {

        // Swap these when debugging and you don't want the test results to be immediately deleted.

        //temp_directory = Files.createTempDirectory(null);
        temp_directory = Paths.get("/Users/gnck/Desktop/temp");

        final Path temp_input_sub_directory = Files.createDirectories(temp_directory.resolve("input"));
        temp_output_sub_directory = Files.createDirectories(temp_directory.resolve("output"));

        final Path resources_root = Paths.get("src/test/resources/" + getResourcesPath() + test_resource_root);
        final Path resources_inputs = resources_root.resolve("input");
        resources_expected_outputs = resources_root.resolve("expected");
        final Path resources_config = resources_inputs.resolve("config.txt");

        copyFilesBetweenDirectories(resources_inputs, temp_input_sub_directory);

        properties = getProperties(resources_config);
        properties.setProperty("WORKING_DIRECTORY", temp_directory.toString());
    }

    private static void copyFilesBetweenDirectories(final Path source, final Path destination) throws IOException {

        try (final Stream<Path> list = Files.list(source)) {

            for (final Iterator<Path> iterator = list.iterator(); iterator.hasNext(); ) {
                Path file = iterator.next();
                Files.copy(file, destination.resolve(file.getFileName()));
            }
        }
    }

    private static Properties getProperties(final Path path) throws IOException {

        try (final FileInputStream in = new FileInputStream(path.toFile())) {

            Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }

    public void testExpectedException(final String configuration_name, final String expected_error_message) throws Exception {

        configureTest(configuration_name);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> makeRace(properties).processResults()
        );

        assertEquals(expected_error_message, thrown.getMessage());
    }

    protected void testExpectedCompletion(final String configuration_name) throws Exception {

        configureTest(configuration_name);
        makeRace(properties).processResults();
        assertThatDirectoryContainsAllExpectedContent(resources_expected_outputs, temp_output_sub_directory);
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

        return Files.readAllLines(path).stream().reduce((s1, s2) -> s1 + s2).orElseThrow();
    }

    private static String removeWhiteSpace(final String s) {

        return s.replaceAll("\t", "").replaceAll("\n", "").replaceAll(" ", "");
    }

    private static boolean fileInExpectedDirectoryShouldBeIgnored(final String file_name) {

        return file_name.equals(".DS_Store");
    }
}
