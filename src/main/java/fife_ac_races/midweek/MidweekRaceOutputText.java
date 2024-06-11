package fife_ac_races.midweek;

import common.Category;
import common.Race;
import common.RaceOutput;
import common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class MidweekRaceOutputText extends RaceOutput {

    public MidweekRaceOutputText(final Race race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        printPrizesText();
    }

    public void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        final List<RaceResult> category_prize_winners = ((MidweekRace)race).prize_winners.get(category);

        if (category_prize_winners != null) {

            final String header = "Category: " + category.getLongName();

            writer.append(header).append("\n");
            writer.append("-".repeat(header.length())).append("\n\n");

            if (category_prize_winners.isEmpty())
                writer.append("No results\n");

            int position = 1;
            for (final RaceResult runner : category_prize_winners) {

                final MidweekRaceResult result = ((MidweekRaceResult)runner);

                writer.append(String.valueOf(position++)).append(": ").
                        append(result.runner.name).append(" (").
                        append(result.runner.club).append(") ").
                        append(String.valueOf(result.totalScore())).append("\n");
            }

            writer.append("\n\n");
        }
    }
}
