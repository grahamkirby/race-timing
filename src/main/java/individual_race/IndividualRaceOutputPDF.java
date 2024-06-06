package individual_race;

import com.lowagie.text.Document;
import com.lowagie.text.*;
import common.Category;

import java.io.IOException;
import java.util.List;

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

        final List<IndividualRaceEntry> category_prize_winners = ((IndividualRace)race).prize_winners.get(category);

        if (category_prize_winners.isEmpty())
            document.add(new Paragraph("No results", PDF_ITALIC_FONT));

        int position = 1;
        for (final IndividualRaceEntry entry : category_prize_winners) {

            final IndividualRaceResult result = ((IndividualRace)race).getOverallResults()[((IndividualRace)race).findResultsIndexOfRunnerWithBibNumber(entry.bib_number)];

            printPrizePDF(document, String.valueOf(position++), result.entry.runner.name, normaliseClubName(result.entry.runner.club), result.duration());
        }
    }
}
