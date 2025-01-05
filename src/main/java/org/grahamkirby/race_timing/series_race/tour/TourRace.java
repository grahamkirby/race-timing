/*
 * Copyright 2024 Graham Kirby:
 * <https://github.com/grahamkirby/race-timing>
 *
 * This file is part of the module race-timing.
 *
 * race-timing is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * race-timing is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with race-timing. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.grahamkirby.race_timing.series_race.tour;

import org.grahamkirby.race_timing.common.Race;
import org.grahamkirby.race_timing.common.RaceInput;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.output.RaceOutputCSV;
import org.grahamkirby.race_timing.common.output.RaceOutputHTML;
import org.grahamkirby.race_timing.common.output.RaceOutputPDF;
import org.grahamkirby.race_timing.common.output.RaceOutputText;
import org.grahamkirby.race_timing.series_race.SeriesRace;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class TourRace extends SeriesRace {

    public TourRace(final Path config_file_path) throws IOException {

        super(config_file_path);
    }

    public static void main(final String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java MinitourRace <config file path>");
        else
            new TourRace(Paths.get(args[0])).processResults();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected RaceInput getInput() {
        return new TourRaceInput(this);
    }

    @Override
    protected RaceOutputCSV getOutputCSV() {
        return new TourRaceOutputCSV(this);
    }

    @Override
    protected RaceOutputHTML getOutputHTML() {
        return new TourRaceOutputHTML(this);
    }

    @Override
    protected RaceOutputText getOutputText() {
        return new TourRaceOutputText(this);
    }

    @Override
    protected RaceOutputPDF getOutputPDF() {
        return new TourRaceOutputPDF(this);
    }

    @Override
    protected void outputResults() throws IOException {

        super.outputResults();
        ((TourRaceOutputHTML) output_HTML).printIndividualRaces();
    }

    @Override
    protected List<Comparator<RaceResult>> getComparators() {

        return List.of(Race::compareCompletion, SeriesRace::comparePossibleCompletion, Race::comparePerformance, Race::compareRunnerLastName, Race::compareRunnerFirstName);
    }

    @Override
    protected List<Comparator<RaceResult>> getDNFComparators() {
        return List.of();
    }

    @Override
    protected RaceResult getOverallResult(final Runner runner) {

        final List<Duration> times = races.stream().
            map(race -> race == null ? null : race.getRunnerTime(runner)).
            toList();

        return new TourRaceResult(runner, times, this);
    }

    @Override
    protected Predicate<RaceResult> getResultInclusionPredicate() {

        return (_ -> true);
    }

    @Override
    protected void processMultipleClubsForRunner(final String runner_name, final List<String> defined_clubs) {
    }

    @Override
    protected void normaliseClubsForRunner(final String runner_name) {
    }
}
