package minitour;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;

public abstract class MinitourRaceOutput {

    public static final String DNF_STRING = "DNF";

    final MinitourRace race;

    String year;
    String race_name_for_results;
    String race_name_for_filenames;
    String overall_results_filename;
    String prizes_filename;
    Path output_directory_path;

    public MinitourRaceOutput(final MinitourRace race) {

        this.race = race;
        configure();
    }

    private void configure() {

        readProperties();
        constructFilePaths();
    }

    private void readProperties() {

        year = race.getProperties().getProperty("YEAR");

        race_name_for_results = race.getProperties().getProperty("RACE_NAME_FOR_RESULTS");
        race_name_for_filenames = race.getProperties().getProperty("RACE_NAME_FOR_FILENAMES");
    }

    private void constructFilePaths() {

        overall_results_filename = race_name_for_filenames + "_overall_" + year;
        prizes_filename = race_name_for_filenames + "_prizes_" + year;

        output_directory_path = race.getWorkingDirectoryPath().resolve("output");
    }

    static void setPositionStrings(final MinitourRaceResult[] series_results) {

        // Sets position strings for dead heats.
        // E.g. if results 3 and 4 have the same time, both will be set to "3=".

        for (int result_index = 0; result_index < series_results.length; result_index++) {

            final MinitourRaceResult result = series_results[result_index];

            // Skip over any following results with the same times.
            result_index = groupEqualResultsAndReturnFollowingIndex(series_results, result, result_index);
        }
    }

    private static int groupEqualResultsAndReturnFollowingIndex(final MinitourRaceResult[] leg_results, final MinitourRaceResult result, final int result_index) {

        final int highest_index_with_same_duration = getHighestIndexWithSameResult(leg_results, result, result_index);

        if (highest_index_with_same_duration > result_index)

            // Record the same position for all the results with equal times.
            for (int i = result_index; i <= highest_index_with_same_duration; i++)
                leg_results[i].position_string = result_index + 1 + "=";

        else
            result.position_string = String.valueOf(result_index + 1);

        return highest_index_with_same_duration;
    }

    private static int getHighestIndexWithSameResult(final MinitourRaceResult[] leg_results, final MinitourRaceResult result, final int result_index) {

        int highest_index_with_same_result = result_index;

        while (highest_index_with_same_result + 1 < leg_results.length &&
                result.duration().equals(leg_results[highest_index_with_same_result + 1].duration()))

            highest_index_with_same_result++;

        return highest_index_with_same_result;
    }

    void printResults(OutputStreamWriter writer, MinitourRaceResult[] category_prize_winner_results, ResultPrinter printer) throws IOException {

        setPositionStrings(category_prize_winner_results);

        for (final MinitourRaceResult result : category_prize_winner_results)
            printer.printResult(writer, result);
    }

    interface ResultPrinter {
        void printResult(OutputStreamWriter writer, MinitourRaceResult result) throws IOException;
    }

    public abstract void printOverallResults() throws IOException;
    public abstract void printPrizes() throws IOException;
    public abstract void printCombined() throws IOException;
}
