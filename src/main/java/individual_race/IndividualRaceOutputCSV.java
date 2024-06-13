package individual_race;

import common.Race;
import common.RaceOutputCSV;
import common.RaceResult;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class IndividualRaceOutputCSV extends RaceOutputCSV {

    public static final String OVERALL_RESULTS_HEADER = "Pos,No,Runner,Club,Category,Time";

    public IndividualRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER).append("\n");
    }

    @Override
    protected ResultPrinter getResultPrinter(OutputStreamWriter writer) {
        return new ResultPrinterCSV(writer);
    }

    @Override
    protected boolean allowEqualPositions() {
        return false;
    }

    private record ResultPrinterCSV(OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final IndividualRaceResult result = (IndividualRaceResult)r;

            if (!result.dnf()) {
                writer.append(String.valueOf(result.position_string));

                writer.append(",").
                        append(String.valueOf(result.entry.bib_number)).append(",").
                        append(result.entry.runner.name).append(",").
                        append((result.entry.runner.club)).append(",").
                        append(result.entry.runner.category.getShortName()).append(",").
                        append(result.dnf() ? "DNF" : format(result.duration())).append("\n");
            }
        }

        @Override
        public void printNoResults() {
        }
    }
}
