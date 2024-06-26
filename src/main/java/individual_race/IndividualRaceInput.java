package individual_race;

import common.Race;
import common.RaceEntry;
import common.RawResult;
import single_race.SingleRaceInput;

import java.io.IOException;
import java.util.List;

public class IndividualRaceInput extends SingleRaceInput {

    public IndividualRaceInput(final Race race) {
        super(race);
    }

    @Override
    protected RaceEntry makeRaceEntry(String[] elements) {
        return new IndividualRaceEntry(elements, race);
    }

    @Override
    public List<RawResult> loadRawResults() throws IOException {

        return loadRawResults(raw_results_path);
    }

    @Override
    protected void checkDuplicateEntry(final List<RaceEntry> entries, final RaceEntry new_entry) {

        for (final RaceEntry entry : entries)
            if (((IndividualRaceEntry)entry).runner.name.equals(((IndividualRaceEntry) new_entry).runner.name) && ((IndividualRaceEntry)entry).runner.club.equals(((IndividualRaceEntry) new_entry).runner.club))
                throw new RuntimeException("duplicate entry: " + new_entry);
    }
}
