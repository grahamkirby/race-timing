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
package org.grahamkirby.race_timing.individual_race;

import org.grahamkirby.race_timing.categories.EntryCategory;
import org.grahamkirby.race_timing.categories.PrizeCategory;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing.common.CommonDataProcessor.readAllLines;
import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.parseTime;

public class IndividualRaceResultsCalculatorImpl implements RaceResultsCalculator {

    private Race race;

    private List<RaceResult> overall_results;
    private StringBuilder notes;
    private int next_fake_bib_number = 1;
    private final Function<String, RaceResult> race_result_mapper = line -> makeRaceResult(new ArrayList<>(Arrays.stream(line.split("\t")).toList()));

    @Override
    public void setRace(final Race race) {

        this.race = race;
        notes = new StringBuilder();
    }

    @Override
    public void calculateResults() {

        initialiseResults();
        adjustTimes();
        addSeparatelyRecordedTimes();
        recordDNFs();
        sortResults();
        allocatePrizes();
    }

    @Override
    public StringBuilder getNotes() {
        return notes;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void adjustTimes() {

        final IndividualRaceImpl impl = (IndividualRaceImpl) race.getSpecific();

        adjustTimesByCategory(impl.category_start_offsets);
        adjustTimes(impl.individual_start_offsets);
        adjustTimes(impl.time_trial_start_offsets);
    }

    private void addSeparatelyRecordedTimes() {

        final Map<Integer, Duration> separately_recorded_finish_times = ((IndividualRaceImpl) race.getSpecific()).separately_recorded_finish_times;

        for (final Map.Entry<Integer, Duration> entry : separately_recorded_finish_times.entrySet())
            overall_results.add(makeResult(new RawResult(entry.getKey(), entry.getValue())));
    }

    private void adjustTimesByCategory(final Map<EntryCategory, Duration> offsets) {

        for (final RaceResult r : overall_results) {

            final SingleRaceResult result = (SingleRaceResult)r;

            if (offsets.containsKey(result.getParticipant().category)) {

                final EntryCategory category = result.getParticipant().category;
                final Duration duration = offsets.get(category);

                result.finish_time = result.finish_time.minus(duration);
            }
        }
    }

    private void adjustTimes(final Map<Integer, Duration> offsets) {

        for (final RaceResult r : overall_results) {

            final SingleRaceResult result = (SingleRaceResult) r;

            if (offsets.containsKey(result.bib_number))
                result.finish_time = result.finish_time.minus(offsets.get(result.bib_number));
        }
    }

    private void allocatePrizes() {

        for (final PrizeCategory category : race.getCategoryDetails().getPrizeCategories())
            setPrizeWinners(category);
    }

    /** Returns prize winners in given category. */
    public List<RaceResult> getPrizeWinners(final PrizeCategory prize_category) {

        final List<RaceResult> prize_results = overall_results.stream().
            filter(result -> result.getCategoriesOfPrizesAwarded().contains(prize_category)).
            toList();

        setPositionStrings(prize_results);

        return prize_results;
    }

    private void setPrizeWinners(final PrizeCategory category) {

        final AtomicInteger position = new AtomicInteger(1);

        overall_results.stream().
            filter(_ -> position.get() <= category.numberOfPrizes()).
            filter(result -> isPrizeWinner(result, category)).
            forEachOrdered(result -> {
                position.getAndIncrement();
                setPrizeWinner(result, category);
            });
    }

    protected boolean isPrizeWinner(final RaceResult result, final PrizeCategory prize_category) {

        return result.canComplete() &&
            isStillEligibleForPrize(result, prize_category) &&
            race.getCategoryDetails().isResultEligibleForPrizeCategory(((Runner)((SingleRaceResult)result).getParticipant()).club, race.getNormalisation().gender_eligibility_map, ((SingleRaceResult)result).getParticipant().category, prize_category);
    }

    private static boolean isStillEligibleForPrize(final RaceResult result, final PrizeCategory new_prize_category) {

        if (!new_prize_category.isExclusive()) return true;

        for (final PrizeCategory category_already_won : result.getCategoriesOfPrizesAwarded())
            if (category_already_won.isExclusive()) return false;

        return true;
    }

    protected static void setPrizeWinner(final RaceResult result, final PrizeCategory category) {

        result.getCategoriesOfPrizesAwarded().add(category);
    }

    private void initialiseResults() {

        final List<RawResult> raw_results = race.getRaceData().getRawResults();

        if (raw_results.isEmpty()) {
            try {
                overall_results = loadOverallResults();
            }
            catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {

            overall_results = raw_results.stream().
                map(this::makeResult).
                toList();
        }

        overall_results = new ArrayList<>(overall_results);
    }

    List<RaceResult> loadOverallResults() throws IOException {

        return readAllLines((Path) race.getConfig().get(KEY_RESULTS_PATH)).stream().
            map(Normalisation::stripEntryComment).
            filter(Predicate.not(String::isBlank)).
            map(race_result_mapper).
            filter(Objects::nonNull).
            toList();
    }

    private RaceResult makeRaceResult(final List<String> elements) {

        elements.addFirst(String.valueOf(next_fake_bib_number++));

        final RaceEntry entry = makeRaceEntry(elements, race);
        final Duration finish_time = parseTime(elements.getLast());

        return new IndividualRaceResult(race, entry, finish_time);
    }

    private static final int BIB_NUMBER_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int CLUB_INDEX = 2;
    private static final int CATEGORY_INDEX = 3;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings({"OverlyBroadCatchBlock", "IfCanBeAssertion"})
    public RaceEntry makeRaceEntry(final List<String> elements, final Race race) {

        final Normalisation normalisation = race.getNormalisation();

        final List<String> mapped_elements = normalisation.mapRaceEntryElements(elements);

        try {
            final int bib_number = Integer.parseInt(mapped_elements.get(BIB_NUMBER_INDEX));

            final String name = normalisation.cleanRunnerName(mapped_elements.get(NAME_INDEX));
            final String club = normalisation.cleanClubOrTeamName(mapped_elements.get(CLUB_INDEX));

            final String category_name = normalisation.normaliseCategoryShortName(mapped_elements.get(CATEGORY_INDEX));
            final EntryCategory category = category_name.isEmpty() ? null : race.getCategoryDetails().lookupEntryCategory(category_name);

            final Participant participant = new Runner(name, club, category);

            return new RaceEntry(participant, bib_number, race);

        } catch (final RuntimeException _) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }

    /** Gets the median finish time for the race. */
    public Duration getMedianTime() {

        // The median time may be recorded explicitly if not all results are recorded.
        final String median_time_string = (String) race.getConfig().get(KEY_MEDIAN_TIME);
        if (median_time_string != null) return parseTime(median_time_string);

        // Calculate median time.
        final List<RaceResult> results = getOverallResults();

        if (results.size() % 2 == 0) {

            final SingleRaceResult median_result1 = (SingleRaceResult) results.get(results.size() / 2 - 1);
            final SingleRaceResult median_result2 = (SingleRaceResult) results.get(results.size() / 2);

            return median_result1.finish_time.plus(median_result2.finish_time).dividedBy(2);

        } else {
            final SingleRaceResult median_result = (SingleRaceResult) results.get(results.size() / 2);
            return median_result.finish_time;
        }
    }

    private RaceResult makeResult(final RawResult raw_result) {

        final int bib_number = raw_result.getBibNumber();
        final Duration finish_time = raw_result.getRecordedFinishTime();

        return new IndividualRaceResult(race, getEntryWithBibNumber(bib_number), finish_time);
    }

    protected void recordDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not completing the
        // course.

        // Cases where there is no recorded result are captured by the
        // default completion status being DNS.

        final String dnf_string = (String) race.getConfig().get(KEY_DNF_FINISHERS);

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(","))
                recordDNF(individual_dnf_string);
    }

