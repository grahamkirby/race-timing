package minitour;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import common.Category;
import common.Race;
import individual_race.Runner;
import lap_race.LapRace;
import lap_race.LapRaceOutput;
import lap_race.LapRaceResult;
import lap_race.Team;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static common.Race.format;

public class MinitourRaceOutputPDF extends MinitourRaceOutput {

    private static final Font PDF_FONT = FontFactory.getFont(FontFactory.HELVETICA);
    private static final Font PDF_BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
    private static final Font PDF_BOLD_UNDERLINED_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, Font.DEFAULTSIZE, Font.UNDERLINE);
    private static final Font PDF_BOLD_LARGE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
    private static final Font PDF_ITALIC_FONT = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE);

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

    private void printPrizes(final Category category, final Document document) throws IOException {

        final Paragraph category_header_paragraph = new Paragraph(48f, "Category: " + category.getShortName(), PDF_BOLD_UNDERLINED_FONT);
        category_header_paragraph.setSpacingAfter(12);
        document.add(category_header_paragraph);

        final List<Runner> category_prize_winners = race.prize_winners.get(category);

        final MinitourRaceResult[] category_prize_winner_results = new MinitourRaceResult[category_prize_winners.size()];
        for (int i = 0; i < category_prize_winners.size(); i++) {
            for (MinitourRaceResult result : race.overall_results) {
                if (result.runner.equals(category_prize_winners.get(i))) {
                    category_prize_winner_results[i] = result;
                    break;
                }
            }
        }

        printResults(getMinitourRaceResults(category), new ResultPrinter() {

            @Override
            public void printResult(MinitourRaceResult result) throws IOException {
                final Paragraph paragraph = new Paragraph();
                paragraph.add(new Chunk(result.position_string + ": ", PDF_FONT));
                paragraph.add(new Chunk(result.runner.name, PDF_BOLD_FONT));
                paragraph.add(new Chunk(" (" + result.runner.category.getShortName() + ") ", PDF_FONT));
                paragraph.add(new Chunk(format(result.duration()), PDF_FONT));
                document.add(paragraph);
            }

            @Override
            public void printNoResults() throws IOException {
                document.add(new Paragraph("No results", PDF_ITALIC_FONT));

            }
        });
    }
}
