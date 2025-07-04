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
package org.grahamkirby.race_timing.series_race.midweek;


import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.single_race.SingleRace;
import org.grahamkirby.race_timing.single_race.SingleRaceResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public final class MidweekRace extends SeriesRace {

    // Configuration file keys.
    public static final String KEY_SCORE_FOR_FIRST_PLACE = "SCORE_FOR_FIRST_PLACE";

    private int score_for_first_place;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private MidweekRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    public static void main(final String[] args) throws IOException {

        commonMain(args, config_file_path -> new MidweekRace(Path.of(config_file_path)));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void configure() throws IOException {

        super.configure();
        configureClubs();
    }

    @Override
    protected void readProperties() {

        super.readProperties();
        score_for_first_place = Integer.parseInt(getRequiredProperty(KEY_SCORE_FOR_FIRST_PLACE));
    }

    @Override
    protected RaceInput getInput() {
        return new MidweekRaceInput(this);
    }

    @Override
    protected RaceOutputCSV getOutputCSV() {
        return new MidweekRaceOutputCSV(this);
    }

    @Override
    protected RaceOutputHTML getOutputHTML() {
        return new MidweekRaceOutputHTML(this);
    }

    @Override
    protected RaceOutputText getOutputText() {
        return new MidweekRaceOutputText(this);
    }

    @Override
    protected RaceOutputPDF getOutputPDF() {
        return new MidweekRaceOutputPDF(this);
    }

    @Override
    protected List<Comparator<RaceResult>> getComparators() {

        return List.of(penaliseDNF(Race::comparePerformance), Race::compareRunnerLastName, Race::compareRunnerFirstName);
    }

    @Override
    protected RaceResult getOverallResult(final Runner runner) {

        final List<Integer> scores = races.stream().
            map(individual_race -> calculateRaceScore(individual_race, runner)).
            toList();

        return new MidweekRaceResult(runner, scores, this);
    }

    @Override
    protected Predicate<RaceResult> getResultInclusionPredicate() {

        return (_ -> true);
    }

    int calculateRaceScore(final SingleRace individual_race, final Runner runner) {

        if (individual_race == null) return 0;

        // The first finisher of each gender gets the maximum score, the next finisher one less, and so on.

        final List<SingleRaceResult> gender_results = individual_race.getOverallResults().stream().
            map(result -> (SingleRaceResult) result).
            filter(SingleRaceResult::canComplete).
            filter(result -> result.getCategory().getGender().equals(runner.category.getGender())).
            toList();

        final int gender_position = (int) gender_results.stream().
            takeWhile(result -> !result.getParticipant().equals(runner)).
            count() + 1;

        return gender_position <= gender_results.size() ? Math.max(score_for_first_place - gender_position + 1, 0) : 0;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    protected void processMultipleClubsForRunner(final String runner_name, final List<String> defined_clubs) {

        noteMultipleClubsForRunnerName(runner_name, defined_clubs);
    }
}
