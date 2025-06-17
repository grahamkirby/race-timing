package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing_experimental.common.Race;

import java.io.IOException;
import java.nio.file.Paths;

public class IndividualRaceTest {

    public static void main(String[] args) throws IOException {

        Race individual_race = IndividualRaceFactory.makeIndividualRace(Paths.get("config.txt"));
        individual_race.processResults();
    }
}
