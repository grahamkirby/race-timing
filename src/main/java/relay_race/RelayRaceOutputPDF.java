package relay_race;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import common.Category;
import common.RaceOutputPDF;
import common.RaceResult;

import java.io.IOException;
import java.util.List;

public class RelayRaceOutputPDF extends RaceOutputPDF {

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

        final List<RaceResult> category_prize_winners = ((RelayRace)race).prize_winners.get(category);

        if (category_prize_winners == null)
            document.add(new Paragraph("No results", PDF_ITALIC_FONT));
        else {
            int position = 1;
            for (final RaceResult team : category_prize_winners) {

                final RelayRaceResult result = ((RelayRaceResult) team);

                printPrizePDF(document, String.valueOf(position++), result.entry.team.name, result.entry.team.category.getLongName(), result.duration());
            }
        }
    }
}
