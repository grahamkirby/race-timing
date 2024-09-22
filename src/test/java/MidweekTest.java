import common.Race;
import org.junit.jupiter.api.Test;
import series_race.fife_ac_midweek.MidweekRace;

import java.io.IOException;
import java.nio.file.Path;

public class MidweekTest extends RaceTest {

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        return new MidweekRace(config_file_path);
    }

    @Override
    protected String getResourcesPath() {
        return "series_race/";
    }

    @Test
    public void completed_1() throws Exception {
        testExpectedCompletion("actual_races/midweek_2023/completed_1");
    }

    @Test
    public void completed_2() throws Exception {
        testExpectedCompletion("actual_races/midweek_2023/completed_2");
    }

    @Test
    public void completed_3() throws Exception {
        testExpectedCompletion("actual_races/midweek_2023/completed_3");
    }

    @Test
    public void completed_4() throws Exception {
        testExpectedCompletion("actual_races/midweek_2023/completed_4");
    }

    @Test
    public void completed_5() throws Exception {
        testExpectedCompletion("actual_races/midweek_2023/completed_5");
    }

    @Test
    public void deadHeats() throws Exception {
        testExpectedCompletion("midweek/dead_heats");
    }

    @Test
    public void duplicateRunnerName() throws Exception {
        testExpectedCompletion("midweek/duplicate_runner_name");
    }
}
