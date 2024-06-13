package fife_ac_races.minitour;

import common.Race;
import common.RaceOutputCSV;
import common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Duration;

public class MinitourRaceOutputCSV extends RaceOutputCSV {

    public MinitourRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        printOverallResultsHeaderRoot(writer);
        writer.append(",Total\n");
    }

    @Override
    protected ResultPrinter getResultPrinter(OutputStreamWriter writer) {
        return new ResultPrinterCSV(writer);
    }

    @Override
    protected boolean allowEqualPositions() {
        return true;
    }

    private record ResultPrinterCSV(OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MinitourRaceResult result = (MinitourRaceResult) r;

            writer.append(result.completedAllRacesSoFar() ? result.position_string : "-").append(",").
                    append(result.runner.name).append(",").
                    append(result.runner.club).append(",").
                    append(result.runner.category.getShortName()).append(",");

            for (final Duration time : result.times)
                writer.append(time != null ? format(time) : "-").append(",");

            writer.append(result.completedAllRacesSoFar() ? format(result.duration()) : "-").append("\n");
        }

        @Override
        public void printNoResults() throws IOException {
            writer.append("No results\n");
        }
    }
}
