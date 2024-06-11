package single_race;

import common.Race;
import common.RaceEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class SingleRace extends Race {

    public List<RaceEntry> entries;

    public SingleRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    protected void configureInputData() throws IOException {

        entries = ((SingleRaceInput)input).loadEntries();
        raw_results = input.loadRawResults();
    }
}
