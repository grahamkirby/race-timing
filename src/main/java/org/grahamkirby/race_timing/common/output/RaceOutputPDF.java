/*
 * Copyright 2024 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.common.output;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import static org.grahamkirby.race_timing.common.Race.SUFFIX_PDF;

public abstract class RaceOutputPDF extends RaceOutput {

    private static final String PRIZE_FONT_NAME = StandardFonts.HELVETICA;
    private static final String PRIZE_FONT_BOLD_NAME = StandardFonts.HELVETICA_BOLD;
    private static final String PRIZE_FONT_ITALIC_NAME = StandardFonts.HELVETICA_OBLIQUE;
    private static final int PRIZE_FONT_SIZE = 24;

    public RaceOutputPDF(final Race race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        final PdfWriter writer = new PdfWriter(output_directory_path.resolve(prizes_filename + getFileSuffix()).toString());

        try (final Document document = new Document(new PdfDocument(writer))) {
            printPrizes(document);
        }
    }

    @Override
    protected String getFileSuffix() {
        return SUFFIX_PDF;
    }

    @Override
    protected String getPrizesSectionHeader() {
        return "";
    }

    @Override
    protected String getResultsHeader() { return ""; }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void printPrizes(final Document document) throws IOException {

        document.add(getPrizesSectionHeaderPDF());

        printPrizes(category -> {
            try {
                printPrizes(document, category);
                return null;

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void printPrizes(final Document document, final PrizeCategory category) throws IOException {

        document.add(getPrizesCategoryHeaderPDF(category));

        final List<RaceResult> category_prize_winners = race.prize_winners.get(category);

        setPositionStrings(category_prize_winners, race.allowEqualPositions());
        new PrizeResultPrinter(race, document).print(category_prize_winners, false);
    }

    private Paragraph getPrizesSectionHeaderPDF() throws IOException {

        return new Paragraph().
                setFont(getFont(PRIZE_FONT_NAME)).
                setFontSize(PRIZE_FONT_SIZE).
                add(race_name_for_results + " " + year + " Category Prizes");
    }

    private Paragraph getPrizesCategoryHeaderPDF(final PrizeCategory category) throws IOException {

        return new Paragraph("Category: " + category.getLongName()).
                setFont(getFont(PRIZE_FONT_BOLD_NAME)).
                setUnderline().
                setPaddingTop(PRIZE_FONT_SIZE);
    }

    // Needs to be static to allow access from inner classes of subclasses of this class.
    protected static void printPrizePDF(final Document document, final String position_string, final String name, final String detail1, final String detail2) throws IOException {

        final PdfFont font = getFont(PRIZE_FONT_NAME);
        final PdfFont bold_font = getFont(PRIZE_FONT_BOLD_NAME);

        final Paragraph paragraph = new Paragraph().setFont(font).setMarginBottom(0);

        paragraph.add(new Text(position_string + ": ").setFont(font));
        paragraph.add(new Text(name).setFont(bold_font));
        paragraph.add(new Text(" (" + detail1 + ") " + detail2).setFont(font));

        document.add(paragraph);
    }

    protected static PdfFont getFont(final String font_name) throws IOException {
        return PdfFontFactory.createFont(font_name);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public record PrizeWinnerDetails(String position_string, String name, String detail1, String detail2) {}

    private static class PrizeResultPrinter extends ResultPrinter {

        private final Document document;

        public PrizeResultPrinter(final Race race, final Document document) {
            super(race, null);
            this.document = document;
        }

        @Override
        public void printResultsHeader() {
        }

        @Override
        public void printResultsFooter(final boolean include_credit_link) {
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final PrizeWinnerDetails details = race.output_PDF.getPrizeWinnerDetails(r);
            printPrizePDF(document, details.position_string, details.name, details.detail1, details.detail2);
        }

        @Override
        public void printNoResults() throws IOException {

            document.add(new Paragraph("No results").setFont(getFont(PRIZE_FONT_ITALIC_NAME)));
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract PrizeWinnerDetails getPrizeWinnerDetails(final RaceResult r);

    // Not implemented since PDF created using PDF document writer rather than output stream.
    @Override
    protected ResultPrinter getOverallResultPrinter(OutputStreamWriter writer) { throw new UnsupportedOperationException(); }

    // Not implemented since PDF created using PDF document writer rather than output stream.
    @Override
    protected ResultPrinter getPrizeResultPrinter(OutputStreamWriter writer) { throw new UnsupportedOperationException(); }

    // Not implemented since we use a specialised version creating a PDF paragraph.
    @Override
    public String getPrizesCategoryHeader(PrizeCategory category) { throw new UnsupportedOperationException(); }

    // Not implemented since we use a specialised version creating a PDF paragraph.
    @Override
    public String getPrizesCategoryFooter() { throw new UnsupportedOperationException(); }
}
