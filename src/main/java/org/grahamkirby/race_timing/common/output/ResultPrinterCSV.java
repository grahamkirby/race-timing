package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;

import java.io.IOException;
import java.io.OutputStreamWriter;

@SuppressWarnings("NoopMethodInAbstractClass")
public abstract class ResultPrinterCSV extends ResultPrinter {

    protected ResultPrinterCSV(final Race race, final OutputStreamWriter writer) {
        super(race, writer);
    }

    @Override
    public void printResultsHeader() throws IOException {
    }

    @Override
    public void printResultsFooter(final CreditLink credit_link_option) {
    }

    @Override
    public void printNoResults() {
    }
}
