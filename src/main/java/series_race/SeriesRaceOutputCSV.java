package series_race;

import common.Race;
import individual_race.IndividualRace;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

public class SeriesRaceOutputCSV extends SeriesRaceOutput {

    public static final String OVERALL_RESULTS_HEADER = "Pos,Runner,Club,Category";

    public SeriesRaceOutputCSV(final SeriesRace race) {
        super(race);
    }

    @Override
    public void printPrizes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printCombined() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printOverallResults() throws IOException {

        final Path overall_results_csv_path = output_directory_path.resolve(overall_results_filename + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(overall_results_csv_path))) {

            printOverallResultsHeader(csv_writer);
            printOverallResults(csv_writer);
        }
    }

    private void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

        for (final Race individual_race : race.races)
            if (individual_race != null)
                writer.append(",").append(individual_race.getProperties().getProperty("RACE_NAME_FOR_RESULTS"));

        writer.append(",Total,Completed\n");
    }

    private void printOverallResults(final OutputStreamWriter writer) throws IOException {

        final SeriesRaceResult[] series_results = race.getOverallResults();

        setPositionStrings(series_results);

        for (final SeriesRaceResult overall_result : series_results) {

            int number_of_races_completed = 0;
            for (Race r : race.races)
                if (r != null) number_of_races_completed++;

            if (number_of_races_completed < race.races.length || overall_result.completed())
                writer.append(overall_result.position_string);

            writer.append(",").
                    append(overall_result.runner.name).append(",").
                    append(overall_result.runner.club).append(",").
                    append(overall_result.runner.category.getShortName()).append(",");

            for (final int score : overall_result.scores)
                if (score >= 0) writer.append(String.valueOf(score)).append(",");

            writer.append(String.valueOf(overall_result.totalScore())).append(",").
                    append(overall_result.completed() ? "Y" : "N").append("\n");
        }
    }

    private static void setPositionStrings(final SeriesRaceResult[] series_results) {

        // Sets position strings for dead heats.
        // E.g. if results 3 and 4 have the same time, both will be set to "3=".

        for (int result_index = 0; result_index < series_results.length; result_index++) {

            final SeriesRaceResult result = series_results[result_index];

            // Skip over any following results with the same times.
            result_index = groupEqualScoresAndReturnFollowingIndex(series_results, result, result_index);
        }
    }

    private static int groupEqualScoresAndReturnFollowingIndex(final SeriesRaceResult[] leg_results, final SeriesRaceResult result, final int result_index) {

        final int highest_index_with_same_duration = getHighestIndexWithSameScore(leg_results, result, result_index);

        if (highest_index_with_same_duration > result_index)

            // Record the same position for all the results with equal times.
            for (int i = result_index; i <= highest_index_with_same_duration; i++)
                leg_results[i].position_string = result_index + 1 + "=";

        else
            result.position_string = String.valueOf(result_index + 1);

        return highest_index_with_same_duration;
    }

    private static int getHighestIndexWithSameScore(final SeriesRaceResult[] leg_results, final SeriesRaceResult result, final int result_index) {

        int highest_index_with_same_score = result_index;

        while (highest_index_with_same_score + 1 < leg_results.length &&
                result.totalScore() == leg_results[highest_index_with_same_score + 1].totalScore())

            highest_index_with_same_score++;

        return highest_index_with_same_score;
    }
}
