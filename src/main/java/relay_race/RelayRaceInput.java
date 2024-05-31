package relay_race;

import common.Race;
import common.RawResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RelayRaceInput {

    final RelayRace race;

    Path input_directory_path, entries_path, raw_results_path, paper_results_path, annotations_path;
    String entries_filename, raw_results_filename, paper_results_filename, annotations_filename;

    int number_of_raw_results;

    public RelayRaceInput(final RelayRace race) {

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
        annotations_filename = race.getProperties().getProperty("ANNOTATIONS_FILENAME");
    }

    private void constructFilePaths() {

        input_directory_path = race.getWorkingDirectoryPath().resolve("input");

        entries_path = input_directory_path.resolve(entries_filename);
        raw_results_path = input_directory_path.resolve(raw_results_filename);
        paper_results_path = paper_results_filename != null ? input_directory_path.resolve(paper_results_filename) : null;
        annotations_path = annotations_filename != null ? input_directory_path.resolve(annotations_filename): null;
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

        entries[entry_index] = new Team(team_elements, race);
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

        for (final String line : Files.readAllLines(raw_results_path))
            loadRawResult(raw_results, line);

        number_of_raw_results = raw_results.size();

        if (paper_results_path != null)
            for (final String line : Files.readAllLines(paper_results_path))
                loadRawResult(raw_results, line);

        return raw_results.toArray(new RawResult[0]);
    }

    int getNumberOfRawResults() {
        return number_of_raw_results;
    }

    private static void loadRawResult(final List<RawResult> raw_results, String line) {

        final int comment_start_index = line.indexOf("#");
        if (comment_start_index > -1) line = line.substring(0, comment_start_index);

        if (!line.isBlank()) {

            try {
                final RawResult result = new RawResult(line);

                checkOrdering(raw_results, result);
                raw_results.add(result);
            }
            catch (NumberFormatException ignored) {
            }
        }
    }

    private static void checkOrdering(List<RawResult> raw_results, RawResult result) {

        final RawResult previous_result = !raw_results.isEmpty() ? raw_results.get(raw_results.size() - 1) : null;

        if (resultsAreOutOfOrder(result, previous_result))
            throw new RuntimeException("result " + (raw_results.size() + 1) + " out of order");
    }

    private static boolean resultsAreOutOfOrder(RawResult result, RawResult previous_result) {

        return result.getRecordedFinishTime() != null &&
                previous_result != null && previous_result.getRecordedFinishTime() != null &&
                previous_result.getRecordedFinishTime().compareTo(result.getRecordedFinishTime()) > 0;
    }

    public void loadTimeAnnotations(RawResult[] raw_results) throws IOException {

        if (annotations_path != null) {

            final List<String> lines = Files.readAllLines(annotations_path);

            // Skip header line.
            for (int line_index = 1; line_index < lines.size(); line_index++) {

                final String[] elements = lines.get(line_index).split("\t");

                // May add insertion option later.
                if (elements[0].equals("Update"))
                    updateResult(raw_results, elements);
            }
        }
    }

    private static void updateResult(RawResult[] raw_results, String[] elements) {

        final int position = Integer.parseInt(elements[1]);
        final RawResult raw_result = raw_results[position - 1];

        if (elements[2].equals("?")) raw_result.setBibNumber(null);
        else if (!elements[2].isEmpty()) raw_result.setBibNumber(Integer.parseInt(elements[2]));

        if (elements[3].equals("?")) raw_result.setRecordedFinishTime(null);
        else if (!elements[3].isEmpty()) raw_result.setRecordedFinishTime(Race.parseTime(elements[3]));

        if (!elements[4].isEmpty()) raw_result.appendComment(elements[4]);
    }
}
