package individual_race;

import common.Category;
import common.RaceOutputText;
import common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class IndividualRaceOutputText extends RaceOutputText {

    public IndividualRaceOutputText(final IndividualRace race) {
        super(race);
    }

    public void printPrizes(final OutputStreamWriter writer, final Category category) throws IOException {

        final List<RaceResult> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners != null) {

            final String header = "Category: " + category.getLongName();

            writer.append(header).append("\n");
            writer.append("-".repeat(header.length())).append("\n\n");

            if (category_prize_winners.isEmpty())
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
