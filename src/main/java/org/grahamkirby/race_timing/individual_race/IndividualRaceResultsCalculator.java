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
import org.grahamkirby.race_timing.common.*;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.grahamkirby.race_timing.common.CommonDataProcessor.readAllLines;
import static org.grahamkirby.race_timing.common.Config.KEY_MEDIAN_TIME;
import static org.grahamkirby.race_timing.common.Config.KEY_RESULTS_PATH;
import static org.grahamkirby.race_timing.common.Normalisation.parseTime;

public class IndividualRaceResultsCalculator extends RaceResultsCalculator {

    private static final int BIB_NUMBER_INDEX = 0;
    private static final int NAME_INDEX = 1;
    private static final int CLUB_INDEX = 2;
    private static final int CATEGORY_INDEX = 3;

    private int next_fake_bib_number = 1;

    //////////////////////////////////////////////////////////////////////////////////////////////////

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
    public boolean areEqualPositionsAllowed() {

        // No dead heats for overall results, since an ordering is imposed at the finish.
        return false;
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

            return median_result1.duration().plus(median_result2.duration()).dividedBy(2);

        } else {
            final SingleRaceResult median_result = (SingleRaceResult) results.get(results.size() / 2);
            return median_result.duration();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void adjustTimes() {

        final IndividualRace impl = (IndividualRace) race;

        adjustTimesByCategory(impl.category_start_offsets);
        adjustTimes(impl.individual_start_offsets);
        adjustTimes(impl.time_trial_start_offsets);
    }

    private void addSeparatelyRecordedTimes() {

        final Map<Integer, Duration> separately_recorded_finish_times = ((IndividualRace) race).separately_recorded_finish_times;

        for (final Map.Entry<Integer, Duration> entry : separately_recorded_finish_times.entrySet())
            overall_results.add(makeRaceResult(new RawResult(entry.getKey(), entry.getValue())));
    }

    private void adjustTimesByCategory(final Map<EntryCategory, Duration> offsets) {

        for (final RaceResult r : overall_results) {

            final SingleRaceResult result = (SingleRaceResult)r;

            if (offsets.containsKey(result.getParticipant().getCategory())) {

                final EntryCategory category = result.getParticipant().getCategory();
                final Duration duration = offsets.get(category);

                result.setFinishTime(result.getFinishTime().minus(duration));
            }
        }
    }

    private void adjustTimes(final Map<Integer, Duration> offsets) {

        for (final RaceResult r : overall_results) {

            final SingleRaceResult result = (SingleRaceResult) r;

            if (offsets.containsKey(result.getBibNumber()))
                result.setFinishTime(result.getFinishTime().minus(offsets.get(result.getBibNumber())));
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

        overall_results = new ArrayList<>(overall_results);
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
            final EntryCategory category = category_name.isEmpty() ? null : race.getCategoryDetails().lookupEntryCategory(category_name);

            final Participant participant = new Runner(name, club, category);

            return new RaceEntry(participant, bib_number);

        } catch (final RuntimeException _) {
            throw new RuntimeException(String.join(" ", elements));
        }
    }

    protected void recordDNF(final String dnf_specification) {

        final int bib_number = Integer.parseInt(dnf_specification);
        final SingleRaceResult result = (SingleRaceResult) getResultWithBibNumber(bib_number);

        result.setDnf(true);
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
}
