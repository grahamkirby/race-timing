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

    @Test
    public void unregisteredRunner() throws Exception {
        testExpectedException("unregistered_runner", "unregistered bib number: 4");
    }

    @Test
    public void duplicateBibNumber() throws Exception {
        testExpectedException("duplicate_bib_number", "duplicate runner number: 3");
    }

    @Test
    public void duplicateRunner() throws Exception {
        testExpectedException("duplicate_runner", "duplicate runner: John Smith, Fife AC");
    }

    @Test
    public void illegalRawTime() throws Exception {
        testExpectedException("illegal_raw_time", "illegal time: XXX");
    }

    @Test
    public void resultsOutOfOrder() throws Exception {
        testExpectedException("results_out_of_order","result 15 out of order");
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
