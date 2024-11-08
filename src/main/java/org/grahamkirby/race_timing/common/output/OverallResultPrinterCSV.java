package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;

import java.io.OutputStreamWriter;

public abstract class OverallResultPrinterCSV extends ResultPrinter {

    public OverallResultPrinterCSV(final Race race, final OutputStreamWriter writer) {
        super(race, writer);
    }

    @Override
    public void printResultsHeader() {
    }

    @Override
    public void printResultsFooter(final boolean include_credit_link) {
    }

    @Override
    public void printNoResults() {
    }
}
