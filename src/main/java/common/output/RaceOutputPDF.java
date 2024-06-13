package common.output;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import common.Race;
import common.categories.Category;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public abstract class RaceOutputPDF extends RaceOutput {

    public RaceOutputPDF(Race race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        final Path prizes_pdf_path = output_directory_path.resolve(prizes_filename + ".pdf");
        final OutputStream pdf_file_output_stream = Files.newOutputStream(prizes_pdf_path);

        final Document document = new Document();
        PdfWriter.getInstance(document, pdf_file_output_stream);

        document.open();
        document.add(new Paragraph(race_name_for_results + " " + year + " Category Prizes", PDF_BOLD_LARGE_FONT));

        for (final Category category : race.categories.getCategoriesInReportOrder())
            printPrizes(document, category);

        document.close();
    }

    protected static void printPrizePDF(Document document, String positionString, String name, String detail, Duration duration) {

        final Paragraph paragraph = new Paragraph();

        paragraph.add(new com.lowagie.text.Chunk(positionString + ": ", PDF_FONT));
        paragraph.add(new Chunk(name, PDF_BOLD_FONT));
        paragraph.add(new Chunk(" (" + detail + ") ", PDF_FONT));
        paragraph.add(new Chunk(format(duration), PDF_FONT));

        document.add(paragraph);
    }
}
