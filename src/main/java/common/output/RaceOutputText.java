package common.output;

import common.Race;
import common.RaceResult;
import common.categories.Category;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class RaceOutputText extends RaceOutput {

    public RaceOutputText(Race race) {
        super(race);
    }

    @Override
    public void printPrizes() throws IOException {

        final Path prizes_text_path = output_directory_path.resolve(prizes_filename + ".txt");

        try (final OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(prizes_text_path))) {

            writer.append(race_name_for_results).append(" Results ").append(year).append("\n");
            writer.append("============================").append("\n\n");

            for (final Category category : race.categories.getPrizeCategoriesInReportOrder())
                printPrizes(writer, category);
        }
    }

    @Override
    public void printPrizes(final OutputStreamWriter writer, final Category category) throws IOException {

        final String header = "Category: " + category.getLongName();

        writer.append(header).append("\n");
        writer.append("-".repeat(header.length())).append("\n\n");

        final List<RaceResult> results = race.prize_winners.get(category);

        setPositionStrings(results, true);
        printPrizes(writer, results);

        writer.append("\n\n");
    }
}
