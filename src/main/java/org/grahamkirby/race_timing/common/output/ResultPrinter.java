package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;

@SuppressWarnings("IncorrectFormatting")
public abstract class ResultPrinter {

    protected final Race race;
    protected final OutputStreamWriter writer;

    ResultPrinter(final Race race, final OutputStreamWriter writer) {
        this.race = race;
        this.writer = writer;
    }

    protected abstract void printResult(RaceResult r) throws IOException;
    protected abstract void printResultsHeader() throws IOException;
    protected abstract void printResultsFooter() throws IOException;
    protected abstract void printNoResults() throws IOException;

    public final void print(final Collection<? extends RaceResult> results) throws IOException {

        if (results.isEmpty())
            printNoResults();

        else {
            printResultsHeader();

            for (final RaceResult result : results)
                if (result.shouldBeDisplayedInResults())
                    printResult(result);

            printResultsFooter();
        }
    }
}
