package individual_race;

import common.Category;
import common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class IndividualRaceOutputText extends IndividualRaceOutput {

    public IndividualRaceOutputText(final IndividualRace race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        printPrizesText();
    }

    public void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        final RaceResult[] category_prize_winners = ((IndividualRace)race).prize_winners.get(category);

        if (category_prize_winners != null) {

            final String header = "Category: " + category.getLongName();

            writer.append(header).append("\n");
            writer.append("-".repeat(header.length())).append("\n\n");

            if (category_prize_winners.length == 0)
                writer.append("No results\n");

            int position = 1;
            for (final RaceResult entry : category_prize_winners) {

                final IndividualRaceResult result = ((IndividualRaceResult)entry);

                writer.append(String.valueOf(position++)).append(": ").
                        append(result.entry.runner.name).append(" (").
                        append(result.entry.runner.club).append(") ").
                        append(format(result.duration())).append("\n");
            }

            writer.append("\n\n");
        }
    }
}
