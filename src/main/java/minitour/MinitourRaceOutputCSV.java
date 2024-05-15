package minitour;

import common.Category;
import common.Race;
import individual_race.IndividualRaceOutput;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class MinitourRaceOutputCSV extends MinitourRaceOutput {

    public static final String OVERALL_RESULTS_HEADER = "Pos,Runner,Club,Category";

    public MinitourRaceOutputCSV(final MinitourRace race) {
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

        writer.append(",Total\n");
    }

    private void printOverallResults(final OutputStreamWriter writer) throws IOException {

        printOverallResults(writer, Arrays.asList(race.categories.getCategory("FU9"), race.categories.getCategory("MU9")));
        printOverallResults(writer, Arrays.asList(race.categories.getCategory("FU11"), race.categories.getCategory("MU11")));
        printOverallResults(writer, Arrays.asList(race.categories.getCategory("FU13"), race.categories.getCategory("MU13")));
        printOverallResults(writer, Arrays.asList(race.categories.getCategory("FU15"), race.categories.getCategory("MU15")));
        printOverallResults(writer, Arrays.asList(race.categories.getCategory("FU18"), race.categories.getCategory("MU18")));
    }

    private void printOverallResults(final OutputStreamWriter writer, List<Category> result_categories) throws IOException {

        final MinitourRaceResult[] series_results = race.getOverallResults(result_categories);

        setPositionStrings(series_results);

        for (final MinitourRaceResult overall_result : series_results) {

            boolean completed_all_races_so_far = true;
            for (int i = 0; i < race.races.length; i++) {
                if (race.races[i] != null && overall_result.times[i] == null) {
                    completed_all_races_so_far = false;
                    break;
                }
            }


            if (completed_all_races_so_far) {
                writer.append(overall_result.position_string);

                writer.append(",").
                        append(overall_result.runner.name).append(",").
                        append(overall_result.runner.club).append(",").
                        append(overall_result.runner.category.getShortName()).append(",");

                for (final Duration time : overall_result.times)
                    if (time != null) writer.append(IndividualRaceOutput.format(time)).append(",");

                writer.append(IndividualRaceOutput.format(overall_result.duration())).
                        append("\n");
            }
        }
    }

    private static void setPositionStrings(final MinitourRaceResult[] series_results) {

        // Sets position strings for dead heats.
        // E.g. if results 3 and 4 have the same time, both will be set to "3=".

        for (int result_index = 0; result_index < series_results.length; result_index++) {

            final MinitourRaceResult result = series_results[result_index];

            // Skip over any following results with the same times.
            result_index = groupEqualScoresAndReturnFollowingIndex(series_results, result, result_index);
        }
    }

    private static int groupEqualScoresAndReturnFollowingIndex(final MinitourRaceResult[] leg_results, final MinitourRaceResult result, final int result_index) {

        final int highest_index_with_same_duration = getHighestIndexWithSameScore(leg_results, result, result_index);

        if (highest_index_with_same_duration > result_index)

            // Record the same position for all the results with equal times.
            for (int i = result_index; i <= highest_index_with_same_duration; i++)
                leg_results[i].position_string = result_index + 1 + "=";

        else
            result.position_string = String.valueOf(result_index + 1);

        return highest_index_with_same_duration;
    }

    private static int getHighestIndexWithSameScore(final MinitourRaceResult[] leg_results, final MinitourRaceResult result, final int result_index) {

        int highest_index_with_same_score = result_index;

        while (highest_index_with_same_score + 1 < leg_results.length &&
                result.duration().equals(leg_results[highest_index_with_same_score + 1].duration()))

            highest_index_with_same_score++;

        return highest_index_with_same_score;
    }
}
