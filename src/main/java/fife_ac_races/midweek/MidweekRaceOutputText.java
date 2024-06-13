package fife_ac_races.midweek;

import common.Race;
import common.RaceOutputText;
import common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class MidweekRaceOutputText extends RaceOutputText {

    public MidweekRaceOutputText(final Race race) {
        super(race);
    }

    protected void printPrizes(OutputStreamWriter writer, List<RaceResult> results) throws IOException {

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
