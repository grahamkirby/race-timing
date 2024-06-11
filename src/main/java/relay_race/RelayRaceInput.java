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

    protected List<RelayRaceEntry> loadEntries() throws IOException {

        final List<String> lines = Files.readAllLines(entries_path);
        final List<RelayRaceEntry> entries = new ArrayList<>();

        for (final String line : lines) {

            final RelayRaceEntry entry = new RelayRaceEntry(line.split("\t"), race);

            checkDuplicateBibNumber(entries, entry.bib_number);
            checkDuplicateTeamNumber(entries, entry.team.name);

            entries.add(entry);
        }

        return entries;
    }

    private void checkDuplicateBibNumber(final List<RelayRaceEntry> entries, final int bib_number) {

        for (final RelayRaceEntry entry : entries)
            if (entry != null && entry.bib_number == bib_number)
                throw new RuntimeException("duplicate team number: " + bib_number);
    }

    private void checkDuplicateTeamNumber(final List<RelayRaceEntry> entries, final String team_name) {

        for (final RelayRaceEntry entry : entries)
            if (entry != null && entry.team.name.equals(team_name))
                throw new RuntimeException("duplicate team name: " + team_name);
    }

    protected List<RawResult> loadRawResults() throws IOException {

        final List<RawResult> raw_results = new ArrayList<>();

        for (final String line : Files.readAllLines(raw_results_path))
            loadRawResult(raw_results, line);

        number_of_raw_results = raw_results.size();

        if (paper_results_path != null)
            for (final String line : Files.readAllLines(paper_results_path))
                loadRawResult(raw_results, line);

        return raw_results;
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

    public void loadTimeAnnotations(final List<RawResult> raw_results) throws IOException {

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

    private static void updateResult(final List<RawResult> raw_results, String[] elements) {

        final int position = Integer.parseInt(elements[1]);
        final RawResult raw_result = raw_results.get(position - 1);

        if (elements[2].equals("?")) raw_result.setBibNumber(null);
        else if (!elements[2].isEmpty()) raw_result.setBibNumber(Integer.parseInt(elements[2]));

        if (elements[3].equals("?")) raw_result.setRecordedFinishTime(null);
        else if (!elements[3].isEmpty()) raw_result.setRecordedFinishTime(Race.parseTime(elements[3]));

        if (!elements[4].isEmpty()) raw_result.appendComment(elements[4]);
    }
}
