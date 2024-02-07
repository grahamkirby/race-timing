package lap_race;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class OutputText extends Output {

    public OutputText(final LapRace results) {
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
    public void printLegResults(int leg) {

        throw new UnsupportedOperationException();
    }

    @Override
    public void printPrizes() throws IOException {

        final Path prizes_text_path = output_directory_path.resolve(prizes_filename + ".txt");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(prizes_text_path))) {

            writer.append(race_name_for_results).append(" Results ").append(year).append("\n");
            writer.append("============================").append("\n\n");

            for (final Category category : CATEGORY_REPORT_ORDER)
                printPrizes(category, writer);
        }
    }

    private void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        final String header = "Category: " + category.shortName();

        writer.append(header).append("\n");
        writer.append("-".repeat(header.length())).append("\n\n");

        final List<Team> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners.isEmpty())
            writer.append("No results\n");

        int position = 1;
        for (final Team team : category_prize_winners) {

            final TeamResult result = race.overall_results[race.findIndexOfTeamWithBibNumber(team.bib_number)];

            writer.append(String.valueOf(position++)).append(": ").
                    append(result.team.name).append(" (").
                    append(result.team.category.shortName()).append(") ").
                    append(format(result.duration())).append("\n");
        }

        writer.append("\n\n");
    }
}
