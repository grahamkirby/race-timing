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
package org.grahamkirby.race_timing.series_race.fife_ac_midweek;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.Category;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;

import java.io.IOException;
import java.util.List;

public class MidweekRaceOutputPDF extends RaceOutputPDF {

    public MidweekRaceOutputPDF(final MidweekRace race) {
        super(race);
    }

    @Override
    public void printPrizes(final Document document, final Category category) throws IOException {

        addCategoryHeader(category, document);

        final List<RaceResult> results = race.prize_winners.get(category);

        setPositionStrings(results, true);
        printResults(results, new ResultPrinterPDF(document));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    record ResultPrinterPDF(Document document) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            MidweekRaceResult result = (MidweekRaceResult) r;
            printPrizePDF(document, result.position_string, result.runner.name, result.runner.club, String.valueOf(result.totalScore()));
        }

        @Override
        public void printNoResults() throws IOException {

            final PdfFont italic_font = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            document.add(new Paragraph("No results").setFont(italic_font));
        }
    }
}
