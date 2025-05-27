package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.grahamkirby.race_timing.common.Normalisation.parseTime;

public class TimedIndividualRace extends TimedRace {

    private static final String KEY_INDIVIDUAL_EARLY_STARTS = "INDIVIDUAL_EARLY_STARTS";

    /**
     * List of individual early starts (usually empty).
     * Values are read from configuration file using key KEY_INDIVIDUAL_EARLY_STARTS.
     */
    private Map<Integer, Duration> early_starts;

    public TimedIndividualRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    @Override
    protected RaceInput getInput() {
        return new TimedIndividualRaceInput(this);
    }

    @Override
    protected void configure() throws IOException {

        super.configure();
        configureIndividualEarlyStarts();
    }

    @Override
    public void calculateResults() {

        initialiseResults();
        super.calculateResults();
    }

    private void initialiseResults() {

        raw_results.forEach(raw_result -> {

            final int bib_number = raw_result.getBibNumber();
            final SingleRaceEntry entry = getEntryWithBibNumber(bib_number);

            // TODO apply in separate operation
            final Duration early_start_offset = early_starts.getOrDefault(bib_number, Duration.ZERO);
            final Duration finish_time = raw_result.getRecordedFinishTime().plus(early_start_offset);

            final TimedIndividualRaceResult result = new TimedIndividualRaceResult(this, (TimedIndividualRaceEntry) entry, finish_time);

            overall_results.add(result);
        });
    }

    private void configureIndividualEarlyStarts() {

        final String individual_early_starts_string = getOptionalProperty(KEY_INDIVIDUAL_EARLY_STARTS);

        // bib number / start time difference
        // Example: INDIVIDUAL_EARLY_STARTS = 2/0:10:00,26/0:20:00

        early_starts = new HashMap<>();

        if (individual_early_starts_string != null)
            Arrays.stream(individual_early_starts_string.split(",")).
                forEach(this::recordEarlyStart);
    }

    private void recordEarlyStart(final String early_starts_string) {

        final String[] split = early_starts_string.split("/");

        final int bib_number = Integer.parseInt(split[0]);
        final Duration start_time = parseTime(split[1]);

        early_starts.put(bib_number, start_time);
    }
}
