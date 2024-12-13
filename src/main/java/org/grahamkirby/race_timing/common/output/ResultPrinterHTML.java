package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;
import static org.grahamkirby.race_timing.common.output.RaceOutputHTML.SOFTWARE_CREDIT_LINK_TEXT;

public abstract class ResultPrinterHTML extends ResultPrinter {

    protected ResultPrinterHTML(final Race race, final OutputStreamWriter writer) {
        super(race, writer);
    }

    @Override
    public void printResultsFooter(final CreditLink credit_link_option) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);

        if (credit_link_option == CreditLink.INCLUDE_CREDIT_LINK) writer.append(SOFTWARE_CREDIT_LINK_TEXT);
    }

    @Override
    public void printNoResults() throws IOException {

        writer.append("<p>No results</p>").append(LINE_SEPARATOR);
    }
}
