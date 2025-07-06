/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing_experimental.actual_races;


import org.grahamkirby.race_timing.AbstractRaceTest;
import org.grahamkirby.race_timing.individual_race.TimedIndividualRace;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceFactory;
import org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceOutputCSV;
import org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceOutputText;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

public class ActualRacesIndividualTest extends AbstractRaceTest {

    @Override
    protected void invokeMain(String[] args) throws Exception {

        try {
            Race individual_race = IndividualRaceFactory.makeIndividualRace(Path.of(args[0]));
            individual_race.processResults();

        } catch (final Exception e) {
            System.err.println(e.getMessage());
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Test
    void balmullo2023() throws Exception {
        testExpectedCompletion("actual_races/individual_race/balmullo/2023");
    }

    @Test
    void balmullo2024() throws Exception {
        testExpectedCompletion("actual_races/individual_race/balmullo/2024");
    }

    @Test
    void cupar52025() throws Exception {
        testExpectedCompletion("actual_races/individual_race/cupar_5/2025");
    }

    @Test
    void dunnikier2024() throws Exception {
        testExpectedCompletion("actual_races/individual_race/dunnikier/2024");
    }

    @Test
    void giffordtown2023() throws Exception {
        testExpectedCompletion("actual_races/individual_race/giffordtown/2023");
    }

    @Test
    void giffordtown2024() throws Exception {
        testExpectedCompletion("actual_races/individual_race/giffordtown/2024");
    }

    @Test
    void hillOfTarvit2024() throws Exception {
        testExpectedCompletion("actual_races/individual_race/hill_of_tarvit/2024");
    }

    @Test
    void hillOfTarvit2025() throws Exception {
        testExpectedCompletion("actual_races/individual_race/hill_of_tarvit/2025");
    }

    @Test
    void stAndrews2023() throws Exception {
        testExpectedCompletion("actual_races/individual_race/st_andrews/2023");
    }

    @Test
    void stAndrews2024() throws Exception {
        testExpectedCompletion("actual_races/individual_race/st_andrews/2024");
    }

    @Test
    void stAndrews2025() throws Exception {
        testExpectedCompletion("actual_races/individual_race/st_andrews/2025");
    }

    @Test
    void strathBlebo2023() throws Exception {
        testExpectedCompletion("actual_races/individual_race/strath_blebo/2023");
    }

    @Test
    void strathBlebo2024() throws Exception {
        testExpectedCompletion("actual_races/individual_race/strath_blebo/2024");
    }

    @Test
    void strathBlebo2025() throws Exception {
        testExpectedCompletion("actual_races/individual_race/strath_blebo/2025");
    }

    @Test
    void juniorHillRaces2017() throws Exception {
        testExpectedCompletion("actual_races/individual_race/junior_hill_races/2017");
    }

    @Test
    void normansLaw2025() throws Exception {
        testExpectedCompletion("actual_races/individual_race/normans_law/2025");
    }
}
