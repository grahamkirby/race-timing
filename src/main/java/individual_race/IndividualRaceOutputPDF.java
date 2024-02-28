package individual_race;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import common.Category;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IndividualRaceOutputPDF extends IndividualRaceOutput {

    private static final Font PDF_FONT = FontFactory.getFont(FontFactory.HELVETICA);
    private static final Font PDF_BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
    private static final Font PDF_BOLD_UNDERLINED_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, Font.DEFAULTSIZE, Font.UNDERLINE);
    private static final Font PDF_BOLD_LARGE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
    private static final Font PDF_ITALIC_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE);

    public IndividualRaceOutputPDF(final IndividualRace results) {
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
    public void printPrizes() throws IOException {

        final Path prizes_pdf_path = output_directory_path.resolve(prizes_filename + ".pdf");
        final OutputStream pdf_file_output_stream = Files.newOutputStream(prizes_pdf_path);

        final Document document = new Document();
        PdfWriter.getInstance(document, pdf_file_output_stream);

        document.open();
        document.add(new Paragraph(race_name_for_results + " " + year + " Category Prizes", PDF_BOLD_LARGE_FONT));

        for (final Category category : IndividualRaceCategory.getCategoriesInReportOrder())
            printPrizes(category, document);

        document.close();
    }

    private void printPrizes(final Category category, final Document document) {

        final List<IndividualRaceEntry> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners != null) {
            final Paragraph category_header_paragraph = new Paragraph(48f, "Category: " + category.shortName(), PDF_BOLD_UNDERLINED_FONT);
            category_header_paragraph.setSpacingAfter(12);
            document.add(category_header_paragraph);

            if (category_prize_winners.isEmpty())
                document.add(new Paragraph("No results", PDF_ITALIC_FONT));

            int position = 1;
            for (final IndividualRaceEntry entry : category_prize_winners) {

                final IndividualRaceResult result = race.overall_results[race.findIndexOfRunnerWithBibNumber(entry.bib_number)];

                final Paragraph paragraph = new Paragraph();
                paragraph.add(new Chunk(position++ + ": ", PDF_FONT));
                paragraph.add(new Chunk(result.entry.runner.name(), PDF_BOLD_FONT));
                paragraph.add(new Chunk(" (" + result.entry.runner.category().shortName() + ") ", PDF_FONT));
                paragraph.add(new Chunk(format(result.duration()), PDF_FONT));
                document.add(paragraph);
            }
        }
    }
}
