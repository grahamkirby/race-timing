/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
package org.grahamkirby.race_timing_experimental.individual_race;


import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.RaceResult;
import org.grahamkirby.race_timing_experimental.common.ResultPrinter;
import org.grahamkirby.race_timing_experimental.common.SingleRaceResult;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Function;

import static org.grahamkirby.race_timing_experimental.common.Config.*;
import static org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceOutputCSV.OVERALL_RESULTS_HEADER;

public class IndividualRaceOutputPDF {

    public record PrizeWinnerDetails(String position_string, String name, String detail1, String detail2) {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String PRIZE_FONT_NAME = StandardFonts.HELVETICA;
    private static final String PRIZE_FONT_BOLD_NAME = StandardFonts.HELVETICA_BOLD;
    private static final String PRIZE_FONT_ITALIC_NAME = StandardFonts.HELVETICA_OBLIQUE;
    private static final int PRIZE_FONT_SIZE = 24;

    private static final OpenOption[] STANDARD_FILE_OPEN_OPTIONS = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};

    private final Race race;

    IndividualRaceOutputPDF(final Race race) {
        this.race = race;
    }

    public String getResultsHeader() {
        return OVERALL_RESULTS_HEADER;
    }

    protected ResultPrinter getOverallResultPrinter(final OutputStreamWriter writer) {
        return new OverallResultPrinter(race, writer);
    }

    public void printPrizes() throws IOException {

        final PdfWriter writer = new PdfWriter(getOutputFilePath((String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES), "prizes", (String) race.getConfig().get(KEY_YEAR)).toString());

        try (final Document document = new Document(new PdfDocument(writer))) {
            printPrizes(document);
        }
    }

    /**
     * Prints prizes using a specified printer, ordered by prize category groups.
     * The printer abstracts over whether output goes to an output stream writer
     * (CSV, HTML and text files) or to a PDF writer.
     */
    void printPrizes(final Function<? super PrizeCategory, Void> prize_category_printer) {

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).                       // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory). // Ignore further categories once all prizes have been output.
            forEachOrdered(prize_category_printer::apply);                       // Print prizes in this category.
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Prints prizes, ordered by prize category groups. */
    private void printPrizes(final Document document) throws IOException {

        document.add(getPrizesSectionHeader());

        printPrizes(category -> {
            printPrizes(document, category);
            return null;
        });
    }

    /** Prints prizes within a given category. */
    private void printPrizes(final Document document, final PrizeCategory category) {

        try {
            document.add(getPrizesCategoryHeader(category));

            final List<RaceResult> category_prize_winners = race.getResultsCalculator().getPrizeWinners(category);

            new PrizeResultPrinter(race, document).print(category_prize_winners);

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Paragraph getPrizesSectionHeader() throws IOException {

        return new Paragraph().
            setFont(getFont(PRIZE_FONT_NAME)).
            setFontSize(PRIZE_FONT_SIZE).
            add(STR."\{race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS)} \{race.getConfig().get(KEY_YEAR)} Category Prizes");
    }

    private static Paragraph getPrizesCategoryHeader(final PrizeCategory category) throws IOException {

        return new Paragraph(STR."Category: \{category.getLongName()}").
            setFont(getFont(PRIZE_FONT_BOLD_NAME)).
            setUnderline().
            setPaddingTop(PRIZE_FONT_SIZE);
    }

    private static PdfFont getFont(final String font_name) throws IOException {
        return PdfFontFactory.createFont(font_name);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private final class PrizeResultPrinter extends ResultPrinter {

        private final Document document;

        private PrizeResultPrinter(final Race race, final Document document) {
            super(race, null);
            this.document = document;
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            printPrize(document, getPrizeWinnerDetails(r));
        }

        @Override
        public void printNoResults() throws IOException {

            document.add(new Paragraph("No results").setFont(getFont(PRIZE_FONT_ITALIC_NAME)));
        }

        private static void printPrize(final Document document, final PrizeWinnerDetails details) throws IOException {

            final PdfFont font = getFont(PRIZE_FONT_NAME);
            final PdfFont bold_font = getFont(PRIZE_FONT_BOLD_NAME);

            final Paragraph paragraph = new Paragraph().setFont(font).setMarginBottom(0);

            paragraph.add(new Text(STR."\{details.position_string}: ").setFont(font));
            paragraph.add(new Text(details.name).setFont(bold_font));
            paragraph.add(new Text(STR." (\{details.detail1}) \{details.detail2}").setFont(font));

            document.add(paragraph);
        }
    }

    /**
     * Constructs an output stream for writing to a file in the project output directory with name constructed from the given components.
     * The file extension is determined by getFileSuffix().
     * The file is created if it does not already exist, and overwritten if it does.
     * Example file name: "balmullo_prizes_2023.html".
     *
     * @param race_name the name of the race in format suitable for inclusion with file name
     * @param output_type the type of output file e.g. "overall", "prizes" etc.
     * @param year the year of the race
     * @return an output stream for the file
     * @throws IOException if an I/O error occurs
     */
    protected OutputStream getOutputStream(final String race_name, final String output_type, final String year) throws IOException {

        return getOutputStream(race_name, output_type, year, STANDARD_FILE_OPEN_OPTIONS);
    }

    /** As {@link #getOutputStream(String, String, String)} with specified file creation options. */
    protected OutputStream getOutputStream(final String race_name, final String output_type, final String year, final OpenOption... options) throws IOException {

        return Files.newOutputStream(getOutputFilePath(race_name, output_type, year), options);
    }

    /**
     * Constructs a path for a file in the project output directory with name constructed from the given components.
     * The file extension is determined by getFileSuffix().
     * Example file name: "balmullo_prizes_2023.html".
     *
     * @param race_name the name of the race in format suitable for inclusion with file name
     * @param output_type the type of output file e.g. "overall", "prizes" etc.
     * @param year the year of the race
     * @return the path for the file
     */
    Path getOutputFilePath(final String race_name, final String output_type, final String year) {

        return race.getOutputDirectoryPath().resolve(STR."\{race_name}_\{output_type}_\{year}.pdf");
    }

//    /** Encodes a single value by surrounding with quotes if it contains a comma. */
//    public static String encode(final String s) {
//        return s.contains(",") ? STR."\"\{s}\"" : s;
//    }
//
//    public static String renderDuration(final Duration duration, final String alternative) {
//
//        return duration != null ? format(duration) : alternative;
//    }
//
//    public static String renderDuration(final RaceResult result, final String alternative) {
//
//        return IndividualRaceOutputCSV.renderDuration(result, alternative);
//    }
//
//    public static String renderDuration(final RaceResult result) {
//        return renderDuration(result, "");
//    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinter {

        // TODO investigate Files.write.
        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        public void printResult(final RaceResult r) throws IOException {
            SingleRaceResult result = (SingleRaceResult) r;

            writer.append(STR."\{result.position_string},\{result.entry.bib_number},\{encode(result.entry.participant.name)},").
                append(STR."\{encode(((Runner)result.entry.participant).club)},\{result.entry.participant.category.getShortName()},\{renderDuration(result, DNF_STRING)}\n");
        }
    }

    protected PrizeWinnerDetails getPrizeWinnerDetails(final RaceResult r) {
        SingleRaceResult result = (SingleRaceResult) r;

        return new PrizeWinnerDetails(result.position_string, result.entry.participant.name, ((Runner) result.entry.participant).club, renderDuration(result, DNF_STRING));
    }
}
