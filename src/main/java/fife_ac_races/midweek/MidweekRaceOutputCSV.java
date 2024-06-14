package fife_ac_races.midweek;

import common.Race;
import common.RaceResult;
import common.output.RaceOutputCSV;

import java.io.IOException;
import java.io.OutputStreamWriter;

public class MidweekRaceOutputCSV extends RaceOutputCSV {

    public MidweekRaceOutputCSV(final Race race) {
        super(race);
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        printOverallResultsHeaderRootSeries(writer);
        writer.append(",Total,Completed\n");
    }

    @Override
    protected ResultPrinter getResultPrinter(OutputStreamWriter writer) {
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
        public void printNoResults() {
        }
    }
}
