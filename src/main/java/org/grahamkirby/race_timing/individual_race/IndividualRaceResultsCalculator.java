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
import org.grahamkirby.race_timing.categories.PrizeCategoryGroup;
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing.common.CommonDataProcessor.readAllLines;
import static org.grahamkirby.race_timing.common.Config.*;
import static org.grahamkirby.race_timing.common.Normalisation.parseTime;

public class IndividualRaceResultsCalculator extends RaceResultsCalculator {

    private static final int BIB_NUMBER_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int CLUB_INDEX = 2;
    private static final int CATEGORY_INDEX = 3;

    private int next_fake_bib_number = 1;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public IndividualRaceResultsCalculator(final RaceInternal race) {
        super(race);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public RaceResults calculateResults() {

        initialiseResults();
        adjustTimes();
        addSeparatelyRecordedTimes();
        recordDNFs();
        sortOverallResults();
        allocatePrizes();

        getPrizeWinners(null);

        return makeRaceResults();
    }

    private RaceResults makeRaceResults() {

        return new RaceResults() {

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
                return race.getResultsCalculator().getPrizeWinners(category);
            }

            @Override
            public List<String> getTeamPrizes() {
                return ((IndividualRace) race).getTeamPrizes();
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

    @Override
    public boolean areEqualPositionsAllowed() {

        // No dead heats for overall results, since an ordering is imposed at the finish.
        return false;
    }

    @Override
    protected void recordDNF(final String dnf_specification) {

        final int bib_number = Integer.parseInt(dnf_specification);
        final SingleRaceResult result = (SingleRaceResult) getResultWithBibNumber(bib_number);

        result.setDnf(true);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

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

            final Duration duration1 = (Duration) median_result1.getPerformance().getValue();
            final Duration duration2 = (Duration) median_result2.getPerformance().getValue();

            return duration1.plus(duration2).dividedBy(2);

        } else {
            final SingleRaceResult median_result = (SingleRaceResult) results.get(results.size() / 2);
            return (Duration) median_result.getPerformance().getValue();
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void initialiseResults() {

        final List<RawResult> raw_results = ((SingleRaceInternal) race).getRawResults();

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
                map(this::makeRaceResult).
                toList();
        }

        overall_results = makeMutableCopy(overall_results);
    }

    private List<RaceResult> loadOverallResults() throws IOException {

        return readAllLines((Path) race.getConfig().get(KEY_RESULTS_PATH)).stream().
            map(Normalisation::stripEntryComment).
            filter(Predicate.not(String::isBlank)).
            map(this::makeRaceResult).
            toList();
    }

    private RaceResult makeRaceResult(final String line) {

        final List<String> elements = new ArrayList<>(Arrays.stream(line.split("\t")).toList());

        elements.addFirst(String.valueOf(next_fake_bib_number++));

        final RaceEntry entry = makeRaceEntry(elements, race);
        final Duration finish_time = parseTime(elements.getLast());

        return new IndividualRaceResult(race, entry, finish_time);
    }

    private RaceResult makeRaceResult(final RawResult raw_result) {

        final int bib_number = raw_result.getBibNumber();
        final Duration finish_time = raw_result.getRecordedFinishTime();

        return new IndividualRaceResult(race, getEntryWithBibNumber(bib_number), finish_time);
    }

    private RaceEntry makeRaceEntry(final List<String> elements, final RaceInternal race) {

        final Normalisation normalisation = race.getNormalisation();

        final List<String> mapped_elements = normalisation.mapRaceEntryElements(elements);

        try {
            final int bib_number = Integer.parseInt(mapped_elements.get(BIB_NUMBER_INDEX));

            final String name = normalisation.cleanRunnerName(mapped_elements.get(NAME_INDEX));
            final String club = normalisation.cleanClubOrTeamName(mapped_elements.get(CLUB_INDEX));

            final String category_name = normalisation.normaliseCategoryShortName(mapped_elements.get(CATEGORY_INDEX));
            final EntryCategory category = category_name.isEmpty() ? null : race.getCategoriesProcessor().lookupEntryCategory(category_name);

            final Participant participant = new Runner(name, club, category);

            return new RaceEntry(participant, bib_number);

        } catch (final RuntimeException _) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }

    private RaceEntry getEntryWithBibNumber(final int bib_number) {

        final List<RaceEntry> entries = ((SingleRaceInternal) race).getEntries();

        return entries.stream().
            filter(entry -> entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }

    private RaceResult getResultWithBibNumber(final int bib_number) {

        return overall_results.stream().
            map(result -> (SingleRaceResult) result).
            filter(result -> result.getBibNumber() == bib_number).
            findFirst().
            orElseThrow();
    }

    private void adjustTimes() {

        setTimesByCategory();
        setIndividualStartTimes();
        setTimeTrialStartTimes();
    }

    private void addSeparatelyRecordedTimes() {

        final Map<Integer, Duration> separately_recorded_finish_times = ((IndividualRace) race).separately_recorded_finish_times;

        for (final Map.Entry<Integer, Duration> entry : separately_recorded_finish_times.entrySet())
            overall_results.add(makeRaceResult(new RawResult(entry.getKey(), entry.getValue())));
    }

    private void setTimesByCategory() {

        // Category / start time
        // Example: CATEGORY_START_OFFSETS =  FU9/00:01:00,MU9/00:01:00,FU11/00:01:00,MU11/00:01:00

        final Consumer<Object> process_category_start_times = category_start_offsets -> {

            final Map<EntryCategory, Duration> category_offsets = new HashMap<>();

            for (final String offset_string : ((String) category_start_offsets).split(",", -1)) {

                final String[] split = offset_string.split("/");
                category_offsets.put(race.getCategoriesProcessor().lookupEntryCategory(split[0]), parseTime(split[1]));
            }

            for (final RaceResult r : overall_results) {

                final SingleRaceResult result = (SingleRaceResult) r;
                final EntryCategory category = result.getParticipant().getCategory();

                final Duration category_start_time = category_offsets.get(category);

                if (category_start_time != null)
                    result.setStartTime(category_start_time);
            }
        };

        race.getConfig().processConfigIfPresent(KEY_CATEGORY_START_OFFSETS, process_category_start_times);
    }

    private void setTimeTrialStartTimes() {

        // This option applies when time-trial runners are assigned to waves in order of bib number,
        // with incomplete waves if there are any gaps in bib numbers.

        final Consumer<Object> process_time_trial_start_times = time_trial_runners_per_wave -> {

            final Duration time_trial_inter_wave_interval = (Duration) race.getConfig().get(KEY_TIME_TRIAL_INTER_WAVE_INTERVAL);

            for (final RaceResult r : overall_results) {

                final SingleRaceResult result = (SingleRaceResult) r;
                final int wave_number = (result.getBibNumber() - 1) / ((int) time_trial_runners_per_wave);

                result.setStartTime(time_trial_inter_wave_interval.multipliedBy(wave_number));
            }
        };

        race.getConfig().processConfigIfPresent(KEY_TIME_TRIAL_RUNNERS_PER_WAVE, process_time_trial_start_times);
    }

    private void setIndividualStartTimes() {

        // Bib number / start time
        // Example: INDIVIDUAL_START_TIMES = 2/0:10:00,26/0:20:00

        final Consumer<Object> process_individual_start_times = individual_start_times -> {

            final Map<Integer, Duration> start_times = new HashMap<>();

            for (final String individual_early_start : ((String) individual_start_times).split(",")) {

                final String[] split = individual_early_start.split("/");

                final int bib_number = Integer.parseInt(split[0]);
                final Duration offset = parseTime(split[1]);

                start_times.put(bib_number, offset);
            }

            for (final RaceResult r : overall_results) {

                final SingleRaceResult result = (SingleRaceResult) r;

                if (start_times.containsKey(result.getBibNumber()))
                    result.setStartTime(start_times.get(result.getBibNumber()));
            }
        };

        race.getConfig().processConfigIfPresent(KEY_INDIVIDUAL_START_TIMES, process_individual_start_times);
    }
}
