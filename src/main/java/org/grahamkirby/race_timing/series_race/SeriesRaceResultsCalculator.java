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

import org.grahamkirby.race_timing.categories.EntryCategory;
import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static org.grahamkirby.race_timing.common.Config.*;

public class SeriesRaceResultsCalculator extends RaceResultsCalculator {

    public static final String UNDEFINED_CLUB = "?";
    private List<SeriesRaceCategory> race_categories;
    private List<Integer> race_temporal_order;
    private List<String> qualifying_clubs;
    private final SeriesRaceScorer scorer;
    private final Map<Runner, SeriesRaceResult> runner_result_map = new HashMap<>();

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public SeriesRaceResultsCalculator(final SeriesRaceScorer scorer, final RaceInternal race) throws IOException {

        super(race);
        this.scorer = scorer;

        loadRaceTemporalPositions();
        loadRaceCategories();
        configureClubs();
        checkCategoryConsistencyOverSeries();
    }

    @Override
    public RaceResults calculateResults() throws IOException {

        initialiseResults();
        sortResults();
        allocatePrizes();

        return makeRaceResults();
    }

    @Override
    public boolean areEqualPositionsAllowed() {

        return true;
    }

    protected List<SeriesRaceCategory> getRaceCategories() {
        return race_categories;
    }

    protected List<Integer> getRaceTemporalOrder() {
        return race_temporal_order;
    }

    protected SeriesRaceScorer getScorer() {
        return scorer;
    }

