package individual_race.st_andrews;

import common.Race;
import common.RaceTest;
import individual_race.IndividualRace;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.*;

public class IndividualRaceTest extends RaceTest {

    @Test
    public void actual2023() throws Exception {
        testExpectedCompletion("actual_2023");
    }

    @Override
    protected Race makeRace(Properties properties) throws IOException {
        return new IndividualRace(properties);
    }

    @Override
    protected String getResourcesPath() {
        return "individual_race/st_andrews/";
    }
}
