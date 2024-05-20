package minitour;

import common.Category;
import common.Race;
import individual_race.IndividualRace;
import individual_race.Runner;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class MinitourRaceOutputText extends MinitourRaceOutput {

    public MinitourRaceOutputText(final MinitourRace race) {
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

        final String header = "Category: " + category.getLongName();

        writer.append(header).append("\n");
        writer.append("-".repeat(header.length())).append("\n\n");

        final List<Runner> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners.isEmpty())
            writer.append("No results\n");

        final MinitourRaceResult[] category_prize_winner_results = new MinitourRaceResult[category_prize_winners.size()];
        for (int i = 0; i < category_prize_winners.size(); i++) {
            for (MinitourRaceResult result : race.overall_results) {
                if (result.runner.equals(category_prize_winners.get(i))) {
                    category_prize_winner_results[i] = result;
                    break;
                }
            }
        }

        setPositionStrings(category_prize_winner_results);

        for (final MinitourRaceResult result : category_prize_winner_results) {

            writer.append(String.valueOf(result.position_string)).append(": ").
                    append(result.runner.name).append(" (").
                    append(result.runner.club).append(") ").
                    append(Race.format(result.duration())).append("\n");
        }

        writer.append("\n\n");
    }
}