    protected SeriesRaceResult getOverallResult(final Runner runner) {

        return runner_result_map.get(runner);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private RaceResults makeRaceResults() {

        final int number_of_races_taken_place = ((SeriesRace) race).getNumberOfRacesTakenPlace();
        final int minimum_number_of_races = (int) race.getConfig().get(KEY_MINIMUM_NUMBER_OF_RACES);

        final boolean multiple_clubs = numberOfClubs((SeriesRace) race) > 1;
        final boolean multiple_race_categories = race_categories.size() > 1;
        final boolean possible_to_have_completed = number_of_races_taken_place >= minimum_number_of_races;

        return new SeriesRaceResults() {

            @Override
            public boolean multipleClubs() {
                return multiple_clubs;
            }

            @Override
            public boolean multipleRaceCategories() {
                return multiple_race_categories;
            }

            @Override
            public boolean possibleToHaveCompleted() {
                return possible_to_have_completed;
            }

            @Override
            public List<String> getRaceNames() {

                return ((SeriesRace) race).getRaces().stream().
                    map(individual_race -> individual_race == null ? null : individual_race.getConfig().getString(KEY_RACE_NAME_FOR_RESULTS)).
                    toList();
            }

            @Override
            public int getNumberOfRacesTakenPlace() {
                return number_of_races_taken_place;
            }

            @Override
            public List<SeriesRaceCategory> getRaceCategories() {
                return SeriesRaceResultsCalculator.this.race_categories;
            }

            @Override
            public Normalisation getNormalisation() {
                return race.getNormalisation();
            }

            @Override
            public Config getConfig() {
                return race.getConfig();
            }

            @Override
            public Notes getNotes() {
                return race.getNotes();
            }

            @Override
            public List<PrizeCategoryGroup> getPrizeCategoryGroups() {
                return race.getCategoriesProcessor().getPrizeCategoryGroups();
            }

            @Override
            public List<? extends RaceResult> getPrizeWinners(final PrizeCategory category) {
                return SeriesRaceResultsCalculator.this.getPrizeWinners(category);
            }

            @Override
            public List<String> getTeamPrizes() {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<? extends RaceResult> getOverallResults() {
                return race.getResultsCalculator().getOverallResults();
            }

            @Override
            public List<? extends RaceResult> getOverallResults(final List<PrizeCategory> categories) {
                return race.getResultsCalculator().getOverallResults(categories);
            }

            @Override
            public boolean arePrizesInThisOrLaterCategory(final PrizeCategory prizeCategory) {
                return race.getResultsCalculator().arePrizesInThisOrLaterCategory(prizeCategory);
            }
        };
    }

    private static int numberOfClubs(final SeriesRace race) {

        return (int) race.getResultsCalculator().getOverallResults().stream().
            map(result -> ((Runner) result.getParticipant()).getClub()).
            distinct().
            count();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static List<String> getDefinedClubs(final Collection<String> clubs) {

        return clubs.stream().
            filter(club -> !club.equals(UNDEFINED_CLUB)).
            toList();
    }

    private SeriesRaceCategory makeRaceCategory(final String line) {

        final String[] elements = line.split(",");

        final String category_name = elements[0];
        final int minimum_number = Integer.parseInt(elements[1]);

        final List<Integer> race_numbers = Arrays.stream(elements).
            skip(2).
            map(Integer::parseInt).
            toList();

        // Race numbers are in display order, which may be different from temporal order.
        return new SeriesRaceCategory(category_name, minimum_number, race_numbers);
    }

    private static void checkForChangeToYoungerAgeCategory(final SingleRaceResult result, final EntryCategory previous_category, final EntryCategory current_category, final String race_name) {

        if (previous_category != null && current_category != null && current_category.getMinimumAge() < previous_category.getMinimumAge())
            throw new RuntimeException("invalid category change: runner '" + result.getParticipantName() + "' changed from " + previous_category.getShortName() + " to " + current_category.getShortName() + " at " + race_name);
    }

    private static void checkForChangeToTooMuchOlderAgeCategory(final SingleRaceResult result, final EntryCategory earliest_category, final EntryCategory last_category) {

        if (earliest_category != null && last_category != null && last_category.getMinimumAge() > earliest_category.getMaximumAge() + 1)
            throw new RuntimeException("invalid category change: runner '" + result.getParticipantName() + "' changed from " + earliest_category.getShortName() + " to " + last_category.getShortName() + " during series");
    }

    private static void checkForChangeToDifferentGenderCategory(final SingleRaceResult result, final EntryCategory previous_category, final EntryCategory current_category, final String race_name) {

        if (previous_category != null && current_category != null && !current_category.getGender().equals(previous_category.getGender()))
            throw new RuntimeException("invalid category change: runner '" + result.getParticipantName() + "' changed from " + previous_category.getShortName() + " to " + current_category.getShortName() + " at " + race_name);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void initialiseResults() {

        overall_results = new ArrayList<>(
            getRacesInTemporalOrder().stream().
                filter(Objects::nonNull).
                flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
                filter(getResultInclusionPredicate()).
                map(result -> (Runner) result.getParticipant()).
                distinct().
                map(this::makeOverallResult).
                toList());
    }

    private RaceResult makeOverallResult(final Runner runner) {

        final List<Performance> scores = ((SeriesRace) race).getRaces().stream().
            filter(Objects::nonNull).
            map(individual_race -> scorer.getIndividualRacePerformance(runner, individual_race)).
            toList();

        final SeriesRaceResult result = new SeriesRaceResult(race, runner, scores);
        runner_result_map.put(runner, result);
        return result;
    }

    private void loadRaceCategories() throws IOException {

        race_categories = race.getConfig().containsKey(KEY_RACE_CATEGORIES_PATH) ?
            Files.readAllLines(race.getConfig().getPath(KEY_RACE_CATEGORIES_PATH)).stream().
                filter(line -> !line.startsWith(COMMENT_SYMBOL)).
                map(this::makeRaceCategory).
                toList() :

            makeDefaultRaceCategories();
    }

    private List<SeriesRaceCategory> makeDefaultRaceCategories() {

        final List<Integer> race_numbers = IntStream.rangeClosed(1, ((SeriesRace)race).getRaces().size()).boxed().toList();
        final SeriesRaceCategory general_race_category = new SeriesRaceCategory("General", 0, race_numbers);
        return List.of(general_race_category);
    }

    private void loadRaceTemporalPositions() {

        race_temporal_order = race.getConfig().containsKey(KEY_RACE_TEMPORAL_ORDER) ?
            Arrays.stream(race.getConfig().getString(KEY_RACE_TEMPORAL_ORDER).split(",")).
                map(Integer::parseInt).toList() :

            makeDefaultRaceTemporalPositions();
    }

    private List<Integer> makeDefaultRaceTemporalPositions() {

        return IntStream.rangeClosed(1, ((SeriesRace)race).getRaces().size()).boxed().toList();
    }

    private Predicate<RaceResult> getResultInclusionPredicate() {

        return result -> qualifying_clubs.isEmpty() || qualifying_clubs.contains(((Runner) result.getParticipant()).getClub());
    }

    private void configureClubs() {

        qualifying_clubs = race.getConfig().containsKey(KEY_QUALIFYING_CLUBS) ?
            Arrays.asList(race.getConfig().getString(KEY_QUALIFYING_CLUBS).split(",")) :
            new ArrayList<>();

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

    private void noteMultipleClubsForRunnerName(final String runner_name, final List<String> defined_clubs) {

        race.getNotes().appendToNotes("Runner " + runner_name + " recorded for multiple clubs: " + String.join(", ", defined_clubs) + LINE_SEPARATOR);
    }

    private List<String> getRunnerClubs(final String runner_name) {

        return ((SeriesRace)race).getRaces().stream().
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

    private void recordDefinedClubForRunnerName(final String runner_name, final String defined_club) {

        ((SeriesRace)race).getRaces().stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            map(CommonRaceResult::getParticipant).
            filter(participant -> participant.getName().equals(runner_name)).
            forEachOrdered(participant -> ((Runner)participant).setClub(defined_club));
    }

    private List<String> getRunnerNames() {

        return ((SeriesRace)race).getRaces().stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            map(CommonRaceResult::getParticipantName).
            distinct().
            toList();
    }

    private void processMultipleClubsForRunner(final String runner_name, final List<String> defined_clubs) {

        if (qualifying_clubs.isEmpty())
            noteMultipleClubsForRunnerName(runner_name, defined_clubs);
        else
        if (new HashSet<>(qualifying_clubs).containsAll(defined_clubs))
            recordDefinedClubForRunnerName(runner_name, qualifying_clubs.getFirst());
        else
            for (final String qualifying_club : qualifying_clubs)
                if (defined_clubs.contains(qualifying_club)) {
                    noteMultipleClubsForRunnerName(runner_name, defined_clubs);
                    break;
                }
    }

    private List<List<SingleRaceResult>> getResultsByRunner() {

        final Map<Runner, List<SingleRaceResult>> map = new HashMap<>();

        getRacesInTemporalOrder().stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            filter(getResultInclusionPredicate()).
            map(result -> ((SingleRaceResult) result)).
            forEachOrdered(result -> {
                final Runner runner = (Runner) result.getParticipant();
                map.putIfAbsent(runner, new ArrayList<>());
                map.get(runner).add(result);
            });

        return new ArrayList<>(map.values());
    }

    private List<SingleRaceInternal> getRacesInTemporalOrder() {

        final List<SingleRaceInternal> races = ((SeriesRace) race).getRaces();
        final List<SingleRaceInternal> races_in_order = new ArrayList<>();

        // Example: RACE_TEMPORAL_ORDER = 1,5,2,9,3,10,4,11,12,6,7,8
        // First race in listing occurs first; fifth race in listing occurs second etc.

        // TODO write as permutation.
        for (int i = 0; i < races.size(); i++)
            races_in_order.add(races.get(race_temporal_order.get(i) - 1));

        return races_in_order;
    }

    private void checkCategoryConsistencyOverSeries() {

        getResultsByRunner().forEach(this::checkCategoryConsistencyOverSeries);
    }

    private void checkCategoryConsistencyOverSeries(final List<SingleRaceResult> runner_results) {

        EntryCategory earliest_category = null;
        EntryCategory previous_category = null;
        EntryCategory latest_category = null;

        for (final SingleRaceResult result : runner_results) {

            final EntryCategory current_category = result.getParticipant().getCategory();

            // Category may not be known for externally organised race.
            if (current_category != null) {

                if (earliest_category == null)
                    earliest_category = current_category;

                latest_category = current_category;

                if (previous_category != null && !previous_category.equals(current_category)) {

                    final String race_name = (String) result.getRace().getConfig().get(KEY_RACE_NAME_FOR_RESULTS);

                    checkForChangeToYoungerAgeCategory(result, previous_category, current_category, race_name);
                    checkForChangeToDifferentGenderCategory(result, previous_category, current_category, race_name);

                    race.getNotes().appendToNotes("Runner " + result.getParticipantName() + " changed category from " + previous_category.getShortName() + " to " + current_category.getShortName() + " at " + race_name + LINE_SEPARATOR);
                }

                previous_category = current_category;
            }
        }

        // It's enough to check just the earliest and latest categories, since if the category changes to too much older part-way through
        // the series, and then back again, this will be caught by the check for change to a younger category.
        checkForChangeToTooMuchOlderAgeCategory(runner_results.getFirst(), earliest_category, latest_category);

        for (final SingleRaceResult result : runner_results)
            result.getParticipant().setCategory(earliest_category);
    }
}
