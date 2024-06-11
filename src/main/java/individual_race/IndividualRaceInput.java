package individual_race;

import common.Race;
import common.RaceEntry;
import common.RaceInput;

import java.util.List;

public class IndividualRaceInput extends RaceInput {

    public IndividualRaceInput(final Race race) {
        super(race);
    }

    @Override
    protected RaceEntry makeRaceEntry(String[] elements) {
        return new IndividualRaceEntry(elements, race);
    }

    @Override
    protected void checkDuplicateEntry(final List<RaceEntry> entries, final RaceEntry new_entry) {

        for (final RaceEntry entry : entries)
            if (((IndividualRaceEntry)entry).runner.name.equals(((IndividualRaceEntry) new_entry).runner.name) && ((IndividualRaceEntry)entry).runner.club.equals(((IndividualRaceEntry) new_entry).runner.club))
                throw new RuntimeException("duplicate entry: " + new_entry);
    }
}
