package relay_race;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import common.Category;
import common.RaceResult;

import java.io.IOException;

public class RelayRaceOutputPDF extends RelayRaceOutput {

    public RelayRaceOutputPDF(final RelayRace results) {
        super(results);
    }

    @Override
    public void printPrizes() throws IOException {

        printPrizesPDF();
    }

    @Override
    public void printPrizes(final Category category, final Document document) {

        addCategoryHeader(category, document);

        final RaceResult[] category_prize_winners = ((RelayRace)race).prize_winners.get(category);

        if (category_prize_winners == null)
            document.add(new Paragraph("No results", PDF_ITALIC_FONT));
        else {
            int position = 1;
            for (final RaceResult team : category_prize_winners) {

                final RelayRaceResult result = ((RelayRaceResult) team);

                printPrizePDF(document, String.valueOf(position++), result.team.name, result.team.category.getLongName(), result.duration());
            }
        }
    }
}
