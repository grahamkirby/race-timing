package relay_race;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import common.RaceResult;
import common.categories.Category;
import common.output.RaceOutputPDF;

import java.io.IOException;
import java.util.List;

public class RelayRaceOutputPDF extends RaceOutputPDF {

    public RelayRaceOutputPDF(final RelayRace results) {
        super(results);
    }

    @Override
    public void printPrizes(final Document document, final Category category) throws IOException {

        final PdfFont italic_font = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

        addCategoryHeader(category, document);

        final List<RaceResult> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners == null)
            document.add(new Paragraph("No results").setFont(italic_font));
        else {
            int position = 1;
            for (final RaceResult r : category_prize_winners) {

                final RelayRaceResult result = ((RelayRaceResult) r);

                printPrizePDF(document, String.valueOf(position++), result.entry.team.name, result.entry.team.category.getLongName(), format(result.duration()));
            }
        }
    }
}
