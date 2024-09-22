import common.Race;
import individual_race.IndividualRace;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;

public class IndividualRaceTest extends RaceTest {

    @Test
    public void balmullo2023() throws Exception {
        testExpectedCompletion("actual_races/balmullo_2023");
    }

    @Test
    public void stAndrews2023() throws Exception {
        testExpectedCompletion("actual_races/st_andrews_2023");
    }

    @Test
    public void strathBlebo2023() throws Exception {
        testExpectedCompletion("actual_races/strath_blebo_2023");
    }

    @Test
    public void deadHeats() throws Exception {
        testExpectedCompletion("dead_heats");
    }

    @Test
    public void DNFs() throws Exception {
        testExpectedCompletion("dnfs");
    }

    @Test
    public void duplicateBibNumber() throws Exception {
        testExpectedException("duplicate_bib_number", "duplicate bib number: 3");
    }

    @Test
    public void duplicateRunner() throws Exception {
        testExpectedException("duplicate_runner", "duplicate entry: John Smith, Fife AC");
    }

    @Test
    public void duplicateRunnerName() throws Exception {
        testExpectedCompletion("duplicate_runner_name");
    }

    @Test
    public void illegalCategory() throws Exception {
        testExpectedException("illegal_category", "illegal category for runner: 92");
    }

    @Test
    public void illegalRawTime() throws Exception {
        testExpectedException("illegal_raw_time", "illegal time: XXX");
    }

    @Test
    public void resultsOutOfOrder() throws Exception {
        testExpectedException("results_out_of_order", "result 15 out of order");
    }

    @Test
    public void seniorNotOpenCategory() throws Exception {
        testExpectedCompletion("senior_not_open_category");
    }

    @Test
    public void unregisteredRunner() throws Exception {
        testExpectedException("unregistered_runner", "unregistered bib number: 4");
    }

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        return new IndividualRace(config_file_path);
    }

    @Override
    protected String getResourcesPath() {
        return "individual_race/";
    }
}
