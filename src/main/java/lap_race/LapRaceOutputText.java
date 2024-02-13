package lap_race;

import common.Category;
import common.RawResult;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
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

                if (i == race.input.getNumberOfRawResults()) {
                    writer.append("""

                            // Remaining times from paper recording sheet only.

                            """);
                }

                RawResult raw_result = race.getRawResults()[i];

                final Integer bib_number = raw_result.getBibNumber();
                final int legs_already_finished = leg_finished_count.getOrDefault(bib_number, 0);

                writer.append(bib_number != null ? String.valueOf(bib_number) : "?").
                        append("\t").
                        append(raw_result.getRecordedFinishTime() != null ? format(raw_result.getRecordedFinishTime()) : "?");

                StringBuilder comment = new StringBuilder(raw_result.getComment());

                if (raw_result.getLegNumber() > 0) {
                    writer.append("\t").append(String.valueOf(raw_result.getLegNumber()));
                    if (legs_already_finished >= raw_result.getLegNumber()) {
                        if (!comment.isEmpty()) comment.append(" ");
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
        }
    }
}
