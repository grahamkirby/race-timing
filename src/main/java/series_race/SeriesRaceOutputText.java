package series_race;

import com.lowagie.text.Document;
import common.Category;
import common.Runner;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SeriesRaceOutputText extends SeriesRaceOutput {

    public SeriesRaceOutputText(final SeriesRace race) {
        super(race);
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

        final Path prizes_text_path = output_directory_path.resolve(prizes_filename + ".txt");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(prizes_text_path))) {

            writer.append(race_name_for_results).append(" Results ").append(year).append("\n");
            writer.append("============================").append("\n\n");

            for (final Category category : race.categories.getCategoriesInReportOrder())
                printPrizes(category, writer);
        }
    }

    private void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        final List<Runner> category_prize_winners = ((SeriesRace)race).prize_winners.get(category);

        if (category_prize_winners != null) {

            final String header = "Category: " + category.getLongName();

            writer.append(header).append("\n");
            writer.append("-".repeat(header.length())).append("\n\n");

            if (category_prize_winners.isEmpty())
                writer.append("No results\n");

            int position = 1;
            for (final Runner runner : category_prize_winners) {

                final SeriesRaceResult result = ((SeriesRace)race).getOverallResults()[((SeriesRace)race).findIndexOfRunner(runner)];

                writer.append(String.valueOf(position++)).append(": ").
                        append(runner.name).append(" (").
                        append(runner.club).append(") ").
                        append(String.valueOf(result.totalScore())).append("\n");
            }

            writer.append("\n\n");
        }
    }

    @Override
    protected void printPrizes(Category category, Document document) throws IOException {
        throw new UnsupportedOperationException();
    }
}
