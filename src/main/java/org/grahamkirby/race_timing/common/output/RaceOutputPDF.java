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

import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.categories.Category;

import java.io.IOException;
import java.util.List;

import static org.grahamkirby.race_timing.common.Race.*;

public abstract class RaceOutputPDF extends RaceOutput {

    public RaceOutputPDF(Race race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        final PdfWriter writer = new PdfWriter(output_directory_path.resolve(prizes_filename + ".pdf").toString());

        try (final Document document = new Document(new PdfDocument(writer))) {

            final PdfFont font = PdfFontFactory.createFont(PRIZE_FONT_NAME);

            document.add(new Paragraph().setFont(font).setFontSize(PRIZE_FONT_SIZE)
                    .add(race_name_for_results + " " + year + " Category Prizes"));

            final List<Category> categories = race.categories.getPrizeCategoriesInReportOrder();

            for (final Category category : categories)
                if (prizesInThisOrLaterCategory(category, categories)) printPrizes(document, category);
        }
    }

    protected static void addCategoryHeader(final Category category, final Document document) throws IOException {

        final Paragraph category_header_paragraph = new Paragraph("Category: " +
                category.getLongName()).
                setFont(PdfFontFactory.createFont(PRIZE_FONT_BOLD_NAME)).
                setUnderline().
                setPaddingTop(PRIZE_FONT_SIZE);

        document.add(category_header_paragraph);
    }

    protected static void printPrizePDF(final Document document, final String position_string, final String name, final String detail1, final String detail2) throws IOException {

        final PdfFont font = PdfFontFactory.createFont(PRIZE_FONT_NAME);
        final PdfFont bold_font = PdfFontFactory.createFont(PRIZE_FONT_BOLD_NAME);

        final Paragraph paragraph = new Paragraph().setFont(font).setMarginBottom(0);

        paragraph.add(new Text(position_string + ": ").setFont(font));
        paragraph.add(new Text(name).setFont(bold_font));
        paragraph.add(new Text(" (" + detail1 + ") " + detail2).setFont(font));

        document.add(paragraph);
    }

    protected abstract void printPrizes(final Document document, final Category category) throws IOException;
}
