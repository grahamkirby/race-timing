package individual_race;

import common.Race;
import common.RaceTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class StrathBleboTest extends RaceTest {

    @Test
    public void actual2023() throws Exception {
        testExpectedCompletion("actual_2023");
    }

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        return new IndividualRace(config_file_path);
    }

    @Override
    protected String getResourcesPath() {
        return "individual_race/strath_blebo/";
    }
}
