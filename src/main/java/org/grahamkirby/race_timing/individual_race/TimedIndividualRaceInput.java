package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.single_race.SingleRaceEntry;

import java.util.List;

import static org.grahamkirby.race_timing.single_race.SingleRace.KEY_DNF_FINISHERS;

class TimedIndividualRaceInput extends TimedRaceInput {

    TimedIndividualRaceInput(final TimedIndividualRace race) {
        super(race);
    }

    @Override
    protected SingleRaceEntry makeRaceEntry(final List<String> elements) {

        return new TimedIndividualRaceEntry(elements, race);
    }

    @Override
    protected void checkConfig() {

        final String dnf_string = race.getOptionalProperty(KEY_DNF_FINISHERS);
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
