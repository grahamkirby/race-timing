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
package org.grahamkirby.race_timing.individual_race;


import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.relay_race.RelayRaceOutputPDF;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.grahamkirby.race_timing.common.Config.*;

public class IndividualRaceOutputPDF {

    private final Race race;

    IndividualRaceOutputPDF(final Race race) {
        this.race = race;
    }

    void printPrizes() throws IOException {

        final Path path = IndividualRaceResultsOutput.getOutputStreamPath(race, "prizes", PDF_FILE_SUFFIX);
        final PdfWriter writer = new PdfWriter(path.toString());

        try (final Document document = new Document(new PdfDocument(writer))) {

            RelayRaceOutputPDF.printPrizes(race, document);

            final List<String> team_prizes = ((IndividualRaceImpl)race.getSpecific()).getTeamPrizes();

            if (!team_prizes.isEmpty()) {
                document.add(new Paragraph("Team Prizes").
                    setFont(getFont(PDF_PRIZE_FONT_BOLD_NAME)).
                    setUnderline().
                    setPaddingTop(PDF_PRIZE_FONT_SIZE));

                for (final String team_prize : team_prizes)
                    document.add(new Paragraph(team_prize));
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static PdfFont getFont(final String font_name) throws IOException {
        return PdfFontFactory.createFont(font_name);
    }
}
