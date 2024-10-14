/*
 * Copyright 2024 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.actual_races;

import org.grahamkirby.race_timing.RaceTest;
import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class ActualRacesIndividualTest extends RaceTest {

    @Override
    protected String getResourcesPath() {
        return "actual_races/individual_race/";
    }

    @Override
    protected Race makeRace(final Path config_file_path) throws IOException {
        return new IndividualRace(config_file_path);
    }

    @Test
    public void balmullo2023() throws Exception {
        testExpectedCompletion("balmullo/2023");
    }

    @Test
    public void balmullo2024() throws Exception {
        testExpectedCompletion("balmullo/2024");
    }

    @Test
    public void dunnikier2024() throws Exception {
        testExpectedCompletion("dunnikier/2024");
    }

    @Test
    public void giffordtown2023() throws Exception {
        testExpectedCompletion("giffordtown/2023");
    }

    @Test
    public void giffordtown2024() throws Exception {
        testExpectedCompletion("giffordtown/2024");
    }

    @Test
    public void hillOfTarvit2024() throws Exception {
        testExpectedCompletion("hill_of_tarvit/2024");
    }

    @Test
    public void stAndrews2023() throws Exception {
        testExpectedCompletion("st_andrews/2023");
    }

    @Test
    public void stAndrews2024() throws Exception {
        testExpectedCompletion("st_andrews/2024");
    }

    @Test
    public void strathBlebo2023() throws Exception {
        testExpectedCompletion("strath_blebo/2023");
    }

    @Test
    public void strathBlebo2024() throws Exception {
        testExpectedCompletion("strath_blebo/2024");
    }
 }
