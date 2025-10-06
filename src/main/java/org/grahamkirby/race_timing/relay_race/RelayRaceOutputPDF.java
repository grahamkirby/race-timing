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
package org.grahamkirby.race_timing.relay_race;


import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.ResultPrinter;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResultsOutput;

import java.io.IOException;
import java.nio.file.Path;

import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.individual_race.IndividualRaceOutputPDF.getFont;

public class RelayRaceOutputPDF {

    private final Race race;

    RelayRaceOutputPDF(final Race race) {
        this.race = race;
    }

    void printPrizes() throws IOException {

        printPrizes(race);
    }

    public static void printPrizes(final Race race) throws IOException {

        final Path path = IndividualRaceResultsOutput.getOutputStreamPath(race, "prizes", PDF_FILE_SUFFIX);
        final PdfWriter writer = new PdfWriter(path.toString());

        try (final Document document = new Document(new PdfDocument(writer))) {

            printPrizes(race, document);
        }
    }

    public static void printPrizes(final Race race, final Document document) throws IOException {

        final String year = (String) race.getConfig().get(KEY_YEAR);

        final Paragraph section_header = new Paragraph().
            setFont(getFont(PDF_PRIZE_FONT_NAME)).
            setFontSize(PDF_PRIZE_FONT_SIZE).
            add(race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS) + " " + year + " Category Prizes");

        document.add(section_header);

        race.getCategoryDetails().getPrizeCategoryGroups().stream().
            flatMap(group -> group.categories().stream()).              // Get all prize categories.
            filter(race.getResultsCalculator()::arePrizesInThisOrLaterCategory).          // Ignore further categories once all prizes have been output.
            forEachOrdered(category -> printPrizes(document, category, race));     // Print prizes in this category.
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /** Prints prizes within a given category. */
    public static void printPrizes(final Document document, final PrizeCategory category, final Race race) {

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

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static final class PrizeResultPrinter extends ResultPrinter {

        private final Document document;

        public PrizeResultPrinter(final Race race, final Document document) {
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
