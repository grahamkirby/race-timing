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

import org.grahamkirby.race_timing.common.RacePrizes;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.EntryCategory;
import org.grahamkirby.race_timing.common.categories.PrizeCategory;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResult;
import org.grahamkirby.race_timing.series_race.SeriesRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

public class MinitourRace extends SeriesRace {

    public MinitourRace(final Path config_file_path) throws IOException {

        super(config_file_path);
        minimum_number_of_races = races.size();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(final String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java MinitourRace <config file path>");
        else
            new MinitourRace(Paths.get(args[0])).processResults();
    }

    @Override
    public boolean allowEqualPositions() {

        // There can be dead heats in overall results, since these are determined by sum of multiple race times.
        return true;
    }

    @Override
    public boolean entryCategoryIsEligibleForPrizeCategoryByGender(EntryCategory entry_category, PrizeCategory prize_category) {
        return entry_category.getGender().equals(prize_category.getGender());
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public EntryCategory getEntryCategory(RaceResult result) {
        return ((MinitourRaceResult) result).runner.category;
    }

    @Override
    public List<Comparator<RaceResult>> getComparators() {
        return List.of(RaceResult::compareRunnerFirstName, RaceResult::compareRunnerLastName, RaceResult::comparePerformanceTo, MinitourRace::compareCompletionSoFar, RaceResult::compareCompletion);
    }

    @Override
    public List<Comparator<RaceResult>> getDNFComparators() {
        return List.of();
    }

    public static int compareCompletionSoFar(final RaceResult r1, final RaceResult r2) {

        return ((MinitourRaceResult) r1).compareCompletionSoFarTo((MinitourRaceResult) r2);
    }

    @Override
    protected void configureHelpers() {

        input = new MinitourRaceInput(this);

        output_CSV = new MinitourRaceOutputCSV(this);
        output_HTML = new MinitourRaceOutputHTML(this);
        output_text = new MinitourRaceOutputText(this);
        output_PDF = new MinitourRaceOutputPDF(this);

        prizes = new RacePrizes(this);
    }

    @Override
    protected void printPrizes() throws IOException {

        output_PDF.printPrizes();
        output_HTML.printPrizes();
        output_text.printPrizes();
    }

    @Override
    protected void printCombined() throws IOException {

        output_HTML.printCombined();
        ((MinitourRaceOutputHTML) output_HTML).printIndividualRaces();
    }

    @Override
    protected RaceResult getOverallResult(final Runner runner) {

        final MinitourRaceResult result = new MinitourRaceResult(runner, this);

        for (final IndividualRace individual_race : races)
            result.times.add(getRaceTime(individual_race, runner));

        return result;
    }

    @Override
    protected void readProperties() {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private Duration getRaceTime(final IndividualRace individual_race, final Runner runner) {

        if (individual_race == null) return null;

        for (RaceResult result : individual_race.getOverallResults())
            if (((IndividualRaceResult)result).entry.runner.equals(runner)) return ((IndividualRaceResult)result).duration();

        return null;
    }
}
