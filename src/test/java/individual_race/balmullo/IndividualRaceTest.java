package individual_race.balmullo;

import common.Race;
import common.RaceTest;
import individual_race.IndividualRace;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.*;

public class IndividualRaceTest extends RaceTest {

    @Test
    public void simple() throws Exception {
        testExpectedCompletion("simple");
    }

    @Override
    protected Race makeRace(Properties properties) throws IOException {
        return new IndividualRace(properties);
    }

    @Override
    protected String getResourcesPath() {
        return "individual_race/balmullo/";
    }
}