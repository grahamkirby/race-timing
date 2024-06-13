package fife_ac_races.minitour;

import common.Race;
import common.RaceOutputCSV;
import common.RaceResult;
import series_race.SeriesRace;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.Duration;
import java.util.List;

public class MinitourRaceOutputCSV extends RaceOutputCSV {

    public MinitourRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        printOverallResultsHeader2(writer);
        writer.append(",Total\n");
    }

    protected void printOverallResultsHeader2(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

        for (final Race individual_race : ((SeriesRace)race).races)
            if (individual_race != null)
                writer.append(",").
                        append(individual_race.getProperties().getProperty("RACE_NAME_FOR_RESULTS"));
    }

    @Override
    protected void printOverallResults(final OutputStreamWriter writer) throws IOException {

        printOverallResultsCSV(writer);
    }

    @Override
    protected List<List<String>> getResultCategoryGroups() {

        return List.of(
                List.of("FU9", "MU9"),
                List.of("FU11", "MU11"),
                List.of("FU13", "MU13"),
                List.of("FU15", "MU15"),
                List.of("FU18", "MU18")
                );
    }

    @Override
    protected ResultPrinter getResultPrinterCSV(OutputStreamWriter writer) {
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
