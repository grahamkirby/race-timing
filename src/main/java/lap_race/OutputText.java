package lap_race;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class OutputText extends Output {

    public OutputText(final Results results) {
        super(results);
    }

    @Override
    public void printOverallResults() {

        throw new UnsupportedOperationException();
    }

    @Override
    public void printDetailedResults() {

        throw new UnsupportedOperationException();
    }

    @Override
    void printLegResults(int leg) {

        throw new UnsupportedOperationException();
    }

    @Override
    public void printPrizes() throws IOException {

        final Path prizes_text_path = output_directory_path.resolve(prizes_filename + ".txt");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(prizes_text_path))) {

            writer.append(race_name_for_results).append(" Results ").append(year).append("\n");
            writer.append("============================").append("\n\n");

            for (final Category category : CATEGORY_REPORT_ORDER) {

                final String header = "Category: " + category;

                writer.append(header).append("\n");
                writer.append("-".repeat(header.length())).append("\n\n");

                final List<Team> category_prize_winners = results.prize_winners.get(category);

                if (category_prize_winners.isEmpty())
                    writer.append("No results\n");

                int position = 1;
                for (final Team team : category_prize_winners) {

                    final OverallResult result = results.overall_results[results.findIndexOfTeamWithBibNumber(team.bib_number)];

                    writer.append(String.valueOf(position++)).append(": ").
                            append(result.team.name).append(" (").
                            append(result.team.category.toString()).append(") ").
                            append(OverallResult.format(result.duration())).append("\n");
                }

                writer.append("\n\n");
            }
        }
    }
}
