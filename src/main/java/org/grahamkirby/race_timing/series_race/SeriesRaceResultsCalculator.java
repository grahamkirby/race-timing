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
import java.nio.file.Path;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.grahamkirby.race_timing.common.Config.*;

public class SeriesRaceResultsCalculator extends RaceResultsCalculator {

    private final SeriesRaceScorer scorer;
    private final Map<Runner, SeriesRaceResult> overall_results_by_runner;
    private final List<SingleRaceInternal> races;

    private List<SeriesRaceCategory> race_categories;
    private Permutation<SingleRaceInternal> race_temporal_permutation;
    private List<String> qualifying_clubs;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public SeriesRaceResultsCalculator(final SeriesRaceScorer scorer, final RaceInternal race) {

        super(race);
        this.scorer = scorer;
        overall_results_by_runner = new HashMap<>();
        races = ((SeriesRace) race).getRaces();
    }

    @Override
    public RaceResults calculateResults() throws IOException {

        loadRaceCategories();
        loadRaceTemporalPermutation();
        loadQualifyingClubs();
        normaliseRunnerClubs();
        checkRunnerCategoryConsistencyOverSeries();

        calculateOverallResults();
        sortOverallResults();
        allocatePrizes();

        return makeRaceResults();
    }

    @Override
    public boolean areEqualPositionsAllowed() {

        // Since overall performances are derived from multiple races, there's no way to distinguish two
        // runners with the same overall performance.
        return true;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected List<SeriesRaceCategory> getRaceCategories() {
        return race_categories;
    }

    protected Permutation<SingleRaceInternal> getRaceTemporalPermutation() {
        return race_temporal_permutation;
    }

    protected SeriesRaceScorer getScorer() {
        return scorer;
    }

    protected SeriesRaceResult getOverallResult(final Runner runner) {

        return overall_results_by_runner.get(runner);
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

                return races.stream().
                    map(individual_race -> individual_race != null ? individual_race.getConfig().getString(KEY_RACE_NAME_FOR_RESULTS) : null).
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

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static int numberOfClubs(final SeriesRace race) {

        return (int) race.getResultsCalculator().getOverallResults().stream().
            map(result -> ((Runner) result.getParticipant()).getClub()).
            distinct().
            count();
    }

    private static List<String> getDefinedClubs(final List<String> clubs) {

        return clubs.stream().
            filter(club -> !club.equals(UNKNOWN_CLUB_INDICATOR)).
            toList();
    }

    private static SeriesRaceCategory makeRaceCategory(final String line) {

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

    private void loadRaceCategories() throws IOException {

        final Path race_categories_path = race.getConfig().getPath(KEY_RACE_CATEGORIES_PATH);

        race_categories = race_categories_path != null ?
            Files.readAllLines(race_categories_path).stream().
                filter(line -> !line.startsWith(COMMENT_SYMBOL)).
                map(SeriesRaceResultsCalculator::makeRaceCategory).
                toList() :

            makeDefaultRaceCategories();
    }

    private void loadRaceTemporalPermutation() {

        final String race_temporal_order_string = race.getConfig().getString(KEY_RACE_TEMPORAL_ORDER);

        race_temporal_permutation = race_temporal_order_string != null ?
            new Permutation<>(
                Arrays.stream(race_temporal_order_string.split(",")).
                    map(Integer::parseInt).
                    toList()) :

            new Permutation<>(races.size());
    }

    private void loadQualifyingClubs() {

        final String qualifying_clubs_string = race.getConfig().getString(KEY_QUALIFYING_CLUBS);

        qualifying_clubs = qualifying_clubs_string != null ?
            List.of(qualifying_clubs_string.split(",")) :
            List.of();
    }

    private RaceResult makeOverallResult(final Runner runner) {

        final List<Performance> performances = races.stream().
            filter(Objects::nonNull).
            map(individual_race -> scorer.getIndividualRacePerformance(runner, individual_race)).
            toList();

        final SeriesRaceResult result = new SeriesRaceResult(race, runner, performances);

        // Cache the result so it can be retrieved without scanning the overall results list.
        // The result needs to be accessed frequently during sorting of results.
        overall_results_by_runner.put(runner, result);
        return result;
    }

    private List<SeriesRaceCategory> makeDefaultRaceCategories() {

        final List<Integer> race_numbers = IntStream.rangeClosed(1, races.size()).boxed().toList();
        final SeriesRaceCategory general_race_category = new SeriesRaceCategory("General", 0, race_numbers);

        return List.of(general_race_category);
    }

    private boolean eligibleForSeries(final SingleRaceResult result) {

        return qualifying_clubs.isEmpty() || qualifying_clubs.contains(((Runner) result.getParticipant()).getClub());
    }

    private void normaliseRunnerClubs() {

        getRunnerNames().forEach(this::normaliseClubsForRunnerName);
    }

    private void calculateOverallResults() {

        // List needs to be mutable to allow sorting.
        overall_results = makeMutableCopy(
            getEligibleIndividualRaceResults(race_temporal_permutation.permute(races)).
                map(result -> (Runner) result.getParticipant()).
                distinct().
                map(this::makeOverallResult).
                toList());
    }

    private List<String> getRunnerNames() {

        return getEligibleIndividualRaceResults(races).
            map(CommonRaceResult::getParticipantName).
            distinct().
            toList();
    }

    private List<String> getRunnerClubs(final String runner_name) {

        return getParticipantsWithName(runner_name).
            map(participant -> ((Runner) participant).getClub()).
            distinct().
            sorted().
            toList();
    }

    private void recordDefinedClubForRunnerName(final String runner_name, final String defined_club) {

        getParticipantsWithName(runner_name).
            map(participant -> (Runner) participant).
            forEachOrdered(runner -> runner.setClub(defined_club));
    }

    private List<List<SingleRaceResult>> getResultsByQualifyingRunner() {

        final Map<Runner, List<SingleRaceResult>> map = new HashMap<>();

        getEligibleIndividualRaceResults(race_temporal_permutation.permute(races)).
            forEachOrdered(result -> recordResult(result, map));

        return new ArrayList<>(map.values());
    }

    private void recordResult(final SingleRaceResult result, final Map<Runner, List<SingleRaceResult>> map) {

        final Runner runner = (Runner) result.getParticipant();
        map.putIfAbsent(runner, new ArrayList<>());
        map.get(runner).add(result);
    }

    private Stream<RaceResult> getAllIndividualRaceResults(final List<SingleRaceInternal> individual_races) {

        return individual_races.stream().
            filter(Objects::nonNull).
            flatMap(individual_race -> individual_race.getResultsCalculator().getOverallResults().stream());
    }

    private Stream<SingleRaceResult> getEligibleIndividualRaceResults(final List<SingleRaceInternal> individual_races) {

        return getAllIndividualRaceResults(individual_races).
            map(result -> ((SingleRaceResult) result)).
            filter(this::eligibleForSeries);
    }

    private Stream<Participant> getParticipantsWithName(final String runner_name) {

        return getAllIndividualRaceResults(races).
            map(result -> (SingleRaceResult) result).
            map(CommonRaceResult::getParticipant).
            filter(participant -> participant.getName().equals(runner_name));
    }

    private void normaliseClubsForRunnerName(final String runner_name) {

        // Where a runner name is associated with a single entry with a defined club
        // plus some other entries with no club defined, add the club to those entries.

        // Where a runner name is associated with multiple clubs, leave as is, under
        // assumption that they are separate runner_names.
        final List<String> clubs_for_runner_name = getRunnerClubs(runner_name);
        final List<String> defined_clubs = getDefinedClubs(clubs_for_runner_name);

        final int number_of_defined_clubs = defined_clubs.size();
        final int number_of_undefined_clubs = clubs_for_runner_name.size() - number_of_defined_clubs;

        if (number_of_defined_clubs == 1 && number_of_undefined_clubs > 0)
            recordDefinedClubForRunnerName(runner_name, defined_clubs.getFirst());

        if (number_of_defined_clubs > 1)
            processMultipleClubsForRunnerName(runner_name, defined_clubs);
    }

    @SuppressWarnings("SlowListContainsAll")
    private void processMultipleClubsForRunnerName(final String runner_name, final List<String> defined_clubs) {

        // TODO clarify distinction between recorded and defined club.
        if (qualifying_clubs.containsAll(defined_clubs))
            recordDefinedClubForRunnerName(runner_name, qualifying_clubs.getFirst());
        else
            if (qualifying_clubs.isEmpty() || qualifying_clubs.stream().anyMatch(defined_clubs::contains))
                noteMultipleClubsForRunnerName(runner_name, defined_clubs);
    }

    private void noteMultipleClubsForRunnerName(final String runner_name, final List<String> defined_clubs) {

        race.getNotes().appendToNotes("Runner " + runner_name + " recorded for multiple clubs: " + String.join(", ", defined_clubs) + LINE_SEPARATOR);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void checkRunnerCategoryConsistencyOverSeries() {

        getResultsByQualifyingRunner().forEach(this::checkRunnerCategoryConsistencyOverSeries);
    }

    private void checkRunnerCategoryConsistencyOverSeries(final List<SingleRaceResult> runner_results) {

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
