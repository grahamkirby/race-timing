package individual_race;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import common.Category;
import common.RaceResult;

import java.io.IOException;

public class IndividualRaceOutputPDF extends IndividualRaceOutput {

    public IndividualRaceOutputPDF(final IndividualRace results) {
        super(results);
    }

    @Override
    public void printPrizes() throws IOException {

        printPrizesPDF();
    }

    @Override
    public void printPrizes(final Category category, final Document document) {

        addCategoryHeader(category, document);

        final RaceResult[] category_prize_winners = ((IndividualRace)race).prize_winners.get(category);

        if (category_prize_winners == null)
            document.add(new Paragraph("No results", PDF_ITALIC_FONT));
        else {
            int position = 1;
            for (final RaceResult entry : category_prize_winners) {

                final IndividualRaceResult result = ((IndividualRaceResult) entry);

                printPrizePDF(document, String.valueOf(position++), result.entry.runner.name, normaliseClubName(result.entry.runner.club), result.duration());
            }
        }
    }
}
