package fife_ac_races.midweek;

import common.Race;
import individual_race.IndividualRace;
import series_race.SeriesRaceInput;

import java.io.IOException;

public class MidweekRaceInput extends SeriesRaceInput {

    public MidweekRaceInput(final Race race) {
        super(race);
    }

    @Override
    protected void configureIndividualRace(final IndividualRace individual_race, final int race_number) throws IOException {

        individual_race.configure();
    }
}
