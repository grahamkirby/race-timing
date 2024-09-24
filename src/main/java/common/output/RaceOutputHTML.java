package common.output;

import common.Race;
import common.categories.Category;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;

import static common.Race.SOFTWARE_CREDIT_LINK_TEXT;

public abstract class RaceOutputHTML extends RaceOutput {

    public RaceOutputHTML(Race race) {
        super(race);
    }

    @Override
    public void printOverallResults(final boolean include_credit_link) throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(overall_results_filename + ".html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {
            printOverallResults(html_writer, include_credit_link);
        }
    }

    @Override
    protected void printOverallResults(final OutputStreamWriter writer, final boolean include_credit_link) throws IOException {

        printOverallResultsHeader(writer);
        printOverallResultsBody(writer);
        printOverallResultsFooter(writer, include_credit_link);
    }

    @Override
    public void printPrizes() throws IOException {

        final OutputStream stream = Files.newOutputStream(output_directory_path.resolve(prizes_filename + ".html"));

        try (final OutputStreamWriter html_writer = new OutputStreamWriter(stream)) {
            printPrizes(html_writer);
        }
    }

    protected void printPrizes(final OutputStreamWriter writer) throws IOException {

        writer.append("<h4>Prizes</h4>\n");

        for (final Category category : race.categories.getPrizeCategoriesInReportOrder())
            printPrizes(writer, category);
    }

    protected void printOverallResultsFooter(final OutputStreamWriter writer, final boolean include_credit_link) throws IOException {

        writer.append("""
                </tbody>
            </table>
            """);

        if (include_credit_link) writer.append(SOFTWARE_CREDIT_LINK_TEXT);
    }
}
