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
package org.grahamkirby.race_timing_experimental.series_race;


import org.grahamkirby.race_timing.common.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.single_race.SingleRace;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.RaceResult;
import org.grahamkirby.race_timing_experimental.common.RaceResultsCalculator;
import org.grahamkirby.race_timing_experimental.common.ResultPrinter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing_experimental.common.Config.*;

class MidweekRaceOutputCSV {

    private static final String OVERALL_RESULTS_HEADER = "Pos,Runner,Club,Category";

    private final Race race;

    MidweekRaceOutputCSV(final Race race) {
        this.race = race;
    }

    protected String getSeriesResultsHeader() {

        return STR."\{OVERALL_RESULTS_HEADER},\{((MidweekRaceImpl) race.getSpecific()).getRaces().stream().
            filter(Objects::nonNull).
            map(race -> (String) race.getConfig().get(KEY_RACE_NAME_FOR_RESULTS)).
            collect(Collectors.joining(","))}";
    }

    public String getResultsHeader() {
        return STR."\{getSeriesResultsHeader()},Total,Completed\{LINE_SEPARATOR}";
    }

    void printResults() throws IOException {

        final String race_name = (String) race.getConfig().get(KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(KEY_YEAR);

        final OutputStream stream = Files.newOutputStream(race.getOutputDirectoryPath().resolve(STR."\{race_name}_overall_\{year}.\{CSV_FILE_SUFFIX}"), STANDARD_FILE_OPEN_OPTIONS);

        try (final OutputStreamWriter writer = new OutputStreamWriter(stream)) {

//            writer.append(OVERALL_RESULTS_HEADER);
            writer.append(getResultsHeader());
            printResults(writer, new OverallResultPrinter(race, writer), _ -> "", race);
        }
    }

    static void printResults(final OutputStreamWriter writer, final ResultPrinter printer, final Function<String, String> get_results_sub_header, final Race race) throws IOException {

        // Don't display category group headers if there is only one group.
        final boolean should_display_category_group_headers = race.getCategoryDetails().getPrizeCategoryGroups().size() > 1;

        boolean not_first_category_group = false;

        for (final PrizeCategoryGroup group : race.getCategoryDetails().getPrizeCategoryGroups()) {

            if (should_display_category_group_headers) {
                if (not_first_category_group)
                    writer.append(LINE_SEPARATOR);
                writer.append(get_results_sub_header.apply(group.group_title()));
            }

            RaceResultsCalculator raceResults = race.getResultsCalculator();
            List<RaceResult> overallResults = raceResults.getOverallResults(group.categories());
            printer.print(overallResults);

            not_first_category_group = true;
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class OverallResultPrinter extends ResultPrinter {

        private OverallResultPrinter(final Race race, final OutputStreamWriter writer) {
            super(race, writer);
        }

        @Override
        public void printResult(final RaceResult r) throws IOException {

            final MidweekRaceResult result = ((MidweekRaceResult) r);

            writer.append(STR."\{result.position_string},\{encode(result.runner.name)},\{encode(result.runner.club)},\{result.runner.category.getShortName()},");

            writer.append(
                ((MidweekRaceImpl) race.getSpecific()).getRaces().stream().
                    filter(Objects::nonNull).
                    map(individual_race -> ((MidweekRaceImpl)race.getSpecific()).calculateRaceScore(individual_race, result.runner)).
                    map(String::valueOf).
                    collect(Collectors.joining(","))
            );

            writer.append(STR.",\{result.totalScore()},\{result.hasCompletedSeries() ? "Y" : "N"}\n");
        }
    }
}
