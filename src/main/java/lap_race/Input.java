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
        final Team[] entries = new Team[lines.size()];

        for (int i = 0; i < entries.length; i++)
            loadEntry(entries, lines, i);

        return entries;
    }

    private void loadEntry(final Team[] entries, final List<String> lines, final int entry_index) {

        final String[] team_elements = lines.get(entry_index).split("\t");

        if (team_elements.length != results.number_of_legs + 3)
            throw new RuntimeException("illegal composition for team: " + team_elements[0]);

        final int bib_number = Integer.parseInt(team_elements[0]);
        final String team_name = team_elements[1];

        if (entriesAlreadyContain(entries, bib_number))
            throw new RuntimeException("duplicate team number: " + bib_number);

        if (entriesAlreadyContain(entries, team_name))
            throw new RuntimeException("duplicate team name: " + team_name);

        entries[entry_index] = new Team(team_elements);
    }

    private boolean entriesAlreadyContain(final Team[] entries, final int bib_number) {

        for (Team team : entries)
            if (team != null && team.bib_number == bib_number) return true;
        return false;
    }

    private boolean entriesAlreadyContain(final Team[] entries, final String team_name) {

        for (Team team : entries)
            if (team != null && team.name.equals(team_name)) return true;
        return false;
    }

    RawResult[] loadRawResults() throws IOException {

        final List<String> lines = Files.readAllLines(raw_results_path);
        final RawResult[] raw_results = new RawResult[lines.size()];

        for (int i = 0; i < raw_results.length; i++)
            loadRawResult(raw_results, lines, i);

        return raw_results;
    }

    private static void loadRawResult(final RawResult[] raw_results, final List<String> lines, final int raw_result_index) {

        final RawResult previous_result = raw_result_index > 0 ? raw_results[raw_result_index -1] : null;
        final RawResult result = new RawResult(lines.get(raw_result_index));

        if (previous_result != null && previous_result.recorded_finish_time.compareTo(result.recorded_finish_time) > 0)
            throw new RuntimeException("result " + (raw_result_index +1) + " out of order");

        raw_results[raw_result_index] = result;
    }
}
