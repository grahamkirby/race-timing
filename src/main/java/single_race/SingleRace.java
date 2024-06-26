package single_race;

import common.Race;
import common.RaceEntry;
import common.RawResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class SingleRace extends Race {

    public List<RaceEntry> entries;
    protected List<RawResult> raw_results;

    protected String dnf_string;

    public SingleRace(final Path config_file_path) throws IOException {

        super(config_file_path);

        // Specifies all the runners who did have a finish
        // time recorded but were declared DNF.
        dnf_string = getProperties().getProperty("DNF_LEGS");
    }

    public List<RawResult> getRawResults() {
        return raw_results;
    }

    protected void configureInputData() throws IOException {

        entries = ((SingleRaceInput)input).loadEntries();
        raw_results = ((SingleRaceInput)input).loadRawResults();
    }

    protected void fillDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not visiting all
        // checkpoints.

        // DNF cases where there is no recorded leg result are captured by the
        // default value of DNF being true.

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(","))
                fillDNF(individual_dnf_string);
    }

    protected abstract void fillDNF(String individualDnfString);
}
