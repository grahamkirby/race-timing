package individual_race;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import common.categories.Category;
import common.output.RaceOutputPDF;
import common.RaceResult;

import java.util.List;

public class IndividualRaceOutputPDF extends RaceOutputPDF {

    public IndividualRaceOutputPDF(final IndividualRace results) {
        super(results);
    }

    @Override
    public void printPrizes(final Document document, final Category category) {

        addCategoryHeader(category, document);

        final List<RaceResult> category_prize_winners = ((IndividualRace)race).prize_winners.get(category);

        if (category_prize_winners == null)
            document.add(new Paragraph("No results", PDF_ITALIC_FONT));
        else {
            int position = 1;
            for (final RaceResult r : category_prize_winners) {

                final IndividualRaceResult result = ((IndividualRaceResult) r);

                printPrizePDF(document, String.valueOf(position++), result.entry.runner.name, (result.entry.runner.club), result.duration());
            }
        }
    }
}
