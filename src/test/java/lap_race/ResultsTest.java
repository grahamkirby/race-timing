package lap_race;

import org.junit.jupiter.api.Test;
import uk.ac.standrews.cs.utilities.FileManipulation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ResultsTest {

    @Test
    public void resultsAsExpected() throws Exception {

        // To run from jar, could use https://github.com/classgraph/classgraph/wiki/Code-examples#finding-and-reading-resource-files
        // or Utilities.getResourceDirectoryEntriesFromJar

        Path temp_directory = Files.createTempDirectory(null);
        Path temp_input_sub_directory = Files.createDirectories(temp_directory.resolve("input"));
        Path temp_output_sub_directory = Files.createDirectories(temp_directory.resolve("output"));

        Path resources_inputs = Paths.get("src/test/resources/devils-burdens/input/");
        Path resources_config = Paths.get("src/test/resources/devils-burdens/race.config");
        Path resources_expected_outputs = Paths.get("src/test/resources/devils-burdens/expected/");

        copyFilesBetweenDirectories(resources_inputs, temp_input_sub_directory);

        Properties properties = getProperties(resources_config);
        properties.setProperty("WORKING_DIRECTORY", temp_directory.toString());

        new Results(properties).processResults();

        assertThatDirectoriesHaveSameContent(resources_expected_outputs, temp_output_sub_directory);

        FileManipulation.deleteDirectory(temp_directory);
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

    private static void assertThatDirectoriesHaveSameContent(final Path directory1, final Path directory2) throws IOException {

        Set<String> directory_listing_1 = getDirectoryEntries(directory1);
        Set<String> directory_listing_2 = getDirectoryEntries(directory2);

        assertEquals(directory_listing_1, directory_listing_2);

        for (String file_name : directory_listing_1) {

            Path path1 = directory1.resolve(file_name);
            Path path2 = directory2.resolve(file_name);

            if (Files.isDirectory(path1)) {
                assertTrue(Files.isDirectory(path2));
                assertThatDirectoriesHaveSameContent(path1, path2);
            }
            else {
                assertFalse(Files.isDirectory(path2));
                assertThatFilesHaveSameContent(path1, path2);
            }
        }
    }

    private static Set<String> getDirectoryEntries(Path directory) throws IOException {

        Set<String> directory_listing = new HashSet<>();

        try (Stream<Path> entries = Files.list(directory)) {
            for (Iterator<Path> iterator = entries.iterator(); iterator.hasNext(); ) {
                Path file = iterator.next();
                directory_listing.add(file.getFileName().toString());
            }
        }

        return directory_listing;
    }

    private static void assertThatFilesHaveSameContent(final Path path1, final Path path2) throws IOException {

        try (BufferedReader reader1 = Files.newBufferedReader(path1); BufferedReader reader2 = Files.newBufferedReader(path2)) {

            String line1;

            while ((line1 = reader1.readLine()) != null) {
                String line2 = reader2.readLine();
                assertEquals(line1, line2);
            }

            assertNull(reader2.readLine());
        }
    }
}
