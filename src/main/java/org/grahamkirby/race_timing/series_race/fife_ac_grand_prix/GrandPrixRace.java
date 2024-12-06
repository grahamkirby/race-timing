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

    @Override
    protected void readProperties() {

        super.readProperties();
        qualifying_clubs = Arrays.asList(getProperty(KEY_QUALIFYING_CLUBS).split(","));
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
        return new GrandPrixRaceOutputHTML(this);
    }

    @Override
    protected RaceOutputText getOutputText() {
        return new GrandPrixRaceOutputText(this);
    }

    @Override
    protected RaceOutputPDF getOutputPDF() {
        return new GrandPrixRaceOutputPDF(this);
    }

    @Override
    protected List<Comparator<RaceResult>> getComparators() {

        return List.of(this::compareCompletion, this::comparePerformance, this::compareRunnerLastName, this::compareRunnerFirstName);
    }

    @Override
    protected List<Comparator<RaceResult>> getDNFComparators() {
        return List.of(this::compareNumberOfRacesCompleted);
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

    public double calculateRaceScore(final IndividualRace individual_race, final Runner runner) {

        final Duration runner_time = getResult(individual_race, runner);

        return runner_time != null ? divide(runner_time, individual_race.getMedianTime()) * SCORE_FOR_MEDIAN_POSITION : 0.0;
    }

    public boolean hasCompletedCategory(GrandPrixRaceResult result, RaceCategory category) {

        return category.race_numbers().stream().
            anyMatch(race_number -> race_number <= result.scores.size() && result.scores.get(race_number - 1) > 0.0);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private double divide(final Duration d1, final Duration d2) {

        return (double)d1.toMillis() / (double)d2.toMillis();
    }

    private int compareNumberOfRacesCompleted(RaceResult r1, RaceResult r2) {

        return -Integer.compare(((GrandPrixRaceResult) r1).numberOfRacesCompleted(), ((GrandPrixRaceResult) r2).numberOfRacesCompleted());
    }

    private void configureRaceCategories() throws IOException {

        final String race_categories_path = getProperty(KEY_RACE_CATEGORIES_PATH);

        race_categories = new ArrayList<>();

        Files.readAllLines(getPath(race_categories_path)).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            forEachOrdered(line -> {
                final String[] elements = line.split(",");

                final String category_name = elements[0];
                final int minimum_number = Integer.parseInt(elements[1]);

                final List<Integer> race_numbers = Arrays.stream(elements).skip(2).map(Integer::parseInt).toList();

                race_categories.add(new RaceCategory(category_name, minimum_number, race_numbers));
            });
    }

    protected void processMultipleClubsForRunner(final String runner_name, final List<String> clubs) {

        if (new HashSet<>(qualifying_clubs).containsAll(clubs))
            recordDefinedClubForRunnerName(runner_name, qualifying_clubs.getFirst());
        else
            noteMultipleClubsForRunnerName(runner_name, clubs);
    }

    private Duration getResult(final IndividualRace individual_race, final Runner runner) {

        return individual_race.getOverallResults().stream().
            map(result -> ((IndividualRaceResult)result)).
            filter(result -> runner.equals(result.entry.runner)).
            map(IndividualRaceResult::duration).
            findFirst().
            orElse(null);
    }
}
