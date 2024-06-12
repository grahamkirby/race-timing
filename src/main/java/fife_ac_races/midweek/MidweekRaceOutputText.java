package fife_ac_races.midweek;

import common.Category;
import common.Race;
import common.RaceResult;
import series_race.SeriesRaceOutput;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class MidweekRaceOutputText extends SeriesRaceOutput {

    public MidweekRaceOutputText(final Race race) {
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

            final MidweekRaceResult result = (MidweekRaceResult) r;

            writer.append(result.position_string).append(": ").
                    append(result.runner.name).append(" (").
                    append(result.runner.club).append(") ").
                    append(String.valueOf(result.totalScore())).append("\n");
        }

        @Override
        public void printNoResults() throws IOException {
            writer.append("No results\n");
        }
    }
}
