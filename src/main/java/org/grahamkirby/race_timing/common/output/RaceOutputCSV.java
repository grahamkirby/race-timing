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
package org.grahamkirby.race_timing.common.output;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

public abstract class RaceOutputCSV extends RaceOutput {

    public RaceOutputCSV(final Race race) {
        super(race);
    }

    protected String getResultsHeader() { return ""; }

    protected void printResults(final OutputStreamWriter writer, final List<PrizeCategory> categories, final String sub_heading, boolean include_credit_link) throws IOException {
        super.printResults(writer, categories, "", false);
    }

    @Override
    public String getFileSuffix() {
        return ".csv";
    }

    @Override
    public String getPrizesSectionHeader() {
        return "";
    }

    @Override
    public String getPrizesCategoryHeader(final PrizeCategory category) {
        return "";
    }

    @Override
    public String getPrizesCategoryFooter() {
        return "";
    }

    protected static String encode(String s) {
        return s.contains(",") ? "\"" + s + "\"" : s;
    }
}
