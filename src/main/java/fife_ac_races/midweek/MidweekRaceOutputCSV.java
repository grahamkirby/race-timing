package fife_ac_races.midweek;

import common.Race;
import common.RaceOutputCSV;
import common.RaceResult;
import series_race.SeriesRace;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class MidweekRaceOutputCSV extends RaceOutputCSV {

    public MidweekRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        printOverallResultsHeader2(writer);
        writer.append(",Total,Completed\n");
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
    protected ResultPrinter getResultPrinterCSV(OutputStreamWriter writer) {
        return new ResultPrinterCSV(race, writer);
    }

    @Override
    protected boolean allowEqualPositions() {
        return true;
    }

    private record ResultPrinterCSV(Race race, OutputStreamWriter writer) implements ResultPrinter {

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MidweekRaceResult result = (MidweekRaceResult) r;

            if (getNumberOfRacesCompleted() < ((MidweekRace)race).races.size() || result.completed())
                writer.append(result.position_string);

            writer.append(",").
                    append(result.runner.name).
                    append(",").
                    append(result.runner.club).
                    append(",").
                    append(result.runner.category.getShortName()).
                    append(",");

            for (final int score : result.scores)
                if (score >= 0) writer.append(String.valueOf(score)).append(",");

            writer.append(String.valueOf(result.totalScore())).
                    append(",").
                    append(result.completed() ? "Y" : "N").
                    append("\n");
        }

        private int getNumberOfRacesCompleted() {

            int number_of_races_completed = 0;
            for (final Race individual_race : ((MidweekRace)race).races)
                if (individual_race != null) number_of_races_completed++;
            return number_of_races_completed;
        }

        @Override
        public void printNoResults() throws IOException {
        }
    }
}
