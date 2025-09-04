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
package org.grahamkirby.race_timing.series_race;


import org.grahamkirby.race_timing.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.grahamkirby.race_timing.common.Config.renderDuration;

class TourRaceOutputCSV {

    private static final String OVERALL_RESULTS_HEADER = "Pos,Runner,Club,Category";

    private final Race race;

    TourRaceOutputCSV(final Race race) {
        this.race = race;
    }

    protected String getSeriesResultsHeader() {

        return STR."\{OVERALL_RESULTS_HEADER},\{((TourRaceImpl) race.getSpecific()).getRaces().stream().
            filter(Objects::nonNull).
            map(race -> (String) race.getConfig().get(Config.KEY_RACE_NAME_FOR_RESULTS)).
            collect(Collectors.joining(","))}";
    }

    public String getResultsHeader() {
        return STR."\{getSeriesResultsHeader()},Total\{Config.LINE_SEPARATOR}";
    }

    void printResults() throws IOException {

        final String race_name = (String) race.getConfig().get(Config.KEY_RACE_NAME_FOR_FILENAMES);
        final String year = (String) race.getConfig().get(Config.KEY_YEAR);

        final OutputStream stream = Files.newOutputStream(race.getOutputDirectoryPath().resolve(STR."\{race_name}_overall_\{year}.\{Config.CSV_FILE_SUFFIX}"), Config.STANDARD_FILE_OPEN_OPTIONS);

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
                    writer.append(Config.LINE_SEPARATOR);
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

            final TourRaceResult result = ((TourRaceResult) r);

            writer.append(STR."\{result.position_string},\{Config.encode(result.runner.name)},\{Config.encode(result.runner.club)},\{result.runner.category.getShortName()},");

            for (final Duration time : result.times)
                writer.append(Config.renderDuration(time, "-")).append(",");

            writer.append(renderDuration(result.duration(), "-")).append(Config.LINE_SEPARATOR);


//            writer.append(
//                ((TourRaceImpl) race.getSpecific()).getRaces().stream().
//                    filter(Objects::nonNull).
//                    map(individual_race -> ((TourRaceImpl)race.getSpecific()).calculateRaceScore(individual_race, result.runner)).
//                    map(String::valueOf).
//                    collect(Collectors.joining(","))
//            );
//
//            writer.append(STR.",\{renderDuration(result.duration(), "-")},\{result.hasCompletedSeries() ? "Y" : "N"}\n");
        }
    }
}
