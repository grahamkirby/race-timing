package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public abstract class ResultPrinter {

    protected final Race race;
    protected final OutputStreamWriter writer;

    public ResultPrinter(Race race, OutputStreamWriter writer) {
        this.race = race;
        this.writer = writer;
    }

    public abstract void printResultsHeader() throws IOException;
    public abstract void printResultsFooter(boolean include_credit_link) throws IOException;
    public abstract void printResult(RaceResult result) throws IOException;
    public abstract void printNoResults() throws IOException;

    public void print(List<RaceResult> results, boolean include_credit_link) throws IOException {

        if (!results.isEmpty()) {
            printResultsHeader();

            for (final RaceResult result : results)
                printResult(result);

            printResultsFooter(include_credit_link);
        }
        else
            printNoResults();
    }
}
