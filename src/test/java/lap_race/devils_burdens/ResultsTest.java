package lap_race.devils_burdens;

import lap_race.Results;
import org.junit.jupiter.api.AfterEach;
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

    // Test partial results cf results.gk 2020,
    // only process dead heat for leg rankings, separate list for each leg
    // test different number of legs
    // simple cases with DNFs
    // all legs DNF
    // 3rd leg DNF but not 4th
    // all legs DNF except 4th
    // leg dead heats with and without same start time - only latter should be heats
    // various category combinations for prizes
    // top 2 prizes are women cf cases in comments
    // add and test early start option
    // annotate detailed results with early and mass starts
    // generate PDF for overall
    // generate HTML
    // generate xls
    // illegal config - dnf lap times, mass start times / mass start times wrong order
    // 3rd leg runner starts after 4th leg mass start
    // 1st leg runner finishes after 3rd/4th leg mass start

    Properties properties;
    Path resources_expected_outputs;
    Path temp_directory;
    Path temp_output_sub_directory;

    @AfterEach
    public void tearDown() throws IOException {
        FileManipulation.deleteDirectory(temp_directory);
    }

    @Test
    public void simple() throws Exception {

        processingCompletes("simple");
    }

    @Test
    public void full() throws Exception {

        processingCompletes("full");
    }

    @Test
    public void missingResult() throws Exception {

        processingCompletes("missing_result");
    }

    @Test
    public void missingTeam() throws Exception {

        configureTest("missing_team");

        RuntimeException thrown = assertThrows(
            RuntimeException.class,
            () -> new Results(properties).processResults()
        );

        assertEquals("undefined team: 4", thrown.getMessage());
    }

    @Test
    public void extraResult() throws Exception {

        configureTest("extra_result");

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> new Results(properties).processResults()
        );

        assertEquals("surplus result recorded for team: 2; no teams missing results", thrown.getMessage());
    }

    @Test
    public void switchedResult() throws Exception {

        configureTest("switched_result");

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> new Results(properties).processResults()
        );

        assertEquals("surplus result recorded for team: 2; team(s) missing results: 4", thrown.getMessage());
    }

    private void processingCompletes(String configuration_name) throws Exception {

        configureTest(configuration_name);
        new Results(properties).processResults();
        assertThatDirectoriesHaveSameContent(resources_expected_outputs, temp_output_sub_directory);
    }

    private void configureTest(String test_resource_root) throws IOException {

        // temp_directory = Files.createTempDirectory(Paths.get("/Users/gnck/Desktop"), "temp");
        temp_directory = Files.createTempDirectory(null);

        Path temp_input_sub_directory = Files.createDirectories(temp_directory.resolve("input"));
        temp_output_sub_directory = Files.createDirectories(temp_directory.resolve("output"));

        Path resources_root = Paths.get("src/test/resources/devils_burdens/" + test_resource_root);
        Path resources_config = resources_root.resolve("race.config");
        Path resources_inputs = resources_root.resolve("input");
        resources_expected_outputs = resources_root.resolve("expected");

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
