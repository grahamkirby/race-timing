package fife_ac_races.minitour;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import common.Category;
import common.RaceResult;
import series_race.SeriesRace;

import java.io.IOException;

public class MinitourRaceOutputPDF extends MinitourRaceOutput {

    public MinitourRaceOutputPDF(final SeriesRace race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        printPrizesPDF();
    }

    @Override
    public void printPrizes(final Category category, final Document document) throws IOException {

        addCategoryHeader(category, document);

        printResults(getMinitourRacePrizeResults(category), new ResultPrinterPDF(document));
    }

    record ResultPrinterPDF(Document document) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) {

            MinitourRaceResult result = (MinitourRaceResult) r;
            printPrizePDF(document, result.position_string, ((MinitourRaceResult)result).runner.name, normaliseClubName(result.runner.club), result.duration());
        }

        @Override
        public void printNoResults() {

            document.add(new Paragraph("No results", PDF_ITALIC_FONT));
        }
    }
}
