package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface RaceInput {

    List<RawResult> loadRawResults(Path raw_results_path) throws IOException;

    List<SingleRaceEntry> loadEntries(Path entries_path) throws IOException;
}
