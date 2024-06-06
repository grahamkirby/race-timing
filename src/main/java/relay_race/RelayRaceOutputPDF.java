package relay_race;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import common.Category;

import java.io.IOException;
import java.util.List;

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

        final List<Team> category_prize_winners = ((RelayRace)race).prize_winners.get(category);

        if (category_prize_winners.isEmpty())
            document.add(new Paragraph("No results", PDF_ITALIC_FONT));

        int position = 1;
        for (final Team team : category_prize_winners) {

            final RelayRaceResult result = ((RelayRace)race).overall_results[((RelayRace)race).findIndexOfTeamWithBibNumber(team.bib_number)];

            printPrizePDF(document, String.valueOf(position++), result.team.name, result.team.category.getLongName(), result.duration());
        }
    }
}
