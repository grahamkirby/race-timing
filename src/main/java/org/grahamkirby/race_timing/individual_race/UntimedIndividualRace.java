package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.single_race.SingleRace;
import org.grahamkirby.race_timing.single_race.SingleRaceInput;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

public class UntimedIndividualRace extends SingleRace {

    public UntimedIndividualRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    @Override
    public void calculateResults() {
    }

    @Override
    protected void recordDNF(final String dnf_specification) {
        throw new UnsupportedOperationException();
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

        ((SingleRaceInput)input).validateInputFiles();

        overall_results = ((UntimedIndividualRaceInput) input).loadOverallResults();
    }

    @Override
    protected void outputResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected List<Comparator<RaceResult>> getComparators() {
        return List.of();
    }
}
