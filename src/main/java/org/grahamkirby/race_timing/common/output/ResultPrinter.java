package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/** Abstracts over the details of how to print out race results. */
@SuppressWarnings("IncorrectFormatting")
public abstract class ResultPrinter {

    protected final Race race;
    protected final OutputStreamWriter writer;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public ResultPrinter(final Race race, final OutputStreamWriter writer) {
        this.race = race;
        this.writer = writer;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract void printResult(RaceResult r) throws IOException;

    protected void printResultsHeader() throws IOException {
    }

    protected void printResultsFooter() throws IOException {
    }

    protected void printNoResults() throws IOException {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Prints out the given list of results. */
    @SuppressWarnings("TypeMayBeWeakened")
    public final void print(final List<? extends RaceResult> results) throws IOException {

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
