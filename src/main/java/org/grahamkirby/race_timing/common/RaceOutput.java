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
import org.grahamkirby.race_timing.series_race.SeriesRaceResults;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.grahamkirby.race_timing.common.Config.*;

public abstract class RaceOutput {

    protected RaceResults race_results;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void outputResults(final RaceResults results) throws IOException {

        this.race_results = results;

        printOverallResults();

        printPrizes();
        printCombined();
        finaliseNotes();
        printNotes();

        results.getConfig().checkUnusedInputFiles();
        results.getConfig().checkUnusedProperties();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract ResultPrinterGenerator getOverallResultCSVPrinterGenerator();
    protected abstract ResultPrinterGenerator getOverallResultHTMLPrinterGenerator();
    protected abstract ResultPrinterGenerator getPrizeHTMLPrinterGenerator();

    protected static PdfFont getFont(final String font_name) throws IOException {

        return PdfFontFactory.createFont(font_name);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected Path getOutputStreamPath(final String output_type, final String file_suffix) {

        final Config config = race_results.getConfig();

        final String race_name = config.getString(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = config.getString(KEY_YEAR);

        return config.getOutputDirectoryPath().resolve(race_name + "_" + output_type + "_" + year + "." + file_suffix);
    }

    protected OutputStream getOutputStream(final String output_type, final String file_suffix) throws IOException {

        return Files.newOutputStream(getOutputStreamPath(output_type, file_suffix), STANDARD_FILE_OPEN_OPTIONS);
    }

    protected void printResultsWithHeaderHTML(final OutputStreamWriter writer, final ResultPrinterGenerator make_overall_result_printer) throws IOException {

        writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);

        printResults(writer, make_overall_result_printer.apply(race_results, writer), this::getResultsSubHeaderHTML);
        writer.append(SOFTWARE_CREDIT_LINK_TEXT);
    }

    protected String getResultsSubHeaderHTML(final String s) {

        return "<p></p>" + LINE_SEPARATOR + "<h4>" + s + "</h4>" + LINE_SEPARATOR;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static String underline(final String header, final String character) {

        return character.repeat(header.length());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printOverallResults() throws IOException {

        printResultsCSV();
        printResultsHTML();
    }

    protected void printPrizes() throws IOException {

        printPrizesPDF();
        printPrizesHTML();
        printPrizesText();
    }

    private void printCombined() throws IOException {

        printCombinedHTML();
    }

    private void finaliseNotes() {

        for (final RaceResult result : race_results.getOverallResults())
            if (result.getCategory() == null)
                race_results.getNotes().appendToNotes("Runner " + result.getParticipantName() + " unknown category so omitted from overall results" + LINE_SEPARATOR);

        final String converted_words = race_results.getNormalisation().getNonTitleCaseWords();

        if (!converted_words.isEmpty())
            race_results.getNotes().appendToNotes("Converted to title case: " + converted_words);
    }

    /** Prints out the words converted to title case, and any other processing notes. */
    private void printNotes() throws IOException {

        final OutputStream stream = getOutputStream("processing_notes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writer.append(race_results.getNotes().getCombinedNotes());
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printResultsHTML() throws IOException {

        printResults(getOverallResultHTMLPrinterGenerator(), this::getResultsSubHeaderHTML, HTML_FILE_SUFFIX);
    }

    private void printResultsCSV() throws IOException {

        printResults(getOverallResultCSVPrinterGenerator(), _ -> "", CSV_FILE_SUFFIX);
    }

    private void printResults(final ResultPrinterGenerator printer_generator, final Function<String, String> get_results_sub_header, final String suffix) throws IOException {

        final OutputStream stream = getOutputStream("overall", suffix);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            final ResultPrinter printer = printer_generator.apply(race_results, writer);
            printResults(writer, printer, get_results_sub_header);
        }
    }

    /** Prints results using a specified printer, ordered by prize category groups. */
    protected void printResults(final OutputStreamWriter writer, final ResultPrinter printer, final Function<String, String> get_results_sub_header) throws IOException {

        final List<PrizeCategoryGroup> category_groups = race_results.getPrizeCategoryGroups();

        boolean not_first_category_group = false;

        for (final PrizeCategoryGroup group : category_groups) {

            // Don't display category group headers if there is only one group.
            if (category_groups.size() > 1) {
                if (not_first_category_group)
                    writer.append(LINE_SEPARATOR);
                writer.append(get_results_sub_header.apply(group.group_title()));
            }

            printer.print(race_results.getOverallResults(group.categories()));

            not_first_category_group = true;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void printPrizesHTML() throws IOException {

        final OutputStream stream = getOutputStream("prizes", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeaderHTML());
            printPrizesHTML(writer, getPrizeHTMLPrinterGenerator().apply(race_results, writer));
        }
    }

    /** Prints prizes, ordered by prize category groups. */
    protected void printPrizesHTML(final OutputStreamWriter writer, final ResultPrinter printer) {

        printPrizes(category -> printPrizesHTML(category, writer, printer));
    }

    /** Prints prizes within a given category. */
    private void printPrizesHTML(final PrizeCategory category, final OutputStreamWriter writer, final ResultPrinter printer) {

        try {
            writer.append("<p><strong>" + category.getLongName() + "</strong></p>" + LINE_SEPARATOR);

            final List<? extends RaceResult> category_prize_winners = race_results.getPrizeWinners(category);
            printer.print(category_prize_winners);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void printPrizesWithHeaderHTML(final OutputStreamWriter writer, final ResultPrinterGenerator make_prize_result_printer) throws IOException {

        writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);
        writer.append(getPrizesHeaderHTML());

        printPrizesHTML(writer, make_prize_result_printer.apply(race_results, writer));
    }

    protected String getPrizesHeaderHTML() {

        final String header = race_results instanceof final SeriesRaceResults series_race_results && series_race_results.getNumberOfRacesTakenPlace() < series_race_results.getRaceNames().size() ? "Current Standings" : "Prizes";
        return "<h4>" + header + "</h4>" + LINE_SEPARATOR;
    }

    protected void printPrizesText(final OutputStreamWriter writer, final ResultPrinter printer) {

        printPrizes(category -> printPrizesText(category, writer, printer));
    }

    private void printPrizes(final Consumer<PrizeCategory> print_category_prizes) {

        race_results.getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).              // Get all prize categories.
            filter(race_results::arePrizesInThisOrLaterCategory).                         // Ignore further categories once all prizes have been output.
            forEachOrdered(print_category_prizes);                                        // Print prizes in this category.
    }

    private void printPrizesText(final PrizeCategory category, final OutputStreamWriter writer, final ResultPrinter printer) {

        try {
            final String header = "Category: " + category.getLongName();
            writer.append(header + LINE_SEPARATOR + underline(header, "-") + LINE_SEPARATOR + LINE_SEPARATOR);

            final List<? extends RaceResult> category_prize_winners = race_results.getPrizeWinners(category);
            printer.print(category_prize_winners);

            writer.append(LINE_SEPARATOR + LINE_SEPARATOR);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void printPrizesHeaderText(final OutputStreamWriter writer) throws IOException {

        final String header = race_results.getConfig().getString(KEY_RACE_NAME_FOR_RESULTS) + " Results " + race_results.getConfig().getString(KEY_YEAR);
        writer.append(header + LINE_SEPARATOR + underline(header, "=") + LINE_SEPARATOR + LINE_SEPARATOR);
    }

    protected void printPrizesText() throws IOException {

        final OutputStream stream = getOutputStream("prizes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            printPrizesHeaderText(writer);
            printPrizesText(writer, new PrizeResultPrinterText(race_results, writer));
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

        final String year = race_results.getConfig().getString(KEY_YEAR);

        final ResultPrinter printer = new PrizeResultPrinterPDF(race_results, document);

        final Paragraph section_header = new Paragraph().
            setFont(getFont(PDF_PRIZE_FONT_NAME)).
            setFontSize(PDF_PRIZE_FONT_SIZE).
            add(race_results.getConfig().get(KEY_RACE_NAME_FOR_RESULTS) + " " + year + " Category Prizes");

        document.add(section_header);
        printPrizes(category -> printPrizesPDF(category, document, printer));
    }

    /** Prints prizes within a given category. */
    private void printPrizesPDF(final PrizeCategory category, final Document document, final ResultPrinter printer) {

        try {
            final PdfFont bold_font = getFont(PDF_PRIZE_FONT_BOLD_NAME);

            final Paragraph category_header = new Paragraph("Category: " + category.getLongName()).
                setFont(bold_font).
                setUnderline().
                setPaddingTop(PDF_PRIZE_FONT_SIZE);

            document.add(category_header);

            printer.print(race_results.getPrizeWinners(category));

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void printCombinedHTML() throws IOException {

        final OutputStream stream = getOutputStream("combined", HTML_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            printPrizesWithHeaderHTML(writer, getPrizeHTMLPrinterGenerator());
            printResultsWithHeaderHTML(writer, getOverallResultHTMLPrinterGenerator());
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static final class PrizeResultPrinterText extends ResultPrinter {

        public PrizeResultPrinterText(final RaceResults race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult result) throws IOException {

            writer.append(result.getPositionString() + ": " + result.getParticipantName() + " " + result.getPrizeDetail() + LINE_SEPARATOR);
        }

        @Override
        public void printNoResults() {
            try {
                writer.append("No results").append(LINE_SEPARATOR);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class PrizeResultPrinterPDF extends ResultPrinter {

        private final Document document;

        public PrizeResultPrinterPDF(final RaceResults race, final Document document) {

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
            paragraph.add(new Text(" " + result.getPrizeDetail()).setFont(font));

            document.add(paragraph);
        }

        @Override
        public void printNoResults() throws IOException {

            document.add(new Paragraph("No results").setFont(getFont(PDF_PRIZE_FONT_ITALIC_NAME)));
        }
    }
}
