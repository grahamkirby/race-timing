package series_race;

import common.Race;

import java.io.IOException;
import java.nio.file.Path;

public abstract class SeriesRace extends Race {

    public SeriesRace(Path config_file_path) throws IOException {
        super(config_file_path);
    }
}
