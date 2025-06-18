package org.grahamkirby.race_timing_experimental.common;

import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RacePrizes;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public interface Race {

    void setPrizes(RacePrizes prizes);

    void setInput(RaceInput input);

    void setResultsCalculator(ResultsCalculator results_calculator);

    void setResultsOutput(ResultsOutput results_output);

    void processResults() throws IOException;

    Properties getProperties();

    Path getPath(String results_path);
}
