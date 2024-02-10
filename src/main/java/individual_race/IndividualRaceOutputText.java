package individual_race;

import common.Category;
import lap_race.LapRace;
import lap_race.LapRaceCategory;
import lap_race.Team;
import lap_race.TeamResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IndividualRaceOutputText extends IndividualRaceOutput {

    public IndividualRaceOutputText(final IndividualRace results) {
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

        final Path prizes_text_path = output_directory_path.resolve(prizes_filename + ".txt");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(prizes_text_path))) {

            writer.append(race_name_for_results).append(" Results ").append(year).append("\n");
            writer.append("============================").append("\n\n");

            for (final Category category : IndividualRaceCategory.getCategoriesInReportOrder())
                printPrizes(category, writer);
        }
    }

    private void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        final String header = "Category: " + category.shortName();

        writer.append(header).append("\n");
        writer.append("-".repeat(header.length())).append("\n\n");

        final List<Runner> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners.isEmpty())
            writer.append("No results\n");

        int position = 1;
        for (final Runner runner : category_prize_winners) {

            final Result result = race.overall_results[race.findIndexOfRunnerWithBibNumber(runner.bib_number)];

            writer.append(String.valueOf(position++)).append(": ").
                    append(result.runner.name).append(" (").
                    append(result.runner.category.shortName()).append(") ").
                    append(format(result.duration())).append("\n");
        }

        writer.append("\n\n");
    }
}
