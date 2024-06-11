package common;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class SingleRace extends Race {

    public List<RaceEntry> entries;

    public SingleRace(Path config_file_path) throws IOException {
        super(config_file_path);
    }
}
