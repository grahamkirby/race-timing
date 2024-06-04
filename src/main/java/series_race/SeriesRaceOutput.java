package series_race;

import common.Race;
import common.RaceOutput;

import java.io.IOException;
import java.io.OutputStreamWriter;

public abstract class SeriesRaceOutput extends RaceOutput {

    protected SeriesRace race;

    public SeriesRaceOutput(SeriesRace race) {

        super(race);
        this.race = race;
    }

    @Override
    protected void printOverallResultsHeader(final OutputStreamWriter writer) throws IOException {

        writer.append(OVERALL_RESULTS_HEADER);

        for (final Race individual_race : race.races)
            if (individual_race != null)
                writer.append(",").
                        append(individual_race.getProperties().getProperty("RACE_NAME_FOR_RESULTS"));
    }
}
