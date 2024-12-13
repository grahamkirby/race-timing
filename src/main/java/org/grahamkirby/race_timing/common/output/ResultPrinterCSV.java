package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;

import java.io.IOException;
import java.io.OutputStreamWriter;

public abstract class ResultPrinterCSV extends ResultPrinter {

    public ResultPrinterCSV(final Race race, final OutputStreamWriter writer) {
        super(race, writer);
    }

    @Override
    public void printResultsHeader() throws IOException {
    }

    @Override
    public void printResultsFooter(final CreditLink include_credit_link) {
    }

    @Override
    public void printNoResults() {
    }
}
