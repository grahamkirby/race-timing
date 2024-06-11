package common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class RaceInput {

    public Race race;
    public Path input_directory_path, entries_path, raw_results_path;
    public String entries_filename, raw_results_filename;

    public RaceInput(Race race) {
        this.race = race;
    }

    public List<RawResult> loadRawResults() throws IOException {

        return loadRawResults(raw_results_path);
    }

    public List<RawResult> loadRawResults(final Path results_path) throws IOException {

        final List<RawResult> raw_results = new ArrayList<>();

        for (String line : Files.readAllLines(results_path))
            loadRawResult(raw_results, line);

        return raw_results;
    }

    public static void loadRawResult(final List<RawResult> raw_results, String line) {

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
}
