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
package org.grahamkirby.race_timing.series_race.fife_ac_minitour;

import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResult;
import org.grahamkirby.race_timing.series_race.SeriesRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class MinitourRace extends SeriesRace {

    public MinitourRace(final Path config_file_path) throws IOException {

        super(config_file_path);
        minimum_number_of_races = races.size();
    }

    public static void main(final String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java MinitourRace <config file path>");
        else
            new MinitourRace(Paths.get(args[0])).processResults();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public int compareCompletionSoFar(final RaceResult r1, final RaceResult r2) {

        final boolean r1_completed_all_races_so_far = ((MinitourRaceResult) r1).completedAllRacesSoFar();
        final boolean r2_completed_all_races_so_far = ((MinitourRaceResult) r2).completedAllRacesSoFar();

        return Boolean.compare(r2_completed_all_races_so_far, r1_completed_all_races_so_far);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void readProperties() {
    }

    @Override
    protected RaceInput getInput() {
        return new MinitourRaceInput(this);
    }

    @Override
    protected RaceOutputCSV getOutputCSV() {
        return new MinitourRaceOutputCSV(this);
    }

    @Override
    protected RaceOutputHTML getOutputHTML() {
        return new MinitourRaceOutputHTML(this);
    }

    @Override
    protected RaceOutputText getOutputText() {
        return new MinitourRaceOutputText(this);
    }

    @Override
    protected RaceOutputPDF getOutputPDF() {
        return new MinitourRaceOutputPDF(this);
    }

    @Override
    protected void printCombined() throws IOException {

        output_HTML.printCombined();
        ((MinitourRaceOutputHTML) output_HTML).printIndividualRaces();
    }

    @Override
    protected List<Comparator<RaceResult>> getComparators() {

        return List.of(this::compareCompletion, this::compareCompletionSoFar, this::comparePerformance, this::compareRunnerLastName, this::compareRunnerFirstName);
    }

    @Override
    protected List<Comparator<RaceResult>> getDNFComparators() {
        return List.of();
    }

    @Override
    protected EntryCategory getEntryCategory(RaceResult result) {
        return ((MinitourRaceResult) result).runner.category;
    }

    @Override
    protected boolean entryCategoryIsEligibleForPrizeCategoryByGender(EntryCategory entry_category, PrizeCategory prize_category) {
        return entry_category.getGender().equals(prize_category.getGender());
    }

    @Override
    protected RaceResult getOverallResult(final Runner runner) {

        final List<Duration> times = races.stream().
            map(race -> getRaceTime(race, runner)).
            toList();

        return new MinitourRaceResult(runner, times, this);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private Duration getRaceTime(final IndividualRace individual_race, final Runner runner) {

        if (individual_race == null) return null;

        return individual_race.getOverallResults().stream().
            map(result -> (IndividualRaceResult)result).
            filter(result -> result.entry.runner.equals(runner)).
            map(IndividualRaceResult::duration).
            map(Optional::ofNullable).
            findFirst().
            orElseThrow().
            orElse(null);
    }
}
