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

import org.grahamkirby.race_timing.categories.CategoriesProcessor;
import org.grahamkirby.race_timing.categories.CategoryDetails;
import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.IndividualRaceFactory;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.grahamkirby.race_timing.common.Config.*;

public class GrandPrixRace implements SpecificRace, SeriesRace, Race2 {

    private List<Race2> races;
    private List<String> race_config_paths;
    private CategoryDetails category_details;
    private RaceResultsCalculator results_calculator;
    private ResultsOutput results_output;
    private final Config config;
    private CategoriesProcessor categories_processor;
    private Normalisation normalisation;

    List<GrandPrixRaceCategory> race_categories;
    List<Integer> race_temporal_positions;
    List<String> qualifying_clubs;
    int score_for_median_position;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public GrandPrixRace(final Config config) {

        this.config = config;
    }

    public void setRace(final Race2 race) {

    }

    @Override
    public void processResults() {
        category_details = categories_processor.getCategoryDetails();
        completeConfiguration2();
        results_calculator.calculateResults();
    }

    @Override
    public void outputResults() throws IOException {
        results_output.outputResults();

    }

    @Override
    public void completeConfiguration() {

    }

    public void setCategoriesProcessor(final CategoriesProcessor categories_processor) {

        this.categories_processor = categories_processor;
        categories_processor.setRace(this);
    }

    public void setResultsCalculator(final RaceResultsCalculator results_calculator) {

        this.results_calculator = results_calculator;
        results_calculator.setRace(this);
    }

    public void setResultsOutput(final ResultsOutput results_output) {

        this.results_output = results_output;
        results_output.setRace(this);
    }

    public void completeConfiguration2() {

        try {
            race_config_paths = Arrays.asList(config.getStringConfig(KEY_RACES).split(",", -1));

            race_categories = loadRaceCategories();
            race_temporal_positions = loadRaceTemporalPositions();

            qualifying_clubs = Arrays.asList((config.getStringConfig(KEY_QUALIFYING_CLUBS)).split(","));
            score_for_median_position = ((int) config.get(KEY_SCORE_FOR_MEDIAN_POSITION));

            races = loadRaces();
            configureClubs();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public RaceResultsCalculator getResultsCalculator() {
        return results_calculator;
    }

    @Override
    public CategoryDetails getCategoryDetails() {
        return category_details;
    }

    @Override
    public synchronized Normalisation getNormalisation() {

        if (normalisation == null)
            normalisation = new Normalisation(this);

        return normalisation;
    }

    @Override
    public void appendToNotes(String s) {
        results_calculator.getNotes().append(s);
    }

    @Override
    public String getNotes() {
        return results_calculator.getNotes().toString();
    }

    @Override
    public Object getSpecific() {
        return this;
    }

    @Override
    public List<RawResult> getRawResults() {
        return List.of();
    }

    @Override
    public List<RaceEntry> getEntries() {
        return List.of();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private List<GrandPrixRaceCategory> loadRaceCategories() throws IOException {

        Path pathConfig = config.getPathConfig(KEY_RACE_CATEGORIES_PATH);
        return Files.readAllLines(pathConfig).stream().
            filter(line -> !line.startsWith(COMMENT_SYMBOL)).
            map(GrandPrixRace::makeRaceCategory).
            toList();
    }

    private static GrandPrixRaceCategory makeRaceCategory(final String line) {

        final String[] elements = line.split(",");

        final String category_name = elements[0];
        final int minimum_number = Integer.parseInt(elements[1]);

        final List<Integer> race_numbers = Arrays.stream(elements).skip(2).map(Integer::parseInt).toList();

        return new GrandPrixRaceCategory(category_name, minimum_number, race_numbers);
    }

    private List<Integer> loadRaceTemporalPositions() {

        return Arrays.stream(config.getStringConfig(KEY_RACE_TEMPORAL_ORDER).split(",")).
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

    public List<Race2> getRaces() {
        return races;
    }

    private void configureClubs() {
        getRunnerNames().forEach(this::normaliseClubsForRunner);
    }

    private void normaliseClubsForRunner(final String runner_name) {

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
            processMultipleClubsForRunner(runner_name, defined_clubs);
    }

    protected void noteMultipleClubsForRunnerName(final String runner_name, final List<String> defined_clubs) {

        results_calculator.getNotes().append("Runner " + runner_name + " recorded for multiple clubs: " + String.join(", ", defined_clubs) + LINE_SEPARATOR);
    }

    private static List<String> getDefinedClubs(final Collection<String> clubs) {

        return clubs.stream().filter(GrandPrixRace::isClubDefined).toList();
    }

    private static boolean isClubDefined(final String club) {
        return !club.equals("?");
    }

    private List<String> getRunnerClubs(final String runner_name) {

        return races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            map(CommonRaceResult::getParticipant).
            filter(participant -> participant.getName().equals(runner_name)).
            map(participant -> ((Runner) participant).getClub()).
            distinct().
            sorted().
            toList();
    }

    protected void recordDefinedClubForRunnerName(final String runner_name, final String defined_club) {

        races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            map(CommonRaceResult::getParticipant).
            filter(participant -> participant.getName().equals(runner_name)).
            forEachOrdered(participant -> ((Runner)participant).setClub(defined_club));
    }

    private List<String> getRunnerNames() {

        return races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            map(CommonRaceResult::getParticipantName).
            distinct().
            toList();
    }

    private List<Race2> loadRaces() throws IOException {

        final int number_of_race_in_series = (int) config.get(KEY_NUMBER_OF_RACES_IN_SERIES);
        if (number_of_race_in_series != race_config_paths.size())
            throw new RuntimeException("invalid number of races specified in file '" + config.getConfigPath().getFileName() + "'");

        final List<Race2> races = new ArrayList<>();
        final List<String> config_paths_seen = new ArrayList<>();

        for (int i = 0; i < number_of_race_in_series; i++) {

            final String race_config_path = race_config_paths.get(i);

            if (race_config_path.isBlank())
                races.add(null);
            else {
                if (config_paths_seen.contains(race_config_path))
                    throw new RuntimeException("duplicate races specified in file '" + config.getConfigPath().getFileName() + "'");
                config_paths_seen.add(race_config_path);
                races.add(getIndividualRace(race_config_path, i + 1));
            }
        }

        return races;
    }

    private Race2 getIndividualRace(final String race_config_path, final int race_number) throws IOException {

        final Path config_path = config.interpretPath(Path.of(race_config_path));

        if (!Files.exists(config_path))
            throw new RuntimeException("invalid config for race " + race_number + " in file '" + config.getConfigPath().getFileName() + "'");

        final Race2 individual_race = new IndividualRaceFactory().makeRace(config_path);
        individual_race.processResults();

        return individual_race;
    }

    public int getNumberOfRacesTakenPlace() {

        return (int) races.stream().filter(Objects::nonNull).count();
    }

    public List<GrandPrixRaceCategory> getRaceCategories() {
        return race_categories;
    }
}
