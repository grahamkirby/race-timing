package series_race;

import common.Race;
import common.RaceInput;
import fife_ac_races.Midweek;
import individual_race.IndividualRace;

import java.io.IOException;

public class SeriesRaceInput extends RaceInput {

    public SeriesRaceInput(final Race race) {

        super(race);
    }

    @Override
    protected void configureIndividualRace(final IndividualRace individual_race, final int race_number) throws IOException {

        individual_race.configure();
    }
}
