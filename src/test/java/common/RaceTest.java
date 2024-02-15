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

    protected void configureTest(String test_resource_root) throws IOException {

        // Swap these when debugging and you don't want the test results to be immediately deleted.

        temp_directory = Files.createTempDirectory(null);
        //temp_directory = Paths.get("/Users/gnck/Desktop/temp");

        Path temp_input_sub_directory = Files.createDirectories(temp_directory.resolve("input"));
        temp_output_sub_directory = Files.createDirectories(temp_directory.resolve("output"));

        Path resources_root = Paths.get("src/test/resources/" + getResourcesPath() + test_resource_root);
        Path resources_inputs = resources_root.resolve("input");
        resources_expected_outputs = resources_root.resolve("expected");
        Path resources_config = resources_inputs.resolve("config.txt");

        copyFilesBetweenDirectories(resources_inputs, temp_input_sub_directory);

        properties = getProperties(resources_config);
        properties.setProperty("WORKING_DIRECTORY", temp_directory.toString());
    }

    private static void copyFilesBetweenDirectories(Path source, Path destination) throws IOException {

        try (Stream<Path> list = Files.list(source)) {
            for (Iterator<Path> iterator = list.iterator(); iterator.hasNext(); ) {
                Path file = iterator.next();
                Files.copy(file, destination.resolve(file.getFileName()));
            }
        }
    }

    private static Properties getProperties(Path path) throws IOException {

        try (FileInputStream in = new FileInputStream(path.toFile())) {
            Properties properties = new Properties();
            properties.load(in);
            return properties;
        }
    }

    public void testExpectedException(String configuration_name, String expected_error_message) throws Exception {

        configureTest(configuration_name);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> makeRace(properties).processResults()
        );

        assertEquals(expected_error_message, thrown.getMessage());
    }

    protected void testExpectedCompletion(String configuration_name) throws Exception {

        configureTest(configuration_name);
        makeRace(properties).processResults();
        assertThatDirectoryContainsAllExpectedContent(resources_expected_outputs, temp_output_sub_directory);
    }

    private static void assertThatDirectoryContainsAllExpectedContent(final Path expected, final Path actual) throws IOException {

        final Set<String> directory_listing_expected = getDirectoryEntries(expected);

        for (final String file_name : directory_listing_expected) {

            if (!file_name.equals(".DS_Store")) {

                final Path path_expected = expected.resolve(file_name);
                final Path path_actual = actual.resolve(file_name);

                if (Files.isDirectory(path_expected)) {
                    assertTrue(Files.isDirectory(path_actual));
                    assertThatDirectoryContainsAllExpectedContent(path_expected, path_actual);
                } else {
                    assertFalse(Files.isDirectory(path_actual));
                    assertThatFilesHaveSameContent(path_expected, path_actual);
                }
            }
        }
    }

    private static Set<String> getDirectoryEntries(final Path directory) throws IOException {

        final Set<String> directory_listing = new HashSet<>();

        try (Stream<Path> entries = Files.list(directory)) {
            for (Iterator<Path> iterator = entries.iterator(); iterator.hasNext(); ) {
                final Path file = iterator.next();
                directory_listing.add(file.getFileName().toString());
            }
        }

        return directory_listing;
    }

    private static void assertThatFilesHaveSameContent(final Path path1, final Path path2) throws IOException {

        byte[] expected = Files.readAllBytes(path1);
        byte[] actual = Files.readAllBytes(path2);

        if (!Arrays.equals(expected, actual) && !filesHaveSameContentIgnoreWhitespace(path1, path2))
            fail("Files differ: " + path1 + ", " + path2);
    }

    private static boolean filesHaveSameContentIgnoreWhitespace(Path path1, Path path2) throws IOException {

        String fileContent1 = getFileContent(path1);
        String fileContent2 = getFileContent(path2);

        if (!fileContent1.equals(fileContent2)) {
            System.out.println("f1: " + fileContent1);
            System.out.println("f2: " + fileContent2);
        }
        return fileContent1.equals(fileContent2);
    }

    private static String getFileContent(Path path) throws IOException {

        return Files.readAllLines(path).stream().reduce((s1, s2) -> s1 + s2).orElseThrow().replaceAll("\t", "").replaceAll("\n", "").replaceAll(" ", "");
    }
}
