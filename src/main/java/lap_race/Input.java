package lap_race;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Input {

    final Results results;

    Path input_directory_path;
    Path entries_path;
    Path raw_results_path;
    String entries_filename;
    String raw_results_filename;

    public Input(final Results results) {

        this.results = results;
        configure();
    }

    private void configure() {

        readProperties();
        constructFilePaths();
    }

    private void readProperties() {

        entries_filename = results.properties.getProperty("ENTRIES_FILENAME");
        raw_results_filename = results.properties.getProperty("RAW_RESULTS_FILENAME");
    }

    private void constructFilePaths() {

        input_directory_path = results.working_directory_path.resolve("input");
        entries_path = input_directory_path.resolve(entries_filename);
        raw_results_path = input_directory_path.resolve(raw_results_filename);
    }

    Team[] loadEntries() throws IOException {

        final List<String> lines = Files.readAllLines(entries_path);
        Team[] entries = new Team[lines.size()];

        for (int i = 0; i < entries.length; i++) {
            String[] strings = lines.get(i).split("\t");
            if (strings.length != results.number_of_legs + 3)
                throw new RuntimeException("illegal composition for team: " + strings[0]);
            entries[i] = new Team(strings);
        }

        return entries;
    }

    RawResult[] loadRawResults() throws IOException {

        final List<String> lines = Files.readAllLines(raw_results_path);
        RawResult[]raw_results = new RawResult[lines.size()];

        RawResult previous_result = null;

        for (int i = 0; i < raw_results.length; i++) {

            final RawResult result = new RawResult(lines.get(i));
            if (previous_result != null && previous_result.recorded_finish_time.compareTo(result.recorded_finish_time) > 0)
                throw new RuntimeException("result " + (i+1) + " out of order");

            raw_results[i] = result;
            previous_result = result;
        }

        return raw_results;
    }
}
