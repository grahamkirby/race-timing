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
package org.grahamkirby.race_timing.series_race.fife_ac_midweek;

import org.grahamkirby.race_timing.common.RacePrizes;
import org.grahamkirby.race_timing.common.RaceResult;
import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing.common.categories.SeniorRaceCategories;
import org.grahamkirby.race_timing.individual_race.IndividualRace;
import org.grahamkirby.race_timing.individual_race.IndividualRaceResult;
import org.grahamkirby.race_timing.series_race.SeriesRace;
import org.grahamkirby.race_timing.series_race.SeriesRaceInput;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MidweekRace extends SeriesRace {

    ////////////////////////////////////////////  SET UP  ////////////////////////////////////////////
    //                                                                                              //
    //  See README.md at the project root for details of how to configure and run this software.    //
    //                                                                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////

    private static final int MAX_RACE_SCORE = 200;
    public static final int DEFAULT_OPEN_PRIZES = 3;

    private boolean open_prize_categories, senior_prize_categories;
    private int number_of_open_prizes, number_of_senior_prizes;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public MidweekRace(final Path config_file_path) throws IOException {
        super(config_file_path);
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(final String[] args) throws IOException {

        // Path to configuration file should be first argument.

        if (args.length < 1)
            System.out.println("usage: java MidweekRace <config file path>");
        else {
            new MidweekRace(Paths.get(args[0])).processResults();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected Comparator<RaceResult> getResultsSortComparator() {
        return MidweekRaceResult::compare;
    }

    @Override
    protected void configureHelpers() {

        input = new SeriesRaceInput(this);

        output_CSV = new MidweekRaceOutputCSV(this);
        output_HTML = new MidweekRaceOutputHTML(this);
        output_text = new MidweekRaceOutputText(this);
        output_PDF = new MidweekRaceOutputPDF(this);

        prizes = new RacePrizes(this);
    }

    @Override
    protected void configureCategories() {

        categories = new SeniorRaceCategories(open_prize_categories, senior_prize_categories, number_of_open_prizes, number_of_senior_prizes, number_of_category_prizes);
    }

    @Override
    protected void printPrizes() throws IOException {

        output_PDF.printPrizes();
        output_HTML.printPrizes();
        output_text.printPrizes();
    }

    @Override
    protected void printCombined() throws IOException {

        output_HTML.printCombined();
    }

    @Override
    protected RaceResult getOverallResult(final Runner runner) {

        final MidweekRaceResult result = new MidweekRaceResult(runner, this);

        for (final IndividualRace individual_race : races)
            result.scores.add(calculateRaceScore(individual_race, runner));

        return result;
    }

    @Override
    protected void configureInputData() throws IOException {

        super.configureInputData();

        for (final String runner_name : getRunnerNames())
            checkClubsForRunner(runner_name);
    }

    @Override
    protected void readProperties() {

        super.readProperties();

        minimum_number_of_races = Integer.parseInt(getProperties().getProperty(MINIMUM_NUMBER_OF_RACES_KEY));
        open_prize_categories = Boolean.parseBoolean(getPropertyWithDefault(OPEN_PRIZE_CATEGORIES_KEY, "true"));
        senior_prize_categories = Boolean.parseBoolean(getPropertyWithDefault(SENIOR_PRIZE_CATEGORIES_KEY, "true"));
        number_of_open_prizes = Integer.parseInt(getPropertyWithDefault(NUMBER_OF_OPEN_PRIZES_KEY, String.valueOf(DEFAULT_OPEN_PRIZES)));
        number_of_senior_prizes = Integer.parseInt(getPropertyWithDefault(NUMBER_OF_SENIOR_PRIZES_KEY, String.valueOf(DEFAULT_OPEN_PRIZES)));
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void checkClubsForRunner(final String runner_name) {

        // Where a runner name is associated with a single entry with a defined club
        // plus some other entries with no club defined, add the club to those entries.

        // Where a runner name is associated with multiple clubs, leave as is, under
        // assumption that they are separate runners.
        final List<String> clubs_for_runner = getRunnerClubs(runner_name);
        final List<String> defined_clubs = getDefinedClubs(clubs_for_runner);

        final int number_of_defined_clubs = defined_clubs.size();
        final int number_of_undefined_clubs = clubs_for_runner.size() - number_of_defined_clubs;

        if (number_of_defined_clubs == 1 && number_of_undefined_clubs > 0)
            recordClubForRunnerName(runner_name, defined_clubs.get(0));

        if (number_of_defined_clubs > 1) {
            getNotes().append("Runner name ").append(runner_name).append(" recorded for multiple clubs: ");
            for (String club : defined_clubs)
                getNotes().append(club).append(" ");
            getNotes().append("\n");
        }
    }

    private void recordClubForRunnerName(final String runner_name, final String defined_club) {

        for (final IndividualRace race : races)
            if (race != null)
                for (final RaceResult result : race.getOverallResults()) {

                    final Runner runner = ((IndividualRaceResult)result).entry.runner;
                    if (runner.name.equals(runner_name) && runner.club.equals("?"))
                        runner.club = defined_club;
                }
    }

    private List<String> getDefinedClubs(final List<String> clubs) {
        return clubs.stream().filter(club -> !club.equals("?")).toList();
    }

    private List<String> getRunnerClubs(final String runner_name) {

        final Set<String> clubs = new HashSet<>();

        for (final IndividualRace race : races) {
            if (race != null)
                for (final RaceResult result : race.getOverallResults()) {

                    final Runner runner = ((IndividualRaceResult)result).entry.runner;
                    if (runner.name.equals(runner_name)) clubs.add(runner.club);
                }
        }

        return new ArrayList<>(clubs);
    }

    private List<String> getRunnerNames() {

        final Set<String> names = new HashSet<>();

        for (final IndividualRace race : races) {
            if (race != null)
                for (RaceResult result : race.getOverallResults()) {
                    final Runner runner = ((IndividualRaceResult)result).entry.runner;
                    names.add(runner.name);
                }
        }

        return new ArrayList<>(names);
    }

    private int calculateRaceScore(final IndividualRace individual_race, final Runner runner) {

        if (individual_race == null) return -1;

        int score = MAX_RACE_SCORE;

        final String gender = runner.category.getGender();

        // The first finisher of each gender gets the maximum score, the next one less, and so on.
        for (final RaceResult result : individual_race.getOverallResults()) {

            final Runner result_runner = ((IndividualRaceResult)result).entry.runner;

            if (result_runner.equals(runner)) return Math.max(score, 0);
            if (gender.equals(result_runner.category.getGender())) score--;
        }

        // Runner didn't compete in this race.
        return 0;
    }
}
