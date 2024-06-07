package fife_ac_races.minitour;

import common.Category;
import common.RaceResult;
import series_race.SeriesRace;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class MinitourRaceOutputText extends MinitourRaceOutput {

    public MinitourRaceOutputText(final SeriesRace race) {
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

        printResults(getMinitourRacePrizeResults(category), new ResultPrinterText(writer));

        writer.append("\n\n");
    }

    record ResultPrinterText(OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            MinitourRaceResult result = (MinitourRaceResult) r;

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
