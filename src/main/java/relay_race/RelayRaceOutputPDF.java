package relay_race;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import common.categories.Category;
import common.output.RaceOutputPDF;
import common.RaceResult;

import java.util.List;

public class RelayRaceOutputPDF extends RaceOutputPDF {

    public RelayRaceOutputPDF(final RelayRace results) {
        super(results);
    }

    @Override
    public void printPrizes(final Document document, final Category category) {

        addCategoryHeader(category, document);

        final List<RaceResult> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners == null)
            document.add(new Paragraph("No results", PDF_ITALIC_FONT));
        else {
            int position = 1;
            for (final RaceResult r : category_prize_winners) {

                final RelayRaceResult result = ((RelayRaceResult) r);

                printPrizePDF(document, String.valueOf(position++), result.entry.team.name, result.entry.team.category.getLongName(), format(result.duration()));
            }
        }
    }
}
