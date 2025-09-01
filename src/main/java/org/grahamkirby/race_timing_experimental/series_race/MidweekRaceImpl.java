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
package org.grahamkirby.race_timing_experimental.series_race;

import org.grahamkirby.race_timing.common.Runner;
import org.grahamkirby.race_timing_experimental.common.CommonRace;
import org.grahamkirby.race_timing_experimental.common.Race;
import org.grahamkirby.race_timing_experimental.common.SingleRaceResult;
import org.grahamkirby.race_timing_experimental.common.SpecificRace;
import org.grahamkirby.race_timing_experimental.individual_race.IndividualRaceFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.grahamkirby.race_timing_experimental.common.Config.*;

public class MidweekRaceImpl implements SpecificRace {

    private Race race;
    private List<Race> races;
    private List<String> race_config_paths;

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public void setRace(Race race) {
        this.race = race;
    }

    @Override
    public void completeConfiguration() {

        try {
            race_config_paths = Arrays.asList(((String) race.getConfig().get(KEY_RACES)).split(",", -1));
            races = loadRaces();
            configureClubs();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    List<Race> getRaces() {
        return races;
    }

    private void configureClubs() {
        getRunnerNames().forEach(this::normaliseClubsForRunner);
    }

    private void normaliseClubsForRunner(final String runner_name) {

        // Where a runner name is associated with a single entry with a defined club
        // plus some other entries with no club defined, add the club to those entries.

        // Where a runner name is associated with multiple clubs, leave as is, under
        // assumption that they are separate runner_names.
        final List<String> clubs_for_runner = getRunnerClubs(runner_name);
        final List<String> defined_clubs = getDefinedClubs(clubs_for_runner);

        final int number_of_defined_clubs = defined_clubs.size();
        final int number_of_undefined_clubs = clubs_for_runner.size() - number_of_defined_clubs;

        if (number_of_defined_clubs == 1 && number_of_undefined_clubs > 0)
            recordDefinedClubForRunnerName(runner_name, defined_clubs.getFirst());

        if (number_of_defined_clubs > 1)
            processMultipleClubsForRunner(runner_name, defined_clubs);
    }

    protected void processMultipleClubsForRunner(final String runner_name, final List<String> defined_clubs) {

        noteMultipleClubsForRunnerName(runner_name, defined_clubs);
    }

    protected void noteMultipleClubsForRunnerName(final String runner_name, final Iterable<String> defined_clubs) {

        race.getResultsCalculator().getNotes().append(STR."Runner \{runner_name} recorded for multiple clubs: \{String.join(", ", defined_clubs)}\n");
    }

    private static List<String> getDefinedClubs(final Collection<String> clubs) {

        return clubs.stream().filter(MidweekRaceImpl::isClubDefined).toList();
    }

    private static boolean isClubDefined(final String club) {
        return !club.equals("?");
    }

    private List<String> getRunnerClubs(final String runner_name) {

        return races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            map(result -> result.entry.participant).
            filter(participant -> participant.name.equals(runner_name)).
            map(participant -> ((Runner)participant).club).
            distinct().
            sorted().
            toList();
    }

    protected void recordDefinedClubForRunnerName(final String runner_name, final String defined_club) {

        races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            map(result -> result.entry.participant).
            filter(participant -> participant.name.equals(runner_name)).
            forEachOrdered(participant -> ((Runner)participant).club = defined_club);
    }

    private List<String> getRunnerNames() {

        return races.stream().
            filter(Objects::nonNull).
            flatMap(race -> race.getResultsCalculator().getOverallResults().stream()).
            map(result -> (SingleRaceResult) result).
            map(result -> result.entry.participant.name).
            distinct().
            toList();
    }

    int calculateRaceScore(final Race individual_race, final Runner runner) {

        if (individual_race == null) return 0;

        // The first finisher of each gender gets the maximum score, the next finisher one less, and so on.

        final List<SingleRaceResult> gender_results = individual_race.getResultsCalculator().getOverallResults().stream().
            map(result -> (SingleRaceResult) result).
            filter(SingleRaceResult::canComplete).
            filter(result -> result.getCategory().getGender().equals(runner.category.getGender())).
            toList();

        final int gender_position = (int) gender_results.stream().
            takeWhile(result -> !result.getParticipant().equals(runner)).
            count() + 1;

        return gender_position <= gender_results.size() ? Math.max(Integer.parseInt((String)race.getConfig().get(KEY_SCORE_FOR_FIRST_PLACE)) - gender_position + 1, 0) : 0;
    }

    private List<Race> loadRaces() throws IOException {

        final int number_of_race_in_series = Integer.parseInt((String) race.getConfig().get(KEY_NUMBER_OF_RACES_IN_SERIES));
        if (number_of_race_in_series != race_config_paths.size())
            throw new RuntimeException(STR."invalid number of races specified in file '\{race.getConfig().getConfigPath().getFileName()}'");

        final List<Race> races = new ArrayList<>();
        final List<String> config_paths_seen = new ArrayList<>();

        for (int i = 0; i < number_of_race_in_series; i++) {

            final String race_config_path = race_config_paths.get(i);

            if (race_config_path.isBlank())
                races.add(null);
            else {
                if (config_paths_seen.contains(race_config_path))
                    throw new RuntimeException(STR."duplicate races specified in file '\{race.getConfig().getConfigPath().getFileName()}'");
                config_paths_seen.add(race_config_path);
                races.add(getIndividualRace(race_config_path, i + 1));
            }
        }

        return races;
    }

    private Race getIndividualRace(final String race_config_path, final int race_number) throws IOException {

        final Path config_path = race.interpretPath(Path.of(race_config_path));

        if (!Files.exists(config_path))
            throw new RuntimeException(STR."invalid config for race \{race_number} in file '\{race.getConfig().getConfigPath().getFileName()}'");

        final Race individual_race = IndividualRaceFactory.makeIndividualRace(config_path);

        configureIndividualRace(individual_race, race_number);
        individual_race.processResults();

        return individual_race;
    }

    protected void configureIndividualRace(final Race individual_race, final int race_number) {

        ((CommonRace)individual_race).completeConfiguration();
    }

    public int getNumberOfRacesTakenPlace() {

        return (int) races.stream().filter(Objects::nonNull).count();
    }
}
