package fife_ac_races.minitour;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import common.Category;
import common.RaceOutputPDF;
import common.RaceResult;

import java.io.IOException;
import java.util.List;

public class MinitourRaceOutputPDF extends RaceOutputPDF {

    public MinitourRaceOutputPDF(final MinitourRace race) {
        super(race);
    }

    @Override
    public void printPrizes(final Document document, final Category category) throws IOException {

        addCategoryHeader(category, document);

        final List<RaceResult> results = race.prize_winners.get(category);

        setPositionStrings(results, true);
        printResults(results, new ResultPrinterPDF(document));
    }

    record ResultPrinterPDF(Document document) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) {

            MinitourRaceResult result = (MinitourRaceResult) r;
            printPrizePDF(document, result.position_string, result.runner.name, result.runner.club, result.duration());
        }

        @Override
        public void printNoResults() {

            document.add(new Paragraph("No results", PDF_ITALIC_FONT));
        }
    }
}
