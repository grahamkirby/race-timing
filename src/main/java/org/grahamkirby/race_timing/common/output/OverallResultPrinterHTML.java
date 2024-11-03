package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.grahamkirby.race_timing.common.output.RaceOutputHTML.SOFTWARE_CREDIT_LINK_TEXT;

public abstract class OverallResultPrinterHTML extends ResultPrinter {

    public OverallResultPrinterHTML(Race race, OutputStreamWriter writer) {
        super(race, writer);
    }

    @Override
    public void printResultsFooter(final boolean include_credit_link) throws IOException {

        writer.append("""
            </tbody>
        </table>
        """);

        if (include_credit_link) writer.append(SOFTWARE_CREDIT_LINK_TEXT);
    }

    @Override
    public void printNoResults() throws IOException {
        writer.append("<p>No results</p>\n");
    }
}
