package individual_race;

import common.RawResult;
import lap_race.LapRace;
import lap_race.Team;

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

    Runner[] loadEntries() throws IOException {

        final List<String> lines = Files.readAllLines(entries_path);
        final Runner[] entries = new Runner[lines.size()];

        for (int i = 0; i < entries.length; i++)
            loadEntry(entries, lines, i);

        return entries;
    }

    private void loadEntry(final Runner[] entries, final List<String> lines, final int entry_index) {

        final String[] runner_elements = lines.get(entry_index).split("\t");

        if (runner_elements.length != 4)
            throw new RuntimeException("illegal composition for runner: " + runner_elements[0]);

        final int bib_number = Integer.parseInt(runner_elements[0]);
        final String runner_name = runner_elements[1] = cleanName(runner_elements[1]);
        final String club = runner_elements[2] = cleanName(runner_elements[2]);

        if (entriesAlreadyContain(entries, bib_number))
            throw new RuntimeException("duplicate runner number: " + bib_number);

        if (entriesAlreadyContain(entries, runner_name, club))
            throw new RuntimeException("duplicate runner: " + runner_name + ", " + club);

        entries[entry_index] = new Runner(runner_elements);
    }

    private String cleanName(String name) {

        while (name.contains("  ")) name = name.replaceAll(" {2}", " ");
        return name.strip();
    }

    private boolean entriesAlreadyContain(final Runner[] entries, final int bib_number) {

        for (Runner runner : entries)
            if (runner != null && runner.bib_number == bib_number) return true;
        return false;
    }

    private boolean entriesAlreadyContain(final Runner[] entries, final String runner_name, final String club) {

        for (Runner runner : entries)
            if (runner != null && runner.name.equals(runner_name) && runner.club.equals(club)) return true;
        return false;
    }

    RawResult[] loadRawResults() throws IOException {

        final List<String> lines = Files.readAllLines(raw_results_path);
        final List<RawResult> raw_results = new ArrayList<>();

        for (String line : lines)
            loadRawResult(raw_results, line);

        return raw_results.toArray(new RawResult[0]);
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
