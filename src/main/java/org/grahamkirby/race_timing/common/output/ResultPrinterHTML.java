package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.grahamkirby.race_timing.common.Race.LINE_SEPARATOR;

public abstract class ResultPrinterHTML extends ResultPrinter {

    protected ResultPrinterHTML(final Race race, final OutputStreamWriter writer) {
        super(race, writer);
    }

    @Override
    public void printResultsFooter() throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);
    }

    @Override
    public void printNoResults() throws IOException {

        writer.append("<p>No results</p>").append(LINE_SEPARATOR);
    }
}
