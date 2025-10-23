/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (race-timing@kirby-family.net)
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
package org.grahamkirby.race_timing.common;

import java.io.IOException;

public interface Race {

    // TODO rationalise Female/Women gender categories.
    // TODO consolidate input validation.
    // TODO output runner list and duplicate runners in series to processing notes.
    // TODO allow negative early starts.
    // TODO add junior hill races.
    // TODO add individual names to team prize results.
    // TODO allow explicitly recorded dead heat in single race.
    // TODO allow overall dead heat in relay race only where at least one team in a mass start.
    // TODO use tree structured set of result comparators.
    // TODO tests - check existence of required config fields.
    // TODO tests - validate required config fields.
    // TODO tests - validate optional config fields.
    // TODO test for illegal bib number in raw times.
    // TODO mutation tests.
    // TODO fuzz tests.
    // TODO test missing output directory.
    // TODO test input directory with different name.
    // TODO test running from jar.
    // TODO update README (https://www.makeareadme.com).
    // TODO individual race with no results - generate output with dummy times to check runner/bib duplicates.
    // TODO individual race with no results -  generate racer list for PocketTimer.
    // TODO series race with separate entries - output sorted runner names to notes.
    // TODO individual race with team prizes - output sorted club names to notes
    // TODO suppress prize output in individual tour races.
    // TODO control category order in prize list output.

    void processResults();
    void outputResults() throws IOException;
}
