package minitour;

import com.lowagie.text.*;
import common.Category;

import java.io.IOException;

public class MinitourRaceOutputPDF extends MinitourRaceOutput {

    public MinitourRaceOutputPDF(final MinitourRace results) {
        super(results);
    }

    @Override
    public void printOverallResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printCombined() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printPrizes(final Category category, final Document document) throws IOException {

        final Paragraph category_header_paragraph = new Paragraph(48f, "Category: " + category.getShortName(), PDF_BOLD_UNDERLINED_FONT);
        category_header_paragraph.setSpacingAfter(12);
        document.add(category_header_paragraph);

        printResults(getMinitourRacePrizeResults(category), new ResultPrinterPDF(document));
    }

    record ResultPrinterPDF(Document document) implements ResultPrinter {

        @Override
        public void printResult(final MinitourRaceResult result) {

            final Paragraph paragraph = new Paragraph();

            paragraph.add(new Chunk(result.position_string + ": ", PDF_FONT));
            paragraph.add(new Chunk(result.runner.name, PDF_BOLD_FONT));
            paragraph.add(new Chunk(" (" + MinitourRace.normaliseClubName(result.runner.club) + ") ", PDF_FONT));
            paragraph.add(new Chunk(format(result.duration()), PDF_FONT));

            document.add(paragraph);
        }

        @Override
        public void printNoResults() throws IOException {
            document.add(new Paragraph("No results", PDF_ITALIC_FONT));
        }
    }
}
