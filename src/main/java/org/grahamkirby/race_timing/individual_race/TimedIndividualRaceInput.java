package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.single_race.SingleRace;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;

import java.util.List;

import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_DNF_FINISHERS;

class TimedIndividualRaceInput extends TimedRaceInput {

    TimedIndividualRaceInput(final TimedIndividualRace race) {
        super(race);
    }

    @Override
    protected SingleRaceEntry makeRaceEntry(final List<String> elements) {

        return new IndividualRaceEntry(elements, race);
    }

    @Override
    protected void validateConfig() {

        super.validateConfig();
        validateDNFRecords();
    }

    private void validateDNFRecords() {

        final String dnf_string = ((SingleRace) race).dnf_string;

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String bib_number : dnf_string.split(",")) {
                try {
                    Integer.parseInt(bib_number);

                } catch (final NumberFormatException e) {
                    throw new RuntimeException(STR."invalid entry '\{bib_number}' for key '\{KEY_DNF_FINISHERS}' in file '\{race.config_file_path.getFileName()}'", e);
                }
            }
    }
}
