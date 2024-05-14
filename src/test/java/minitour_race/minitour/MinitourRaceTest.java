package minitour_race.minitour;

import common.Race;
import common.RaceTest;
import minitour.MinitourRace;
import org.junit.jupiter.api.Test;
import series_race.SeriesRace;

import java.io.IOException;
import java.nio.file.Path;

public class MinitourRaceTest extends RaceTest {

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        MinitourRace minitourRace = new MinitourRace(config_file_path);
        minitourRace.configure();
        return minitourRace;
    }

    @Override
    protected String getResourcesPath() {
        return "minitour_race/minitour/";
    }

    @Test
    public void completed_1() throws Exception {
        testExpectedCompletion("actual_2023/completed_1");
    }
}
