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
package org.grahamkirby.race_timing.series_race;


import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.ResultPrinter;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.io.IOException;

import static org.grahamkirby.race_timing.common.Config.*;

class GrandPrixRaceOutputPDF {

    private final Race race;

    GrandPrixRaceOutputPDF(final Race race) {
        this.race = race;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    void printPrizes() throws IOException {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        final PdfWriter writer = new PdfWriter(race.getOutputDirectoryPath().resolve(race_name + "_prizes_" + year + "." + PDF_FILE_SUFFIX).toString());

        try (final Document document = new Document(new PdfDocument(writer))) {

            final Paragraph section_header = new Paragraph().
                setFont(getFont(PDF_PRIZE_FONT_NAME)).
                setFontSize(PDF_PRIZE_FONT_SIZE).
                add(race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS) + " " + race.getConfig().get(KEY_YEAR) + " Category Prizes");

            document.add(section_header);

            race.getCategoryDetails().getPrizeCategoryGroups().stream().
                flatMap(group -> group.categories().stream()).               // Get all prize categories.
                filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).           // Ignore further categories once all prizes have been output.
                forEachOrdered(category -> printPrizes(document, category));      // Print prizes in this category.
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Prints prizes within a given category. */
    private void printPrizes(final Document document, final PrizeCategory category) {

        try {
            final Paragraph category_header = new Paragraph("Category: " + category.getLongName()).
                setFont(getFont(PDF_PRIZE_FONT_BOLD_NAME)).
                setUnderline().
                setPaddingTop(PDF_PRIZE_FONT_SIZE);

            document.add(category_header);

            new PrizeResultPrinter(race, document).print(race.getResultsCalculator().getPrizeWinners(category));

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
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

        public void printResult(final RaceResult r) throws IOException {

            printPrize(document, getPrizeWinnerDetails(r));
        }

        @Override
        public void printNoResults() throws IOException {

            document.add(new Paragraph("No results").setFont(getFont(PDF_PRIZE_FONT_ITALIC_NAME)));
        }

        private void printPrize(final Document document, final PrizeWinnerDetails details) throws IOException {

            final PdfFont font = getFont(PDF_PRIZE_FONT_NAME);
            final PdfFont bold_font = getFont(PDF_PRIZE_FONT_BOLD_NAME);

            final Paragraph paragraph = new Paragraph().setFont(font).setMarginBottom(0);

            paragraph.add(new Text(details.position_string + ": ").setFont(font));
            paragraph.add(new Text(details.name).setFont(bold_font));
            paragraph.add(new Text(" (" + details.detail1 + ") " + details.detail2).setFont(font));

            document.add(paragraph);
        }
    }

    protected PrizeWinnerDetails getPrizeWinnerDetails(final RaceResult r) {

        final GrandPrixRaceResult result = (GrandPrixRaceResult) r;
        final Runner runner = (Runner) result.getParticipant();

        return new PrizeWinnerDetails(result.getPositionString(), runner.name, runner.club, String.valueOf(result.totalScore()));
    }

    public record PrizeWinnerDetails(String position_string, String name, String detail1, String detail2) {
    }
}