    protected void recordDNF(final String dnf_specification) {

        final int bib_number = Integer.parseInt(dnf_specification);
        final SingleRaceResult result = (SingleRaceResult) getResultWithBibNumber(bib_number);

        result.dnf = true;
    }

    /** Sorts all results by relevant comparators. */
    private void sortResults() {

        overall_results.sort(null);
    }

    protected RaceEntry getEntryWithBibNumber(final int bib_number) {

        final List<RaceEntry> entries = race.getRaceData().getEntries();

        return entries.stream().
            filter(entry -> entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }

    private CommonRaceResult getResultWithBibNumber(final int bib_number) {

        return overall_results.stream().
            map(result -> (SingleRaceResult) result).
            filter(result -> result.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }

    public boolean areEqualPositionsAllowed() {

        // No dead heats for overall results, since an ordering is imposed at the finish.
        return false;
    }

    /** Sets the position string for each result. These are recorded as strings rather than ints so
     *  that equal results can be recorded as e.g. "13=". Whether or not equal positions are allowed
     *  is determined by the particular race type. */
    void setPositionStrings(final List<RaceResult> results) {

        setPositionStrings(results, areEqualPositionsAllowed());
    }

    /** Sets the position string for each result. These are recorded as strings rather than ints so
     *  that equal results can be recorded as e.g. "13=". Whether or not equal positions are allowed
     *  is determined by the second parameter. */
    protected static void setPositionStrings(final List<RaceResult> results, final boolean allow_equal_positions) {

        // Sets position strings for dead heats, if allowed by the allow_equal_positions flag.
        // E.g. if results 3 and 4 have the same time, both will be set to "3=".

        // The flag is passed in rather than using race.allowEqualPositions() since that applies to the race overall.
        // In a series race the individual races don't allow equal positions, but the race overall does.
        // Conversely in a relay race the legs after the first leg do allow equal positions.

        for (int result_index = 0; result_index < results.size(); result_index++) {

            final RaceResult result = results.get(result_index);

            if (result.canComplete()) {
                if (allow_equal_positions) {

                    // Skip over any following results with the same performance.
                    // Defined in terms of performance rather than duration, since in some races ranking is determined
                    // by scores rather than times.
                    final int highest_index_with_same_performance = getHighestIndexWithSamePerformance(results, result_index);

                    if (highest_index_with_same_performance > result_index) {

                        // There are results following this one with the same performance.
                        recordEqualPositions(results, result_index, highest_index_with_same_performance);
                        result_index = highest_index_with_same_performance;
                    } else
                        // The following result has a different performance, so just record current position for this one.
                        result.setPositionString(String.valueOf(result_index + 1));
                } else {
                    result.setPositionString(String.valueOf(result_index + 1));
                }
            } else {
                result.setPositionString("-");
            }
        }
    }

    /** Records the same position for the given range of results. */
    private static void recordEqualPositions(final List<RaceResult> results, final int start_index, final int end_index) {

        final String position_string = (start_index + 1) + "=";

        for (int i = start_index; i <= end_index; i++)
            results.get(i).setPositionString(position_string);
    }

    /** Finds the highest index for which the performance is the same as the given index. */
    private static int getHighestIndexWithSamePerformance(final List<RaceResult> results, final int start_index) {

        int highest_index_with_same_result = start_index;

        while (highest_index_with_same_result < results.size() - 1 &&
            results.get(highest_index_with_same_result).comparePerformanceTo(results.get(highest_index_with_same_result + 1)) == 0)

            highest_index_with_same_result++;

        return highest_index_with_same_result;
    }

    public List<RaceResult> getOverallResults() {
        return overall_results;
    }

    /** Gets all the results eligible for the given prize categories. */
    public List<RaceResult> getOverallResults(final List<PrizeCategory> prize_categories) {

        final Predicate<RaceResult> prize_category_filter = r -> {
            final SingleRaceResult result = (SingleRaceResult) r;
            return race.getCategoryDetails().isResultEligibleInSomePrizeCategory(((Runner) result.getParticipant()).club, race.getNormalisation().gender_eligibility_map, result.getParticipant().category, prize_categories);
        };

        final List<RaceResult> results = overall_results.stream().filter(prize_category_filter).toList();
        setPositionStrings(results);
        return results;
    }

    public boolean arePrizesInThisOrLaterCategory(final PrizeCategory category) {

        for (final PrizeCategory category2 : race.getCategoryDetails().getPrizeCategories().reversed()) {

            if (!getPrizeWinners(category2).isEmpty()) return true;
            if (category.equals(category2) && !arePrizesInOtherCategorySameAge(category)) return false;
        }
        return false;
    }

    private boolean arePrizesInOtherCategorySameAge(final PrizeCategory category) {

        return race.getCategoryDetails().getPrizeCategories().stream().
            filter(cat -> !cat.equals(category)).
            filter(cat -> cat.getMinimumAge() == category.getMinimumAge()).
            anyMatch(cat -> !getPrizeWinners(cat).isEmpty());
    }
}
