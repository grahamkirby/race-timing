package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;

import java.io.IOException;
import java.io.OutputStreamWriter;

public abstract class OverallResultPrinterText extends ResultPrinter {

    public OverallResultPrinterText(Race race, OutputStreamWriter writer) {
        super(race, writer);
    }

    @Override
    public void printResultsHeader() throws IOException {
    }

    @Override
    public void printResultsFooter(final boolean include_credit_link) throws IOException {
    }

    @Override
    public void printNoResults() throws IOException {
        writer.append("No results\n");
    }
}
