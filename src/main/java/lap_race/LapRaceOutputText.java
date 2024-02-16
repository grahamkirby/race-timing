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
    public void printCombined() throws IOException {
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

        writer.append(header).append("\n");
        writer.append("-".repeat(header.length())).append("\n\n");

        final List<Team> category_prize_winners = race.prize_winners.get(category);

        if (category_prize_winners.isEmpty())
            writer.append("No results\n");

        int position = 1;
        for (final Team team : category_prize_winners) {

            final TeamResult result = race.overall_results[race.findIndexOfTeamWithBibNumber(team.bib_number)];

            writer.append(String.valueOf(position++)).append(": ").
                    append(result.team.name).append(" (").
                    append(result.team.category.shortName()).append(") ").
                    append(format(result.duration())).append("\n");
        }

        writer.append("\n\n");
    }

    public void printCollatedTimes() throws IOException {

        final Path collated_times_text_path = output_directory_path.resolve(collated_times_filename + ".txt");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(collated_times_text_path))) {

            Map<Integer, Integer> leg_finished_count = new HashMap<>();

            for (int i = 0; i < race.getRawResults().length; i++) {

                RawResult raw_result = race.getRawResults()[i];

                if (i == race.input.getNumberOfRawResults() - 1 && race.input.getNumberOfRawResults() < race.getRawResults().length)
                    raw_result.appendComment("Remaining times from paper recording sheet only.");

                final Integer bib_number = raw_result.getBibNumber();
                final int legs_already_finished = leg_finished_count.getOrDefault(bib_number, 0);

                writer.append(bib_number != null ? String.valueOf(bib_number) : "?").
                        append("\t").
                        append(raw_result.getRecordedFinishTime() != null ? format(raw_result.getRecordedFinishTime()) : "?");

                StringBuilder comment = new StringBuilder(raw_result.getComment());

                if (raw_result.getLegNumber() > 0) {
                    writer.append("\t").append(String.valueOf(raw_result.getLegNumber()));
                    if (legs_already_finished >= raw_result.getLegNumber()) {
                        comment.append("Leg ").
                                append(raw_result.getLegNumber()).
                                append(" finisher was runner ").
                                append(legs_already_finished + 1).
                                append(" to finish for team.");
                    }
                }

                if (!comment.isEmpty()) {
                    if (raw_result.getLegNumber() == 0) writer.append("\t");
                    writer.append("\t# ").append(comment);
                }

                leg_finished_count.put(bib_number, legs_already_finished + 1);

                writer.append("\n");
            }

            List<Integer> bib_numbers_with_missing_times = new ArrayList<>();

            for (Team team : race.entries) {

                int legs_finished = leg_finished_count.getOrDefault(team.bib_number, 0);
                for (int i = 0; i < race.number_of_legs - legs_finished; i++)
                    bib_numbers_with_missing_times.add(team.bib_number);
            }

            bib_numbers_with_missing_times.sort(Integer::compareTo);

            List<Duration> times_with_missing_bib_numbers = new ArrayList<>();

            for (int i = 0; i < race.getRawResults().length; i++) {

                if (race.getRawResults()[i].getBibNumber() == null)
                    times_with_missing_bib_numbers.add(race.getRawResults()[i].getRecordedFinishTime());
            }

            if (!bib_numbers_with_missing_times.isEmpty()) {

                writer.append("\nDiscrepancies:\n-------------\n\nBib numbers with missing times: ");
                for (int i = 0; i < bib_numbers_with_missing_times.size(); i++) {
                    if (i > 0) writer.append(", ");
                    writer.append(String.valueOf(bib_numbers_with_missing_times.get(i)));
                }

                writer.append("\n\nTimes with missing bib numbers:\n\n");
                for (Duration timesWithMissingBibNumber : times_with_missing_bib_numbers)
                    writer.append(format(timesWithMissingBibNumber)).append("\n");
            }

        }
    }
}
