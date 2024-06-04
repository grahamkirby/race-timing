package fife_ac_races.midweek;

import common.Race;
import common.RaceInput;
import individual_race.IndividualRace;

import java.io.IOException;

public class MidweekRaceInput extends RaceInput {

    public MidweekRaceInput(final Race race) {

        super(race);
    }

    @Override
    protected void configureIndividualRace(final IndividualRace individual_race, final int race_number) throws IOException {

        individual_race.configure();
    }
}
