package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.single_race.SingleRace;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class UntimedIndividualRace extends SingleRace {

    public UntimedIndividualRace(Path config_file_path) throws IOException {
        super(config_file_path);
    }

    @Override
    public void calculateResults() {

    }

    @Override
    protected void recordDNF(String dnf_specification) {

    }

    @Override
    protected RaceInput getInput() {
        return new UntimedIndividualRaceInput(this);
    }

    @Override
    protected RaceOutputCSV getOutputCSV() {
        return null;
    }

    @Override
    protected RaceOutputHTML getOutputHTML() {
        return null;
    }

    @Override
    protected RaceOutputText getOutputText() {
        return null;
    }

    @Override
    protected RaceOutputPDF getOutputPDF() {
        return null;
    }

    @Override
    protected void configureInputData() throws IOException {

        input.validateInputFiles();

        final UntimedIndividualRaceInput single_race_input = (UntimedIndividualRaceInput) input;

//        entries = single_race_input.loadEntries();

        // Only one of raw_results and overall_results will be fully initialised at this point,
        // depending on whether raw results are available, or just overall results (perhaps for
        // an externally organised race included in a race series).
        // The other list will be initialised as an empty list.
//        raw_results = single_race_input.loadRawResults();
        overall_results = single_race_input.loadOverallResults();
    }

    @Override
    protected void outputResults() throws IOException {

    }

    @Override
    protected List<Comparator<RaceResult>> getComparators() {
        return List.of();
    }
}
