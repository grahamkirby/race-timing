package individual_race.balmullo;

import common.Race;
import common.RaceTest;
import individual_race.IndividualRace;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class IndividualRaceTest extends RaceTest {

    @Test
    public void simple() throws Exception {
        testExpectedCompletion("simple");
    }

    @Test
    public void actual2023() throws Exception {
        testExpectedCompletion("actual_2023");
    }

    @Test
    public void openCategory() throws Exception {
        testExpectedCompletion("open_category");
    }

//    @Override
//    protected Race makeRace(Properties properties) throws IOException {
//        return new IndividualRace(properties);
//    }

    protected Race makeRace(final Path config_file_path) throws IOException {
        return new IndividualRace(config_file_path);
    }

    @Override
    protected String getResourcesPath() {
        return "individual_race/balmullo/";
    }
}
