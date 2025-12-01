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

import org.grahamkirby.race_timing.categories.CategoriesProcessor;
import org.grahamkirby.race_timing.common.*;
import org.grahamkirby.race_timing.individual_race.IndividualRaceFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.grahamkirby.race_timing.common.Config.KEY_NUMBER_OF_RACES_IN_SERIES;
import static org.grahamkirby.race_timing.common.Config.KEY_RACES;

public class SeriesRace implements RaceInternal {

    private List<SingleRaceInternal> races;
    private final Config config;
    private final CategoriesProcessor categories_processor;
    private RaceResultsCalculator results_calculator;
    private RaceOutput results_output;
    private final Normalisation normalisation;
    private final Notes notes;

    private final List<Path> input_files_used_by_individual_races =  new ArrayList<>();

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public SeriesRace(final Config config) throws IOException {

        this.config = config;
        categories_processor = new CategoriesProcessor(config);
        normalisation = new Normalisation(config);
        notes = new Notes();

        loadRaces();
    }

    @Override
    public RaceResults processResults() throws IOException {

        return results_calculator.calculateResults();
    }

    @Override
    public void outputResults(final RaceResults results) throws IOException {

        config.checkUnusedInputFiles(input_files_used_by_individual_races);
        config.checkUnusedProperties();

        results_output.outputResults(results);
    }

    @Override
    public RaceResultsCalculator getResultsCalculator() {
        return results_calculator;
    }

    @Override
    public void setResultsCalculator(final RaceResultsCalculator results_calculator) {
        this.results_calculator = results_calculator;
    }

    @Override
    public void setResultsOutput(final RaceOutput results_output) {
        this.results_output = results_output;
    }

    @Override
    public Config getConfig() {
        return config;
    }

    @Override
    public CategoriesProcessor getCategoriesProcessor() {
        return categories_processor;
    }

    @Override
    public Normalisation getNormalisation() {

        return normalisation;
    }

    @Override
    public Notes getNotes() {
        return notes;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    public List<SingleRaceInternal> getRaces() {
        return races;
    }

    public int getNumberOfRacesTakenPlace() {

        return (int) races.stream().filter(Objects::nonNull).count();
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    private void loadRaces() throws IOException {

        final String races_string = config.getString(KEY_RACES);
        final List<String> race_config_paths = Arrays.asList(races_string.split(",", -1));

        final int number_of_races_in_series = (int) config.get(KEY_NUMBER_OF_RACES_IN_SERIES);
        if (race_config_paths.size() != number_of_races_in_series)
            throw new RuntimeException("invalid number of races specified in file '" + config.getConfigPath().getFileName() + "'");

        loadRaces(race_config_paths);
    }

    private void loadRaces(final List<String> race_config_paths) throws IOException {

        races = new ArrayList<>();
        final List<String> config_paths_seen = new ArrayList<>();

        for (int i = 0; i < race_config_paths.size(); i++) {

            final String race_config_path = race_config_paths.get(i);

            if (race_config_path.isBlank())
                // Race has not yet taken place.
                races.add(null);
            else {
                if (config_paths_seen.contains(race_config_path))
                    throw new RuntimeException("duplicate races specified in file '" + config.getConfigPath().getFileName() + "'");

                config_paths_seen.add(race_config_path);

                final SingleRaceInternal individual_race = getIndividualRace(race_config_path, i + 1);
                races.add(individual_race);

                individual_race.getConfig().checkUnusedInputFiles();
                input_files_used_by_individual_races.addAll(individual_race.getConfig().getUsedInputFiles());
            }
        }
    }

    private SingleRaceInternal getIndividualRace(final String individual_race_config_path, final int race_number) throws IOException {

        final Path config_path = config.interpretPath(Path.of(individual_race_config_path));

        if (!Files.exists(config_path))
            throw new RuntimeException("invalid config for race " + race_number + " in file '" + config.getConfigPath().getFileName() + "'");

        final SingleRaceInternal individual_race = new IndividualRaceFactory().makeRace(config_path);
        individual_race.processResults();

        return individual_race;
    }
}
