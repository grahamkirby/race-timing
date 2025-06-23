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

import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.RaceResults;
import org.grahamkirby.race_timing_experimental.common.ResultsCalculator;

import java.util.List;

public class IndividualRaceResultsCalculator implements ResultsCalculator {

    private Race race;

    private List<RaceResult> overall_results;


    @Override
    public void setRace(Race race) {
        this.race = race;
    }

    @Override
    public RaceResults calculateResults() {

        recordDNFs();
//        sortResults();
//        allocatePrizes();

        return new IndividualRaceResults(overall_results);
    }

    protected void recordDNFs() {

        // This fills in the DNF results that were specified explicitly in the config
        // file, corresponding to cases where the runners reported not completing the
        // course.

        // Cases where there is no recorded result are captured by the
        // default completion status being DNS.

//        if (dnf_string != null && !dnf_string.isBlank())
//            for (final String individual_dnf_string : dnf_string.split(","))
//                recordDNF(individual_dnf_string);
    }

    protected void recordDNF(final String dnf_specification) {

        final int bib_number = Integer.parseInt(dnf_specification);
        final SingleRaceResult result = getResultWithBibNumber(bib_number);

        result.dnf = true;
    }

    private SingleRaceResult getResultWithBibNumber(final int bib_number) {

        return overall_results.stream().
            map(result -> ((SingleRaceResult) result)).
            filter(result -> result.entry.bib_number == bib_number).
            findFirst().
            orElseThrow();
    }
}
