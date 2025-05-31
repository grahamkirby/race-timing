/*
 * Copyright 2025 Graham Kirby:
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
package org.grahamkirby.race_timing.series_race.grand_prix;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceInput;
import org.grahamkirby.race_timing.single_race.SingleRace;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.function.Predicate;

public final class GrandPrixRace extends SeriesRace {

    // Configuration file keys.
    private static final String KEY_RACE_CATEGORIES_PATH = "RACE_CATEGORIES_PATH";
    private static final String KEY_RACE_TEMPORAL_ORDER = "RACE_TEMPORAL_ORDER";
    private static final String KEY_QUALIFYING_CLUBS = "QUALIFYING_CLUBS";
    private static final String KEY_SCORE_FOR_MEDIAN_POSITION = "SCORE_FOR_MEDIAN_POSITION";

    List<RaceCategory> race_categories;
    private List<Integer> race_temporal_positions;
    private List<String> qualifying_clubs;
    private int score_for_median_position;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private GrandPrixRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(final String[] args) throws IOException {

        commonMain(args, config_file_path -> new GrandPrixRace(Paths.get(config_file_path)), "GrandPrixRace");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configure() throws IOException {

        super.configure();

        race_categories = loadRaceCategories();
        race_temporal_positions = loadRaceTemporalPositions();
        configureClubs();
    }

    @Override
    protected void readProperties() {

        super.readProperties();
        qualifying_clubs = Arrays.asList(getRequiredProperty(KEY_QUALIFYING_CLUBS).split(","));
        score_for_median_position = Integer.parseInt(getRequiredProperty(KEY_SCORE_FOR_MEDIAN_POSITION));
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

        return List.of(SeriesRace::comparePossibleCompletion, SeriesRace::compareNumberOfRacesCompleted, Race::comparePerformance, Race::compareRunnerLastName, Race::compareRunnerFirstName);
    }

    @Override
    protected RaceResult getOverallResult(final Runner runner) {

        final List<Integer> scores = races.stream().
            map(individual_race -> calculateRaceScore(individual_race, runner)).
            toList();

        return new GrandPrixRaceResult(runner, scores, this);
    }

    @Override
    protected Predicate<RaceResult> getResultInclusionPredicate() {

        return result -> qualifying_clubs.contains(((Runner)((SingleRaceResult) result).entry.participant).club);
    }

    int calculateRaceScore(final SingleRace individual_race, final Runner runner) {

        if (individual_race == null) return 0;

        final Duration runner_time = individual_race.getRunnerTime(runner);

        return runner_time == null ? 0 : (int) Math.round(divide(runner_time, individual_race.getMedianTime()) * score_for_median_position);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static double divide(final Duration d1, final Duration d2) {

        return d1.toMillis() / (double) d2.toMillis();
    }

    private List<RaceCategory> loadRaceCategories() throws IOException {

        return Files.readAllLines(getPath(getRequiredProperty(KEY_RACE_CATEGORIES_PATH))).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            map(GrandPrixRace::makeRaceCategory).
            toList();
    }

    private static RaceCategory makeRaceCategory(final String line) {

        final String[] elements = line.split(",");

        final String category_name = elements[0];
        final int minimum_number = Integer.parseInt(elements[1]);

        final List<Integer> race_numbers = Arrays.stream(elements).skip(2).map(Integer::parseInt).toList();

        return new RaceCategory(category_name, minimum_number, race_numbers);
    }

    @Override
    protected int getRaceNumberInTemporalPosition(final int position) {
        return race_temporal_positions.get(position) - 1;
    }

    private List<Integer> loadRaceTemporalPositions() {

        return Arrays.stream(getRequiredProperty(KEY_RACE_TEMPORAL_ORDER).split(",")).
            map(Integer::parseInt).toList();
    }

    protected void processMultipleClubsForRunner(final String runner_name, final List<String> defined_clubs) {

        if (new HashSet<>(qualifying_clubs).containsAll(defined_clubs))
            recordDefinedClubForRunnerName(runner_name, qualifying_clubs.getFirst());
        else
            for (final String qualifying_club : qualifying_clubs) {
                if (defined_clubs.contains(qualifying_club)) {
                    noteMultipleClubsForRunnerName(runner_name, defined_clubs);
                    break;
                }
            }
    }
}
