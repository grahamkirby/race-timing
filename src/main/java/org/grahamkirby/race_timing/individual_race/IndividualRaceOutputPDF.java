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
package org.grahamkirby.race_timing.individual_race;

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.categories.Category;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;

import java.io.IOException;
import java.util.List;

import static org.grahamkirby.race_timing.common.Normalisation.format;
import static org.grahamkirby.race_timing.common.Race.PRIZE_FONT_ITALIC_NAME;

public class IndividualRaceOutputPDF extends RaceOutputPDF {

    public IndividualRaceOutputPDF(final IndividualRace results) {
        super(results);
    }

    @Override
    protected void printPrizes(final Document document, final Category category) throws IOException {

        final PdfFont italic_font = PdfFontFactory.createFont(PRIZE_FONT_ITALIC_NAME);
        final List<RaceResult> category_prize_winners = race.prize_winners.get(category);

        addCategoryHeader(category, document);

        if (category_prize_winners == null)
            document.add(new Paragraph("No results").setFont(italic_font));
        else {
            int position = 1;
            for (final RaceResult r : category_prize_winners) {

                final IndividualRaceResult result = ((IndividualRaceResult) r);

                printPrizePDF(document, String.valueOf(position++), result.entry.runner.name, result.entry.runner.club, format(result.duration()));
            }
        }
    }
}
