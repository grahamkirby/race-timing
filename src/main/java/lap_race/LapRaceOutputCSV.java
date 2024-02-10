package lap_race;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class LapRaceOutputCSV extends LapRaceOutput {

    public static final String OVERALL_RESULTS_HEADER = "Pos,No,Team,Category,";

    public LapRaceOutputCSV(final LapRace results) {
        super(results);
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

    @Override
    public void printDetailedResults() throws IOException {

        final Path detailed_results_csv_path = output_directory_path.resolve(detailed_results_filename + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(detailed_results_csv_path))) {

            printDetailedResultsHeader(csv_writer);
            printDetailedResults(csv_writer);
        }
    }

    @Override
    public void printLegResults(final int leg) throws IOException {

        final Path leg_results_csv_path = output_directory_path.resolve(race_name_for_filenames + "_leg_" + leg + "_" + year + ".csv");

        try (final OutputStreamWriter csv_writer = new OutputStreamWriter(Files.newOutputStream(leg_results_csv_path))) {

            printLegResultsHeader(csv_writer, leg);
            printLegResults(csv_writer, getLegResults(leg));
        }
    }

    private void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER).append("Total\n");
    }

    private void printOverallResults(final OutputStreamWriter writer) throws IOException {

        for (int i = 0; i < race.overall_results.length; i++) {

            final TeamResult overall_result = race.overall_results[i];

            if (!overall_result.dnf()) writer.append(String.valueOf(i + 1));

            writer.append(",").
                    append(String.valueOf(overall_result.team.bib_number)).append(",").
                    append(overall_result.team.name).append(",").
                    append(overall_result.team.category.shortName()).append(",").
                    append(overall_result.dnf() ? "DNF" : LapRaceOutput.format(overall_result.duration())).append("\n");
        }
    }

    private void printDetailedResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

        for (int leg = 1; leg <= race.number_of_legs; leg++) {
            writer.append("Runners ").append(String.valueOf(leg)).append(",Leg ").append(String.valueOf(leg)).append(",");
            if (leg < race.number_of_legs) writer.append("Split ").append(String.valueOf(leg)).append(",");
        }

        writer.append("Total\n");
    }

    private void printDetailedResults(final OutputStreamWriter writer) throws IOException {

        for (int result_index = 0; result_index < race.overall_results.length; result_index++)
            printDetailedResult(writer, result_index);
    }

    private void printDetailedResult(final OutputStreamWriter writer, final int result_index) throws IOException {

        final TeamResult result = race.overall_results[result_index];

        if (!result.dnf()) writer.append(String.valueOf(result_index + 1));

        writer.append(",");
        writer.append(String.valueOf(result.team.bib_number)).append(",");
        writer.append(result.team.name).append(",");
        writer.append(result.team.category.shortName()).append(",");

        printLegDetails(writer, result, result.team);

        writer.append("\n");
    }

    private void printLegDetails(final OutputStreamWriter writer, final TeamResult result, final Team team) throws IOException {

        boolean any_previous_leg_dnf = false;

        for (int leg = 1; leg <= race.number_of_legs; leg++) {

            final LegResult leg_result = result.leg_results[leg - 1];

            writer.append(team.runners[leg-1]);
            addMassStartAnnotation(writer, leg_result, leg);
            writer.append(",");
            writer.append(leg_result.DNF ? DNF_STRING : format(leg_result.duration())).append(",");
            writer.append(leg_result.DNF || any_previous_leg_dnf ? DNF_STRING : format(sumDurationsUpToLeg(result.leg_results, leg)));

            if (leg < race.number_of_legs) writer.append(",");
            if (leg_result.DNF) any_previous_leg_dnf = true;
        }
    }

    private void printLegResultsHeader(final OutputStreamWriter writer, final int leg) throws IOException {

        writer.append("Pos,Runner");
        if (race.paired_legs[leg-1]) writer.append("s");
        writer.append(",Time\n");
    }

    private void printLegResults(final OutputStreamWriter writer, final LegResult[] leg_results) throws IOException {

        // Deal with dead heats in legs after the first.
        setPositionStrings(leg_results);

        for (final LegResult leg_result : leg_results)
            printLegResult(writer, leg_result);
    }

    private static void printLegResult(final OutputStreamWriter writer, final LegResult leg_result) throws IOException {

        if (!leg_result.DNF) {
            writer.append(leg_result.position_string).append(",");
            writer.append(leg_result.team.runners[leg_result.leg_number - 1]).append(",");
            writer.append(format(leg_result.duration())).append("\n");
        }
    }

    private static void setPositionStrings(final LegResult[] leg_results) {

        // Sets position strings for dead heats.
        // E.g. if results 3 and 4 have the same time, both will be set to "3=".

        for (int result_index = 0; result_index < leg_results.length; result_index++) {

            final LegResult result = leg_results[result_index];

            if (result.leg_number == 1)
                // No dead heats for leg 1; positions determined by order of recording.
                result.position_string = String.valueOf(result_index + 1);

            else
                // Skip over any following results with the same times.
                result_index = groupEqualDurationsAndReturnFollowingIndex(leg_results, result, result_index);
        }
    }

    private static int groupEqualDurationsAndReturnFollowingIndex(final LegResult[] leg_results, final LegResult result, final int result_index) {

        final int highest_index_with_same_duration = getHighestIndexWithSameDuration(leg_results, result, result_index);

        if (highest_index_with_same_duration > result_index) {

            // Record the same position for all the results with equal times.
            for (int i = result_index; i <= highest_index_with_same_duration; i++)
                leg_results[i].position_string = result_index + 1 + "=";
        }
        else {
            result.position_string = String.valueOf(result_index + 1);
        }

        return highest_index_with_same_duration;
    }

    private static int getHighestIndexWithSameDuration(final LegResult[] leg_results, final LegResult result, final int result_index) {

        int highest_index_with_same_duration = result_index;

        while (highest_index_with_same_duration + 1 < leg_results.length && result.duration().equals(leg_results[highest_index_with_same_duration + 1].duration()))
            highest_index_with_same_duration++;

        return highest_index_with_same_duration;
    }
}
