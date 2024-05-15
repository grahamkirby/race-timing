package series_race.midweek;

import common.Race;
import common.RaceTest;
import org.junit.jupiter.api.Test;
import series_race.SeriesRace;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class SeriesRaceTest extends RaceTest {

    /*
    Tie break - good point. We've been close a few times, there's always the first time tho'. I reckon to save trawling through earlier results in the series, the higher placed runner in the final race at Balmullo should be declared the top man. However, if you think of a better way, feel free to decide.


1) Apart from the prizes going to the top 3 in each gender category, someone can only get a prize in their exact age category. E.g. an under 20 or over 40 will never get a senior prize even if they beat the senior prize winner.
2) For overall, there are 3 prizes in every category.
3) 70+ is the oldest category, no 80+.

    */


    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        SeriesRace seriesRace = new SeriesRace(config_file_path);
        seriesRace.configure();
        return seriesRace;
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
