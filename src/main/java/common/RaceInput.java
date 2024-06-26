package common;

import java.nio.file.Path;

public abstract class RaceInput {

    protected Race race;
    protected Path input_directory_path, entries_path, raw_results_path;
    protected String entries_filename, raw_results_filename;

    public RaceInput(Race race) {
        this.race = race;
    }
}
