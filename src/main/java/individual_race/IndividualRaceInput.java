package individual_race;

import common.RawResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class IndividualRaceInput {

    final IndividualRace race;

    Path input_directory_path;
    Path entries_path;
    Path raw_results_path;
    String entries_filename;
    String raw_results_filename;

    public IndividualRaceInput(final IndividualRace race) {

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
    }

    private void constructFilePaths() {

        input_directory_path = race.getWorkingDirectoryPath().resolve("input");
        entries_path = input_directory_path.resolve(entries_filename);
        raw_results_path = input_directory_path.resolve(raw_results_filename);
    }

    protected List<IndividualRaceEntry> loadEntries() throws IOException {

        final List<String> lines = Files.readAllLines(entries_path);
        final List<IndividualRaceEntry> entries = new ArrayList<>();

        for (final String line : lines) {

            final IndividualRaceEntry entry = new IndividualRaceEntry(line.split("\t"), race);

            checkDuplicateBibNumber(entries, entry.bib_number);
            checkDuplicateRunner(entries, entry.runner.name, entry.runner.club);

            entries.add(entry);
        }

        return entries;
    }

    private void checkDuplicateBibNumber(final List<IndividualRaceEntry> entries, final int bib_number) {

        for (IndividualRaceEntry entry : entries)
            if (entry != null && entry.bib_number == bib_number)
                throw new RuntimeException("duplicate runner number: " + bib_number);
    }

    private void checkDuplicateRunner(final List<IndividualRaceEntry> entries, final String runner_name, final String club) {

        for (IndividualRaceEntry entry : entries)
            if (entry.runner.name.equals(runner_name) && entry.runner.club.equals(club))
                throw new RuntimeException("duplicate runner: " + runner_name + ", " + club);
    }

    protected List<RawResult> loadRawResults() throws IOException {

        final List<String> lines = Files.readAllLines(raw_results_path);
        final List<RawResult> raw_results = new ArrayList<>();

        for (String line : lines)
            loadRawResult(raw_results, line);

        return raw_results;
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

            if (previous_result != null && previous_result.getRecordedFinishTime().compareTo(result.getRecordedFinishTime()) > 0)
                throw new RuntimeException("result " + (raw_results.size() + 1) + " out of order");

            raw_results.add(result);
        }
    }
}
