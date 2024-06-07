package fife_ac_races.midweek;

import common.Race;
import series_race.SeriesRace;
import series_race.SeriesRaceOutput;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class MidweekRaceOutputCSV extends SeriesRaceOutput {

    public MidweekRaceOutputCSV(final SeriesRace race) {
        super(race);
    }

    public void printOverallResults() throws IOException {

        final Path overall_results_csv_path = output_directory_path.resolve(overall_results_filename + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(overall_results_csv_path))) {

            printOverallResultsHeader(csv_writer);
            printOverallResults(csv_writer);
        }
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        super.printOverallResultsHeader(writer);
        writer.append(",Total,Completed\n");
    }

    @Override
    protected void printOverallResults(final OutputStreamWriter writer) throws IOException {

        final MidweekRaceResult[] series_results = ((MidweekRace)race).getOverallResults();

        setPositionStrings(series_results);

        for (final MidweekRaceResult overall_result : series_results) {

            int number_of_races_completed = 0;
            for (final Race r : race.races)
                if (r != null) number_of_races_completed++;

            if (number_of_races_completed < race.races.length || overall_result.completed())
                writer.append(overall_result.position_string);

            writer.append(",").
                    append(overall_result.runner.name).
                    append(",").
                    append(overall_result.runner.club).
                    append(",").
                    append(overall_result.runner.category.getShortName()).
                    append(",");

            for (final int score : overall_result.scores)
                if (score >= 0) writer.append(String.valueOf(score)).append(",");

            writer.append(String.valueOf(overall_result.totalScore())).
                    append(",").
                    append(overall_result.completed() ? "Y" : "N").
                    append("\n");
        }
    }
}
