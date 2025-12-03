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
import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResult;
import org.grahamkirby.race_timing.individual_race.Runner;

import java.io.IOException;
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
    private List<String> eligible_clubs;

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
        loadEligibleClubs();
        processClubsForRunnerNames();
        ensureRunnerCategoryConsistencyOverSeries();

        calculateOverallResults();
        sortOverallResults();
        allocatePrizes();

        return makeRaceResults();
    }

    @Override
    public boolean areEqualPositionsAllowed() {

        // Dead heats are allowed in overall results, since each overall score is composed of multiple
        // individual scores.

        // Depending on the nature of the particular scorer used, each individual score may be:
        //
        // * an individually recorded race duration
        // * an abstract score derived from an individually recorded race duration
        // * an abstract score derived from the runner's position in an individual race
        //
        // In the first and second cases, a precise overall score does exist for every result, but
        // we can't determine what it is, since individual durations are rounded to the nearest second
        // at recording. Therefore, there's no way to distinguish two equal overall results.
        //
        // In the third case, there is intrinsically no way to distinguish two overall results with
        // the same total score.

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

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadRaceCategories() throws IOException {

        final Path race_categories_path = race.getConfig().getPath(KEY_RACE_CATEGORIES_PATH);

        race_categories = race_categories_path != null ?
            readAllLines(race_categories_path).stream().
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

    private void loadEligibleClubs() {

        final String eligible_clubs_string = race.getConfig().getString(KEY_ELIGIBLE_CLUBS);

        eligible_clubs = eligible_clubs_string != null ?
            List.of(eligible_clubs_string.split(",")) :
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

        return eligible_clubs.isEmpty() || eligible_clubs.contains(((Runner) result.getParticipant()).getClub());
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

    private List<List<IndividualRaceResult>> getResultsByEligibleRunner() {

        final Map<Runner, List<IndividualRaceResult>> map = new HashMap<>();

        getEligibleIndividualRaceResults(race_temporal_permutation.permute(races)).
            forEachOrdered(result -> recordResult(result, map));

        return new ArrayList<>(map.values());
    }

    private void recordResult(final IndividualRaceResult result, final Map<Runner, List<IndividualRaceResult>> map) {

        final Runner runner = (Runner) result.getParticipant();

        map.putIfAbsent(runner, new ArrayList<>());
        map.get(runner).add(result);
    }

    private Stream<RaceResult> getAllIndividualRaceResults(final List<SingleRaceInternal> individual_races) {

        return individual_races.stream().
            filter(Objects::nonNull).
            flatMap(individual_race -> individual_race.getResultsCalculator().getOverallResults().stream());
    }

    private Stream<IndividualRaceResult> getEligibleIndividualRaceResults(final List<SingleRaceInternal> individual_races) {

        return getAllIndividualRaceResults(individual_races).
            map(result -> ((IndividualRaceResult) result)).
            filter(this::eligibleForSeries);
    }

    private Stream<Participant> getParticipantsWithName(final String runner_name) {

        return getAllIndividualRaceResults(races).
            map(result -> (SingleRaceResult) result).
            map(CommonRaceResult::getParticipant).
            filter(participant -> participant.getName().equals(runner_name));
    }

    private void processClubsForRunnerNames() {

        getRunnerNames().forEach(this::processClubsForRunnerName);
    }

    private void processClubsForRunnerName(final String runner_name) {

        // Where a runner name is associated with a single entry with a known club plus some other entries with unknown
        // club, add the club to those entries.

        // Where a runner name is associated with multiple clubs, leave as is, under assumption that there are multiple
        // distinct runners with the same name.

        // An unknown club is different from no club being recorded in the original entries for an individual race. The
        // former represents the case where a runner does have a club but it's unknown, arising only when externally
        // produced overall results that don't specify clubs are imported directly. The latter represents the case of an
        // unattached runner, where a blank entry for club in the entries is transformed to 'Unatt.' via the process for
        // normalising club name variants.

        final List<String> clubs_for_runner_name = getClubsForRunnerName(runner_name);
        final List<String> known_clubs_for_runner_name = getKnownClubsForRunnerName(runner_name);

        final int number_of_known_clubs = known_clubs_for_runner_name.size();
        final int number_of_unknown_clubs = clubs_for_runner_name.size() - number_of_known_clubs;

        final boolean one_known_club = number_of_known_clubs == 1;
        final boolean multiple_known_clubs = number_of_known_clubs > 1;
        final boolean some_unknown_clubs = number_of_unknown_clubs > 0;

        if (one_known_club && some_unknown_clubs)
            updateUnknownClubResultsForRunnerName(runner_name, known_clubs_for_runner_name.getFirst());

        if (multiple_known_clubs)
            noteRunnerNameRepresentsMultipleRunners(runner_name, known_clubs_for_runner_name);
    }

    private List<String> getClubs() {

        return overall_results.stream().
            map(result -> ((Runner) result.getParticipant()).getClub()).
            distinct().
            toList();
    }

    private List<String> getClubsForRunnerName(final String runner_name) {

        return getParticipantsWithName(runner_name).
            map(participant -> ((Runner) participant).getClub()).
            distinct().
            sorted().
            toList();
    }

    private List<String> getKnownClubsForRunnerName(final String runner_name) {

        return getClubsForRunnerName(runner_name).stream().
            filter(club -> !club.equals(UNKNOWN_CLUB_INDICATOR)).
            toList();
    }

    private void updateUnknownClubResultsForRunnerName(final String runner_name, final String defined_club) {

        getParticipantsWithName(runner_name).
            map(participant -> (Runner) participant).
            forEachOrdered(runner -> runner.setClub(defined_club));

        race.getNotes().appendToNotes("Club " + defined_club + " substituted for unknown clubs for runner name " + runner_name + LINE_SEPARATOR);
    }

    private void noteRunnerNameRepresentsMultipleRunners(final String runner_name, final List<String> known_clubs) {

        race.getNotes().appendToNotes("Runner name " + runner_name + " recorded for multiple clubs: " + String.join(", ", known_clubs) + "; assuming there are multiple runners with this name" + LINE_SEPARATOR);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void ensureRunnerCategoryConsistencyOverSeries() {

        getResultsByEligibleRunner().forEach(results_for_runner -> {

            checkRunnerCategoryConsistencyOverSeries(results_for_runner);
            setRunnerCategoryToEarliestCategory(results_for_runner);
        });
    }

    private void checkRunnerCategoryConsistencyOverSeries(final List<IndividualRaceResult> results_for_runner) {

        EntryCategory earliest_category = null;
        EntryCategory previous_category = null;

        // Individual race results are in temporal race order.

        for (final IndividualRaceResult result : results_for_runner) {

            final EntryCategory current_category = result.getParticipant().getCategory();

            // Category may not be known for externally organised race.
            if (current_category != null) {

                if (earliest_category == null)
                    earliest_category = current_category;

                if (previous_category != null)
                    checkCategoriesInSuccessiveRaces(previous_category, current_category, result);

                previous_category = current_category;
            }
        }

        // It's enough to check just the earliest and latest (in previous_category) categories, since if the category changes to too much older part-way through
        // the series, and then back again, this will be caught by the check for change to a younger category.
        checkAgeCategoryRangeOverSeries(results_for_runner.getFirst().getParticipantName(), earliest_category, previous_category);
    }

    private void setRunnerCategoryToEarliestCategory(final List<IndividualRaceResult> results_for_runner) {

        EntryCategory earliest_category = null;

        // Individual race results are in temporal race order.

        for (final IndividualRaceResult result : results_for_runner)
            if (earliest_category == null) earliest_category = result.getParticipant().getCategory();

        for (final IndividualRaceResult result : results_for_runner)
            result.getParticipant().setCategory(earliest_category);
    }

    private void checkCategoriesInSuccessiveRaces(final EntryCategory previous_category, final EntryCategory current_category, final IndividualRaceResult result) {

        if (!previous_category.equals(current_category)) {

            final String race_name = (String) result.getRace().getConfig().get(KEY_RACE_NAME_FOR_RESULTS);

            checkAgeCategoriesInSuccessiveRaces(previous_category, current_category, result.getParticipantName(), race_name);
            checkGenderCategoriesInSuccessiveRaces(previous_category, current_category, result.getParticipantName(), race_name);

            race.getNotes().appendToNotes("Runner " + result.getParticipantName() + " changed category from " + previous_category.getShortName() + " to " + current_category.getShortName() + " at " + race_name + LINE_SEPARATOR);
        }
    }

    private static void checkGenderCategoriesInSuccessiveRaces(final EntryCategory previous_category, final EntryCategory current_category, final String runner_name, final String race_name) {

        if (previous_category != null && current_category != null && !current_category.getGender().equals(previous_category.getGender()))
            throw new RuntimeException("invalid category change: runner '" + runner_name + "' changed from " + previous_category.getShortName() + " to " + current_category.getShortName() + " at " + race_name);
    }

    private static void checkAgeCategoriesInSuccessiveRaces(final EntryCategory previous_category, final EntryCategory current_category, final String runner_name, final String race_name) {

        if (previous_category != null && current_category != null && current_category.getAgeRange().getMinimumAge() < previous_category.getAgeRange().getMinimumAge())
            throw new RuntimeException("invalid category change: runner '" + runner_name + "' changed from " + previous_category.getShortName() + " to " + current_category.getShortName() + " at " + race_name);
    }

    private static void checkAgeCategoryRangeOverSeries(final String runner_name, final EntryCategory earliest_category, final EntryCategory latest_category) {

        if (earliest_category != null && latest_category != null && latest_category.getAgeRange().getMinimumAge() > earliest_category.getAgeRange().getMaximumAge() + 1)
            throw new RuntimeException("invalid category change: runner '" + runner_name + "' changed from " + earliest_category.getShortName() + " to " + latest_category.getShortName() + " during series");
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected RaceResults makeRaceResults() {

        final int number_of_races_taken_place = ((SeriesRace) race).getNumberOfRacesTakenPlace();
        final int minimum_number_of_races = (int) race.getConfig().get(KEY_MINIMUM_NUMBER_OF_RACES);

        final boolean multiple_clubs = getClubs().size() > 1;
        final boolean multiple_race_categories = race_categories.size() > 1;
        final boolean possible_to_have_completed = number_of_races_taken_place >= minimum_number_of_races;

        return new SeriesRaceResults() {

            @Override
            public Config getConfig() {
                return race.getConfig();
            }

            @Override
            public Normalisation getNormalisation() {
                return race.getNormalisation();
            }

            @Override
            public Notes getNotes() {
                return race.getNotes();
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
            public List<? extends RaceResult> getPrizeWinners(final PrizeCategory category) {
                return SeriesRaceResultsCalculator.this.getPrizeWinners(category);
            }

            @Override
            public List<String> getPrizeCategoryGroups() {
                return race.getCategoriesProcessor().getPrizeCategoryGroups();
            }

            @Override
            public List<PrizeCategory> getPrizeCategoriesByGroup(final String group) {
                return race.getCategoriesProcessor().getPrizeCategoriesByGroup(group);
            }

            @Override
            public boolean arePrizesInThisOrLaterCategory(final PrizeCategory prizeCategory) {
                return race.getResultsCalculator().arePrizesInThisOrLaterCategory(prizeCategory);
            }

            //////////////////////////////////////////////////////////////////////////////////////////////////

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
            public List<SeriesRaceCategory> getRaceCategories() {
                return SeriesRaceResultsCalculator.this.race_categories;
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
        };
    }
}
