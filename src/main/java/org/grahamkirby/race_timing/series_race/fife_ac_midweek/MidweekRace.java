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
package org.grahamkirby.race_timing.series_race.fife_ac_midweek;

import org.grahamkirby.race_timing.common.CompletionStatus;
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
import org.grahamkirby.race_timing.series_race.SeriesRaceInput;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MidweekRace extends SeriesRace {

    private int max_race_score;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public MidweekRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(final String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java MidweekRace <config file path>");
        else {
            new MidweekRace(Paths.get(args[0])).processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void readProperties() {

        minimum_number_of_races = Integer.parseInt(getProperty(KEY_MINIMUM_NUMBER_OF_RACES));
        max_race_score = Integer.parseInt(getProperty(KEY_MAX_RACE_SCORE));
    }

    @Override
    protected void configureInputData() throws IOException {

        super.configureInputData();
        getRunnerNames().forEach(this::checkClubsForRunner);
    }

    @Override
    protected RaceInput getInput() {
        return new SeriesRaceInput(this);
    }

    @Override
    protected RaceOutputCSV getOutputCSV() {
        return new MidweekRaceOutputCSV(this);
    }

    @Override
    protected RaceOutputHTML getOutputHTML() {
        return new MidweekRaceOutputHTML(this);
    }

    @Override
    protected RaceOutputText getOutputText() {
        return new MidweekRaceOutputText(this);
    }

    @Override
    protected RaceOutputPDF getOutputPDF() {
        return new MidweekRaceOutputPDF(this);
    }

    @Override
    protected List<Comparator<RaceResult>> getComparators() {

        return List.of(this::compareCompletion, this::comparePerformance, this::compareRunnerLastName, this::compareRunnerFirstName);
    }

    @Override
    protected List<Comparator<RaceResult>> getDNFComparators() {
        return List.of();
    }

    @Override
    protected boolean entryCategoryIsEligibleForPrizeCategoryByGender(final EntryCategory entry_category, final PrizeCategory prize_category) {
        return entry_category.getGender().equals(prize_category.getGender());
    }

    @Override
    protected EntryCategory getEntryCategory(final RaceResult result) {
        return ((MidweekRaceResult) result).runner.category;
    }

    @Override
    protected RaceResult getOverallResult(final Runner runner) {

        final List<Integer> scores = races.stream().
            map(race -> calculateRaceScore(race, runner)).
            toList();

        return new MidweekRaceResult(runner, scores, this);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void checkClubsForRunner(final String runner_name) {

        // Where a runner name is associated with a single entry with a defined club
        // plus some other entries with no club defined, add the club to those entries.

        // Where a runner name is associated with multiple clubs, leave as is, under
        // assumption that they are separate runner_names.
        final List<String> clubs_for_runner = getRunnerClubs(runner_name);
        final List<String> defined_clubs = getDefinedClubs(clubs_for_runner);

        final int number_of_defined_clubs = defined_clubs.size();
        final int number_of_undefined_clubs = clubs_for_runner.size() - number_of_defined_clubs;

        if (number_of_defined_clubs == 1 && number_of_undefined_clubs > 0)
            recordDefinedClubForRunnerName(runner_name, defined_clubs.getFirst());

        if (number_of_defined_clubs > 1)
            noteMultipleClubsForRunnerName(runner_name, defined_clubs);
    }

    private void noteMultipleClubsForRunnerName(String runner_name, List<String> defined_clubs) {

        getNotes().append(STR."Runner name \{runner_name} recorded for multiple clubs: \{String.join(", ", defined_clubs)}\n");
    }

    private List<String> getDefinedClubs(final List<String> clubs) {

        return clubs.stream().filter(this::clubIsDefined).toList();
    }

    private boolean clubIsDefined(final String club) {
        return !club.equals("?");
    }

    private List<String> getRunnerClubs(final String runner_name) {

        return races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getOverallResults().stream()).
            map(result -> (IndividualRaceResult)result).
            map(result -> result.entry.runner).
            filter(runner -> runner.name.equals(runner_name)).
            map(runner -> runner.club).
            distinct().
            sorted().
            toList();
    }

    private List<String> getRunnerNames() {

        return races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getOverallResults().stream()).
            map(result -> (IndividualRaceResult)result).
            map(result -> result.entry.runner.name).
            distinct().
            toList();
    }

    private void recordDefinedClubForRunnerName(final String runner_name, final String defined_club) {

        races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getOverallResults().stream()).
            map(result -> (IndividualRaceResult)result).
            map(result -> result.entry.runner).
            filter(runner -> runner.name.equals(runner_name)).
            forEach(runner -> runner.club = defined_club);
    }

    public int calculateRaceScore(final IndividualRace individual_race, final Runner runner) {

        if (individual_race == null) return 0;

        final String gender = runner.category.getGender();
        final AtomicInteger score = new AtomicInteger(max_race_score + 1);

        // The first finisher of each gender gets the maximum score, the next finisher one less, and so on.

        return individual_race.getOverallResults().stream().
            map(result -> (IndividualRaceResult) result).
            filter(result -> result.getCompletionStatus() == CompletionStatus.COMPLETED).
            map(result -> result.entry.runner).
            peek(result_runner -> { if (gender.equals(result_runner.category.getGender())) score.decrementAndGet(); }).
            filter(result_runner -> result_runner.equals(runner)).
            findFirst().
            map(_ -> Math.max(score.get(), 0)).
            orElse(0);                             // Runner didn't compete in this race.
    }
}
