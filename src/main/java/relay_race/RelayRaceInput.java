package relay_race;

import common.Race;
import common.RaceEntry;
import common.RawResult;
import single_race.SingleRaceInput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RelayRaceInput extends SingleRaceInput {

    private Path paper_results_path, annotations_path;
    private String paper_results_filename, annotations_filename;
    private int number_of_raw_results;

    public RelayRaceInput(final Race race) {
        super(race);
    }

    @Override
    protected void readProperties() {

        super.readProperties();

        paper_results_filename = race.getProperties().getProperty("PAPER_RESULTS_FILENAME");
        annotations_filename = race.getProperties().getProperty("ANNOTATIONS_FILENAME");
    }

    @Override
    protected void constructFilePaths() {

        super.constructFilePaths();

        paper_results_path = paper_results_filename != null ? input_directory_path.resolve(paper_results_filename) : null;
        annotations_path = annotations_filename != null ? input_directory_path.resolve(annotations_filename): null;
    }

    @Override
    protected RaceEntry makeRaceEntry(final String[] elements) {
        return new RelayRaceEntry(elements, race);
    }

    @Override
    protected void checkDuplicateEntry(final List<RaceEntry> entries, final RaceEntry new_entry) {

        final String new_team_name = ((RelayRaceEntry) new_entry).team.name;

        for (final RaceEntry entry : entries)
            if (entry != null)
                if (((RelayRaceEntry) entry).team.name.equals(new_team_name))
                    throw new RuntimeException("duplicate entry: " + new_entry);
    }

    @Override
    public List<RawResult> loadRawResults() throws IOException {

        final List<RawResult> raw_results = loadRawResults(raw_results_path);
        number_of_raw_results = raw_results.size();

        if (paper_results_path != null)
            raw_results.addAll(loadRawResults(paper_results_path));

        return raw_results;
    }

    protected int getNumberOfRawResults() {
        return number_of_raw_results;
    }

    protected void loadTimeAnnotations(final List<RawResult> raw_results) throws IOException {

        if (annotations_path != null) {

            final List<String> lines = Files.readAllLines(annotations_path);

            // Skip header line.
            for (int line_index = 1; line_index < lines.size(); line_index++) {

                final String[] elements = lines.get(line_index).split("\t");

                // May add insertion option later.
                if (elements[0].equals("Update"))
                    updateResult(raw_results, elements);
            }
        }
    }

    private static void updateResult(final List<RawResult> raw_results, String[] elements) {

        final int position = Integer.parseInt(elements[1]);
        final RawResult raw_result = raw_results.get(position - 1);

        if (elements[2].equals("?")) raw_result.setBibNumber(null);
        else if (!elements[2].isEmpty()) raw_result.setBibNumber(Integer.parseInt(elements[2]));

        if (elements[3].equals("?")) raw_result.setRecordedFinishTime(null);
        else if (!elements[3].isEmpty()) raw_result.setRecordedFinishTime(Race.parseTime(elements[3]));

        if (!elements[4].isEmpty()) raw_result.appendComment(elements[4]);
    }
}
