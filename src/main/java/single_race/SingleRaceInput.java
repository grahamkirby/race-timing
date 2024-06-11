package single_race;

import common.Race;
import common.RaceEntry;
import common.RaceInput;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public abstract class SingleRaceInput extends RaceInput {

    public SingleRaceInput(Race race) {

        super(race);

        readProperties();
        constructFilePaths();
    }

    protected void readProperties() {

        entries_filename = race.getProperties().getProperty("ENTRIES_FILENAME");
        raw_results_filename = race.getProperties().getProperty("RAW_RESULTS_FILENAME");
    }

    protected void constructFilePaths() {

        input_directory_path = race.getWorkingDirectoryPath().resolve("input");
        entries_path = input_directory_path.resolve(entries_filename);
        raw_results_path = input_directory_path.resolve(raw_results_filename);
    }

    public List<RaceEntry> loadEntries() throws IOException {

        final List<String> lines = Files.readAllLines(entries_path);
        final List<RaceEntry> entries = new ArrayList<>();

        for (final String line : lines) {

            final RaceEntry entry = makeRaceEntry(line.split("\t"));

            checkDuplicateBibNumber(entries, entry);
            checkDuplicateEntry(entries, entry);

            entries.add(entry);
        }

        return entries;
    }

    protected void checkDuplicateBibNumber(final List<RaceEntry> entries, final RaceEntry new_entry) {

        for (final RaceEntry entry : entries)
            if (entry != null && entry.bib_number == new_entry.bib_number)
                throw new RuntimeException("duplicate bib number: " + new_entry.bib_number);
    }

    protected abstract void checkDuplicateEntry(final List<RaceEntry> entries, final RaceEntry new_entry);
    protected abstract RaceEntry makeRaceEntry(final String[] elements);
}
