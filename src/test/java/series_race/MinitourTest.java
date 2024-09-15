package series_race;

import common.Race;
import common.RaceTest;
import series_race.fife_ac_minitour.MinitourRace;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class MinitourTest extends RaceTest {

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        return new MinitourRace(config_file_path);
    }

    @Override
    protected String getResourcesPath() {
        return "series_race/minitour/";
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
