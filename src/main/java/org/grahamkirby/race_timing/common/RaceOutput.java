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
import static org.grahamkirby.race_timing.individual_race.IndividualRaceOutput.getFont;

public abstract class RaceOutput implements ResultsOutput {

    protected Race race;

    public Path getOutputStreamPath(final String output_type, final String file_suffix) {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        return race.getOutputDirectoryPath().resolve(race_name + "_" + output_type + "_" + year + "." + file_suffix);
    }

    public OutputStream getOutputStream(final String output_type, final String file_suffix) throws IOException {

        return getOutputStream(output_type, file_suffix, STANDARD_FILE_OPEN_OPTIONS);
    }

    public OutputStream getOutputStream(final String output_type, final String file_suffix, final OpenOption[] file_open_options) throws IOException {

        return Files.newOutputStream(getOutputStreamPath(output_type, file_suffix), file_open_options);
    }

    /** Prints out the words converted to title case, and any other processing notes. */
    public void printNotes() throws IOException {

        final String converted_words = race.getNormalisation().getNonTitleCaseWords();

        if (!converted_words.isEmpty())
            race.appendToNotes("Converted to title case: " + converted_words);

        final OutputStream stream = getOutputStream("processing_notes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writer.append(race.getNotes());
        }
    }

    private static String getPrizeCategoryHeader(final PrizeCategory category) {

        final String header = "Category: " + category.getLongName();
        return header + LINE_SEPARATOR + "-".repeat(header.length()) + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    public void printPrizesText(final OutputStreamWriter writer, final PrizeCategory category) {

        try {
            writer.append(getPrizeCategoryHeader(category));

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);
            new PrizeResultPrinterText(race, writer).print(category_prize_winners);

            writer.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }
        // Called from lambda that can't throw checked exception.
        catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void printPrizesWithHeaderHTML(final OutputStreamWriter writer, final ResultPrinterGenerator make_prize_result_printer) throws IOException {

        writer.append("<h3>Results</h3>").append(LINE_SEPARATOR);
        writer.append(getPrizesHeaderHTML());

        printPrizesHTML(writer, make_prize_result_printer);
    }

    public void printResultsWithHeaderHTML(final OutputStreamWriter writer, final ResultPrinterGenerator make_overall_result_printer) throws IOException {

        writer.append("<h4>Overall</h4>").append(LINE_SEPARATOR);

        printResults(writer, make_overall_result_printer.apply(race, writer), this::getResultsSubHeaderHTML);
        writer.append(SOFTWARE_CREDIT_LINK_TEXT);
    }

    public String getPrizesHeaderHTML() {

        final String header = race.getSpecific() instanceof final SeriesRace series_race && series_race.getNumberOfRacesTakenPlace() < (int) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES) ? "Current Standings" : "Prizes";
        return "<h4>" + header + "</h4>" + LINE_SEPARATOR;
    }

    public void printPrizesText(final OutputStreamWriter writer) {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).              // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).          // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizesText(writer, category));       // Print prizes in this category.
    }

    public String getPrizesHeaderText() {

        final String header = race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS) + " Results " + race.getConfig().get(KEY_YEAR);
        return header + LINE_SEPARATOR + "=".repeat(header.length()) + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    public static final class PrizeResultPrinterText extends ResultPrinterText {

        public PrizeResultPrinterText(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult result) throws IOException {

            writer.append(result.getPositionString() + ": " + result.getParticipantName() + " " + result.getPrizeDetailText() + LINE_SEPARATOR);
        }
    }

    /** Prints prizes, ordered by prize category groups. */
    public void printPrizesHTML(final OutputStreamWriter writer, final ResultPrinterGenerator make_prize_result_printer) {

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
    public void printResults(final OutputStreamWriter writer, final ResultPrinter printer, final Function<String, String> get_results_sub_header) throws IOException {

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

    public void printResults(final ResultPrinterGenerator make_result_printer) throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream("overall", HTML_FILE_SUFFIX))) {

            final ResultPrinter printer = make_result_printer.apply(race, writer);
            printResults(writer, printer, this::getResultsSubHeaderHTML);
        }
    }

    public String getResultsSubHeaderHTML(final String s) {
        return "<p></p>" + LINE_SEPARATOR + "<h4>" + s + "</h4>" + LINE_SEPARATOR;
    }

    public void printPrizesText() throws IOException {

        final OutputStream stream = getOutputStream("prizes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeaderText());
            printPrizesText(writer);
        }
    }

    public void printPrizesPDF() throws IOException {

        final Path path = getOutputStreamPath("prizes", PDF_FILE_SUFFIX);
        final PdfWriter writer = new PdfWriter(path.toString());

        try (final Document document = new Document(new PdfDocument(writer))) {

            printPrizesPDF(document);
        }
    }

    public void printPrizesPDF(final Document document) throws IOException {

        final String year = (String) race.getConfig().get(KEY_YEAR);

        final Paragraph section_header = new Paragraph().
            setFont(getFont(PDF_PRIZE_FONT_NAME)).
            setFontSize(PDF_PRIZE_FONT_SIZE).
            add(race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS) + " " + year + " Category Prizes");

        document.add(section_header);

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).              // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).          // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizesPDF(document, category));     // Print prizes in this category.
    }


    /** Prints prizes within a given category. */
    public void printPrizesPDF(final Document document, final PrizeCategory category) {

        try {
            final Paragraph category_header = new Paragraph("Category: " + category.getLongName()).
                setFont(getFont(PDF_PRIZE_FONT_BOLD_NAME)).
                setUnderline().
                setPaddingTop(PDF_PRIZE_FONT_SIZE);

            document.add(category_header);

            new PrizeResultPrinterPDF(race, document).print(race.getResultsCalculator().getPrizeWinners(category));

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static final class PrizeResultPrinterPDF extends ResultPrinter {

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
