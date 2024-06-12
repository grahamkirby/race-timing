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

    @Override
    public void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        printPrizesText(category, writer);
    }

    protected void printPrizes(List<RaceResult> results, OutputStreamWriter writer) throws IOException {

        printResults(results, new ResultPrinterText(writer));
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
