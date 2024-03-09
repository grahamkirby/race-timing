package series_race.midweek;

import common.Race;
import common.RaceTest;
import org.junit.jupiter.api.Test;
import series_race.SeriesRace;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class SeriesRaceTest extends RaceTest {

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        return new SeriesRace(config_file_path);
    }

    @Override
    protected String getResourcesPath() {
        return "series_race/midweek/";
    }

    @Test
    public void completed_1() throws Exception {
        testExpectedCompletion("actual_2023/completed_1");
    }

    @Test
    public void completed_2() throws Exception {
        testExpectedCompletion("actual_2023/completed_2");
    }
}
