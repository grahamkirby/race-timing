package fife_ac_races.minitour;

import common.Category;
import common.RaceResult;
import series_race.SeriesRaceOutput;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class MinitourRaceOutputText extends SeriesRaceOutput {

    public MinitourRaceOutputText(final MinitourRace race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        printPrizesText();
    }

    public void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        final String header = "Category: " + category.getLongName();

        writer.append(header).append("\n");
        writer.append("-".repeat(header.length())).append("\n\n");

        final List<RaceResult> results = race.prize_winners.get(category);

        setPositionStrings(results, true);
        printResults(results, new ResultPrinterText(writer));

        writer.append("\n\n");
    }

    record ResultPrinterText(OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MinitourRaceResult result = (MinitourRaceResult) r;

            writer.append(result.position_string).append(": ").
                    append(result.runner.name).append(" (").
                    append(result.runner.club).append(") ").
                    append(format(result.duration())).append("\n");
        }

        @Override
        public void printNoResults() throws IOException {
            writer.append("No results\n");
        }
    }
}
