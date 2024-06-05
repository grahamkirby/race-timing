package fife_ac_races.minitour;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import common.Category;
import common.Race;
import series_race.SeriesRace;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class MinitourRaceOutputPDF extends MinitourRaceOutput {

    public MinitourRaceOutputPDF(final SeriesRace race) {
        super(race);
    }

    public void printPrizes() throws IOException {

        final Path prizes_pdf_path = output_directory_path.resolve(prizes_filename + ".pdf");
        final OutputStream pdf_file_output_stream = Files.newOutputStream(prizes_pdf_path);

        final Document document = new Document();
        PdfWriter.getInstance(document, pdf_file_output_stream);

        document.open();
        document.add(new Paragraph(race_name_for_results + " " + year + " Category Prizes", PDF_BOLD_LARGE_FONT));

        for (final Category category : race.categories.getCategoriesInReportOrder())
            printPrizes(category, document);

        document.close();
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
            paragraph.add(new Chunk(" (" + Race.normaliseClubName(result.runner.club) + ") ", PDF_FONT));
            paragraph.add(new Chunk(format(result.duration()), PDF_FONT));

            document.add(paragraph);
        }

        @Override
        public void printNoResults() {
            document.add(new Paragraph("No results", PDF_ITALIC_FONT));
        }
    }
}
