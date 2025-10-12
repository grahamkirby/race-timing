/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.common;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.series_race.SeriesRace;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Config.*;

public abstract class RaceOutput implements ResultsOutput {

    protected Race race;

    protected Path getOutputStreamPath(final String output_type, final String file_suffix) {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        return race.getOutputDirectoryPath().resolve(race_name + "_" + output_type + "_" + year + "." + file_suffix);
    }

    protected OutputStream getOutputStream(final String output_type, final String file_suffix) throws IOException {

        return getOutputStream(output_type, file_suffix, STANDARD_FILE_OPEN_OPTIONS);
    }

    private OutputStream getOutputStream(final String output_type, final String file_suffix, final OpenOption[] file_open_options) throws IOException {

        return Files.newOutputStream(getOutputStreamPath(output_type, file_suffix), file_open_options);
    }

    /** Prints out the words converted to title case, and any other processing notes. */
    protected void printNotes() throws IOException {

        finaliseNotes();

        final OutputStream stream = getOutputStream("processing_notes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writer.append(race.getNotes());
        }
    }

    private void finaliseNotes() {

        for (final RaceResult result : race.getResultsCalculator().getOverallResults())
            if (result.getCategory() == null)
                race.appendToNotes("Runner " + result.getParticipantName() + " unknown category so omitted from overall results" + LINE_SEPARATOR);

        final String converted_words = race.getNormalisation().getNonTitleCaseWords();

        if (!converted_words.isEmpty())
            race.appendToNotes("Converted to title case: " + converted_words);
    }

    private void printPrizeCategoryHeader(final OutputStreamWriter writer, final PrizeCategory category) throws IOException {

        final String header = "Category: " + category.getLongName();
        writer.append(header + LINE_SEPARATOR + "-".repeat(header.length()) + LINE_SEPARATOR + LINE_SEPARATOR);
    }

    private void printPrizesText(final OutputStreamWriter writer, final PrizeCategory category) {

        try {
            printPrizeCategoryHeader(writer, category);

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            new PrizeResultPrinterText(race, writer).print(category_prize_winners);

            writer.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void printOverallResults() throws IOException {

        printResultsCSV();
        printResultsHTML();
    }

    protected abstract void printResultsCSV() throws IOException;
    protected abstract void printResultsHTML() throws IOException;

    protected void printPrizesWithHeaderHTML(final OutputStreamWriter writer, final ResultPrinterGenerator make_prize_result_printer) throws IOException {

        writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);
        writer.append(getPrizesHeaderHTML());

        printPrizesHTML(writer, make_prize_result_printer);
    }

    protected void printResultsWithHeaderHTML(final OutputStreamWriter writer, final ResultPrinterGenerator make_overall_result_printer) throws IOException {

        writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);

        printResults(writer, make_overall_result_printer.apply(race, writer), this::getResultsSubHeaderHTML);
        writer.append(SOFTWARE_CREDIT_LINK_TEXT);
    }

