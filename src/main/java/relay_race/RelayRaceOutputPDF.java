package relay_race;

import com.lowagie.text.*;
import common.Category;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class RelayRaceOutputPDF extends RelayRaceOutput {

    public RelayRaceOutputPDF(final RelayRace results) {
        super(results);
    }

    @Override
    public void printOverallResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void printOverallResultsHeader(OutputStreamWriter csv_writer) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void printOverallResults(OutputStreamWriter csv_writer) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printDetailedResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printLegResults(int leg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printCombined() {
        throw new UnsupportedOperationException();
    }

    public void printPrizes(final Category category, final Document document) {

        final Paragraph category_header_paragraph = new Paragraph(48f, "Category: " + category.getShortName(), PDF_BOLD_UNDERLINED_FONT);
        category_header_paragraph.setSpacingAfter(12);
        document.add(category_header_paragraph);

        final List<Team> category_prize_winners = ((RelayRace)race).prize_winners.get(category);

        if (category_prize_winners.isEmpty())
            document.add(new Paragraph("No results", PDF_ITALIC_FONT));

        int position = 1;
        for (final Team team : category_prize_winners) {

            final RelayRaceResult result = ((RelayRace)race).overall_results[((RelayRace)race).findIndexOfTeamWithBibNumber(team.bib_number)];

            final Paragraph paragraph = new Paragraph();
            paragraph.add(new Chunk(position++ + ": ", PDF_FONT));
            paragraph.add(new Chunk(result.team.name, PDF_BOLD_FONT));
            paragraph.add(new Chunk(" (" + result.team.category.getShortName() + ") ", PDF_FONT));
            paragraph.add(new Chunk(format(result.duration()), PDF_FONT));
            document.add(paragraph);
        }
    }
}
