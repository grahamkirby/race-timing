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

    public static Path getOutputStreamPath(final Race race, final String output_type, final String file_suffix) {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        return race.getOutputDirectoryPath().resolve(race_name + "_" + output_type + "_" + year + "." + file_suffix);
    }

    public static OutputStream getOutputStream(final Race race, final String output_type, final String file_suffix) throws IOException {

        return getOutputStream(race, output_type, file_suffix, STANDARD_FILE_OPEN_OPTIONS);
    }

    public static OutputStream getOutputStream(final Race race, final String output_type, final String file_suffix, final OpenOption[] file_open_options) throws IOException {

        return Files.newOutputStream(getOutputStreamPath(race, output_type, file_suffix), file_open_options);
    }

    public static void printNotes(final Race race) throws IOException {

        final String converted_words = race.getNormalisation().getNonTitleCaseWords();

        if (!converted_words.isEmpty())
            race.appendToNotes("Converted to title case: " + converted_words);

        final OutputStream stream = getOutputStream(race, "processing_notes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {
            writer.append(race.getNotes());
        }
    }

    private static String getPrizeCategoryHeader(final PrizeCategory category) {

        final String header = "Category: " + category.getLongName();
        return header + LINE_SEPARATOR + "-".repeat(header.length()) + LINE_SEPARATOR + LINE_SEPARATOR;
    }

    public static void printPrizesText(final OutputStreamWriter writer, final PrizeCategory category, final Race race) {

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

    public static String getPrizesHeaderHTML(final Race race) {

        final String header = race.getSpecific() instanceof final SeriesRace series_race && series_race.getNumberOfRacesTakenPlace() < (int) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES) ? "Current Standings" : "Prizes";
        return "<h4>" + header + "</h4>" + LINE_SEPARATOR;
    }

    public static void printPrizesText(final OutputStreamWriter writer, final Race race) {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).              // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).          // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizesText(writer, category, race));       // Print prizes in this category.
    }

    public static String getPrizesHeaderText(final Race race) {

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
    public static void printPrizesHTML(final Race race, final OutputStreamWriter writer, final ResultPrinterGenerator make_prize_result_printer) {

        final ResultPrinter printer = make_prize_result_printer.apply(race, writer);

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                        // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).                    // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizes(category, race, writer, printer));
    }

    /** Prints prizes within a given category. */
    private static void printPrizes(final PrizeCategory category, final Race race, final OutputStreamWriter writer, final ResultPrinter printer) {

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
    public static void printResults(final OutputStreamWriter writer, final ResultPrinter printer, final Function<String, String> get_results_sub_header, final Race race) throws IOException {

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

    public static void printResults(final Race race, final ResultPrinterGenerator make_result_printer) throws IOException {

        try (final OutputStreamWriter writer = new OutputStreamWriter(getOutputStream(race, "overall", HTML_FILE_SUFFIX))) {

            final ResultPrinter printer = make_result_printer.apply(race, writer);
            printResults(writer, printer, RaceOutput::getResultsSubHeaderHTML, race);
        }
    }

    public static String getResultsSubHeaderHTML(final String s) {
        return "<p></p>" + LINE_SEPARATOR + "<h4>" + s + "</h4>" + LINE_SEPARATOR;
    }

    public static void printPrizesCSV(final Race race) throws IOException {

        final OutputStream stream = getOutputStream(race, "prizes", TEXT_FILE_SUFFIX);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

            writer.append(getPrizesHeaderText(race));
            printPrizesText(writer, race);
        }
    }

    public static void printPrizesPDF(final Race race) throws IOException {

        final Path path = getOutputStreamPath(race, "prizes", PDF_FILE_SUFFIX);
        final PdfWriter writer = new PdfWriter(path.toString());

        try (final Document document = new Document(new PdfDocument(writer))) {

            printPrizesPDF(race, document);
        }
    }

    public static void printPrizesPDF(final Race race, final Document document) throws IOException {

        final String year = (String) race.getConfig().get(KEY_YEAR);

        final Paragraph section_header = new Paragraph().
            setFont(getFont(PDF_PRIZE_FONT_NAME)).
            setFontSize(PDF_PRIZE_FONT_SIZE).
            add(race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS) + " " + year + " Category Prizes");

        document.add(section_header);

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).              // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).          // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizesPDF(document, category, race));     // Print prizes in this category.
    }


    /** Prints prizes within a given category. */
    public static void printPrizesPDF(final Document document, final PrizeCategory category, final Race race) {

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
