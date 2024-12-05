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
package org.grahamkirby.race_timing.series_race.fife_ac_grand_prix;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

public class GrandPrixRace extends SeriesRace {

    // TODO check duplication with MidweekRace.
    public static final int SCORE_FOR_MEDIAN_POSITION = 1000;

    public List<RaceCategory> race_categories;
    private List<String> qualifying_clubs;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public GrandPrixRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(final String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java GrandPrixRace <config file path>");
        else {
            new GrandPrixRace(Paths.get(args[0])).processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configure() throws IOException {

        super.configure();

        configureRaceCategories();
        configureClubs();
    }

    private void configureClubs() {
        getRunnerNames().forEach(this::checkClubsForRunner);
    }

    private void configureRaceCategories() throws IOException {

        final String race_categories_path = getProperty(KEY_RACE_CATEGORIES_PATH);

        race_categories = new ArrayList<>();

        Files.readAllLines(getPath(race_categories_path)).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            forEachOrdered(line -> {
                final String[] elements = line.split(",");
                String category_name = elements[0];
                int minimum_number = Integer.parseInt(elements[1]);
                List<Integer> race_numbers = new ArrayList<>();
                for (int i = 2; i < elements.length; i++) {
                    race_numbers.add(Integer.parseInt(elements[i]));
                }
                RaceCategory category = new RaceCategory(category_name, minimum_number, race_numbers);
                race_categories.add(category);
            });
    }

    @Override
    protected void readProperties() {

        minimum_number_of_races = Integer.parseInt(getProperty(KEY_MINIMUM_NUMBER_OF_RACES));
        qualifying_clubs = Arrays.asList(getProperty(KEY_QUALIFYING_CLUBS).split(","));
    }

    protected void printOverallResults() throws IOException {

        output_CSV.printResults();
//        output_HTML.printResults();
    }

    protected void printPrizes() throws IOException {

//        output_PDF.printPrizes();
//        output_HTML.printPrizes();
//        output_text.printPrizes();
    }

    protected void printNotes() throws IOException {

//        output_text.printNotes();
    }

    protected void printCombined() throws IOException {

//        output_HTML.printCombined();
    }

    @Override
    protected void configureInputData() throws IOException {

        super.configureInputData();
    }

    @Override
    protected RaceInput getInput() {
        return new SeriesRaceInput(this);
    }

    @Override
    protected RaceOutputCSV getOutputCSV() {
        return new GrandPrixRaceOutputCSV(this);
    }

    @Override
    protected RaceOutputHTML getOutputHTML() {
//        return new GrandPrixRaceOutputHTML(this);
        return null;
    }

    @Override
    protected RaceOutputText getOutputText() {
//        return new GrandPrixRaceOutputText(this);
        return null;
    }

    @Override
    protected RaceOutputPDF getOutputPDF() {
//        return new GrandPrixRaceOutputPDF(this);
        return null;
    }

    @Override
    protected List<Comparator<RaceResult>> getComparators() {

        return List.of(this::compareCompletion, this::comparePerformance, this::compareRunnerLastName, this::compareRunnerFirstName);
    }

    private int compareNumberOfRacesCompleted(RaceResult r1, RaceResult r2) {

        return -Integer.compare(((GrandPrixRaceResult) r1).numberOfRacesCompleted(), ((GrandPrixRaceResult) r2).numberOfRacesCompleted());
    }

    @Override
    protected List<Comparator<RaceResult>> getDNFComparators() {
        return List.of(this::compareNumberOfRacesCompleted);
    }

    @Override
    protected boolean entryCategoryIsEligibleForPrizeCategoryByGender(final EntryCategory entry_category, final PrizeCategory prize_category) {

        return entry_category != null && entry_category.getGender().equals(prize_category.getGender());
    }

    @Override
    protected EntryCategory getEntryCategory(final RaceResult result) {

        return ((GrandPrixRaceResult) result).runner.category;
    }

    @Override
    protected RaceResult getOverallResult(final Runner runner) {

        final List<Double> scores = races.stream().
            map(race -> calculateRaceScore(race, runner)).
            toList();

        return new GrandPrixRaceResult(runner, scores, this);
    }

    @Override
    protected Predicate<RaceResult> getResultInclusionPredicate() {

        return (result -> qualifying_clubs.contains(((IndividualRaceResult) result).entry.runner.club));
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

        if (number_of_defined_clubs > 1) {
            if (new HashSet<>(qualifying_clubs).containsAll(defined_clubs))
                recordDefinedClubForRunnerName(runner_name, qualifying_clubs.getFirst());
            else
                noteMultipleClubsForRunnerName(runner_name, defined_clubs);
        }
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
            forEachOrdered(runner -> runner.club = defined_club);
    }

    public double calculateRaceScore(final IndividualRace individual_race, final Runner runner) {

        final Duration median_time = individual_race.getMedianTime();
        final Duration runner_time = getResult(individual_race, runner);

        if (runner_time == null) return 0.0;

        return (double)runner_time.toMillis() / (double)median_time.toMillis() * SCORE_FOR_MEDIAN_POSITION;
    }

    private Duration getResult(IndividualRace individualRace, Runner runner) {

        return individualRace.getOverallResults().stream().
            map(result -> ((IndividualRaceResult)result)).
            filter(result -> runner.equals(result.entry.runner)).
            map(IndividualRaceResult::duration).
            findFirst().
            orElse(null);
    }

    public boolean hasCompletedCategory(GrandPrixRaceResult result, RaceCategory category) {

        for (int race_number : category.race_numbers())
            if (race_number <= result.scores.size() && result.scores.get(race_number - 1) > 0.0) return true;

        return false;
    }
}
