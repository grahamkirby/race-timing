package lap_race;

import common.MissingTimeException;
import common.RawResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LapRaceInput {

    final LapRace race;

    Path input_directory_path, entries_path, raw_results_path, paper_results_path;
    String entries_filename, raw_results_filename, paper_results_filename;

    int number_of_raw_results;

    public LapRaceInput(final LapRace race) {

        this.race = race;
        configure();
    }

    private void configure() {

        readProperties();
        constructFilePaths();
    }

    private void readProperties() {

        entries_filename = race.getProperties().getProperty("ENTRIES_FILENAME");
        raw_results_filename = race.getProperties().getProperty("RAW_RESULTS_FILENAME");
        paper_results_filename = race.getProperties().getProperty("PAPER_RESULTS_FILENAME");
    }

    private void constructFilePaths() {

        input_directory_path = race.getWorkingDirectoryPath().resolve("input");
        entries_path = input_directory_path.resolve(entries_filename);
        raw_results_path = input_directory_path.resolve(raw_results_filename);
        paper_results_path = input_directory_path.resolve(paper_results_filename);
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

        if (team_elements.length != race.number_of_legs + 3)
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

        final List<RawResult> raw_results = new ArrayList<>();

        for (String line : Files.readAllLines(raw_results_path))
            loadRawResult(raw_results, line);

        number_of_raw_results = raw_results.size();

        for (String line : Files.readAllLines(paper_results_path))
            loadRawResult(raw_results, line);

        return raw_results.toArray(new RawResult[0]);
    }

    int getNumberOfRawResults() {
        return number_of_raw_results;
    }

    private static void loadRawResult(final List<RawResult> raw_results, String line) {

        int comment_start_index = line.indexOf("//");
        if (comment_start_index > -1) line = line.substring(0, comment_start_index);

        if (!line.isBlank()) {

            RawResult result;
            try {
                result = new RawResult(line);
            }
            catch (NumberFormatException e) {
                return;
            }

            final RawResult previous_result = !raw_results.isEmpty() ? raw_results.get(raw_results.size() - 1) : null;

            if (result.getRecordedFinishTime() != null && previous_result != null && previous_result.getRecordedFinishTime() != null && previous_result.getRecordedFinishTime().compareTo(result.getRecordedFinishTime()) > 0)
                throw new RuntimeException("result " + (raw_results.size() + 1) + " out of order");

            raw_results.add(result);
        }
    }
}
