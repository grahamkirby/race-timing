package individual_race;

import common.Category;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class IndividualRaceOutputText extends IndividualRaceOutput {

    public IndividualRaceOutputText(final IndividualRace race) {
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

        final List<IndividualRaceEntry> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners != null) {

            final String header = "Category: " + category.getLongName();

            writer.append(header).append("\n");
            writer.append("-".repeat(header.length())).append("\n\n");

            if (category_prize_winners.isEmpty())
                writer.append("No results\n");

            int position = 1;
            for (final IndividualRaceEntry entry : category_prize_winners) {

                final IndividualRaceResult result = race.getOverallResults()[race.findIndexOfRunnerWithBibNumber(entry.bib_number)];

                writer.append(String.valueOf(position++)).append(": ").
                        append(result.entry.runner.name).append(" (").
                        //append(IndividualRace.normaliseClubName(result.entry.runner.club)).append(") ").
                        append(result.entry.runner.club).append(") ").
                        append(format(result.duration())).append("\n");
            }

            writer.append("\n\n");
        }
    }
}
