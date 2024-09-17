package series_race.fife_ac_minitour;

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

    //////////////////////////////////////////////////////////////////////////////////////////////////

    record ResultPrinterPDF(Document document) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            MinitourRaceResult result = (MinitourRaceResult) r;
            printPrizePDF(document, result.position_string, result.runner.name, result.runner.club, format(result.duration()));
        }

        @Override
        public void printNoResults() throws IOException {

            final PdfFont italic_font = PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE);

            document.add(new Paragraph("No results").setFont(italic_font));
        }
    }
}