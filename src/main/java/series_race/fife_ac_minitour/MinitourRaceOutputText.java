package series_race.fife_ac_minitour;

import common.output.RaceOutputText;
import common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public class MinitourRaceOutputText extends RaceOutputText {

    public MinitourRaceOutputText(final MinitourRace race) {
        super(race);
    }

    @Override
    protected void printPrizes(final OutputStreamWriter writer, final List<RaceResult> results) throws IOException {

        printResults(results, new ResultPrinterText(writer));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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
