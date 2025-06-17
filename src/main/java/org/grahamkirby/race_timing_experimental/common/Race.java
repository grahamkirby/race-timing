package org.grahamkirby.race_timing_experimental.common;

import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RacePrizes;

import java.io.IOException;
import java.nio.file.Path;

public interface Race {

    void setPrizes(RacePrizes prizes);

    void setInput(RaceInput input);

    void setRaceImpl(RaceImpl race_impl);

    void processResults() throws IOException;

    String getOptionalProperty(String key);
    String getRequiredProperty(String key);

    Path getPath(String results_path);
}
