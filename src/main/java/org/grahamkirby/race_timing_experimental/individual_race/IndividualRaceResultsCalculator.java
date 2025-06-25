/*
 * race-timing - <https://github.com/grahamkirby/race-timing>
 * Copyright © 2025 Graham Kirby (graham.kirby@st-andrews.ac.uk)
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
package org.grahamkirby.race_timing_experimental.individual_race;

import org.grahamkirby.race_timing.common.Normalisation;
import org.grahamkirby.race_timing.common.RawResult;
import org.grahamkirby.race_timing.single_race.SingleRaceEntry;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.RaceResults;
import org.grahamkirby.race_timing_experimental.common.ResultsCalculator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceConfigProcessor.KEY_DNF_FINISHERS;

public class IndividualRaceResultsCalculator implements ResultsCalculator {

    private Race race;

    private List<IndividualRaceResult> overall_results;

    @Override
    public void setRace(Race race) {
        this.race = race;
    }

    @Override
    public RaceResults calculateResults() {

        initialiseResults();
        recordDNFs();
        sortResults();
//        allocatePrizes();

        return new IndividualRaceResults(overall_results);
    }

    private void initialiseResults() {

        List<RawResult> raw_results = race.getRaceData().getRawResults();

        overall_results = raw_results.stream().
            map(this::makeResult).
            toList();

        overall_results = new ArrayList<>(overall_results);
    }

    private IndividualRaceResult makeResult(final RawResult raw_result) {

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

        String dnf_string = (String) race.getConfig().get(KEY_DNF_FINISHERS);

        if (dnf_string != null && !dnf_string.isBlank())
            for (final String individual_dnf_string : dnf_string.split(","))
                recordDNF(individual_dnf_string);
    }

    protected void recordDNF(final String dnf_specification) {

        final int bib_number = Integer.parseInt(dnf_specification);
        final IndividualRaceResult result = getResultWithBibNumber(bib_number);

        result.dnf = true;
    }

    /** Sorts all results by relevant comparators. */
    protected void sortResults() {

        overall_results.sort(combineComparators(getComparators()));
    }

    public List<Comparator<IndividualRaceResult>> getComparators() {

        return List.of(
            ignoreIfBothResultsAreDNF(penaliseDNF(IndividualRaceResultsCalculator::comparePerformance)),
            ignoreIfEitherResultIsDNF(this::compareRecordedPosition),
            IndividualRaceResultsCalculator::compareRunnerLastName,
            IndividualRaceResultsCalculator::compareRunnerFirstName);
    }

    /** Compares the given results on the basis of their finish positions. */
    private int compareRecordedPosition(final IndividualRaceResult r1, final IndividualRaceResult r2) {

        final int recorded_position1 = getRecordedPosition((r1).entry.bib_number);
        final int recorded_position2 = getRecordedPosition(( r2).entry.bib_number);

        return Integer.compare(recorded_position1, recorded_position2);
    }

    private int getRecordedPosition(final int bib_number) {

        List<RawResult> raw_results = race.getRaceData().getRawResults();

        return (int) raw_results.stream().
            takeWhile(result -> result.getBibNumber() != bib_number).
            count();
    }

    /** Compares two results based on their performances, which may be based on a single or aggregate time,
     *  or a score. Gives a negative result if the first result has a better performance than the second. */
    protected static int comparePerformance(final IndividualRaceResult r1, final IndividualRaceResult r2) {

        return r1.comparePerformanceTo(r2);
    }

    /** Compares two results based on alphabetical ordering of the runners' first names. */
    protected static int compareRunnerFirstName(final IndividualRaceResult r1, final IndividualRaceResult r2) {

        return Normalisation.getFirstName(r1.getParticipantName()).compareTo(Normalisation.getFirstName(r2.getParticipantName()));
    }

    /** Compares two results based on alphabetical ordering of the runners' last names. */
    protected static int compareRunnerLastName(final IndividualRaceResult r1, final IndividualRaceResult r2) {

        return Normalisation.getLastName(r1.getParticipantName()).compareTo(Normalisation.getLastName(r2.getParticipantName()));
    }

    protected static Comparator<IndividualRaceResult> penaliseDNF(final Comparator<? super IndividualRaceResult> base_comparator) {

        return (r1, r2) -> {

            if (!r1.canComplete() && r2.canComplete()) return 1;
            if (r1.canComplete() && !r2.canComplete()) return -1;

            return base_comparator.compare(r1, r2);
        };
    }

    protected static Comparator<IndividualRaceResult> ignoreIfEitherResultIsDNF(final Comparator<? super IndividualRaceResult> base_comparator) {

        return (r1, r2) -> {

            if (!r1.canComplete() || !r2.canComplete()) return 0;
            else return base_comparator.compare(r1, r2);
        };
    }

    protected static Comparator<IndividualRaceResult> ignoreIfBothResultsAreDNF(final Comparator<? super IndividualRaceResult> base_comparator) {

        return (r1, r2) -> {

            if (!r1.canComplete() && !r2.canComplete()) return 0;
            else return base_comparator.compare(r1, r2);
        };
    }

    /** Combines multiple comparators into a single comparator. */
    protected static Comparator<IndividualRaceResult> combineComparators(final Collection<Comparator<IndividualRaceResult>> comparators) {

        return comparators.stream().
            reduce((_, _) -> 0, Comparator::thenComparing);
    }

    protected IndividualRaceEntry getEntryWithBibNumber(final int bib_number) {

        List<IndividualRaceEntry> entries = race.getRaceData().getEntries();

        return entries.stream().
            filter(entry -> entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }

    private IndividualRaceResult getResultWithBibNumber(final int bib_number) {

        return overall_results.stream().
            filter(result -> result.entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }
}