    protected String getPrizesHeaderHTML() {

        final String header = race.getSpecific() instanceof final SeriesRace series_race && series_race.getNumberOfRacesTakenPlace() < (int) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES) ? "Current Standings" : "Prizes";
        return "<h4>" + header + "</h4>" + LINE_SEPARATOR;
    }

    protected void printPrizesText(final OutputStreamWriter writer) {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).              // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).          // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizesText(writer, category));   // Print prizes in this category.
    }

    protected void printPrizesHeaderText(final OutputStreamWriter writer) throws IOException {

        final String header = race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS) + " Results " + race.getConfig().get(KEY_YEAR);
        writer.append(header + LINE_SEPARATOR + "=".repeat(header.length()) + LINE_SEPARATOR + LINE_SEPARATOR);
    }

    private static final class PrizeResultPrinterText extends ResultPrinterText {

        public PrizeResultPrinterText(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult result) throws IOException {

            writer.append(result.getPositionString() + ": " + result.getParticipantName() + " " + result.getPrizeDetailText() + LINE_SEPARATOR);
        }
    }

    /** Prints prizes, ordered by prize category groups. */
    protected void printPrizesHTML(final OutputStreamWriter writer, final ResultPrinterGenerator make_prize_result_printer) {

        final ResultPrinter printer = make_prize_result_printer.apply(race, writer);

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                        // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).                    // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizes(category, writer, printer));
    }

    /** Prints prizes within a given category. */
    private void printPrizes(final PrizeCategory category, final OutputStreamWriter writer, final ResultPrinter printer) {

        try {
            writer.append("<p><strong>" + category.getLongName() + "</strong></p>" + LINE_SEPARATOR);

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            printer.print(category_prize_winners);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Prints results using a specified printer, ordered by prize category groups. */
    protected void printResults(final OutputStreamWriter writer, final ResultPrinter printer, final Function<String, String> get_results_sub_header) throws IOException {

        // Don't display category group headers if there is only one group.
        final boolean should_display_category_group_headers = race.getCategoryDetails().getPrizeCategoryGroups().size() > 1;

        boolean not_first_category_group = false;

        for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups()) {

            if (should_display_category_group_headers) {
                if (not_first_category_group)
                    writer.append(LINE_SEPARATOR);
                writer.append(get_results_sub_header.apply(group.group_title()));
            }

            printer.print(race.getResultsCalculator().getOverallResults(group.categories()));

            not_first_category_group = true;
        }
    }

    protected void printResultsHTML(final ResultPrinterGenerator make_result_printer) throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream("overall", HTML_FILE_SUFFIX))) {

            final ResultPrinter printer = make_result_printer.apply(race, writer);
            printResults(writer, printer, this::getResultsSubHeaderHTML);
        }
    }

    protected String getResultsSubHeaderHTML(final String s) {
        return "<p></p>" + LINE_SEPARATOR + "<h4>" + s + "</h4>" + LINE_SEPARATOR;
    }

    protected void printPrizesText() throws IOException {

        final OutputStream stream = getOutputStream("prizes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            printPrizesHeaderText(writer);
            printPrizesText(writer);
        }
    }

    protected void printPrizesPDF() throws IOException {

        final Path path = getOutputStreamPath("prizes", PDF_FILE_SUFFIX);
        final PdfWriter writer = new PdfWriter(path.toString());

        try (final Document document = new Document(new PdfDocument(writer))) {

            printPrizesPDF(document);
        }
    }

    protected void printPrizesPDF(final Document document) throws IOException {

        final String year = (String) race.getConfig().get(KEY_YEAR);

        final Paragraph section_header = new Paragraph().
            setFont(getFont(PDF_PRIZE_FONT_NAME)).
            setFontSize(PDF_PRIZE_FONT_SIZE).
            add(race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS) + " " + year + " Category Prizes");

        document.add(section_header);

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                 // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).             // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizesPDF(document, category));     // Print prizes in this category.
    }

    protected static PdfFont getFont(final String font_name) throws IOException {
        return PdfFontFactory.createFont(font_name);
    }

    /** Prints prizes within a given category. */
    private void printPrizesPDF(final Document document, final PrizeCategory category) {

        try {

            final PdfFont bold_font = getFont(PDF_PRIZE_FONT_BOLD_NAME);

            final Paragraph category_header = new Paragraph("Category: " + category.getLongName()).
                setFont(bold_font).
                setUnderline().
                setPaddingTop(PDF_PRIZE_FONT_SIZE);

            document.add(category_header);

            new PrizeResultPrinterPDF(race, document).print(race.getResultsCalculator().getPrizeWinners(category));

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class PrizeResultPrinterPDF extends ResultPrinter {

        private final Document document;

        public PrizeResultPrinterPDF(final Race race, final Document document) {
            super(race, null);
            this.document = document;
        }

        @Override
        public void printResult(final RaceResult result) throws IOException {

            final PdfFont font = getFont(PDF_PRIZE_FONT_NAME);
            final PdfFont bold_font = getFont(PDF_PRIZE_FONT_BOLD_NAME);

            final Paragraph paragraph = new Paragraph().setFont(font).setMarginBottom(0);

            paragraph.add(new Text(result.getPositionString() + ": ").setFont(font));
            paragraph.add(new Text(result.getParticipantName()).setFont(bold_font));
            paragraph.add(new Text(" " + result.getPrizeDetailPDF()).setFont(font));

            document.add(paragraph);
        }

        @Override
        public void printNoResults() throws IOException {

            document.add(new Paragraph("No results").setFont(getFont(PDF_PRIZE_FONT_ITALIC_NAME)));
        }
    }
}
