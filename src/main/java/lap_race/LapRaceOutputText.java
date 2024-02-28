package lap_race;

import common.Category;
import common.RawResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LapRaceOutputText extends LapRaceOutput {

    public LapRaceOutputText(final LapRace results) {
        super(results);
    }

    @Override
    public void printOverallResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printDetailedResults() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printLegResults(int leg) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printCombined() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printPrizes() throws IOException {

        final Path prizes_text_path = output_directory_path.resolve(prizes_filename + ".txt");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(prizes_text_path))) {

            writer.append(race_name_for_results).append(" Results ").append(year).append("\n");
            writer.append("============================").append("\n\n");

            for (final Category category : LapRaceCategory.getCategoriesInReportOrder())
                printPrizes(category, writer);
        }
    }

    private void printPrizes(final Category category, final OutputStreamWriter writer) throws IOException {

        final String header = "Category: " + category.shortName();
        final List<Team> category_prize_winners = race.prize_winners.get(category);

        writer.append(header).append("\n");
        writer.append("-".repeat(header.length())).append("\n\n");

        if (category_prize_winners.isEmpty())
            writer.append("No results\n");

        printPrizes(category_prize_winners, writer);
    }

    private void printPrizes(List<Team> category_prize_winners, OutputStreamWriter writer) throws IOException {

        int position = 1;
        for (final Team team : category_prize_winners) {

            final LapRaceResult result = race.overall_results[race.findIndexOfTeamWithBibNumber(team.bib_number)];

            writer.append(String.valueOf(position++)).append(": ").
                    append(result.team.name).append(" (").
                    append(result.team.category.shortName()).append(") ").
                    append(format(result.duration())).append("\n");
        }

        writer.append("\n\n");
    }

    public void printCollatedResults() throws IOException {

        final Path collated_times_text_path = output_directory_path.resolve(collated_times_filename + ".txt");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(collated_times_text_path))) {

            final Map<Integer, Integer> leg_finished_count = printResults(writer);
            final List<Duration> times_with_missing_bib_numbers = getTimesWithMissingBibNumbers();
            final List<Integer> bib_numbers_with_missing_times = getBibNumbersWithMissingTimes(leg_finished_count);

            if (!bib_numbers_with_missing_times.isEmpty())
                printDiscrepancies(bib_numbers_with_missing_times, times_with_missing_bib_numbers, writer);
        }
    }

    private Map<Integer, Integer> printResults(final OutputStreamWriter writer) throws IOException {

        final Map<Integer, Integer> leg_finished_count = new HashMap<>();

        for (int i = 0; i < race.getRawResults().length; i++) {

            final RawResult raw_result = race.getRawResults()[i];
            final boolean last_electronically_recorded_result = i == race.input.getNumberOfRawResults() - 1;

            if (last_electronically_recorded_result && race.input.getNumberOfRawResults() < race.getRawResults().length)
                raw_result.appendComment("Remaining times from paper recording sheet only.");

            printResult(raw_result, leg_finished_count, writer);
        }

        return leg_finished_count;
    }

    private List<Duration> getTimesWithMissingBibNumbers() {

        final List<Duration> times_with_missing_bib_numbers = new ArrayList<>();

        for (final RawResult raw_result : race.getRawResults()) {

            if (raw_result.getBibNumber() == null)
                times_with_missing_bib_numbers.add(raw_result.getRecordedFinishTime());
        }

        return times_with_missing_bib_numbers;
    }

    private List<Integer> getBibNumbersWithMissingTimes(final Map<Integer, Integer> leg_finished_count) {

        final List<Integer> bib_numbers_with_missing_times = new ArrayList<>();

        for (final Team team : race.entries) {

            final int number_of_legs_finished = leg_finished_count.getOrDefault(team.bib_number, 0);

            for (int i = 0; i < race.number_of_legs - number_of_legs_finished; i++)
                bib_numbers_with_missing_times.add(team.bib_number);
        }

        return bib_numbers_with_missing_times;
    }

    private static void printDiscrepancies(final List<Integer> bib_numbers_with_missing_times, final List<Duration> times_with_missing_bib_numbers, final OutputStreamWriter writer) throws IOException {

        bib_numbers_with_missing_times.sort(Integer::compareTo);

        writer.append("\nDiscrepancies:\n-------------\n\nBib numbers with missing times: ");

        boolean first = true;
        for (final int bib_number : bib_numbers_with_missing_times) {
            if (!first) writer.append(", ");
            first = false;
            writer.append(String.valueOf(bib_number));
        }

        writer.append("\n\nTimes with missing bib numbers:\n\n");

        for (final Duration time : times_with_missing_bib_numbers)
            writer.append(format(time)).append("\n");
    }

    private void printResult(final RawResult raw_result, final Map<Integer, Integer> leg_finished_count, final OutputStreamWriter writer) throws IOException {

        final Integer bib_number = raw_result.getBibNumber();

        final int legs_already_finished = leg_finished_count.getOrDefault(bib_number, 0);
        leg_finished_count.put(bib_number, legs_already_finished + 1);

        printBibNumberAndTime(raw_result, bib_number, writer);
        printLegNumber(raw_result, legs_already_finished, writer);
        printComment(raw_result, writer);
    }

    private static void printBibNumberAndTime(final RawResult raw_result, final Integer bib_number, final OutputStreamWriter writer) throws IOException {

        writer.append(bib_number != null ? String.valueOf(bib_number) : "?").
                append("\t").
                append(raw_result.getRecordedFinishTime() != null ? format(raw_result.getRecordedFinishTime()) : "?");
    }

    private static void printLegNumber(final RawResult raw_result, final int legs_already_finished, final OutputStreamWriter writer) throws IOException {

        if (raw_result.getLegNumber() > 0) {

            writer.append("\t").append(String.valueOf(raw_result.getLegNumber()));

            if (legs_already_finished >= raw_result.getLegNumber())
                raw_result.appendComment("Leg "+ raw_result.getLegNumber() + " finisher was runner " + (legs_already_finished + 1) + " to finish for team.");
        }
    }

    private static void printComment(final RawResult raw_result, final OutputStreamWriter writer) throws IOException {

        if (!raw_result.getComment().isEmpty()) {

            if (raw_result.getLegNumber() == 0) writer.append("\t");
            writer.append("\t# ").append(raw_result.getComment());
        }

        writer.append("\n");
    }
}
