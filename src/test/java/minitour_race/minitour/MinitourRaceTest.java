package minitour_race.minitour;

import common.Race;
import common.RaceTest;
import fife_ac_races.minitour.MinitourRace;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class MinitourRaceTest extends RaceTest {

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        return new MinitourRace(config_file_path);
    }

    @Override
    protected String getResourcesPath() {
        return "minitour_race/minitour/";
    }

    @Test
    public void completed_1() throws Exception {
        testExpectedCompletion("actual_2023/completed_1");
    }

    @Test
    public void completed_2() throws Exception {
        testExpectedCompletion("actual_2023/completed_2");
    }

    @Test
    public void completed_3() throws Exception {
        testExpectedCompletion("actual_2023/completed_3");
    }

    @Test
    public void completed_4() throws Exception {
        testExpectedCompletion("actual_2023/completed_4");
    }

    @Test
    public void completed_5() throws Exception {
        testExpectedCompletion("actual_2023/completed_5");
    }
}
